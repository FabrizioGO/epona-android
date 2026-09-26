// Deletes a user's account and every row/file it owns.
//
// Called two ways:
//
//   * By the app (AuthService.deleteAccount): Authorization: Bearer <the user's
//     own access token>. The caller is resolved from that token, so a user can
//     only ever delete themselves.
//   * By us, manually, for a deletion request that arrives by email because the
//     person no longer has the app installed (see docs/delete-account.md):
//     x-epona-admin-key: <ACCOUNT_DELETION_ADMIN_KEY> plus {"user_id": "..."} in
//     the body.
//
// Deployed with verify_jwt = false, same reasoning as push-notification: JWT
// verification at the gateway would accept the anon key, which ships inside the
// APK and proves nothing about who is calling. The user's own token is verified
// here instead, against GET /auth/v1/user -- the one call that can't be forged
// without actually holding a valid session.
//
// Every step is safe to run twice. delete_user_data's deletes are all
// `where ... = p_user_id`, so a retry after a partial failure matches zero rows
// instead of erroring. The storage removal and the admin user delete are treated
// the same way: "already gone" is success, not failure.

const CONCURRENCY = 5;
const PHOTO_BUCKETS = ["avatars", "pets", "sightings"];

function json(payload: unknown, status = 200): Response {
  return new Response(JSON.stringify(payload), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

/** Resolves the account to delete, and refuses anything it can't verify itself. */
async function resolveUserId(req: Request, supabaseUrl: string, anonKey: string): Promise<string> {
  const adminKey = req.headers.get("x-epona-admin-key");
  const expectedAdminKey = Deno.env.get("ACCOUNT_DELETION_ADMIN_KEY") ?? "";

  if (adminKey && expectedAdminKey.length > 0 && adminKey === expectedAdminKey) {
    const body = await req.json().catch(() => ({}));
    const userId = typeof body?.user_id === "string" ? body.user_id : "";
    if (userId.length === 0) throw new HttpError(400, "user_id is required");
    return userId;
  }

  const authHeader = req.headers.get("Authorization") ?? "";
  if (!authHeader.toLowerCase().startsWith("bearer ")) {
    throw new HttpError(401, "unauthorized");
  }

  const res = await fetch(`${supabaseUrl}/auth/v1/user`, {
    headers: { Authorization: authHeader, apikey: anonKey },
  });
  if (!res.ok) throw new HttpError(401, "unauthorized");

  const user = await res.json();
  if (typeof user?.id !== "string" || user.id.length === 0) {
    throw new HttpError(401, "unauthorized");
  }
  return user.id;
}

class HttpError extends Error {
  constructor(readonly status: number, message: string) {
    super(message);
  }
}

/** bucket + object path from a Supabase Storage public URL, or null if it doesn't match. */
function parsePublicUrl(url: string): { bucket: string; path: string } | null {
  const marker = "/storage/v1/object/public/";
  const i = url.indexOf(marker);
  if (i === -1) return null;
  const rest = url.slice(i + marker.length);
  const slash = rest.indexOf("/");
  if (slash === -1) return null;
  return { bucket: decodeURIComponent(rest.slice(0, slash)), path: decodeURIComponent(rest.slice(slash + 1)) };
}

async function listUserObjects(
  supabaseUrl: string,
  serviceKey: string,
  bucket: string,
  userId: string,
): Promise<string[]> {
  const res = await fetch(`${supabaseUrl}/storage/v1/object/list/${bucket}`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${serviceKey}`,
      apikey: serviceKey,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ prefix: `${userId}/`, limit: 1000 }),
  });
  if (!res.ok) return []; // an empty/missing folder listing is not fatal
  const entries = await res.json().catch(() => []);
  if (!Array.isArray(entries)) return [];
  return entries
    .filter((e) => typeof e?.name === "string")
    .map((e) => `${userId}/${e.name}`);
}

/** Supabase's bulk-delete endpoint calls these "prefixes"; they are exact object keys. */
async function removeObjects(
  supabaseUrl: string,
  serviceKey: string,
  bucket: string,
  paths: string[],
): Promise<void> {
  if (paths.length === 0) return;
  const res = await fetch(`${supabaseUrl}/storage/v1/object/${bucket}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${serviceKey}`,
      apikey: serviceKey,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ prefixes: paths }),
  });
  if (!res.ok) {
    console.error(`delete-account: failed to remove ${paths.length} object(s) from ${bucket}`, await res.text());
  }
}

Deno.serve(async (req) => {
  if (req.method !== "POST") return json({ error: "method not allowed" }, 405);

  const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
  const anonKey = Deno.env.get("SUPABASE_ANON_KEY")!;
  const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

  let userId: string;
  try {
    userId = await resolveUserId(req, supabaseUrl, anonKey);
  } catch (e) {
    if (e instanceof HttpError) return json({ error: e.message }, e.status);
    return json({ error: "bad request" }, 400);
  }

  // 1. Delete every row this account owns, in one transaction, and collect the
  // photo URLs those rows carried -- see delete_user_data in
  // supabase/migrations/20260926000100_account_deletion.sql.
  const rpcRes = await fetch(`${supabaseUrl}/rest/v1/rpc/delete_user_data`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${serviceKey}`,
      apikey: serviceKey,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ p_user_id: userId }),
  });
  if (!rpcRes.ok) {
    const detail = await rpcRes.text();
    console.error("delete-account: delete_user_data failed", detail);
    return json({ error: "failed to delete account data" }, 500);
  }
  const photoUrls: string[] = await rpcRes.json().catch(() => []);

  // 2. Remove this account's files from Storage: everything under "{uid}/" in
  // each bucket (covers the normal case), plus whatever the RPC's returned URLs
  // point at (a defensive second pass in case a path ever drifts from that
  // convention -- see StorageService's own note about PhotoUploader owning it).
  const extraByBucket = new Map<string, Set<string>>();
  for (const url of photoUrls) {
    const parsed = parsePublicUrl(url);
    if (!parsed) continue;
    const set = extraByBucket.get(parsed.bucket) ?? new Set<string>();
    set.add(parsed.path);
    extraByBucket.set(parsed.bucket, set);
  }

  for (let i = 0; i < PHOTO_BUCKETS.length; i += CONCURRENCY) {
    const slice = PHOTO_BUCKETS.slice(i, i + CONCURRENCY);
    await Promise.allSettled(
      slice.map(async (bucket) => {
        const listed = await listUserObjects(supabaseUrl, serviceKey, bucket, userId);
        const extra = extraByBucket.get(bucket);
        const paths = new Set(listed);
        if (extra) for (const p of extra) paths.add(p);
        await removeObjects(supabaseUrl, serviceKey, bucket, [...paths]);
      }),
    );
  }

  // 3. Finally, the auth user itself. A 404 here means a retry landed after the
  // user was already removed -- not a failure of this request.
  const authRes = await fetch(`${supabaseUrl}/auth/v1/admin/users/${userId}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${serviceKey}`,
      apikey: serviceKey,
    },
  });
  if (!authRes.ok && authRes.status !== 404) {
    const detail = await authRes.text();
    console.error("delete-account: auth user delete failed", detail);
    return json({ error: "account data was removed, but the login itself could not be" }, 500);
  }

  return json({ deleted: true });
});

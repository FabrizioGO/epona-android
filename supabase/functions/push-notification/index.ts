// Turns one batch of notification rows into FCM HTTP v1 sends.
//
// Called by trg_notifications_push via pg_net with a body of
// {"messages":[{notification_id, token, title, body, type, alert_id}, ...]}.
//
// Deployed with verify_jwt = false and gated on x-epona-push-key instead. JWT
// verification would accept the anon key, which ships inside the Android APK and
// is therefore public; the shared secret is the only thing here that an attacker
// does not already have.
//
// Data-only messages, deliberately. Including a `notification` block would let the
// system draw the tray entry itself, which loses the epona_alerts channel, the
// per-type icon and the alert_id extra that EponaFirebaseMessagingService puts on
// the launch intent -- and would double up in the foreground. The cost is that a
// force-stopped app receives nothing until it is next opened, which is the right
// trade for an app whose notification IS a deep link.

import { getAccessToken, serviceAccount } from "../_shared/googleAuth.ts";

type PushMessage = {
  notification_id?: string;
  token: string;
  title?: string;
  body?: string;
  type?: string;
  alert_id?: string;
};

const CONCURRENCY = 10;

function json(payload: unknown, status = 200): Response {
  return new Response(JSON.stringify(payload), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

async function sendOne(endpoint: string, accessToken: string, m: PushMessage) {
  const res = await fetch(endpoint, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${accessToken}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      message: {
        token: m.token,
        // Keys fixed by EponaFirebaseMessagingService. All values must be strings.
        data: {
          title: m.title ?? "",
          body: m.body ?? "",
          type: m.type ?? "new_alert",
          alert_id: m.alert_id ?? "",
          notification_id: m.notification_id ?? "",
        },
        android: { priority: "HIGH", ttl: "86400s" },
      },
    }),
  });

  if (res.ok) return { ok: true, stale: false, detail: "" };

  const detail = await res.text();
  // Only these two mean "this token is dead". A 400 INVALID_ARGUMENT usually means
  // the message is malformed -- our bug -- and clearing tokens on it would wipe
  // every registration in the batch.
  const stale =
    (res.status === 404 && detail.includes("UNREGISTERED")) ||
    (res.status === 403 && detail.includes("SENDER_ID_MISMATCH"));

  return { ok: false, stale, detail: `${res.status} ${detail}` };
}

async function clearTokens(tokens: string[]): Promise<number> {
  const url = `${Deno.env.get("SUPABASE_URL")}/rest/v1/rpc/clear_fcm_tokens`;
  const key = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
  const res = await fetch(url, {
    method: "POST",
    headers: {
      apikey: key,
      Authorization: `Bearer ${key}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ p_tokens: tokens }),
  });
  if (!res.ok) {
    console.error("clear_fcm_tokens failed", res.status, await res.text());
    return 0;
  }
  return Number(await res.json()) || 0;
}

Deno.serve(async (req) => {
  if (req.method !== "POST") return json({ error: "method not allowed" }, 405);

  const expected = Deno.env.get("PUSH_SHARED_SECRET") ?? "";
  const got = req.headers.get("x-epona-push-key") ?? "";
  if (expected.length === 0 || got !== expected) return json({ error: "unauthorized" }, 401);

  let messages: PushMessage[];
  try {
    const payload = await req.json();
    messages = Array.isArray(payload?.messages) ? payload.messages : [];
  } catch {
    return json({ error: "bad request" }, 400);
  }
  messages = messages.filter((m) => typeof m?.token === "string" && m.token.length > 0);
  if (messages.length === 0) return json({ sent: 0, failed: 0, cleared: 0 });

  const sa = serviceAccount();
  const projectId = Deno.env.get("FCM_PROJECT_ID") ?? sa.project_id;
  const accessToken = await getAccessToken(sa);
  const endpoint = `https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`;

  // FCM v1 has no multicast endpoint -- the /batch endpoint was retired in 2024 --
  // so the batching this function receives is unrolled into concurrent single
  // sends. The DB->function hop is where the N-to-1 saving actually happens.
  const stale = new Set<string>();
  let sent = 0;
  let failed = 0;

  for (let i = 0; i < messages.length; i += CONCURRENCY) {
    const slice = messages.slice(i, i + CONCURRENCY);
    const results = await Promise.allSettled(slice.map((m) => sendOne(endpoint, accessToken, m)));
    results.forEach((r, idx) => {
      if (r.status === "fulfilled" && r.value.ok) {
        sent++;
        return;
      }
      failed++;
      if (r.status === "fulfilled") {
        if (r.value.stale) stale.add(slice[idx].token);
        console.error("fcm send failed", r.value.detail);
      } else {
        console.error("fcm send threw", r.reason);
      }
    });
  }

  const cleared = stale.size > 0 ? await clearTokens([...stale]) : 0;
  console.log(`push batch: sent=${sent} failed=${failed} cleared=${cleared}`);
  return json({ sent, failed, cleared });
});

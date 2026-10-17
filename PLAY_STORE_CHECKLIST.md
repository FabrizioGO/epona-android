# Play Store Checklist

Internal notes for publishing Epona to Google Play. Not part of the app; not
published anywhere. See `docs/` for the public legal pages this checklist
depends on, and the plan this was built from for the full rationale.

## 1. Before you touch Play Console

- [ ] Fill in every placeholder in `docs/_config.yml` (`legal_name`,
      `contact_email`, `server_region`). `contact_email` currently ships as a
      literal placeholder in the published pages — the app is **not**
      submittable until it's real.
- [ ] Enable GitHub Pages on this repo: **Settings → Pages → Source** = your
      default branch, folder `/docs`. Confirm each page loads at
      `https://fabriziogo.github.io/epona-android/…` (both `/` and `/es/`).
      If you rename the repo or move to a custom domain, update the
      `url_*` strings in `values/strings.xml` and `values-es/strings.xml` to
      match.
- [ ] Deploy the new Supabase migrations and function (nothing in this repo
      has touched your live project):
      ```
      supabase link --project-ref uantchroigqmbwordchb
      supabase db push
      supabase functions deploy delete-account push-notification
      supabase secrets set ACCOUNT_DELETION_ADMIN_KEY=<a long random value>
      ```
      `ACCOUNT_DELETION_ADMIN_KEY` is only for deletion requests that arrive by
      email (see `docs/delete-account.md`) — keep it out of the app and out of
      git.
- [ ] **Supabase dashboard → Authentication** (`config.toml` only affects local
      dev; set these on the live project too): confirm email **on**, minimum
      password length **8**, secure password change **on**, leaked-password
      protection **on**. Consider CAPTCHA (hCaptcha/Turnstile) on sign-up.
- [ ] **Verify RLS on the tables this repo doesn't create** (`users`, `pets`,
      `alerts`, `sightings`). Their policies exist only in the live project.
      Run `select tablename, policyname, cmd, roles, qual, with_check from
      pg_policies where schemaname = 'public' order by 1, 3;` and the
      dashboard's **Advisors → Security** report. What to look for:
      - RLS enabled on all four.
      - INSERT / UPDATE / DELETE limited to the owner (`owner_id` / `user_id`
        = `auth.uid()`) in **both** USING and WITH CHECK, so an owner can't
        reassign a row to someone else.
      - `users` must not let other users read `email`, `phone`, `fcm_token`
        or `location`. If SELECT is open to `authenticated`, revoke table
        SELECT and grant only the public columns (`id`, `display_name`,
        `avatar_url`, …).
      - Once settled, export the policies into a migration so they're in git.
- [ ] Restrict the Firebase API key from `app/google-services.json` in Google
      Cloud Console → Credentials (Android apps: package `com.fabriziogo.epona`
      + release/Play signing SHA-1).
- [ ] Read through `docs/privacy.md` and `docs/terms.md` yourself, and have
      someone who can speak to Venezuelan/consumer law review them before
      launch. They're accurate to what the app's code does today, but they are
      templates, not legal advice.

## 2. Play Console — Store presence

- [ ] **Privacy Policy URL**: `https://fabriziogo.github.io/epona-android/privacy.html`
- [ ] **App category / target audience**: 18+ (see the Terms' eligibility
      clause — this must match what you declare here).
- [ ] **Ads**: No ads SDK is present — answer "No ads".

## 3. Data Safety form

Based on what the app actually collects (see `docs/privacy.md` §1 for the
full detail):

| Category | Collected? | Shared? | Notes |
|---|---|---|---|
| Name | Yes | Yes, with other users | Display name shown on every alert/sighting |
| Email address | Yes | No | Account only |
| Phone number | Yes (optional per alert; required per adoption post) | Yes, with other users | `contact_phone` on an alert or adoption post |
| Precise location | Yes | Yes, with other users | Attached to alerts/sightings; foreground only |
| Approximate location | Yes | No | Used for the nearby feed |
| Photos | Yes | Yes, publicly (direct URL) | Pet/sighting/adoption/avatar photos, public storage buckets |
| Other user-generated content | Yes | Yes, with other users | Alert/sighting/adoption-post descriptions |
| Device or other IDs | Yes | No | FCM push token |

- **Data is encrypted in transit**: Yes (HTTPS/TLS to Supabase and Google).
- **Users can request data deletion**: Yes —
  `https://fabriziogo.github.io/epona-android/delete-account.html`, and
  in-app via Profile → Delete account.
- **Data collection is required, not optional**: account fields and location
  are required to use core features; note that in the form.

## 4. Content rating & policy declarations

- **User-generated content**: Yes. Declare that the app has:
  - Terms users must accept before posting (Register screen checkbox).
  - Community Guidelines (`docs/terms.md#community-guidelines`), linked from
    Settings and Register.
  - In-app reporting of content (the alert flag action — this ships in
    this change).
  - **No in-app blocking of users.** Play's UGC policy asks for this on apps
    with direct user interaction; Epona doesn't have it yet. This is a known
    gap — see §6 below.
- **Location shared with other users**: Yes — disclose this specifically, it's
  a common rejection reason if missed.

## 5. App access (for the reviewer)

Play reviewers need a way in. Provide either:
- A demo account (email + password) with at least one pet, one alert, and one
  sighting already posted, so every screen has data to show, **or**
- Instructions for creating an account inside the review notes (email/password
  sign-up currently has no confirmation-required friction unless
  `enable_confirmations` is turned on in your Supabase project).

## 6. Known gaps this change does not fix

Flagging these so they're a decision, not a surprise at review time:

- **No user blocking.** Play's UGC policy language asks for it on apps with
  direct user interaction and public UGC. Content reporting is in place;
  blocking is a larger feature (a `blocked_users` table, feed/RPC filtering)
  that was out of scope here.
- **Google Sign-In is hidden for the first release (decided).**
  `GOOGLE_SIGN_IN_ENABLED = false` in `feature/auth/AuthFeatureFlags.kt` hides
  the button and its "or" divider on Login and Register. Email/password is the
  only sign-in method. **Post-approval follow-up:** implement Credential
  Manager in `MainActivity.launchGoogleSignIn()`, uncomment/finish the
  `signInWithGoogle` call in `AuthService`, register the release + Play App
  Signing SHA-1 in Google Cloud/Firebase, then flip the flag to `true`. Update
  the Data Safety form and Privacy Policy if Google account data is collected.
- **Release signing is configured** (`RELEASE_STORE_*` / `RELEASE_KEY_*` in
  `local.properties` or env vars; release builds fail if they or the API keys
  are missing). You still need to generate the upload keystore and back it up.
  R8 is on for release (`proguard-rules.pro` keeps the DTO serializers and the
  Ktor engine). Smoke-test a signed release build on a device before uploading,
  and upload `app/build/outputs/mapping/release/mapping.txt` with the bundle so
  crash reports deobfuscate.
- **No explicit permission-rationale screen** before the system location
  prompt. Not required by Play, but Google Play's location policy expects the
  in-context request to make clear why it's needed — `LocationPermissionBanner`
  covers the post-denial case but not the first ask.

# CRM Connector — Project Handoff & Status

This document is the single source of truth for the **CRM Connector** Android app.
Give it (plus the "Prompt for a new chat" at the bottom) to any new chat/agent so
they can continue the work with full context.

---

## 1. What this is
A thin Android client (Kotlin + Jetpack Compose) that connects a Samsung/Android
phone to an existing PHP CRM at **https://crm.digibrood.com**. The app logs in,
registers the device, waits for admin approval, then syncs call logs, uploads call
recordings to Cloudflare R2, shows a dashboard, and shows an after-call popup.

- **Package:** `com.digibrood.crmconnector`
- **Min SDK:** 29  •  **Target/Compile SDK:** 35  •  **Language:** Kotlin
- **Stack:** Jetpack Compose (Material 3), MVVM + Repository, Hilt, Room, Retrofit
  + OkHttp + Moshi, WorkManager, EncryptedSharedPreferences, Navigation Compose.

## 2. Links
- **GitHub repo:** https://github.com/digibroodgraphics-lgtm/crm-connector
- **Ready-to-install APK (direct download):**
  https://github.com/digibroodgraphics-lgtm/crm-connector/raw/main/CRM-Connector.apk
- Branch: `main`

## 3. How it is built (important for agents)
There is **no Android SDK by default** in the sandbox; it was installed manually:
- Android SDK at `/projects/android-sdk` (platform 35, build-tools 35, platform-tools)
- JDK 17 at `/root/.local/share/mise/installs/java/17`
- `local.properties` contains `sdk.dir=/projects/android-sdk` (git-ignored)

Build command (from `/projects/sandbox/crm-connector`):
```bash
export JAVA_HOME=/root/.local/share/mise/installs/java/17
export ANDROID_HOME=/projects/android-sdk
export PATH=$JAVA_HOME/bin:$PATH
./gradlew :app:assembleDebug --no-daemon
```
Output APK: `app/build/outputs/apk/debug/app-debug.apk` → also copied to repo root as
`CRM-Connector.apk` and pushed so the user can download it from GitHub.

If the SDK is missing in a fresh sandbox, reinstall command-line tools from
`https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip`,
unzip to `/projects/android-sdk/cmdline-tools/latest`, accept licenses, and
`sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"`.

Push to GitHub uses the Kiro **github power** tool `push_to_remote` (the repo was set
up via the `repo_set_up` tool so origin is the gateway; a plain `git push` will NOT
work). Always commit, then `push_to_remote` to branch `main`.

## 4. Verified live API contract (https://crm.digibrood.com/api/mobile/v1)
All confirmed by live testing. **Field names and quirks matter — they differ from
generic assumptions.**

- **POST /auth/login** `{email,password}` → `{ok, token, refresh_token, expires_in, user{...}}`
  (access token is in **`token`**, not `access_token`). Errors: HTTP 401 `{ok:false, code, message}`
  with codes `INVALID_CREDENTIALS`, `APP_LOGIN_DISABLED`.
- **POST /auth/refresh** `{refresh_token}` → `{ok, token, expires_in}`.
- **POST /device/register** `{phone_number, device_id, device_name, device_model, os_version, app_version}`
  → `{ok, status}`. (CRM accepts phone in any of phone/phone_number/number/mobile/msisdn.)
- **POST /device/change-number** `{phone_number, device_id}` → `{ok, status}`.
- **GET /device/status?device_id=...** → `{ok, status, activated_at, revoked, message}`.
  ⚠️ **Requires `device_id` as a query param**, else returns "Not registered/pending".
  `status` values: pending_approval | approved | denied | revoked | inactive.
- **POST /heartbeat** `{device_id, app_version, ...}` → `{ok, status, action}`.
- **POST /calls/sync** — ⚠️ **ONE call per request, fields at TOP LEVEL** (not a `calls` array):
  `{device_id, client_call_id, phone (E.164), call_type (incoming|outgoing|missed|rejected),
  start_time (ISO-8601 w/ offset), end_time (ISO-8601), duration (seconds), has_recording}`
  → `{ok, results:[{client_call_id, call_id, status:"stored", contact_id}]}`. A bad item
  returns `status:"rejected", reason:"missing phone"`.
  **VoIP/app calls (D6):** send the SAME endpoint with `{client_call_id, platform (e.g. "com.whatsapp"
  or "whatsapp"), name (display name), call_type/direction, started_at, ended_at, duration,
  has_recording:false}` and OMIT number/phone. The CRM matches/creates the contact by name, tags it
  with the platform, and returns `{status:"stored", contact_id, platform}`. No recording for VoIP.
- **GET /contacts/lookup?phone=...** → `{ok, found, name, company, status, country, contact:{...}}`
  (name/company both top-level AND nested under `contact`). ⚠️ param is **`phone`** (not phone_number).
- **POST /calls/remark** `{device_id, client_call_id, phone, call_type, name, company, remark, status}`
  → `{ok, contact_id, name, company}`. Name/company applied "smartly" (only adopted if contact
  has none). Sending call_type lets the CRM create/label the log from the popup.
- **POST /recordings/presign** `{device_id, client_call_id, phone, file_name, mime_type, file_size}`
  → `{ok, upload_url, method:"PUT", headers{Content-Type}, object_key, key, recording_id,
  content_type, expires_in, max_bytes:52428800}`. Allowed types: mp3, m4a, wav, amr, 3gp, ogg.
  (`phone` is sent in presign AND confirm so the CRM can attach by number when ids differ.)
- **POST /recordings/confirm** `{device_id, client_call_id, object_key (or key/recording_id),
  phone, file_size, success}` → `{ok, call_id, recording_id}`. Upload to R2 (PUT) must
  succeed (HTTP 200) BEFORE confirm; only confirm with success:true.
- **GET /recordings/trace?client_call_id=...** → `{ok, call_synced, call:{has_recording,
  recording_attached, ai_status, synced_at}, recording_held_pending, hint}`. The dashboard
  diagnostics use this to show the last synced call's recording state
  (Attached / Uploaded, attaching… / Call not synced yet / No recording file).
- **GET /settings** → `{ok, call_popup_enabled, recording_path, recording_upload, auto_sync,
  heartbeat_interval_sec, ...}`.
- **GET /meta** → `{ok, tags:[{id,name}], statuses:[{value,label}], stages:[{id,name}]}`.
  Drives the Status (single) + Tags (multi-select) dropdowns in the after-call popup.
  Cached; refreshed on launch / each sync cycle so new CRM tags appear automatically.
- **GET /branding** → `{ok, app_name, logo_url, icon_url, use_crm_favicon}`.
- **GET /stats?device_id=...** → `{ok, connection_status, calls_today, uploads_today, last_sync}`.
- **POST /whitelist/propose** `{device_id, number, note?}` → `{ok, id, status:"pending", message}`
  (D7, LIVE). status is `pending` (or `approved` if the admin had already added it).
- **GET /device/status** also returns a **`whitelist`** array (D7). **Canonical shape (final):**
  an array of objects `[{ "number": "+91…", "status": "approved|pending|rejected" }]` where
  `number` is normalised +E.164 and entries are per-device. The app excludes a number from
  upload ONLY when `status == "approved"`; `pending`, `rejected`, and any unknown value keep
  uploading (a number approved then removed by the admin returns as `rejected`). The earlier
  plain-string form (`["+91…"]`) is DEPRECATED and no longer sent, but the app's
  `WhitelistItemAdapter` still tolerates it (treating a bare string as approved) as a safety net.
  The Whitelist screen re-polls device/status after a propose and every ~10s while open
  (CRM responses are `Cache-Control: no-store`, so each poll is fresh).
  CRM safety net: if a whitelisted number slips through, `/calls/sync` returns
  `status:"whitelisted_skipped"` and stores nothing (app treats it as terminal, not an error).

**D3 extended /calls/sync payload (superset, all optional extras):** in addition to the
core fields above, the app now also sends `number`, `direction`, `started_at`, `ended_at`
(duplicates of phone/call_type/start_time/end_time for compatibility) plus optional
`platform`, `note`, `status`, `tags:[int]`, `stage_id`. When the popup is filled the app
re-syncs the SAME `client_call_id` with note/status/tags so the CRM applies them to the
matched/created contact; if the popup is dismissed the call is still uploaded (empty extras).

**Recording rule:** Use ONE stable `client_call_id` (UUID) per call across
/calls/sync, /recordings/presign, /recordings/confirm and /calls/remark so the CRM
links everything. The app captures the call in real time (in CallReceiver) to
generate that id and passes it to the popup.

## 5. Architecture (package map under com.digibrood.crmconnector)
```
CrmConnectorApp (Hilt app + crash handler + WorkManager config)
MainActivity (Compose host + splash)
data/local        Room: AppDatabase(v4), CallEntity, RecordingEntity, RemarkEntity, DAOs
data/prefs        SecurePrefs (EncryptedSharedPreferences; tokens, crmOrigin, activation)
data/remote       CrmApiService, dto/*, interceptors (Auth, DynamicBaseUrl, HttpsEnforcement),
                  TokenAuthenticator (auto-refresh + clearTokensForReauth on dead session),
                  NetworkResult + safeApiCall
data/repository   Auth, Device, Call, Recording, Contact, Settings, Branding, Stats
di                NetworkModule, DatabaseModule, AppModule, Qualifiers (MainClient/RefreshClient/UploadClient)
domain/model      DeviceStatus, CallType
service           SyncForegroundService (dataSync), NotificationHelper
sync              SyncController (orchestration), SyncManager (start/stop by status)
worker            SyncWorker, RecordingUploadWorker, HeartbeatWorker, SyncScheduler
receiver          CallReceiver (real-time capture + popup), BootReceiver
overlay           CallPopupActivity + CallPopupViewModel (Name/Company/Phone/Status/Tags/Remark)
data/repository   ... + MetaRepository (caches /meta tags+statuses, DEFAULT_STATUSES fallback)
ui/...            theme, navigation (CrmNavGraph + session re-login), screens
                  (splash, login, permissions, register, dashboard), components
util              Constants, UrlValidator, DeviceInfoProvider, ConnectivityObserver,
                  PermissionManager, CallLogReader, RecordingScanner, ContactReader,
                  TimeUtils, PhoneUtils, CrashReporter
```

Key behaviors:
- Dynamic CRM base URL: user enters CRM URL at login; an OkHttp interceptor swaps
  scheme/host/port; API path `/api/mobile/v1/` is constant.
- HTTPS-only (network security config + interceptor). JWT in EncryptedSharedPreferences.
- Activation-forward only: never imports calls before approval (`activated_at`, with a
  client-side fallback to "now" on first approval).
- Call capture: real-time via CallReceiver (with retry to wait for the call-log row),
  plus a WorkManager safety-net scan; de-duped by (number, startTime).
- Recordings: scans ONLY call-recording folders + MediaStore paths containing
  record/call/voice, and only files modified after activation. 50 MB cap. Direct PUT to R2.
  **Recording backfill (important):** because phones finalise the recording FILE a few seconds
  after a call ends, the file usually doesn't exist yet at capture time. `CallRepository.backfillRecordings()`
  re-scans recent calls (last 3 days) that still have no recording on every sync cycle and queues
  any file that has since appeared (flipping has_recording and re-syncing the SAME client_call_id).
  After a call, the app also schedules delayed recording-upload passes (~60s and ~180s) so a late
  file uploads promptly instead of waiting for the next periodic cycle. The CRM links a recording
  to its call by client_call_id/phone even if the recording is confirmed before/after the call sync.
- Missed/rejected calls: logged, but NO popup.
- Call capture: **real-time from the phone-state broadcast** (CallReceiver builds the record from
  the number + ring/offhook/idle timing — independent of the system call log, because some OEM
  dialers (Samsung) expose a stale/incomplete call log to apps). Captures incoming-answered
  (incoming), incoming-not-answered/rejected (missed, no popup), and outgoing answered-or-not
  (outgoing). The call is queued BEFORE the popup shows, so dismissing the popup never drops it.
  A WorkManager call-log scan remains as a safety net; both paths de-dupe by number within ~2 min.
- After-call popup name pre-fill (unknown numbers): shows instantly from device contacts /
  caller-ID (Truecaller) cached name, then the CRM lookup refines it. A blank CRM name falls
  through to the device name. Editable; only saved to the CRM on Save.
- VoIP/WhatsApp calls: **now captured (best-effort, D6)** via `VoipCallListenerService`
  (NotificationListenerService). It watches CALL-style notifications from a curated package list
  (Constants.VOIP_PACKAGES: WhatsApp, Telegram, Signal, Messenger, Instagram, Skype, Viber, Meet,
  Zoom, Botim), records start on post + end on removal, and enqueues a VoIP call (platform=package,
  display name, no number) via `CallRepository.enqueueVoipCall` → `/calls/sync`. Requires the user
  to grant **Notification Access** (optional row on the permissions screen). Limitations: direction
  can't be derived reliably, so connected VoIP calls are logged as "incoming"; VoIP audio cannot be
  recorded on modern Android. Never blocks the PSTN flow. VoIP rows are excluded from recording
  backfill/whitelist (no number).
- Dashboard: status, connection, registered number, calls synced today, last synced
  number, recordings uploaded today, pending queue, last sync. Diagnostics behind a toggle.

## 6. Current status (working, verified live)
- ✅ Login, device register, approval detection (device_id in status), heartbeat, settings, branding
- ✅ Call sync (top-level one-per-request, phone + ISO times) → stored in CRM
- ✅ Recording upload to R2 (presign → PUT → confirm) → linked to call
- ✅ Contact lookup prefill (name + company), remark with name/company/call_type
- ✅ Auto re-login when session dies (e.g., CRM redeploy / INVALID_TOKEN)
- ✅ Cleaner dashboard; crash catcher; missed-call logging without popup

### D1–D7 spec (CRM developer change request) — status
- ✅ **D1 — Status & Tags in popup:** `GET /meta` wired (`MetaRepository`, cached + refreshed
  each sync). Popup now has a **Status** dropdown (single) and **Tags** chips (multi-select).
  Selections are sent with the call (note/status/tags) on save.
- ✅ **D2 — Never silently log out:** `TokenAuthenticator` refreshes on 401 and retries once;
  only clears the session on `APP_LOGIN_DISABLED`. Transient/5xx/network keep the session +
  refresh token (which persist across app updates in EncryptedSharedPreferences).
- ✅ **D3 — Upload whether popup filled OR dismissed:** the call is queued + synced the moment
  it ends (CallReceiver), independent of the popup. Saving the popup re-syncs the SAME
  `client_call_id` with note/status/tags via `CallRepository.applyPopupFields()`. Persistent
  Room queue with retry guarantees no call is lost.
- ⏳ **D4 / D5 — AI analysis & auto-summary toggle:** CRM-side, later phase. No app work needed
  (recordings already upload via presign→PUT→confirm).
- 🔶 **D6 — Capture VoIP (WhatsApp etc.):** payload supports a `platform` field; full VoIP
  detection (Accessibility/notification listener) is best-effort and NOT yet implemented —
  scheduled as a follow-up so it never blocks the core PSTN flow.
- ⏳ **D7 — Whitelist propose/approve:** ✅ IMPLEMENTED & aligned to LIVE contract. Whitelist
  screen reachable from a Dashboard top-bar shield button + a labelled button. Users propose a
  number (`POST /whitelist/propose` with `{device_id, number, note}`), see its status (Pending /
  Approved / Rejected). The app reads the `whitelist` STRING array of approved E.164 numbers from
  `GET /device/status`: numbers present = APPROVED (excluded from upload); numbers that drop out
  revert to PENDING (uploads resume). Local Room table `whitelist` (DB v5). APPROVED numbers are
  excluded from capture AND sync. `/calls/sync` `whitelisted_skipped` is handled as terminal.
- ✅ **Cross-cutting:** unknown numbers are logged but NOT auto-created as contacts (only the
  popup save / CRM-side logic creates a contact).

### Session-logout codes (confirmed with CRM dev)
- `401 TOKEN_EXPIRED` / `401 INVALID_TOKEN` / `401 NO_TOKEN` → `TokenAuthenticator` recovery ladder:
  it reads the CRM's `code` and `action` hint — `action:"refresh"`/TOKEN_EXPIRED refreshes first;
  `action:"reauth"`/INVALID_TOKEN/NO_TOKEN skips straight to a **silent re-login** with the encrypted
  stored credentials; then retries. If recovery fails AND no stored credentials exist (e.g. upgraded
  from an old build), it routes to Login. Syncing never visibly stops on a token error.
- Tags/Status dropdowns are persisted (SecurePrefs) so they always show in the popup even before a
  fresh `/meta` fetch / during a token outage.
- `403 APP_LOGIN_DISABLED` (account disabled) and `403 DEVICE_REVOKED` (device revoked) →
  the `SessionGuardInterceptor` clears tokens and routes to Login. These are the ONLY two
  logout conditions.
- Credentials (email/password) are saved in EncryptedSharedPreferences on login solely to enable
  the silent re-login. Access token (~7 days) is proactively refreshed ~1h before expiry by the
  foreground service so there is no visible interruption; the refresh token is long-lived (~90 days).

### Steady heartbeat
- The foreground service pings `POST /heartbeat` every ~60s while the device is approved (WorkManager
  can't go below 15 min, so the 60s cadence runs in the service). The CRM uses this heartbeat to
  advance automations (e.g. WhatsApp). The same loop proactively refreshes the access token.

### Realtime sync & Dismiss
- Each call is captured and queued the moment it ends (CallReceiver), then an immediate full sync
  runs — independent of the after-call popup. Tapping **Dismiss** skips the note, NOT the call: the
  call is already queued and syncs regardless. A persistent Room queue with exponential backoff
  retries over mobile data or wifi whether or not the CRM is open in a browser.

## 7. Open items / notes
- Recording upload only happens if the phone actually saves call recordings to disk
  (Samsung: Phone → Settings → Record calls → Auto record calls → On).
- **D6 (VoIP capture)** is agreed but not yet implemented in-app (best-effort/follow-up).
- Test account credentials are NOT stored in this repo for security; the owner provides
  them privately for live testing.

## 8. Build/version history (recent commits on main)
Aligned all DTO field names to the live CRM, fixed the Samsung call-log "Invalid token
LIMIT" crash, added device_id to /device/status, robust ISO-8601 parsing, real-time
capture + shared client_call_id, one-call-per-request /calls/sync, recording-confirm
phone safety net. **Latest:** implemented D1 (Status/Tags dropdowns via /meta), D2
(never silently log out — refresh-and-retry, only logout on APP_LOGIN_DISABLED), and D3
(upload on dismiss + send note/status/tags via applyPopupFields). **Newest:** D7 whitelist
(propose + admin-approval status UI, DB v5, upload gating) and a SessionGuardInterceptor
that logs out only on 403 APP_LOGIN_DISABLED / DEVICE_REVOKED. See `git log` for details.

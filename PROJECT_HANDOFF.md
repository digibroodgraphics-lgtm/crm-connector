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
- **GET /contacts/lookup?phone=...** → `{ok, found, name, company, status, country, contact:{...}}`
  (name/company both top-level AND nested under `contact`). ⚠️ param is **`phone`** (not phone_number).
- **POST /calls/remark** `{device_id, client_call_id, phone, call_type, name, company, remark, status}`
  → `{ok, contact_id, name, company}`. Name/company applied "smartly" (only adopted if contact
  has none). Sending call_type lets the CRM create/label the log from the popup.
- **POST /recordings/presign** `{device_id, client_call_id, file_name, mime_type, file_size}`
  → `{ok, upload_url, method:"PUT", headers{Content-Type}, object_key, key, recording_id,
  content_type, expires_in, max_bytes:52428800}`. Allowed types: mp3, m4a, wav, amr, 3gp, ogg.
- **POST /recordings/confirm** `{device_id, client_call_id, object_key (or key/recording_id),
  phone, file_size, success}` → `{ok, call_id, recording_id}`. Upload to R2 (PUT) must
  succeed (HTTP 200) BEFORE confirm; only confirm with success:true.
- **GET /settings** → `{ok, call_popup_enabled, recording_path, recording_upload, auto_sync,
  heartbeat_interval_sec, ...}`.
- **GET /branding** → `{ok, app_name, logo_url, icon_url, use_crm_favicon}`.
- **GET /stats?device_id=...** → `{ok, connection_status, calls_today, uploads_today, last_sync}`.

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
overlay           CallPopupActivity + CallPopupViewModel (Name/Company/Phone/Remark)
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
- Missed/rejected calls: logged, but NO popup.
- Dashboard: status, connection, registered number, calls synced today, last synced
  number, recordings uploaded today, pending queue, last sync. Diagnostics behind a toggle.

## 6. Current status (working, verified live)
- ✅ Login, device register, approval detection (device_id in status), heartbeat, settings, branding
- ✅ Call sync (top-level one-per-request, phone + ISO times) → stored in CRM
- ✅ Recording upload to R2 (presign → PUT → confirm) → linked to call
- ✅ Contact lookup prefill (name + company), remark with name/company/call_type
- ✅ Auto re-login when session dies (e.g., CRM redeploy / INVALID_TOKEN)
- ✅ Cleaner dashboard; crash catcher; missed-call logging without popup

## 7. Open items / notes
- Recording upload only happens if the phone actually saves call recordings to disk
  (Samsung: Phone → Settings → Record calls → Auto record calls → On).
- No CRM-developer changes are currently outstanding (presign + lookup were fixed).
- Test account credentials are NOT stored in this repo for security; the owner provides
  them privately for live testing.

## 8. Build/version history (recent commits on main)
Aligned all DTO field names to the live CRM, fixed the Samsung call-log "Invalid token
LIMIT" crash, added device_id to /device/status, robust ISO-8601 parsing, real-time
capture + shared client_call_id, one-call-per-request /calls/sync, recording-confirm
phone safety net. See `git log` for details.

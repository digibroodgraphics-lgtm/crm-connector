# CRM Connector

A thin Android client that connects an Android phone to your existing PHP CRM.
All business logic lives in the CRM — this app only signs in, registers the
device, waits for admin approval, then syncs call activity, uploads call
recordings, shows a dashboard, and shows a popup after each call.

> **Good news:** This is **one complete project**. There is nothing to merge or
> assemble. Just download this whole folder, open it in Android Studio, and
> build. If you received it as a ZIP, simply unzip it first — keep all files and
> folders exactly where they are.

---

# HOW TO BUILD THE APK (FOR NON-TECHNICAL USERS)

This guide assumes you have **never used Android Studio before**. Follow each
step exactly. Total time the first time: about **45–90 minutes**, most of which
is downloading and waiting for the computer to do its work.

You will need:
- A Windows, Mac, or Linux computer.
- A reliable internet connection (several gigabytes will be downloaded).
- About 15 GB of free disk space.

---

## Step 1 — Download Android Studio

1. Open your web browser and go to: **https://developer.android.com/studio**
2. Click the big green **"Download Android Studio"** button.
3. A page with terms and conditions appears. Tick the checkbox to agree, then
   click the download button again.
4. The download starts. The file is large (about 1 GB), so this can take a few
   minutes.

*What you should see:* a file downloading in your browser, named something like
`android-studio-...-windows.exe` (Windows), `...-mac.dmg` (Mac), or
`...-linux.tar.gz` (Linux).

---

## Step 2 — Install Android Studio

### On Windows
1. Double-click the downloaded `.exe` file.
2. If Windows asks "Do you want to allow this app to make changes?", click **Yes**.
3. The setup wizard opens. Click **Next** on each screen, leaving everything at
   its default setting.
4. Click **Install**, then wait, then **Finish**. Leave "Start Android Studio"
   ticked.

### On Mac
1. Double-click the downloaded `.dmg` file.
2. A window appears showing the Android Studio icon and an **Applications**
   folder. **Drag the Android Studio icon onto the Applications folder.**
3. Open **Applications** and double-click **Android Studio**.

### First launch (all systems)
1. The first time it opens it asks about importing settings. Choose
   **"Do not import settings"** and click **OK**.
2. A **Setup Wizard** appears. Click **Next**.
3. Choose **Standard** installation and click **Next**.
4. Accept any licenses it shows (select each license on the left and click
   **Accept**), then click **Finish**.
5. Android Studio now downloads the Android SDK and other tools. **This takes a
   while** (10–30 minutes depending on your internet). Let it finish.

*What you should see:* a "Welcome to Android Studio" window once everything is
downloaded.

---

## Step 3 — Open the Project

1. On the **Welcome to Android Studio** window, click **"Open"**
   (not "New Project").
2. A file browser opens. Navigate to the **CRM Connector** folder you downloaded
   (the folder that contains this `README.md` file and a file called
   `settings.gradle.kts`).
3. Select that folder (click it once so it is highlighted) and click **Open**.
4. If a message says *"Trust and Open Project?"*, click **Trust Project**.

*What you should see:* Android Studio opens and the project files appear on the
left-hand side.

---

## Step 4 — Wait for Gradle Sync

The first time you open the project, Android Studio automatically downloads all
the building blocks (libraries) the app needs. This is called **Gradle sync**.

1. Look at the **bottom** of the Android Studio window. You will see a progress
   bar and messages like *"Gradle: Downloading..."* or *"Sync in progress..."*.
2. **Do nothing — just wait.** The first sync can take **5–20 minutes**.
3. Sync is finished when the bottom progress bar disappears and you see
   **"Gradle sync finished"** (or "BUILD SUCCESSFUL") in the status area.

*If a yellow bar appears at the top saying something needs to be installed or
updated* (for example, "Install build tools" or "Update Gradle plugin"), click
the blue link in that bar and accept. Then let it sync again.

---

## Step 5 — Build the APK

1. In the very top menu bar, click **Build**.
2. In the menu that drops down, hover over **"Build Bundle(s) / APK(s)"**.
3. Click **"Build APK(s)"**.
4. Android Studio now builds the app. Watch the bottom of the screen for
   progress. The first build takes **2–10 minutes**.

*What you should see:* a small notification box in the **bottom-right corner**
saying **"APK(s) generated successfully"** with a blue **"locate"** link.

---

## Step 6 — Locate the APK

1. In that bottom-right notification, click the blue **"locate"** link.
   Your computer's file browser opens directly to the APK file.
2. If you missed the notification, the file is here inside the project folder:

   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

3. The file you want is named **`app-debug.apk`**. This is your installable app.

> **Tip:** "debug" APKs are perfect for installing and testing on your own
> phones. They install without any extra signing setup.

---

## Step 7 — Copy the APK to your Samsung phone

Pick whichever method is easiest for you:

**Method A — USB cable**
1. Connect the phone to the computer with a USB cable.
2. On the phone, swipe down and tap the USB notification; choose
   **"File transfer"** / **"Transferring files"**.
3. On the computer, open the phone's storage, open the **Download** folder, and
   copy `app-debug.apk` into it.

**Method B — Email or cloud**
1. Email the `app-debug.apk` file to yourself, or upload it to Google Drive.
2. Open the email / Drive **on the phone** and download the file.

---

## Step 8 — Enable Unknown Sources (allow installing the app)

Because the app does not come from the Play Store, the phone must be told it is
allowed to install it.

1. On the phone, open the **Files** app (Samsung calls it **"My Files"**) and tap
   the `app-debug.apk` file in the **Download** folder.
2. A message appears: *"For your security, your phone is not allowed to install
   unknown apps from this source."* Tap **Settings**.
3. Turn **ON** the switch **"Allow from this source"**.
4. Press the **back** button to return to the install screen.

---

## Step 9 — Install the APK

1. You should now see an **Install** button. Tap **Install**.
2. Wait a few seconds. When it says **"App installed"**, tap **Open**.
3. You will find the **CRM Connector** icon in your app drawer for next time.

---

## Step 10 — Grant Permissions and connect

When you first open the app it walks you through everything:

1. **Sign in:** enter your **CRM URL** (it must start with `https://`), your
   **email**, and your **password**, then tap **Sign in**.
2. **Permissions screen:** the app lists the permissions it needs (call log,
   phone, contacts, recordings, notifications, and "display over other apps").
   Tap **Grant** on each one and accept the system dialogs. Nothing syncs until
   these are granted.
3. **Register device:** enter the phone number used on this device and tap
   **Register device**.
4. **Wait for approval:** the dashboard shows **"Waiting for admin approval"**.
   Your CRM administrator approves the device from the CRM side.
5. Once approved, the dashboard shows a green **Active** status and the app
   begins syncing automatically in the background.

That's it — the app keeps running quietly and syncs new calls and recordings as
they happen.

---

# Frequently Asked Questions

**Do I need to merge multiple parts together?**
No. This is a single, complete project. Open the whole folder as-is.

**The build failed with a red error. What do I do?**
1. Make sure **Gradle sync** (Step 4) fully finished first.
2. Click **File → Sync Project with Gradle Files**, wait, then try **Build APK**
   again.
3. If it mentions missing SDK components, accept the yellow prompt at the top of
   the window to install them, then rebuild.

**Where is the APK again?**
`app/build/outputs/apk/debug/app-debug.apk` inside the project folder.

**It says the connection must be secure / HTTPS.**
The app only talks to CRMs over a secure `https://` address. Make sure your CRM
URL starts with `https://`.

**Can I change the logo?**
Yes. The splash/login logo is fetched from your CRM's branding endpoint. If the
CRM provides no logo, a built-in logo is used. A manual logo override is also
supported in the app's stored settings.

---

# For developers (optional technical notes)

- **Language / UI:** Kotlin + Jetpack Compose (Material 3).
- **Architecture:** MVVM + Repository pattern.
- **DI:** Hilt. **Database:** Room. **Networking:** Retrofit + OkHttp + Moshi.
- **Background:** WorkManager (periodic + retrying) and a persistent foreground
  service (`dataSync`) with connectivity-aware offline queue flushing.
- **Security:** HTTPS-only (cleartext disabled in network security config and
  rejected in code), JWT stored in `EncryptedSharedPreferences`, automatic token
  refresh on 401, input validation, and graceful handling of malformed JSON and
  API failures with exponential backoff.
- **Min SDK:** 29. **Target/Compile SDK:** 35.
- **Package:** `com.digibrood.crmconnector`.
- **API base path:** `https://<your-crm-host>/api/mobile/v1/`. The host is taken
  from the CRM URL you enter at login; the API path is constant.

## Project structure

```
app/src/main/java/com/digibrood/crmconnector/
├── CrmConnectorApp.kt            # Application (Hilt + WorkManager config + notif channel)
├── MainActivity.kt               # Single-activity Compose host
├── data/
│   ├── local/                    # Room: AppDatabase, entities, DAOs
│   ├── prefs/SecurePrefs.kt      # EncryptedSharedPreferences storage
│   ├── remote/                   # Retrofit API, DTOs, interceptors, NetworkResult
│   └── repository/               # Auth, Device, Call, Recording, Contact, Settings, Branding, Stats
├── di/                           # Hilt modules (Network, Database, App) + qualifiers
├── domain/model/                 # DeviceStatus, CallType enums
├── service/                      # Foreground sync service + notification helper
├── sync/                         # SyncController (orchestration) + SyncManager
├── worker/                       # SyncWorker, RecordingUploadWorker, HeartbeatWorker, SyncScheduler
├── receiver/                     # CallReceiver, BootReceiver
├── overlay/                      # After-call popup activity + view model
├── ui/                           # theme, navigation, components, screens (+ view models)
└── util/                         # Constants, validators, readers, scanners
```

## Building from the command line (advanced, optional)

If you prefer a terminal and already have the Android SDK installed and the
`ANDROID_HOME` (or `ANDROID_SDK_ROOT`) environment variable set:

```bash
# macOS / Linux
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

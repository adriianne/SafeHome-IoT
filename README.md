🏠 SafeHome

A Smart Home IoT Application with Real-Time Electricity Monitoring

Android app + ESP32 IoT device + Firebase Cloud Backend

---

📖 Table of Contents

· Overview
· Features
· Architecture
· Tech Stack
· Project Structure
· Screenshots
· Setup Instructions
  · Prerequisites
  · Firebase Setup
  · Android App Setup
  · ESP32 Setup
· How It Works
· Database Structure
· Testing
· Troubleshooting
· Team
· License

---

🎯 Overview

SafeHome is a smart home IoT application that lets users monitor and control electrical devices remotely using an ESP32 microcontroller connected to Firebase.

The system combines:

Component Purpose
📱 Android App User interface — login, signup, dashboard, device control
🔌 ESP32 Device IoT microcontroller — reads sensors, controls relays, sends data
☁️ Firebase Cloud backend — authentication, real-time database, user data
🏗️ MVP Architecture Clean separation of concerns in the Android app

---

✨ Features

🔐 Authentication & Security

· ✅ Email/Password signup with strict password complexity (8+ chars, uppercase, lowercase, number, special character)
· ✅ Secure login with Firebase Authentication
· ✅ Forgot Password via email reset link
· ✅ Change Password with re-authentication (Firebase security requirement)
· ✅ Session management — auto-login for returning users
· ✅ Sign-out support from Settings

🏠 Dashboard

· ✅ Personalized welcome message with user's name
· ✅ Real-time device count and power usage stats
· ✅ Device list with live status indicators
· ✅ Toggle devices ON/OFF remotely
· ✅ Remove devices from the list
· ✅ Quick actions (Add Device, Scan)

⚙️ Settings

· ✅ User profile display (name, email)
· ✅ Change Password shortcut
· ✅ App version info
· ✅ Sign Out option

🔌 IoT Integration (ESP32)

· ✅ Wi-Fi connectivity via mobile hotspot (2.4 GHz)
· ✅ Firebase Authentication (Email/Password)
· ✅ Real-time data sync to Firebase Realtime Database
· ✅ Remote command handling (ON/OFF/TOGGLE)
· ✅ Automatic reconnection on Wi-Fi loss

---

🏗️ Architecture

This app follows the MVP (Model-View-Presenter) architecture:

```
┌─────────────────────────────────────────────────┐
│                   USER INTERFACE                │
│                                                 │
│   ┌─────────────┐         ┌─────────────────┐   │
│   │    VIEW     │◄───────►│   PRESENTER     │   │
│   │ (Activity)  │         │  (Business      │   │
│   │             │         │   Logic)        │   │
│   └─────────────┘         └────────┬────────┘   │
│          │                         │            │
│          │                         │            │
│          ▼                         ▼            │
│   ┌─────────────┐         ┌─────────────────┐   │
│   │  CONTRACT   │         │     MODEL       │   │
│   │ (Interface) │         │  (Repository)   │   │
│   └─────────────┘         └────────┬────────┘   │
│                                    │            │
└────────────────────────────────────┼────────────┘
                                     │
                                     ▼
                        ┌─────────────────────────┐
                        │  FIREBASE (Cloud)       │
                        │  - Authentication       │
                        │  - Realtime Database    │
                        └────────────┬────────────┘
                                     │
                                     ▼
                        ┌─────────────────────────┐
                        │   ESP32 IoT DEVICE      │
                        │   - Wi-Fi               │
                        │   - Sensors             │
                        │   - Relay Control       │
                        └─────────────────────────┘
```

Layer Responsibilities

Layer Responsibility Files
Contract Defines interfaces between View & Presenter contract/*.kt
View UI only — no business logic view/activities/*.kt
Presenter Business logic, validation, coordination presenter/*.kt
Model Data access, Firebase calls model/repository/*.kt

---

🛠️ Tech Stack

Android App

Technology Version Purpose
Kotlin 1.9.20 Primary language
Android SDK 34 (compileSdk) Target platform
Min SDK 24 Minimum supported Android
Firebase Auth BOM 32.7.0 User authentication
Firebase Realtime DB BOM 32.7.0 Cloud data storage
Material Components 1.11.0 UI components
RecyclerView 1.3.2 Device list
CardView 1.0.0 Card layouts
Coroutines 1.7.3 Async operations
ViewBinding Enabled Type-safe view access

ESP32 Firmware

Technology Version Purpose
Arduino IDE 2.x Development environment
ESP32 Board Package 3.x Hardware support
Firebase ESP Client 4.4.17+ Firebase communication
ArduinoJson 7.x JSON serialization

Backend

Service Purpose
Firebase Authentication User accounts (Email/Password)
Firebase Realtime Database Real-time device/user data

---

📁 Project Structure

```
SafeHome/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/safehome/
│   │   │   ├── contract/                    # MVP Contracts (Interfaces)
│   │   │   │   ├── LoginContract.kt
│   │   │   │   ├── SignupContract.kt
│   │   │   │   ├── ForgotPasswordContract.kt
│   │   │   │   ├── ChangePasswordContract.kt
│   │   │   │   ├── HomeContract.kt
│   │   │   │   └── SettingsContract.kt
│   │   │   │
│   │   │   ├── presenter/                   # MVP Presenters (Logic)
│   │   │   │   ├── LoginPresenter.kt
│   │   │   │   ├── SignupPresenter.kt
│   │   │   │   ├── ForgotPasswordPresenter.kt
│   │   │   │   ├── ChangePasswordPresenter.kt
│   │   │   │   ├── HomePresenter.kt
│   │   │   │   └── SettingsPresenter.kt
│   │   │   │
│   │   │   ├── view/                        # MVP View (UI)
│   │   │   │   ├── activities/
│   │   │   │   │   ├── LoginActivity.kt
│   │   │   │   │   ├── SignupActivity.kt
│   │   │   │   │   ├── ForgotPasswordActivity.kt
│   │   │   │   │   ├── ChangePasswordActivity.kt
│   │   │   │   │   ├── HomeActivity.kt
│   │   │   │   │   └── SettingsActivity.kt
│   │   │   │   └── adapters/
│   │   │   │       └── DeviceAdapter.kt
│   │   │   │
│   │   │   ├── model/                       # Data Layer
│   │   │   │   ├── User.kt
│   │   │   │   ├── Device.kt
│   │   │   │   ├── database/
│   │   │   │   │   └── FirebaseManager.kt
│   │   │   │   └── repository/
│   │   │   │       ├── AuthRepository.kt
│   │   │   │       └── DeviceRepository.kt
│   │   │   │
│   │   │   ├── utils/                       # Helpers
│   │   │   │   ├── Validators.kt
│   │   │   │   └── DateUtils.kt
│   │   │   │
│   │   │   ├── ui/theme/                    # Theme
│   │   │   │   ├── Color.kt
│   │   │   │   ├── Theme.kt
│   │   │   │   └── Type.kt
│   │   │   │
│   │   │   └── MainActivity.kt
│   │   │
│   │   ├── res/
│   │   │   ├── drawable/
│   │   │   │   ├── bg_logo.xml
│   │   │   │   └── ic_settings.xml
│   │   │   ├── layout/
│   │   │   │   ├── activity_login.xml
│   │   │   │   ├── activity_signup.xml
│   │   │   │   ├── activity_forgot_password.xml
│   │   │   │   ├── activity_change_password.xml
│   │   │   │   ├── activity_home.xml
│   │   │   │   ├── activity_settings.xml
│   │   │   │   └── item_device.xml
│   │   │   ├── values/
│   │   │   │   ├── colors.xml
│   │   │   │   ├── strings.xml
│   │   │   │   └── themes.xml
│   │   │   └── mipmap/
│   │   │
│   │   └── AndroidManifest.xml
│   │
│   ├── google-services.json                 # Firebase config
│   └── build.gradle.kts
│
├── esp32/                                   # ESP32 Firmware
│   └── sketch_sep10a.ino
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md                                # This file
```

---

📸 Screenshots

Login Signup Dashboard
Email + password form 5-field registration Device list + stats
Forgot password link Complex password validation Real-time device control
Auto-login check Firebase Auth integration Toggle/Remove devices

Forgot Password Change Password Settings
Email reset form Current + new + confirm User info + options
Firebase reset email Re-authentication required Sign out

---

🚀 Setup Instructions

Prerequisites

Tool Version Download
Android Studio Hedgehog (2023.1.1)+ developer.android.com
JDK 17+ adoptium.net
Arduino IDE 2.x arduino.cc
Firebase Account Free tier firebase.google.com

---

Firebase Setup

1. Create a Firebase Project
   · Go to Firebase Console
   · Create new project: SafeHome (or your name)
   · Note your Project ID and Web API Key
2. Enable Authentication
   · Go to Authentication → Sign-in method
   · Enable Email/Password
3. Create Realtime Database
   · Go to Realtime Database → Create database
   · Choose region: asia-southeast1 (or closest to you)
   · Start in Test Mode (change rules later for production)
4. Set Database Rules
   ```json
   {
     "rules": {
       ".read": "auth != null",
       ".write": "auth != null"
     }
   }
   ```
5. Register an App
   · Project Settings → Your apps
   · Click Android icon → package name: com.example.safehome
   · Download google-services.json
   · Place in app/ folder
6. Add Web App (for ESP32)
   · Project Settings → Your apps → </> (Web)
   · App nickname: SafeHome Web
   · Copy the apiKey from the config

---

Android App Setup

1. Clone the Repository
   ```bash
   git clone https://github.com/your-username/SafeHome.git
   cd SafeHome
   ```
2. Add Firebase Config
   · Place google-services.json in app/ folder (downloaded from Firebase Console)
3. Verify build.gradle.kts Dependencies
   ```kotlin
   dependencies {
       // Firebase
       implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
       implementation("com.google.firebase:firebase-auth-ktx")
       implementation("com.google.firebase:firebase-database-ktx")
       
       // UI
       implementation("com.google.android.material:material:1.11.0")
       implementation("androidx.recyclerview:recyclerview:1.3.2")
       implementation("androidx.cardview:cardview:1.0.0")
       
       // Coroutines
       implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
   }
   ```
4. Build & Run
   · Open project in Android Studio
   · Build → Clean Project
   · Build → Rebuild Project
   · Connect emulator or device
   · Click Run ▶

---

ESP32 Setup

1. Install Arduino IDE Libraries
   · Sketch → Include Library → Manage Libraries
   · Install:
     · Firebase Arduino Client Library by Mobizt
     · ArduinoJson by Benoit Blanchon
2. Create Test User in Firebase
   · Firebase Console → Authentication → Users → Add user
   · Email: esp32@safehome.com
   · Password: ESP32Test!2026 (or your choice)
3. Configure ESP32 Code
   ```cpp
   #define WIFI_SSID     "YourHotspotName"
   #define WIFI_PASSWORD "YourHotspotPassword"
   
   #define API_KEY       "AIzaSy..."           // Web API Key from Firebase
   #define DATABASE_URL  "https://your-project-default-rtdb.REGION.firebasedatabase.app"
   #define USER_EMAIL    "esp32@safehome.com"
   #define USER_PASSWORD "ESP32Test!2026"
   ```
4. Upload to ESP32
   · Connect ESP32 via USB
   · Tools → Board → ESP32 Dev Module
   · Tools → Port → COMx
   · Click Upload ▶
5. Verify Serial Monitor
   ```
   Connecting to Wi-Fi....
   Connected. IP: 192.168.x.x
   Sign-up OK
   Write OK: 0
   Read value: 0
   ```

---

🔄 How It Works

Data Flow: User Registration

```
User fills Signup form
    ↓
SignupPresenter validates input
    ↓
AuthRepository.signup() calls Firebase
    ↓
Firebase creates user + auto-signs-in
    ↓
User data saved to Realtime Database /users/{uid}
    ↓
Navigate to Home screen
```

Data Flow: Remote Device Control

```
User taps "Turn On" in app
    ↓
HomePresenter.toggleDevice()
    ↓
App writes "ON" to /devices/{id}/command
    ↓
ESP32 polls for commands
    ↓
ESP32 reads "ON" → toggles relay → updates /devices/{id}/isOn
    ↓
App sees update → UI reflects new state
```

---

🗄️ Database Structure

```
SafeHome Realtime Database:
│
├── users/
│   └── {uid}/
│       ├── firstName: "Juan"
│       ├── lastName: "Dela Cruz"
│       ├── email: "juan@example.com"
│       └── createdAt: 1726567890
│
├── devices/
│   └── ESP32_001/
│       ├── name: "Living Room Light"
│       ├── type: "smart_plug"
│       ├── isConnected: true
│       ├── isOn: false
│       ├── powerUsage: 45.2
│       ├── lastSeen: 1726567890
│       └── command: "none"           ← App writes here
│
└── test/
    └── data: 42                      ← ESP32 test data
```

---

🧪 Testing

Manual Test Cases

# Test Expected
1 Signup with weak password Error shown
2 Signup with existing email Error: "Email already in use"
3 Signup with valid data Navigate to Home
4 Login with wrong password Error: "Invalid credentials"
5 Login with correct credentials Navigate to Home
6 Forgot Password flow Reset email sent
7 Change Password (wrong current) Error shown
8 Change Password (correct) Success + re-login
9 Toggle device ON/OFF UI updates + Firebase reflects
10 Sign Out Return to Login

Run Unit Tests

```bash
./gradlew test
```

Run Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

---

🚨 Troubleshooting

Common Issues

Issue Cause Fix
Firebase_ESP_Client.h not found Wrong library installed Install "Firebase Arduino Client Library" (not "Firebase ESP32 Client")
TOO_MANY_ATTEMPTS_TRY_LATER Firebase rate limit Wait 30 min, unplug ESP32
SERVICE_NOT_AVAILABLE Network issue Check hotspot is 2.4 GHz
Spinner stuck on signup Coroutine timeout Check SignupPresenter.kt has hideLoading() in finally
UninitializedPropertyAccessException lateinit var accessed before init Make presenter nullable + use ?.
App crashes on Settings → Change Password Activity not extending AppCompatActivity Verify class ChangePasswordActivity : AppCompatActivity()
Email already in use error Testing with same email Use a fresh email or delete from Firebase Console
Unresolved reference: R Build cache stale Build → Clean → Rebuild
Redeclaration error Duplicate class Search for duplicate file with Ctrl+Shift+F

---

👥 Team

Role Name Contribution
Project Leader [Your Name] Architecture, Integration
Android Developer [Name] MVP screens, Firebase Auth
IoT Developer [Name] ESP32 firmware, sensor integration
QA / Testing [Name] Test cases, bug reports
Documentation [Name] README, user manual

---

📝 License

This project is developed as part of an academic requirement for [Course Name] at [University Name].

Academic Use Only — Not for commercial distribution.

---

🙏 Acknowledgments

· Firebase for cloud infrastructure
· Mobizt for the Firebase ESP Client library
· Wokwi for ESP32 simulation tools
· Android Developer Community for documentation and support

---

📞 Contact

For questions or issues:

· 📧 Email: [your-email@example.com]
· 🐛 Issues: GitHub Issues
· 📚 Wiki: Project Wiki

---

<div align="center">

⭐ If you find this project useful, please give it a star! ⭐

Made with ❤️ by the SafeHome Team

</div>

---

📌 Version History

Version Date Changes
1.0.0 Sept 2026 Initial release — MVP architecture, Firebase Auth, ESP32 integration
0.9.0 Aug 2026 Beta — login, signup, home dashboard
0.5.0 Jul 2026 Alpha — Firebase connection prototype

---

🎯 Roadmap

☑ Email/Password authentication
☑ Forgot Password flow
☑ Change Password with re-auth
☑ Real-time device dashboard
☑ ESP32 ↔ Firebase integration
☐ Bluetooth device scanning
☐ Power usage analytics
☐ Push notifications for high usage
☐ Device scheduling (timers)
☐ Multi-user device sharing
☐ Widget support
☐ Dark mode

---

Last Updated: September 2026

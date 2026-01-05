# StepSync - Android Fitness Tracking Application

<div align="center">

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)
![Target SDK](https://img.shields.io/badge/Target%20SDK-34-blue.svg)
![License](https://img.shields.io/badge/License-Educational-lightgrey.svg)

A comprehensive Android fitness tracking application built with Kotlin and Jetpack Compose. 

**[Features](#features) • [Setup](#-setup-instructions) • [Architecture](#architecture) • [Technologies](#technologies-used) • [Contributing](#contributing)**

</div>

---

## 📋 Table of Contents

- [Features](#features)
- [Setup Instructions](#-setup-instructions)
  - [Prerequisites](#prerequisites)
  - [Installation Steps](#installation-steps)
  - [Firebase Configuration](#firebase-configuration)
  - [Running the App](#running-the-app)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
- [Technologies Used](#technologies-used)
- [Permissions](#required-permissions)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#contributing)
- [Authors](#authors)

---

## Features

### Core Features
- ✅ **User Authentication & Profile Management**:  Register, login, and manage your profile
- 📊 **Step Tracking**: Real-time step counting with background service support
- 🏃 **Activity Tracking**: Track multiple activity types (Walking, Running, Cycling, Gym, Swimming)
- 🎯 **Goals & Challenges**: Set and track daily, weekly, and monthly fitness goals
- 👥 **Social Features**: Add friends, view leaderboards, and participate in challenges
- 📈 **Data Visualization**: Charts and graphs for step count, activities, and calories
- 🔔 **Notifications**: Daily reminders and achievement notifications

### Technical Features
- **MVVM Architecture** with Clean Architecture principles
- **Firebase Authentication** for secure user authentication
- **Firestore Database** for cloud-based data persistence with offline support
- **Hilt** for Dependency Injection
- **Jetpack Compose** for modern UI with Material Design 3
- **Coroutines & Flow** for asynchronous operations
- **Foreground Service** for background step counting
- **WorkManager** for periodic sync tasks

---

## 🚀 Setup Instructions

### Prerequisites

Before you begin, ensure you have the following installed:

| Requirement | Version | Download Link |
|------------|---------|---------------|
| **Android Studio** | Hedgehog (2023.1.1) or later | [Download](https://developer.android.com/studio) |
| **JDK** | Java 17 | Bundled with Android Studio |
| **Android SDK** | API 26+ (Android 8.0+) | Android Studio SDK Manager |
| **Gradle** | 8.2 or later | Bundled with project |
| **Git** | Latest | [Download](https://git-scm.com/downloads) |
| **Firebase Account** | Free tier | [Sign up](https://firebase.google.com/) |

**Hardware Requirements:**
- Physical Android device with step counter sensor (recommended) OR
- Android Emulator with API 26+ (limited step tracking functionality)

---

### Installation Steps

#### 1️⃣ Clone the Repository

```bash
# Clone the repository
git clone https://github.com/EliasAzar240935/StepSync_Elias-Azar_Anthony-Chahine.git

# Navigate to the project directory
cd StepSync_Elias-Azar_Anthony-Chahine
```

#### 2️⃣ Open in Android Studio

1. Launch **Android Studio**
2. Click **File** → **Open**
3. Navigate to the cloned project folder
4. Click **OK** and wait for Android Studio to load the project

#### 3️⃣ Sync Gradle Dependencies

Android Studio should automatically start syncing Gradle.  If not: 

1. Click **File** → **Sync Project with Gradle Files**
2. Wait for the sync to complete (this may take a few minutes)
3. Resolve any dependency conflicts if prompted

> **Note**: If you encounter Gradle sync errors, try: 
> - **File** → **Invalidate Caches / Restart**
> - Update Gradle wrapper: `./gradlew wrapper --gradle-version=8.2`

---

### Firebase Configuration

#### 📌 Step 1: Create a Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **Add project** or **Create a project**
3. Enter project name:  `StepSync` (or any name you prefer)
4. (Optional) Enable Google Analytics
5. Click **Create project**

#### 📌 Step 2: Add Android App to Firebase

1. In Firebase Console, click **Add app** → **Android (icon)**
2. Fill in the registration form:
   - **Android package name**: `com.stepsync` ⚠️ **Must match exactly**
   - **App nickname**: `StepSync` (optional)
   - **Debug signing certificate SHA-1**: (optional, needed for Google Sign-In)
3. Click **Register app**

#### 📌 Step 3: Download `google-services.json`

1. Download the `google-services.json` file
2. Place it in the `app/` directory of your project: 
   ```
   StepSync_Elias-Azar_Anthony-Chahine/
   ├── app/
   │   ├── google-services.json  ← Place here
   │   ├── build.gradle. kts
   │   └── src/
   ```

> ⚠️ **IMPORTANT**: The `google-services.json` file is already included in the repository, but it contains placeholder configuration. You **MUST** replace it with your own Firebase configuration file.

#### 📌 Step 4: Enable Firebase Services

In Firebase Console, enable the following services:

**1. Authentication**
- Navigate to **Build** → **Authentication**
- Click **Get started**
- Enable **Email/Password** sign-in method
- Click **Save**

**2. Firestore Database**
- Navigate to **Build** → **Firestore Database**
- Click **Create database**
- Choose **Start in test mode** (for development)
- Select a Cloud Firestore location (choose nearest to you)
- Click **Enable**

**3. Cloud Messaging** (Optional for push notifications)
- Navigate to **Build** → **Cloud Messaging**
- Cloud Messaging is automatically enabled

**4. Analytics** (Optional)
- Navigate to **Build** → **Analytics**
- Follow setup instructions if needed

#### 📌 Step 5: Configure Firestore Security Rules (Recommended)

Replace the default Firestore rules with secure production rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection
    match /users/{userId} {
      allow read:  if request.auth != null;
      allow write: if request. auth != null && request.auth. uid == userId;
    }
    
    // Step records
    match /stepRecords/{recordId} {
      allow read, write: if request.auth != null;
    }
    
    // Activities
    match /activities/{activityId} {
      allow read, write: if request.auth != null;
    }
    
    // Goals
    match /goals/{goalId} {
      allow read, write: if request.auth != null;
    }
    
    // Friends
    match /friends/{friendId} {
      allow read, write: if request.auth != null;
    }
    
    // Achievements
    match /achievements/{achievementId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

#### 📌 Step 6: Verify Firebase Setup

Check that these files exist and are configured: 

- ✅ `app/google-services.json` (your Firebase config)
- ✅ `app/build.gradle.kts` contains `id("com.google.gms.google-services")`
- ✅ `build.gradle.kts` contains Firebase dependencies

---

### Running the App

#### 🔧 Option 1: Run on Physical Device (Recommended)

1. **Enable Developer Options** on your Android device: 
   - Go to **Settings** → **About phone**
   - Tap **Build number** 7 times
   - Go back to **Settings** → **Developer options**
   - Enable **USB debugging**

2. **Connect Device**: 
   - Connect your device via USB
   - Allow USB debugging when prompted
   - In Android Studio, select your device from the device dropdown

3. **Run the App**:
   - Click the **Run** button (▶️) or press `Shift + F10`
   - Grant permissions when prompted: 
     - ✅ Activity Recognition
     - ✅ Notifications
     - ✅ Location (optional)

#### 🖥️ Option 2: Run on Emulator

1. **Create Virtual Device**:
   - Click **Tools** → **Device Manager**
   - Click **Create Device**
   - Select a device (e.g., Pixel 6)
   - Download and select system image (API 26+, recommended:  API 34)
   - Click **Finish**

2. **Run the App**:
   - Select the emulator from the device dropdown
   - Click **Run** (▶️)

> ⚠️ **Note**: Step counting functionality is limited on emulators as they don't have physical sensors.

---

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/stepsync/
│   │   │   ├── MainActivity.kt           # Main entry point
│   │   │   ├── StepSyncApplication.kt    # Application class
│   │   │   ├── data/                     # Data layer
│   │   │   │   ├── repository/           # Repository implementations
│   │   │   │   └── model/                # Data models
│   │   │   ├── domain/                   # Domain layer
│   │   │   │   ├── usecase/              # Use cases (business logic)
│   │   │   │   └── repository/           # Repository interfaces
│   │   │   ├── presentation/             # Presentation layer
│   │   │   │   ├── auth/                 # Authentication screens
│   │   │   │   ├── home/                 # Home/Dashboard
│   │   │   │   ├── profile/              # Profile management
│   │   │   │   ├── activity/             # Activity tracking
│   │   │   │   ├── goals/                # Goals & challenges
│   │   │   │   ├── social/               # Social features
│   │   │   │   ├── statistics/           # Charts & statistics
│   │   │   │   ├── settings/             # App settings
│   │   │   │   └── theme/                # Material Design 3 theme
│   │   │   ├── service/                  # Background services
│   │   │   │   └── StepCounterService.kt # Foreground step counter
│   │   │   ├── worker/                   # WorkManager workers
│   │   │   ├── util/                     # Utility classes
│   │   │   └── di/                       # Dependency injection modules
│   │   ├── res/                          # Resources
│   │   │   ├── drawable/                 # Images & icons
│   │   │   ├── values/                   # Strings, colors, themes
│   │   │   └── xml/                      # XML configurations
│   │   └── AndroidManifest.xml           # App manifest
│   └── build. gradle.kts                  # App-level Gradle config
├── build.gradle.kts                      # Project-level Gradle config
├── gradle.properties                     # Gradle properties
└── settings.gradle.kts                   # Gradle settings
```

---

## Architecture

The app follows **Clean Architecture** principles with **MVVM** pattern:

```
┌─────────────────────────────────────────────┐
│         Presentation Layer (UI)             │
│  ┌────────────────────────────────────────┐ │
│  │  Jetpack Compose UI Components         │ │
│  │  • Screens                              │ │
│  │  • ViewModels (StateFlow)              │ │
│  └────────────────────────────────────────┘ │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│          Domain Layer (Business)            │
│  ┌────────────────────────────────────────┐ │
│  │  Use Cases                              │ │
│  │  Repository Interfaces                  │ │
│  └────────────────────────────────────────┘ │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│           Data Layer (Storage)              │
│  ┌────────────────────────────────────────┐ │
│  │  Repository Implementations             │ │
│  │  • Firebase Firestore                   │ │
│  │  • Firebase Authentication              │ │
│  │  • DataStore (Preferences)              │ │
│  └────────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
```

### Key Design Patterns
- **MVVM (Model-View-ViewModel)**: Separation of UI and business logic
- **Repository Pattern**:  Abstraction over data sources
- **Dependency Injection**:  Hilt for loose coupling
- **Observer Pattern**:  Reactive UI updates with StateFlow/Flow
- **Offline-First**: Firestore offline persistence

---

## Technologies Used

### 🎨 UI & Design
- **Jetpack Compose** - Modern declarative UI
- **Material Design 3** - Google's latest design system
- **Coil** - Image loading library

### 🏗️ Architecture & DI
- **Hilt/Dagger** - Dependency injection
- **ViewModel** - Lifecycle-aware UI state
- **Navigation Component** - Screen navigation

### 🔥 Backend & Storage
- **Firebase Authentication** - User authentication
- **Cloud Firestore** - NoSQL cloud database with offline support
- **Firebase Cloud Messaging** - Push notifications
- **Firebase Analytics** - User analytics
- **DataStore** - Key-value storage

### ⚙️ Background Processing
- **WorkManager** - Background job scheduling
- **Foreground Service** - Continuous step tracking
- **Kotlin Coroutines** - Asynchronous programming
- **Flow/StateFlow** - Reactive data streams

### 📊 Data Visualization
- **MPAndroidChart** - Charts and graphs

### 🌐 Networking (Ready for Backend)
- **Retrofit** - Type-safe HTTP client
- **OkHttp** - HTTP/HTTPS implementation
- **Gson** - JSON serialization

### 🧪 Testing
- **JUnit** - Unit testing
- **Espresso** - UI testing
- **Compose UI Test** - Compose testing

---

## Required Permissions

The app requires the following permissions:

| Permission | Purpose | Required |
|-----------|---------|----------|
| `ACTIVITY_RECOGNITION` | Step counting using device sensors | ✅ Yes |
| `FOREGROUND_SERVICE` | Background step tracking service | ✅ Yes |
| `FOREGROUND_SERVICE_HEALTH` | Health-related foreground service | ✅ Yes |
| `POST_NOTIFICATIONS` | Display notifications to user | ✅ Yes |
| `INTERNET` | Firebase connectivity | ✅ Yes |
| `ACCESS_NETWORK_STATE` | Check network availability | ✅ Yes |
| `WAKE_LOCK` | Keep CPU awake for step counting | ✅ Yes |
| `ACCESS_FINE_LOCATION` | GPS-based activity tracking | ⚠️ Optional |
| `ACCESS_COARSE_LOCATION` | Approximate location | ⚠️ Optional |

**Runtime Permissions**: The app requests `ACTIVITY_RECOGNITION` and `POST_NOTIFICATIONS` at runtime. 

---

## 🔍 Troubleshooting

### Common Issues and Solutions

#### ❌ **Gradle Sync Failed**
```
Solution 1: File → Invalidate Caches / Restart
Solution 2: Update Gradle wrapper:  ./gradlew wrapper --gradle-version=8.2
Solution 3: Check internet connection and proxy settings
```

#### ❌ **Firebase Initialization Failed**
```
Error: Default FirebaseApp is not initialized
Solution: 
1. Verify google-services.json is in app/ directory
2. Check package name matches:  com.stepsync
3. Sync Gradle files
4. Clean and rebuild:  Build → Clean Project → Rebuild Project
```

#### ❌ **Step Counter Not Working**
```
Solution 1: Grant ACTIVITY_RECOGNITION permission
Solution 2: Ensure device has step counter sensor (physical device)
Solution 3: Start foreground service by granting all permissions
```

#### ❌ **Duplicate Class Error (WorkManager)**
```
Error: Duplicate class androidx.work... 
Solution: Remove duplicate dependencies in app/build.gradle.kts
- Lines 90 and 139 have duplicate work-runtime-ktx
- Lines 91-92 and 144-145 have duplicate hilt-work
```

#### ❌ **Compilation Error with Kotlin**
```
Error: Could not resolve org.jetbrains.kotlin:kotlin-stdlib
Solution:  Ensure Kotlin version in build.gradle.kts is 1.9.20 or compatible
```

#### ❌ **App Crashes on Launch**
```
Solution 1: Check Logcat for stack trace
Solution 2: Verify all Firebase services are enabled
Solution 3: Ensure all required permissions are granted
Solution 4: Clear app data and reinstall
```

#### ❌ **Charts Not Displaying**
```
Solution:  Add JitPack repository to settings.gradle.kts:
maven { url = uri("https://jitpack.io") }
```

---

## 🧪 Running Tests

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Code Coverage
```bash
./gradlew jacocoTestReport
```

---

## 📱 Build Variants

### Debug Build
```bash
./gradlew assembleDebug
```
- Includes debugging tools
- Connects to Firebase Debug instance

### Release Build
```bash
./gradlew assembleRelease
```
- Minified and optimized
- Requires signing configuration

---

## 🔐 Security Best Practices

⚠️ **Before deploying to production:**

1. **Update Firestore Security Rules** (see Firebase Configuration section)
2. **Remove test mode** from Firebase
3. **Enable App Check** in Firebase Console
4. **Add ProGuard rules** for code obfuscation
5. **Store API keys** in local. properties (not in version control)
6. **Enable Play Integrity API** for app verification

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork** the repository
2. Create a **feature branch**:  `git checkout -b feature/amazing-feature`
3. **Commit** your changes: `git commit -m 'Add amazing feature'`
4. **Push** to the branch: `git push origin feature/amazing-feature`
5. Open a **Pull Request**

### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add comments for complex logic
- Write unit tests for new features

---

## 👨‍💻 Authors

**Elias Azar** & **Anthony Chahine**

- GitHub: [@EliasAzar240935](https://github.com/EliasAzar240935)
- Repository: [StepSync_Elias-Azar_Anthony-Chahine](https://github.com/EliasAzar240935/StepSync_Elias-Azar_Anthony-Chahine)

---

## 📄 License

This project is created as a demonstration of Android development best practices.  
Educational use only. 

---

## 📞 Support

For questions or issues: 
- Open an [Issue](https://github.com/EliasAzar240935/StepSync_Elias-Azar_Anthony-Chahine/issues)
- Check existing [Discussions](https://github.com/EliasAzar240935/StepSync_Elias-Azar_Anthony-Chahine/discussions)

---

## 📚 Additional Resources

- [Android Developer Documentation](https://developer.android.com/docs)
- [Jetpack Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Material Design 3](https://m3.material.io/)

---

## ⚡ Quick Start Checklist

- [ ] Install Android Studio Hedgehog or later
- [ ] Clone the repository
- [ ] Create Firebase project
- [ ] Download and add `google-services.json`
- [ ] Enable Firebase Authentication (Email/Password)
- [ ] Enable Firestore Database
- [ ] Sync Gradle dependencies
- [ ] Connect physical device or create emulator
- [ ] Run the app
- [ ] Grant all requested permissions
- [ ] Create a test account and explore features

---

<div align="center">

**Built with ❤️ using Kotlin & Jetpack Compose**

⭐ Star this repo if you find it helpful!

</div>

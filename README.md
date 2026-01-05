# StepSync - Android Fitness Tracking Application

A comprehensive Android fitness tracking application built with Kotlin and Jetpack Compose.

## Features

### Core Features
- **User Authentication & Profile Management**: Register, login, and manage your profile
- **Step Tracking**: Real-time step counting with background service support
- **Activity Tracking**: Track multiple activity types (Walking, Running, Cycling, Gym, Swimming)
- **Goals & Challenges**: Set and track daily, weekly, and monthly fitness goals
- **Social Features**: Add friends, view leaderboards, and participate in challenges
- **Data Visualization**: Charts and graphs for step count, activities, and calories
- **Notifications**: Daily reminders and achievement notifications

### Technical Features
- **MVVM Architecture** with Clean Architecture principles
- **Firebase Authentication** for secure user authentication
- **Firestore Database** for cloud-based data persistence with offline support
- **Hilt** for Dependency Injection
- **Jetpack Compose** for modern UI with Material Design 3
- **Coroutines & Flow** for asynchronous operations
- **Foreground Service** for background step counting
- **WorkManager** for periodic sync tasks

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/stepsync/
│   │   │   ├── data/                  # Data layer
│   │   │   │   ├── local/             # Room database
│   │   │   │   │   ├── database/      # Database definition
│   │   │   │   │   ├── dao/           # Data Access Objects
│   │   │   │   │   └── entities/      # Database entities
│   │   │   │   ├── repository/        # Repository implementations
│   │   │   │   └── model/             # Data models
│   │   │   ├── domain/                # Domain layer
│   │   │   │   ├── usecase/           # Use cases (business logic)
│   │   │   │   └── repository/        # Repository interfaces
│   │   │   ├── presentation/          # Presentation layer
│   │   │   │   ├── auth/              # Authentication screens
│   │   │   │   ├── home/              # Home/Dashboard
│   │   │   │   ├── profile/           # Profile management
│   │   │   │   ├── activity/          # Activity tracking
│   │   │   │   ├── goals/             # Goals & challenges
│   │   │   │   ├── social/            # Social features
│   │   │   │   ├── statistics/        # Charts & statistics
│   │   │   │   ├── settings/          # App settings
│   │   │   │   └── theme/             # Material Design 3 theme
│   │   │   ├── service/               # Background services
│   │   │   ├── util/                  # Utility classes
│   │   │   ├── di/                    # Dependency injection modules
│   │   │   └── StepSyncApplication.kt # Application class
│   │   ├── res/                       # Resources
│   │   └── AndroidManifest.xml        # App manifest
│   └── build.gradle.kts               # App-level Gradle configuration
└── build.gradle.kts                   # Project-level Gradle configuration
```

## Database Schema

### Firestore Collections
The app uses Firebase Firestore with the following collections:
- **users**: User profile information
- **stepRecords**: Daily step count records
- **activities**: Tracked fitness activities
- **goals**: User-defined fitness goals
- **friends**: Friend relationships
- **achievements**: Unlocked achievements

See [FIREBASE_SETUP.md](FIREBASE_SETUP.md) for detailed Firestore structure and setup instructions.

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 26 or higher
- Kotlin 1.9.20 or later
- Gradle 8.2 or later
- **Firebase Project** (see [FIREBASE_SETUP.md](FIREBASE_SETUP.md))

### Building the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/EliasAzar240935/StepSync.git
   cd StepSync
   ```

2. Set up Firebase:
   - Follow the instructions in [FIREBASE_SETUP.md](FIREBASE_SETUP.md)
   - Download and place `google-services.json` in the `app/` directory

3. Open the project in Android Studio

4. Sync Gradle files

5. Build and run the application on an emulator or physical device

### Required Permissions
- `ACTIVITY_RECOGNITION` - For step counting
- `FOREGROUND_SERVICE` - For background step tracking
- `POST_NOTIFICATIONS` - For notifications
- `ACCESS_FINE_LOCATION` - For GPS-based activities (optional)
- `WAKE_LOCK` - For background service

## Technologies Used

### Core Libraries
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern Android UI toolkit
- **Material Design 3** - Design system

### Architecture Components
- **Firebase Authentication** - User authentication
- **Firestore** - Cloud database with offline support
- **ViewModel** - UI state management
- **Flow** - Reactive data streams
- **Navigation Component** - Screen navigation

### Dependency Injection
- **Hilt/Dagger** - Dependency injection framework

### Asynchronous Programming
- **Kotlin Coroutines** - Asynchronous programming
- **Flow** - Reactive streams

### Background Tasks
- **WorkManager** - Background job scheduling
- **Foreground Service** - Continuous step tracking

### Data Visualization
- **MPAndroidChart** - Charts and graphs

### Image Loading
- **Coil** - Image loading library

### Networking (Ready for Backend)
- **Retrofit** - HTTP client
- **OkHttp** - HTTP/HTTPS implementation
- **Gson** - JSON serialization

## Features in Detail

### Authentication
- Firebase email/password authentication
- User registration with profile creation
- Secure user login with validation
- Session management with Firebase Auth

### Step Tracking
- Real-time step counting using device sensors
- Daily step goal tracking
- Progress visualization
- Step history with charts

### Activity Tracking
- Start/stop activity tracking
- Multiple activity types support
- Distance and calorie calculations
- Activity history logs

### Goals & Achievements
- Create custom fitness goals
- Daily, weekly, and monthly challenges
- Achievement system with badges
- Progress tracking and notifications

### Social Features
- Friend management (add/remove friends)
- Activity feed
- Leaderboards
- Group challenges

### Statistics & Visualization
- Step count charts (daily, weekly, monthly)
- Activity distribution graphs
- Calorie burn visualization
- Trend analysis

## Architecture

The app follows Clean Architecture principles with three main layers:

1. **Data Layer**: Firebase repositories, Firestore data access, and data models
2. **Domain Layer**: Repository interfaces and business logic (use cases)
3. **Presentation Layer**: ViewModels, UI screens (Composables)

### Key Design Patterns
- **MVVM (Model-View-ViewModel)**: Separation of UI and business logic
- **Repository Pattern**: Abstraction over data sources (Firebase)
- **Dependency Injection**: Loose coupling and testability
- **Observer Pattern**: Reactive UI updates with StateFlow
- **Offline-First**: Firestore offline persistence for seamless user experience

## Future Enhancements

- Real-time friend activity updates
- Push notifications with Firebase Cloud Messaging
- Advanced analytics with machine learning
- Integration with wearable devices
- Social media sharing
- Premium features (custom themes, advanced stats)
- Multi-device synchronization

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Data Migration

**Note**: This app now uses Firebase for data storage. If you were using a previous version with Room Database, your local data will not be automatically migrated. For information about migrating from Room to Firebase, see [FIREBASE_MIGRATION.md](FIREBASE_MIGRATION.md).

## License

This project is created as a demonstration of Android development best practices.

## Contact

For questions or suggestions, please open an issue on GitHub.

---

**Note**: This application requires an Android device with a step counter sensor for accurate step tracking. The app will work in emulators but step tracking functionality will be limited.

# Stream / DealShot - Android Project Architecture & Guidelines

This document provides a comprehensive overview of the **Stream / DealShot** Android codebase, detailing its build system, architectural layers, key components, functions, data flow, and **strict guidelines for AI agents and human developers** to follow when extending or modifying the project.

---

## 1. Project Overview & Target Platforms

- **Project Name:** Stream / DealShot (`com.dealshot.app`)
- **Primary Domain:** Video Streaming Platform (Mobile & Android TV)
- **Programming Language:** Kotlin (100% Jetpack Compose)
- **Minimum SDK:** API 26 (Android 8.0)
- **Target SDK:** API 36 | **Compile SDK:** API 37
- **Java Compatibility:** Java 17

### Build Flavors (`deviceType` Dimension)
The project is built as a single codebase supporting two distinct device form-factors using Gradle product flavors:
1. **`mobile` Flavor**:
   - **Application ID:** `com.dealshot.app`
   - **Launcher Category:** `android.intent.category.LAUNCHER`
   - **UI Framework:** Standard Jetpack Compose Material3.
   - **Target Devices:** Smartphones and Tablets (Touchscreen required).
2. **`tv` Flavor**:
   - **Application ID:** `com.dealshot.app.tv`
   - **Launcher Category:** `android.intent.category.LEANBACK_LAUNCHER`
   - **UI Framework:** Jetpack Compose for TV (`androidx.tv:tv-foundation`, `androidx.tv:tv-material`).
   - **Target Devices:** Android TV / Google TV (D-Pad / Leanback navigation).

---

## 2. Technology Stack & Key Dependencies

| Category | Library / Dependency | Purpose / Usage |
| :--- | :--- | :--- |
| **UI Framework** | Jetpack Compose (Material3 / TV Material3) | Declarative UI for both Mobile and TV form factors |
| **Navigation** | Navigation Compose (`2.9.6`) | Single-Activity navigation handling route parameters |
| **Media Playback** | Jetpack Media3 ExoPlayer (`1.8.0`) | Video playback, HLS streaming, ExoPlayer controls |
| **Networking** | Retrofit (`2.11.0`) + OkHttp3 (`4.12.0`) | REST API communication |
| **Serialization** | Kotlinx Serialization JSON (`1.8.0`) | JSON parsing & DTO serialization |
| **Storage & Security** | Jetpack DataStore Preferences + Android KeyStore | Local preference persistence & AES/GCM encrypted tokens |
| **Image Loading** | Coil Compose (`2.7.0`) + Glide Compose | Image and thumbnail loading |
| **Splash Screen** | `androidx.core:core-splashscreen` | Standardized Android splash screen |

---

## 3. High-Level Architectural Pattern (Clean Architecture + MVI)

The project strictly follows **Clean Architecture** combined with **MVI (Model-View-Intent) / Unidirectional Data Flow (UDF)**:

```
                  ┌─────────────────────────────────────────┐
                  │          UI Layer (Mobile / TV)         │
                  │  (Composables, Routes, Activities)      │
                  └────────────────────┬────────────────────┘
                                       │
                              Emits    │ Observes
                             Intents   │ UiState & Effects
                                       ▼
                  ┌─────────────────────────────────────────┐
                  │    Shared Presentation Layer (MVI)      │
                  │    (ViewModels, Contracts, States)      │
                  └────────────────────┬────────────────────┘
                                       │ Calls
                                       ▼
                  ┌─────────────────────────────────────────┐
                  │               Data Layer                │
                  │  (Repositories, API Services, DataStore)│
                  └────────────────────┬────────────────────┘
                                       │ Fetch / Persist
                                       ▼
                  ┌─────────────────────────────────────────┐
                  │          Core & Remote Infrastructure   │
                  │  (ApiClient, Media3Manager, Network)    │
                  └─────────────────────────────────────────┘
```

---

## 4. Detailed Layer Breakdown & File Index

### 4.1. Core Layer (`com.dealshot.app.core`)
Contains foundational, app-wide utilities decoupled from business logic:
- **`core/media/Media3Manager.kt`**:
  - *Job:* Central manager for Jetpack Media3 / ExoPlayer instance.
  - *Key Functions:*
    - `initialize(videoUrl, position)`: Resolves streaming URL vs local raw resource (`android.resource://`) and prepares playback.
    - `play()`, `pause()`, `togglePlayPause()`, `seekTo()`, `rewind10Seconds()`, `forward10Seconds()`, `setPlaybackSpeed()`.
    - `addListener()`, `removeListener()`, `release()`.
- **`core/error/AppErrorHandler.kt`**:
  - *Job:* Maps network/system exceptions (`UnknownHostException`, `SocketTimeoutException`, Retrofit `HttpException` status codes 401, 403, 404, 5xx) to standardized, human-readable user messages.

---

### 4.2. Data Layer (`com.dealshot.app.data`)

#### Preferences & Security (`data/preferences/`)
- **`UserPreferences.kt`**:
  - *Job:* Reactive wrapper around Jetpack DataStore. Manages login tokens, user ID, phone number, subscription states, and session data.
  - *Key Functions:* `saveLoginSession()`, `authToken`, `userId`, `clearUser()`.
- **`SecureTokenStorage.kt`**:
  - *Job:* Encrypts and decrypts authentication tokens using Android KeyStore with `AES/GCM/NoPadding`.
  - *Key Functions:* `encrypt(token)`, `decrypt(encryptedToken)`, `clearKey()`.

#### Network & API (`data/remote/`)
- **`ApiClient.kt`**:
  - *Job:* Singleton entry point for Retrofit and OkHttp.
  - *Features:* Integrates `authInterceptor` which automatically injects `Authorization: Bearer <token>` into outgoing network requests via `UserPreferences`.
  - *Exposed API Services:*
    - `authApi` (`AuthApiService`) - OTP send/verify, user details.
    - `videoApi` (`VideoApiService`) - Video catalog and category list.
    - `videoDetailsApiService` (`VideoDetailsApiService`) - Detailed video info.
    - `savedVideosApiService` (`SavedVideosApiService`) - Watchlist / Saved videos.
    - `likedVideosApiService` (`LikedVideosApiService`) - Liked videos catalog.
    - `watchProgressApiService` (`WatchProgressApiService`) - Resume progress sync.
    - `subscriptionApi` (`SubscriptionApiService`) - Membership plans & subscriptions.
    - `profileApiService` (`ProfileApiService`) - User profile data & updates.

#### DTOs & Mappers (`data/remote/dto/`, `data/remote/mapper/`)
- Contains serializable Data Transfer Objects matching backend JSON schemas.
- `VideoMapper.kt`: Maps `VideoDto` to domain model `Video`.

#### Repositories (`data/remote/repository/`)
Implement clean repository interfaces for the application:
- `AuthRepositoryImpl`: Interacts with `AuthApiService` for OTP & authentication.
- `VideoRepositoryImpl`: Interacts with `VideoApiService` & `VideoDetailsApiService`.
- `SavedVideosRepositoryImpl`: Handles watchlist additions/removals.
- `LikedVideosRepositoryImpl`: Manages user video likes.
- `WatchProgressRepositoryImpl`: Syncs playback progress (`saveProgress`, `getProgress`).
- `SubscriptionRepositoryImpl`: Fetches plans and creates subscriptions.
- `ProfileRepository`: Manages user profile information.

---

### 4.3. Shared Presentation Layer (`com.dealshot.app.presentation.shared`)

Contains form-factor-agnostic ViewModels and MVI contracts shared by both **Mobile** and **TV**.

Each feature module contains:
1. **Contract/State**: Data classes for UI State (`State`), Intent (`Intent`), and Effect (`Effect`).
2. **ViewModel**: Exposes `uiState: StateFlow<State>` and `effect: Flow<Effect>`, and processes user actions via `onIntent(intent)`.
3. **ViewModelFactory**: Custom factory for instantiating ViewModels with required repositories.

#### Features Overview:
- **`auth/` (`SessionViewModel`)**: Manages session state, token verification, and logout triggers.
- **`home/` (`HomeViewModel`)**: Manages video feeds, banner items, categories, watchlist status, and section filtering.
- **`player/` (`VideoPlayerViewModel`)**: Manages ExoPlayer UI state, playback position restoration, and periodic watch progress saving.
- **`details/` (`DetailsViewModel`)**: Manages individual video details, related videos, and bookmark toggles.
- **`login/` & `otp/` (`LoginViewModel`, `OtpViewModel`)**: Manages phone number entry, OTP dispatch, timer, and verification.
- **`myList/` (`SavedVideosViewModel`)**: Manages saved videos (Watch Later / My List) collections.
- **`like/` (`LikedVideosViewModel`)**: Manages user's liked videos feed.
- **`profile/` (`ProfileViewModel`)**: Manages user profile display and edit requests.
- **`subscription/` (`SubscriptionViewModel`)**: Handles active subscription plans and purchase intents.

---

### 4.4. Platform-Specific UI Layers

#### Mobile UI Layer (`app/src/mobile/java/com/dealshot/app/`)
- **`StreamApp.kt`**: Root Mobile Composable containing `NavHost` and `StreamRoutes` (`LOGIN`, `OTP`, `HOME`, `DETAILS`, `PLAYER`, `CATEGORY`, `MY_LIST`, `LIKED_VIDEOS`, `PROFILE`, `SUBSCRIPTION`).
- **`presentation/mobile/`**:
  - `home/`: Mobile home screen layout, banner carousel, horizontal video rows.
  - `details/`: Mobile video detail screen with action buttons.
  - `player/`: Mobile full-screen video player UI with touch controls.
  - `login/` & `otp/`: Phone/OTP entry UI.
  - `myList/`, `category/`, `profile/`, `subscription/`: Feature composables optimized for mobile screens.

#### TV UI Layer (`app/src/tv/java/com/dealshot/app/`)
- **`StreamApp.kt`**: Root TV Composable containing `NavHost` and `TvRoutes`.
- **`presentation/tv/`**:
  - Uses `androidx.tv.material3` components (`Carousel`, `Card`, `Glow`, `Surface`).
  - Specialized D-Pad spatial focus, row navigation, and 10-foot television user interface.

---

## 5. Summary of Main Functions & Their Jobs

| Component | Key Function | Job / Responsibility |
| :--- | :--- | :--- |
| `Media3Manager` | `initialize(url, pos)` | Prepares ExoPlayer for video streaming or local raw resources and seeks to saved position. |
| `ApiClient` | `authInterceptor` | Automatically injects `Authorization: Bearer <token>` from `UserPreferences` into all Retrofit requests. |
| `AppErrorHandler` | `getUserMessage(throwable)` | Converts technical network and HTTP errors into clear user-facing messages. |
| `UserPreferences` | `saveLoginSession(...)` | Atomically persists user authentication state, profile data, and subscription details to DataStore. |
| `SecureTokenStorage` | `encrypt(token)` / `decrypt(token)` | Protects sensitive tokens using Android KeyStore AES/GCM encryption. |
| `HomeViewModel` | `onIntent(HomeIntent)` | Central handler for user actions on the Home screen (video clicks, section changes, list toggles). |
| `VideoPlayerViewModel`| `saveWatchProgress()` | Asynchronously posts current playback position to the backend via `WatchProgressRepository`. |
| `SessionViewModel` | `checkSession()` | Verifies saved tokens on app startup to determine whether to launch Login or Home. |

---

## 6. Strict Rules & AI Agent Guidelines for Future Tasks

When adding new features, fixing bugs, or refactoring code in this project, **all AI agents and developers MUST strictly observe the following rules**:

### 1. Architectural Integrity & Reuse
- **REUSE EXISTING CLIENTS:** Do NOT create new Retrofit or OkHttp client instances. Add new API endpoints directly to `data/remote/api/` and expose them through `ApiClient.kt`.
- **REUSE TOKEN STORAGE:** Always rely on `UserPreferences` and `ApiClient`'s built-in `authInterceptor` for authentication headers.
- **REUSE ERROR HANDLING:** Always pass caught exceptions through `AppErrorHandler.getUserMessage(throwable)` for UI error states.
- **REUSE MEDIA MANAGER:** Use `Media3Manager` for ExoPlayer operations. Do NOT instantiate raw `ExoPlayer` directly in Composables.

### 2. Separation of Shared Logic vs. UI Form-Factors
- **Keep Business Logic Shared:** New ViewModels, states, contracts, repositories, and DTOs MUST be placed in `presentation/shared/` or `data/` so they are fully reusable across Mobile and TV.
- **Do Not Mix Mobile and TV UIs:**
  - Place Mobile-specific Composables in `app/src/mobile/java/...` using `androidx.compose.material3`.
  - Place TV-specific Composables in `app/src/tv/java/...` using `androidx.tv.material3` with D-Pad focus support.

### 3. Strict MVI Pattern Rules
- Every feature module in `presentation/shared/` MUST adhere to the MVI structure:
  - Expose UI state using `StateFlow<T>`.
  - Expose single-shot side effects (navigation, toasts) using `Channel<Effect>`.
  - Handle user events exclusively via a single `onIntent(intent)` method.

### 4. Navigation & Theme Standards
- Route names and parameters MUST be managed through `StreamRoutes` (Mobile) or `TvRoutes` (TV) with URL encoding (`Uri.encode`) for dynamic arguments.
- Always use colors and typography from `ui/theme/Theme.kt` (`StreamTheme`) to maintain branding consistency.

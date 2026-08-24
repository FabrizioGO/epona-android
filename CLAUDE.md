# Epona — Claude Code Guide

## Project Overview

Epona is an Android application for lost and found pet alerts with real-time updates. Users can post alerts, report sightings, and discover nearby pets using location-based feeds.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt 2.59.2 |
| Backend | Supabase (Auth, Postgrest, Realtime, Storage) |
| HTTP | Ktor Client Android 3.1.3 |
| Local DB | Room 2.7.1 |
| Image Loading | Coil 3 |
| Async | Kotlin Coroutines 1.8.1 |
| Navigation | Compose Navigation 2.9.7 |
| Serialization | Kotlin Serialization 1.7.3 |
| Build | AGP 9.0.1, Gradle version catalog (`libs.versions.toml`) |
| Min SDK | 26 / Target SDK 36 / Compile SDK 36 |

## Project Structure

```
app/src/main/java/com/fabriziogo/epona/
├── core/
│   ├── data/          # Repository implementations, mappers (DTO ↔ Domain)
│   ├── database/      # Room database, entities, DAOs
│   ├── domain/        # Models, repository interfaces, use cases
│   └── network/       # Supabase services, DTOs
├── feature/
│   ├── auth/          # Login, Register, Onboarding screens
│   ├── home/          # Main alert feed with real-time updates
│   ├── detail/        # Alert detail + sighting history
│   └── profile/       # User profile, pets management, settings
└── ui/                # Shared Compose components, theme, colors, typography
```

## Architecture

**Clean Architecture** with three layers:

1. **Domain** (`core/domain/`) — Pure Kotlin. Models, repository interfaces, ~25 use cases.
2. **Data** (`core/data/`) — Repository implementations with network-first + Room cache fallback.
3. **Presentation** (`feature/`) — ViewModels with `UiState + Event` pattern, Compose screens.

Data flow:
```
Compose Screen → ViewModel → UseCase → Repository → Supabase / Room
```

### Use Case Pattern
```kotlin
class GetAlertsUseCase @Inject constructor(
    private val repository: AlertRepository
) {
    operator fun invoke(...): Flow<Result<List<Alert>>> = ...
}
```

### ViewModel Pattern
- State via `MutableStateFlow<UiState>`
- One-shot navigation/effects via `Channel` (consumed as `Flow`)
- Hilt `@HiltViewModel`

### Repository Pattern
- Interface defined in `core/domain/`
- Implementation in `core/data/` — tries network, falls back to Room cache
- Returns `Result<T>` with `runCatching`

## Key Domain Models

- **Alert** — Lost/found pet alert with location, status, reward flag
- **Pet** — Species, size, gender, description, images
- **User** — Profile, avatar, location, alert-radius preference
- **Sighting** — Reports of a spotted pet tied to an alert
- **Notification** — Unread count tracking

## Dependency Injection

Hilt modules in `core/`:
- `DataModule` — binds repository interfaces to implementations
- `DatabaseModule` — provides Room database and DAOs
- `NetworkModule` — provides Supabase client and service classes

## Room Database

- Schema exported to `/app/schemas/` — commit schema files when changing entities
- Destructive migration fallback configured (dev only)
- Type converters for custom types (e.g., lists, enums)

## Dependency Management

All versions centralized in `gradle/libs.versions.toml`. When adding a new library:
1. Add version + library alias to `libs.versions.toml`
2. Reference via `libs.<alias>` in `build.gradle.kts`

## Testing

Minimal test coverage — JUnit 4 + Espresso + Compose UI Test infrastructure is set up but not extensively used. Prefer writing unit tests for use cases and integration tests for repositories.

## Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Conventions

- Follow the existing use-case-per-operation pattern — one public `invoke` function per use case.
- Keep Compose screens stateless; pass state and callbacks from the ViewModel.
- Mappers live in `core/data/` — one file per entity (e.g., `AlertMapper.kt`).
- DTOs live in `core/network/`; domain models live in `core/domain/`.
- Shared UI components go in `ui/components/`; feature-specific composables stay inside their `feature/` package.
- Use `@Preview` annotations on all composables during development.
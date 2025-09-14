# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LunaTracker is a native Android baby tracking app written in Kotlin. It allows parents to log baby-related events (feedings, diaper changes, medicine, etc.) with support for local storage or WebDAV synchronization for multi-device sharing.

## Technology Stack

- **Language**: Kotlin 2.0.0
- **Platform**: Android (minSdk 21, targetSdk 34, compileSdk 34)
- **UI Framework**: Jetpack Compose + Traditional Android Views (hybrid approach)
- **Build System**: Gradle with Kotlin DSL
- **Architecture**: Activity-based with Repository pattern
- **Storage**: JSON files (local or WebDAV)
- **Dependencies**: AndroidX libraries, Material3, Sardine WebDAV client

## Build Commands

```bash
# Clean and build
./gradlew clean build

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Install debug APK to connected device
./gradlew installDebug
```

## Code Architecture

### Core Components

**MainActivity** (`MainActivity.kt`): The main screen that displays the list of logged events and provides buttons for quick event logging. Handles UI interactions, event creation dialogs, and logbook management.

**Repository Layer**: Data persistence abstraction with two implementations:
- `FileLogbookRepository`: Local file storage using app's internal storage
- `WebDAVLogbookRepository`: Remote storage using WebDAV protocol with Sardine client
- `LocalSettingsRepository`: Manages app preferences and WebDAV credentials

**Entity Models**:
- `LunaEvent`: Represents a single logged event (feeding, diaper change, etc.). Uses JSONObject internally for extensibility and backward compatibility
- `Logbook`: Container for events with automatic sorting and size management (trims at 30k events)

### Event Types

The app supports these event types (defined in `LunaEvent` constants):
- Breastfeeding (left/right/both nipples)
- Baby bottle feeding with quantity
- Diaper changes (pee/poo)
- Medicine with notes
- Temperature with slider input
- Weight measurements
- Food with notes
- Colic episodes
- Enemas
- General notes

### Data Flow

1. User taps event button → `MainActivity` creates `LunaEvent`
2. Event added to current `Logbook` and RecyclerView
3. `LogbookRepository` saves logbook asynchronously
4. UI shows loading state and success/error feedback
5. Auto-refresh every 60 seconds when not actively adding events

### Key Patterns

- **Async Repository Pattern**: All data operations use callback interfaces (`LogbookLoadedListener`, `LogbookSavedListener`, etc.)
- **JSON Extensibility**: Events stored as JSON to allow forward/backward compatibility
- **Error Handling**: Comprehensive error types (IO, WebDAV, JSON, generic) with user-friendly messages
- **State Management**: Activity-scoped state with loading indicators and event saving locks

## Project Structure

```
app/src/main/java/it/danieleverducci/lunatracker/
├── MainActivity.kt                    # Main UI controller
├── SettingsActivity.kt               # App settings screen
├── adapters/
│   └── LunaEventRecyclerAdapter.kt   # RecyclerView adapter for events list
├── entities/
│   ├── LunaEvent.kt                  # Event model with JSON backing
│   └── Logbook.kt                    # Event container with management logic
├── repository/
│   ├── LogbookRepository.kt          # Repository interface + listeners
│   ├── FileLogbookRepository.kt      # Local file implementation
│   ├── WebDAVLogbookRepository.kt    # WebDAV implementation
│   └── LocalSettingsRepository.kt    # App preferences
└── ui/theme/                         # Compose theme definitions
utils/
├── DateUtils.kt                      # Date formatting utilities
└── NumericUtils.kt                   # Number formatting/validation
```

## Development Guidelines

### Testing
- Unit tests go in `app/src/test/`
- Instrumented tests go in `app/src/androidTest/`
- Use `./gradlew test` for unit tests
- Use `./gradlew connectedAndroidTest` for instrumented tests (requires device/emulator)

### Adding New Event Types
1. Add type constant to `LunaEvent.TYPE_*`
2. Add emoji/description resources to strings.xml
3. Add case to `getTypeEmoji()` and `getTypeDescription()`
4. Create UI button in MainActivity with appropriate dialog if needed
5. Handle quantity/notes input as required by event type

### WebDAV Integration
- Uses Sardine library for WebDAV operations
- Credentials stored in SharedPreferences via `LocalSettingsRepository`
- All operations are asynchronous with proper error handling
- Supports standard WebDAV servers (Nextcloud, etc.)

### Build Variants
- `debug`: Development builds with debugging enabled
- `release`: Production builds with ProGuard/R8 optimization disabled (see `proguard-rules.pro`)
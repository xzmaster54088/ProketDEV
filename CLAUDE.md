# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PocketDev is an Android application that integrates AI code generation with GitHub automation. The app allows users to chat with AI models (DeepSeek, OpenAI, or local Ollama) to generate code, then push the generated code directly to GitHub repositories.

## Build Commands

### Development
```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run Android tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean build

# Install debug APK on connected device
./gradlew installDebug
```

### Dependency Management
- Dependencies are managed via Version Catalog in `gradle/libs.versions.toml`
- Uses Kotlin DSL for Gradle configuration
- Minimum SDK: 26 (Android 8.0)
- Target SDK: 34
- Java version: 17

## Architecture

### Clean Architecture Layers
The project follows Clean Architecture with three main layers:

1. **Data Layer** (`com.pocketdev.data`)
   - `local/`: DataStore for user settings, Room database (planned)
   - `remote/`: Retrofit services for AI APIs and GitHub API
   - `repository/`: Repository implementations
   - `di/`: Dependency injection modules (Hilt)

2. **Domain Layer** (`com.pocketdev.domain`)
   - `model/`: Business models and entities
   - `usecase/`: Business logic use cases
   - `repository/`: Repository interfaces

3. **Presentation Layer** (`com.pocketdev.ui`)
   - `screens/`: Compose screens (ChatScreen)
   - `components/`: Reusable UI components
   - `theme/`: Material 3 theming with dynamic colors
   - `navigation/`: Navigation components

### Key Architectural Patterns
- **MVVM**: ViewModel manages UI state, Compose for UI
- **Repository Pattern**: Abstracts data sources
- **Dependency Injection**: Hilt for DI
- **Reactive Programming**: Kotlin Flows for state management

## Core Components

### Dynamic Network Layer
The most critical component is `DynamicHostInterceptor` which enables switching between different AI providers:
- Intercepts Retrofit requests to dynamically change base URLs
- Reads configuration from `UserSettingsRepository` (DataStore)
- Supports DeepSeek, OpenAI, and local Ollama endpoints
- Injects API keys dynamically

### GitHub Integration
- `GithubAuthManager`: OAuth 2.0 authentication using Chrome Custom Tabs
- `CommitFileUseCase`: Handles file creation/updates on GitHub (GET sha → PUT file)
- `DispatchWorkflowUseCase`: Triggers GitHub Actions workflows

### AI Chat System
- `ChatUseCase`: Communicates with AI APIs using structured JSON prompts
- `SystemPrompt.PROMPT`: Forces AI to respond with structured JSON containing code actions
- `ChatScreen`: Main UI with Markdown rendering and code action cards

## Configuration Requirements

### Required Setup
1. **GitHub OAuth**: Update `CLIENT_ID` and `CLIENT_SECRET` in `GithubAuthManager.kt`
2. **AI API Keys**: Configure in app settings (stored in DataStore)
3. **Local Ollama**: Use `10.0.2.2` for Android emulator localhost access

### Build Configuration
- Android SDK path configured in `local.properties`
- Uses Android Gradle Plugin 8.5.0
- Kotlin 2.0.21 with Compose compiler 1.5.14

## Development Notes

### Code Generation Flow
1. User sends message → `ChatUseCase` calls AI with system prompt
2. AI returns JSON with `explanation` and `actions` array
3. `ChatScreen` renders explanation as Markdown
4. Code actions displayed as cards with "Push to GitHub" buttons
5. Button click triggers `CommitFileUseCase` → GitHub API

### Network Configuration Flow
1. User settings stored in DataStore via `UserSettingsRepository`
2. `DynamicHostInterceptor` caches settings and updates Retrofit requests
3. Base URL, API key, and model name can be changed at runtime

### Security Considerations
- API keys stored in EncryptedSharedPreferences (DataStore)
- OAuth uses Custom Tabs instead of WebView
- No sensitive data in version control

## Testing Strategy
- Unit tests for use cases and repositories
- Instrumentation tests for UI components
- MockWebServer for API testing
- Room database tests with in-memory database

## Common Development Tasks

### Adding New AI Provider
1. Add provider configuration to `UserSettingsRepository`
2. Update settings UI to include new provider
3. Ensure provider uses OpenAI-compatible API format

### Adding New GitHub Feature
1. Extend `GitHubApi` interface
2. Create corresponding use case in domain layer
3. Add UI components in presentation layer

### Modifying UI Theme
1. Update colors in `ui/theme/Color.kt`
2. Modify typography in `ui/theme/Type.kt`
3. Theme supports dynamic colors from wallpaper

## Troubleshooting

### Build Issues
- Ensure Java 17 is installed and configured
- Check Android SDK path in `local.properties`
- Clean build if dependency issues: `./gradlew clean build`

### Network Issues
- Verify API keys in app settings
- Check base URL format (must include protocol)
- For local Ollama in emulator: use `http://10.0.2.2:11434`

### GitHub Integration
- OAuth requires registered GitHub OAuth app
- Repository must exist before pushing files
- User needs appropriate permissions (write access)
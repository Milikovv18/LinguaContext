# 🔎 Lingua Context
[![Android CI/CD](https://github.com/Milikovv18/LinguaContext/actions/workflows/android.yml/badge.svg)](https://github.com/Milikovv18/LinguaContext/actions/workflows/android.yml)

<img src="https://github.com/user-attachments/assets/e63cb1d8-910a-488d-9749-4f105245b1a4" align="right"/>

*This project was created specifically for my CV, so this README includes technical details to showcase my skills and implementation decisions.*

Lingua Context is an accessibility-focused app that brings contextual word translation to any Android screen.
Powered by Ollama LLMs, it explains not just the meaning of a selected word, but how its context and formality influence its translation.
No third-party APIs are used — only your own Ollama server.

You can also monitor current TODO list in an attached GitHub Projects board (Backlog specifically).
It's especially useful for contributors.

- [Features](#features)
- [How it works](#how-it-works)
- [Setup](#setup)
- [Technical details](#technical-details)
  - [Tech Stack](#tech-stack)
  - [Architecture](#architecture)
  - [Project Structure](#project-structure)
  - [CI/CD](#cicd)
  - [Accessibility](#accessibility)
  - [Testing](#testing)
- [License](#license)
- [Contribution](#contribution)

## Features

- **On-screen Word Highlighting:** Activate via accessibility shortcut to highlight all words on the screen in yellow.
- **Contextual Translation:** Tap any highlighted word to get a bottom sheet with:
    - Context-aware translation (LLM explains how surrounding words affect meaning and translation)
    - Formality level assessment (“How formal is this word?”)
- **Seamless Workflow:** Dismiss the translation sheet to select another word or leave the app.
- **Custom Ollama Backend:** Configure your Ollama server API endpoint and model name directly in the launcher activity.

> ⚠️ **Attention:**  
> The app should always be closed using the accessibility shortcut to ensure proper resource release and avoid accessibility issues.

## How It Works

1. **Setup:**
    - Configure your Ollama server API and model in the launcher activity.
2. **Activate:**
    - Enable the Android accessibility shortcut for this app in system settings.
3. **Translate:**
    - On any screen, trigger the shortcut. All words become highlighted.
    - Tap a word. Wait for the contextual analysis (powered by your Ollama LLM).
    - View translation and formality in the bottom sheet.
    - Dismiss and repeat or exit via the accessibility shortcut.


## Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/contextual-translator.git
   ```
2. **Set up your base Ollama URL** in-app or via DataStore (see in-app settings)
3. **Build and run** with Android Studio
4. **Run tests** with `./gradlew test` and `./gradlew connectedAndroidTest`



# Technical details

## Tech Stack

This app leverages modern Android development tools and libraries for a robust, scalable, and maintainable codebase:

- **Jetpack Compose** – Declarative UI toolkit for building native Android interfaces
- **Kotlin** – Main programming language
- **Hilt** – Dependency injection
- **Jetpack DataStore** – Modern data storage solution for user preferences and configuration
- **Retrofit** – HTTP client for API integration
- **OkHttp** – Network layer, including dynamic base URL support
- **Kotlin Coroutines & Flow** – Asynchronous programming and reactive streams
- **Accessibility Service** – For enhanced interaction features
- **Kotlin tests** – Unit and instrumented testing
- **GitHub Actions** – CI/CD automation
- **Ollama LLM (local server integration)**

## Architecture

The app follows a clean, modular MVVM architecture inspired by Google’s best practices:

- **UI Layer:** Jetpack Compose, ViewModels, state management
- **Domain Layer:** Business logic, use cases, models
- **Data Layer:** Repository pattern, Retrofit/OkHttp for remote data, DataStore for local data

### PlantUML Diagram

> Note: The following UML diagram is a simplified representation of the app’s architecture. It may not directly correspond to every class or method in the source code and is intended to illustrate the overall project structure. As the project evolves, some components, relationships, or extensions may differ from what is shown here.

![uml_diagram](https://github.com/user-attachments/assets/bf043f41-c4fc-4ac5-8f55-4fbfabeab907)


## Project Structure

```
/app
  /src
    /main
      /java
        /ui            # Jetpack Compose screens and components
        /viewmodel     # ViewModels, state management
        /domain        # Use cases, business logic, models
        /data
          /repository  # Repository interfaces & implementations
          /remote      # Retrofit API, OkHttp, DTOs
          /local       # DataStore, local persistence
        /accessibility # Accessibility Service implementation
    /test              # Unit & Robolectric tests
    /androidTest       # Instrumented & Compose UI tests
```

## CI/CD

- **GitHub Actions**: Automated build, test, and linting on every pull request and push to main.
- **Test coverage**: Unit and instrumented tests run in CI.

## Accessibility

- Implements a custom **Accessibility Service** for advanced interactions.
- Easily trigger app by clicking accessibility shortcut.

## Testing

- **Unit tests**: ViewModel logic, repositories, data parsing, and utilities.
- **Instrumented tests**: ML Kit Text Recognition v2 API test.
- **MockWebServer**: For API contract and integration testing.


## License
Apache-2.0 license. See [LICENSE](LICENSE) for details.

## Contribution
Contributions are welcome! Please fork the repository, create a feature branch, and submit a pull request.

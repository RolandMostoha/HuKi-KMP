# HuKi-KMP - Hungarian Hiking Map #

HuKi-KMP is a Kotlin Multiplatform project targeting Android and iOS.

The app helps you plan trips and discover the hiking trails of Hungary.

The purpose of the KMP project is to implement HuKi on iOS platform as well, so KMP was my choice to transform the app to support both platforms.

HuKi (legacy) is a live Android app:
- Implemented under: https://github.com/RolandMostoha/HuKi-Android
- Published on Google Play: https://play.google.com/store/apps/details?id=hu.mostoha.mobile.android.huki

## Goals

The project was born for the following reasons:

1. My personal entertainment - it's my beloved pet project in which I can try out tech outside of my job.
2. It comes in handy for hikers to have trips in Hungary. No need to download tiles or setup layers manually.
3. Learn and improve

## Project Overview
- **Domain**: Hiking application for Hungarian landscapes, trails, destinations.
- **Type**: Kotlin Multiplatform (KMP).
- **Target Platforms**: Android, iOS.
- **KMP approach**: "Do not share UI", so iOS UI is written in SwiftUI.
- **UI Frameworks**: Jetpack Compose for Android, SwiftUi for iOS.
- **Target Platform APIs**:
    - Android: minSdk=26, targetSdk=36
    - iOS: Xcode=26.1.1+, Deployment Target=18.2
- **Project Structure**:
    - `:composeApp`: Android native code.
    - `:iosApp`: iOS native code.
    - `:shared`: Shared kotlin code.
        - `:shared:commonMain`: Common code.
        - `:shared:androidMain`: Android specific shared code.
        - `:shared:iosMain`: iOS specific shared code.
- **Supported app languages**: English, Hungarian.

### Tech stack & architecture

Always refer to the [AGENTS.md](AGENTS.md) file for detailed technical documentation, architecture rules, and coding constraints.

## Integration & Delivery

The project uses `GitHub Actions` to ensure code quality.

The following steps are running on the CI server on `main` push:

### Kotlin (shared code) + Android

1. Detekt - Static code analysis for Kotlin code
2. Ktlint - Enforces industry standard Kotlin style & formatting rules
3. Android Lint - Standard Android linter from Google
4. Compose Lints - Lint extension to avoid common Jetpack Compose mistakes
5. Unit tests
6. Android build

```shell
./gradlew detekt ktlintCheck lint test assembleDebug
```

### iOS
1. SwiftLint - Enforces Swift style and conventions
2. Xcode build

```shell
swiftlint xcodebuild 
```

## Security

### MapBox

MapBox access token is stored in an XML under `composeApp/src/androidMain/res/values/mapbox_access_token.xml`.

The XML token is converted to GitHub secret with: 

```shell
cat composeApp/src/androidMain/res/values/mapbox_access_token.xml | base64
```

## Project License

```
MIT License

Copyright (c) 2020-2026 Roland Mostoha

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

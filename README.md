# nthlink Android App

An open-source VPN app framework built with [Leaf](https://github.com/eycorsican/leaf)
and [nthlink-outline](https://github.com/nthlink/nthlink-outline) VPN SDKs. This project provides a
complete Android VPN app template - you just need to implement the core VPN logic and backend
integration.

## Quick Start

Search for `TODO` in the project to find all implementation points. You need to implement **2 files
** in the `core` module:

### 1. Backend Integration (`core/.../Core.kt`)

Implement these 5 functions:

```kotlin
// Encrypt/decrypt data for secure storage
fun encrypt(text: String): String
fun decrypt(cipherText: String): String

// Fetch VPN servers and app content from your backend
// Returns JSON with: servers, redirectUrl, headlineNews, notifications, current_versions
fun getConfig(): String

// Send user feedback to your backend
fun feedback(feedbackType: String, description: String, appVersion: String, email: String)

// Run diagnostics and return a report ID
fun startDiagnostics(): String
```

### 2. VPN Client (`core/.../RootVpnClient.kt`)

Implement these 3 functions:

```kotlin
// Start VPN with server list (auto-select best server)
override suspend fun runVpn(servers: List<Config.Server>)

// Start VPN with custom config string
override suspend fun runVpn(config: String)

// Stop VPN and clean up resources
override fun disconnect()
```

## Implementation Steps

1. Clone the repository
2. Search for `TODO` markers in the code
3. Implement `Core.kt` backend functions
4. Implement `RootVpnClient.kt` VPN functions using Leaf/Outline SDK
5. Test connection, disconnection, and error handling
6. Customize branding (app name, icon, colors, strings)
7. Build and release your VPN app

## Learn More

- [Leaf VPN SDK](https://github.com/eycorsican/leaf)
- [nthlink-outline](https://github.com/nthlink/nthlink-outline)
- [Outline VPN](https://getoutline.org/)
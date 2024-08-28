# nthlink Android App

Welcome to nthlink open source! This project is designed to help you efficiently build your own VPN
app.

We're using [leaf](https://github.com/eycorsican/leaf)
and [nthlink-outline](https://github.com/nthlink/nthlink-outline) as our VPN protocol SDK.
More details about [Outline](https://getoutline.org/).

## Build your own VPN App

You can globally search `TODO` in the project to find the places where you need to implement.

### The functions need to be implemented

Open `Core.kt` in the `core` module, there are couple of functions you have to implement.

- `fun encrypt(text: String): String`

> Preference content encryption.

- `fun decrypt(cipherText: String): String`

> Preference content decryption.

- `fun getConfig(): String`

> Get VPN servers and other information

- `fun feedback(feedbackType: String, description: String = EMPTY, appVersion: String = EMPTY, email: String = EMPTY)`

> Send users' feedback

Open `RootVpnClient.kt` in the `core` module, there are couple of functions you have to implement.

- `fun runVpn(servers: List<Config.Server>)`

> Start VPN connection with the `Config` class.

- `fun runVpn(config: String)`

> Start VPN connection with string.

- `fun disconnect()`

> Stop VPN connection
# nthlink Android App

Welcome to nthlink open source! This project is designed to help you efficiently build your own VPN
app.

We're using [leaf](https://github.com/eycorsican/leaf)
and [nthlink-outline](https://github.com/nthlink/nthlink-outline) as our VPN protocol SDK.
More details about [Outline](https://getoutline.org/).

## Build your own VPN App

You can globally search `TODO` in the project to find the places where you need to implement.

### Using the existing protocols

Open `Core.kt` in the `core` module, there are couple of functions you have to implement.

- `fun encrypt(text: String): String`

> Preference content encryption.

- `fun decrypt(cipherText: String): String`

> Preference content decryption.

- `fun getConfig(): String`

> Get VPN servers and other information

- `fun feedback(feedbackType: String, description: String = EMPTY, appVersion: String = EMPTY, email: String = EMPTY)`

> Send users' feedback

### `getConfig()`

This is the key function that makes the VPN app work. Example JSON below:

```json
{
  "servers": [
    {
      "protocol": "outline",
      "host": "1.2.3.4",
      "port": 443,
      "password": "password",
      "encrypt_method": "encrypt method",
      "sni": "www.domain.com",
      "ws": false,
      "ws_path": "/path"
    }
  ],
  "redirectUrl": "https://www.nthlink.com/",
  "headlineNews": [
    {
      "title": "title",
      "excerpt": "",
      "image": "",
      "url": "url"
    }
  ],
  "notifications": [
    {
      "title": "title",
      "url": "url"
    }
  ]
}
```

### Using your own protocols

If you want to use other protocols, you need to do:

1. Modify the JSON object in `servers` in `Core.kt`
2. Modify the `Server` class in `DsConfig.kt`
3. Insert a new subclass of `proxy` in `RootProxy.kt`
4. Add a new branch of `when` in `Util.kt`.

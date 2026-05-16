# Tender - The go-to economy API for developers

[![License](https://img.shields.io/github/license/MagnusHJensen/Tender)](LICENSE)
[![Release](https://img.shields.io/github/v/release/MagnusHJensen/Tender)](https://github.com/MagnusHJensen/Tender/releases)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen)](https://www.minecraft.net)

Tender is a lightweight and easy-to-use economy API for Minecraft mods. 
Whether you're building shops, rewards, or any economy-driven feature, 
Tender provides a clean and consistent API to manage player balances with ease.

## Features

- Simple and intuitive economy API
- Easy integration for developers
- Lightweight with minimal overhead
- Supports balance retrieval, deposit, withdrawal, and more
- Fallback provider for development and testing

## Installation

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.MagnusHJensen:Tender:VERSION'
}
```

## Usage

TBD (For now check out the [EconomyProvider](./common-api/src/main/java/dk/magnusjensen/tender/api/EconomyProvider.java) interface)

## Wiki

Check out the [wiki](https://github.com/magnushjensen/tender/wiki) for detailed documentation.

If you think anything is missing from the wiki that is unclear, send a message in the discord or open an [improve documentation issue](https://github.com/MagnusHJensen/simpleafk/issues/new?template=3.Improve_docs.md).

## Roadmap

Check out the version [milestones](https://github.com/MagnusHJensen/tender/milestones)

## Links

Join my [Discord](https://discord.gg/PHu8k32M3q) for support and updates!
- [CurseForge page](https://www.curseforge.com/minecraft/mc-mods/tender)
- [Modrinth page](https://modrinth.com/mod/tender)

## Contributing

Contributions are welcome! Feel free to open an issue or submit a pull request.

## Thanks to

- [jaredlll08](https://github.com/jaredlll08) for creating [`Multiloader-Template`](https://github.com/jaredlll08/MultiLoader-Template)
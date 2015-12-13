# SpongeRecording
Note: This has not yet been fully updated to the latest Sponge version and therefore might not yet work.
## About SpongeRecording
BukkitRecording is a library for recording the connection of a player on a Sponge server.

The resulting replay files are basically packet dumps of the connection from a Minecraft Client to the Sponge Server.
These packet dumps are bi-directional and do not yet emulate the recorded player as usual replay files do.
Processing via the [replay-converter](https://github.com/ReplayMod/replay-converter) is required to generate 'normal' replay files.

### Replay files
Replay files (file extension ".mcpr") can be created using the [ReplayMod](http://replaymod.com) (Minecraft Forge Mod).
You can modify replay files using [ReplayStudio](https://github.com/ReplayMod/ReplayStudio).
They can be played back using the same [ReplayMod](http://replaymod.com) (which has tons of more features during replay, such as smooth camera movement, slow motion, time lapse, video exporting and more) or the [ReplayServer](https://github.com/ReplayMod/ReplayServer) (which doesn't require any client modifications but in turn doesn't do any of that fancy stuff).

### Features
- Record the connection of a player from the very beginning (custom event for early login phase)
- Files produced are bi-directional and can be converter to normal replay files at any time
- Supported Platforms:
    - Every Sponge server based on [SpongeCommon](https://github.com/SpongePowered/SpongeCommon) (includes SpongeForge and SpongeVanilla)

## Building
SpongeRecording is build using the [Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (Version 8 or above) and [Gradle](http://gradle.org/).

You can build ReplayStudio by using the command `./gradlew`. You can also use a local installation of gradle.

The API jar file will be in `api/build/libs`, the example plugin in `example/build/libs` and the plugin itself in `build/libs`.

## Using
To use SpongeRecording, simply add it to your dependencies and make sure the user installs the SpongeRecording plugin itself.
An example plugin is provided in the `example` directory.

## License
SpongeRecording is free software licensed under the GPLv3 license. See `LICENSE` for more information.
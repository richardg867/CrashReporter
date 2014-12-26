CrashReporter
=============

You know how frustrating it is to have your modded Minecraft server crash, and there's nobody to solve it, and no clue as to what caused the crash. CrashReporter was created to get around that, by instantly posting and notifying any player (Internal Server Error) or server crash to the destinations you choose.

**Download: [v1.0 for Minecraft 1.5.1 to 1.6.4](https://github.com/richardg867/CrashReporter/releases/tag/v1.0)**

**How to install:** Requires Forge (or just FML) to be installed. Put the jar in the mods folder, start the server once, then look into config/crashreporter.cfg for configuring the reporter. Please note CrashReporter will only work on a dedicated server, since logging is not available on the integrated server.

**How to build:** Point FORGE_PATH on build.py to a Forge source install and run the script. Requires Python 2 and command-line 7z (for Windows, take 7z.exe out of 7-Zip's folder)

**Backstory:** CrashReporter was originally part of my constantly-reborn ServerMod project to help keep ForgeCraft up and running, but since the crash reporter was its only useful part, I decided to separate it and make it public.
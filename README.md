# AuraMateClient (Fabric, Minecraft 1.21.11)

Small draggable task-manager panel (Right Shift, rebindable) with an on/off switch per feature:
Lunge Swap (G, delay stepper in panel), Fast Shield (restart to apply), See Hearts
(`/iseeyourhearts [on|off]`), Effect Timer, Armor HUD.

Settings are saved in `config/auramateclient.properties`.

All files sit in one folder (no src/ needed). Build with GitHub Actions
(.github/workflows/main.yml) and use the jar from the artifact.

Install: put the jar and Fabric API in `mods/`.

To add another mod: make a `Feature`, register it in `Features.java`; it gets a row automatically.

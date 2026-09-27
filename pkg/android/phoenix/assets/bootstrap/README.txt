RetroArch GBA LCD bundle

The APK installs these managed files on first launch and refreshes them when
the bundle version changes:

- RGUI menu assets
- GBA reflective LCD shader and RGB curve LUT
- Two selectable shader presets installed in
  /storage/emulated/0/RetroArch/shaders/
  - native-lcd-v0.1.1 (default)
  - native-lcd-v0.1.0 (legacy)
- mGBA core options and automatic shader preset
- mGBA libretro core and core information

The bundled retroarch.cfg is installed only when no user configuration exists.
User changes are preserved across APK updates. Saves, states, screenshots and
other writable data use /storage/emulated/0/RetroArch-gyrotest/.

Bundle version 5 installs the final v0.1.1 reflective-cell shader, keeps the
v0.1.0 preset for comparison, and removes obsolete preview presets. It also
migrates the Video Shaders directory to the shared RetroArch shader directory
once. Later user changes to that setting are preserved.

The GBA Native LCD project files are MIT licensed. RetroArch remains
GPL-3.0-or-later and the bundled mGBA core remains MPL-2.0. License texts are
included in bootstrap/licenses/.

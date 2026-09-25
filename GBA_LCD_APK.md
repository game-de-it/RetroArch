# RetroArch GBA LCD APK

This tree builds an Arm64 RetroArch package for the KONKR Pocket ADVANCE. It
adds accelerometer uniforms for shaders and bundles the calibrated reflective
GBA LCD preset.

## Compatibility scope

The initial profile targets the KONKR Pocket ADVANCE (Android model: GT78-VN).
The APK is not
locked to that product name, but its defaults assume all of the following:

- Android on an `arm64-v8a` device
- OpenGL ES with the RetroArch `gl` video driver
- a 960 x 640 display showing the 240 x 160 GBA image at exact 4x scale
- an Android accelerometer whose axes match the target's landscape orientation
- the bundled mGBA core

Other Arm64 Android handhelds may run the APK, but the LCD matrix, pixel-based
shadow distances, color LUT and sensor direction require device-specific
validation. AYANEO Equalizer integration is only useful on firmware exposing
the compatible Android Dynamics Processing effect. On other devices the audio
still works, but the effect-capable OpenSL path may add latency without adding
an EQ benefit.

## Bundled content

- RetroArch with accelerometer shader support and a 0.20 low-pass filter
- mGBA libretro core and core information
- Reflective GBA LCD shader, RGB curve LUT and mGBA automatic preset
- RGUI menu assets, including the Japanese bitmap font
- A configuration migrated from the device's existing RetroArch installation,
  with late input polling and fixed-refresh-rate synchronization defaults

## Installed paths

Managed files are installed under `/data/user/0/com.retroarch.aarch64/`:

- `assets/rgui/`
- `config/mGBA/`
- `cores/mgba_libretro_android.so`
- `info/mgba_libretro.info`
- `shaders/gba-reflective-v2/`

Writable user data is kept under `/storage/emulated/0/RetroArch-gyrotest/` so
APK removal does not delete saves, states, screenshots or playlists. The main
configuration remains in Android's standard package-specific external files
directory.

## Update behavior

Bundled managed files are refreshed when `BUNDLE_VERSION` changes. The bundled
`retroarch.cfg` is copied only when the user does not already have one, so APK
updates preserve settings. Existing installs should back up and migrate the
configuration explicitly when adopting a new default configuration. The
required `input_sensors_enable` key is repaired automatically without replacing
the rest of the user's configuration. Bundle version 3 migrates existing
installations to late input polling and disables the VRR-only exact content
framerate mode once; subsequent user changes remain persistent.

## Build

```sh
cd pkg/android/phoenix
./gradlew assembleAarch64Release
```

The release task uses the debug key unless release signing properties are
provided. Public releases must use a persistent release keystore.

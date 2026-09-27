package com.retroarch.playcore;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Installs the GBA LCD bundle before the native activity starts. */
public class RetroArchApplication extends Application
{
    private static final String TAG = "RetroArchBootstrap";
    private static final String PREFS = "bundled_content";
    private static final String VERSION_KEY = "installed_version";
    private static final int BUNDLE_VERSION = 5;

    @Override
    public void onCreate()
    {
        super.onCreate();

        try
        {
            installBundledContent();
        }
        catch (IOException e)
        {
            Log.e(TAG, "Unable to install bundled RetroArch content", e);
        }
    }

    private void installBundledContent() throws IOException
    {
        SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        int installedVersion = preferences.getInt(VERSION_KEY, 0);

        boolean bundleUpgrade = installedVersion < BUNDLE_VERSION;

        if (bundleUpgrade)
        {
            File privateData = new File(getApplicationInfo().dataDir);
            copyAssetTree("bootstrap/private", privateData, true);

            File sharedShaders = new File(Environment.getExternalStorageDirectory(),
                    "RetroArch/shaders");
            copyAssetTree("bootstrap/external/shaders", sharedShaders, true);

            // These were temporary presets in pre-release v0.1.1 builds.
            // Only delete directories previously owned by this APK.
            deleteManagedTree(new File(sharedShaders, "gba-rugged-preview"));
            deleteManagedTree(new File(sharedShaders, "gba-dev-active"));
            deleteManagedTree(new File(sharedShaders, "gba-native-lcd-v0.1.0"));
            deleteManagedTree(new File(privateData,
                    "shaders/gba-dot-aperture-preview-1"));
        }

        File externalFiles = getExternalFilesDir(null);
        if (externalFiles == null)
            throw new IOException("External files directory is unavailable");

        File config = new File(externalFiles, "retroarch.cfg");
        if (!config.exists())
            copyAssetFile("bootstrap/retroarch.cfg", config, false);

        // Sensor-driven lighting is a required feature of this build. Older
        // bundled configurations inherited a disabled value from stock
        // RetroArch, so repair only this key without replacing user settings.
        ensureConfigValue(config, "input_sensors_enable", "true");

        // Version 3 migrates existing installations to the recommended fixed
        // 60 Hz KPA settings once. Later user changes remain untouched.
        if (bundleUpgrade)
        {
            ensureConfigValue(config, "input_poll_type_behavior", "2");
            ensureConfigValue(config, "vrr_runloop_enable", "false");
            ensureConfigValue(config, "video_shader_dir",
                    "/storage/emulated/0/RetroArch/shaders");
            preferences.edit().putInt(VERSION_KEY, BUNDLE_VERSION).apply();
        }
    }

    private void ensureConfigValue(File config, String key, String value)
            throws IOException
    {
        String prefix = key + " =";
        String replacement = key + " = \"" + value + "\"";
        File temporary = new File(config.getPath() + ".tmp");
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(config));
             BufferedWriter writer = new BufferedWriter(new FileWriter(temporary)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (line.startsWith(prefix))
                {
                    line = replacement;
                    found = true;
                }
                writer.write(line);
                writer.newLine();
            }
            if (!found)
            {
                writer.write(replacement);
                writer.newLine();
            }
        }

        if (config.exists() && !config.delete())
            throw new IOException("Unable to replace " + config);
        if (!temporary.renameTo(config))
            throw new IOException("Unable to install " + config);
    }

    private void copyAssetTree(String assetPath, File destination,
                               boolean overwrite) throws IOException
    {
        AssetManager assets = getAssets();
        String[] children = assets.list(assetPath);

        if (children != null && children.length > 0)
        {
            if (!destination.isDirectory() && !destination.mkdirs())
                throw new IOException("Unable to create " + destination);

            for (String child : children)
                copyAssetTree(assetPath + "/" + child,
                              new File(destination, child), overwrite);
            return;
        }

        copyAssetFile(assetPath, destination, overwrite);
    }

    private void copyAssetFile(String assetPath, File destination,
                               boolean overwrite) throws IOException
    {
        if (!overwrite && destination.exists())
            return;

        File parent = destination.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs())
            throw new IOException("Unable to create " + parent);

        File temporary = new File(destination.getPath() + ".tmp");
        try (InputStream input = getAssets().open(assetPath);
             OutputStream output = new FileOutputStream(temporary))
        {
            byte[] buffer = new byte[64 * 1024];
            int count;
            while ((count = input.read(buffer)) != -1)
                output.write(buffer, 0, count);
        }

        if (destination.exists() && !destination.delete())
            throw new IOException("Unable to replace " + destination);
        if (!temporary.renameTo(destination))
            throw new IOException("Unable to install " + destination);

        if (destination.getName().endsWith(".so"))
            destination.setExecutable(true, false);
    }

    private void deleteManagedTree(File file) throws IOException
    {
        if (!file.exists())
            return;

        if (file.isDirectory())
        {
            File[] children = file.listFiles();
            if (children == null)
                throw new IOException("Unable to list " + file);
            for (File child : children)
                deleteManagedTree(child);
        }

        if (!file.delete())
            throw new IOException("Unable to delete " + file);
    }
}

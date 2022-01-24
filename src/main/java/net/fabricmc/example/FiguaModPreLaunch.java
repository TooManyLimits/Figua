package net.fabricmc.example;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

/**
 * Exists as an injection point pre-launch.
 * Is used for loading RenderDoc in development.
 */
public class FiguaModPreLaunch implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        //System.loadLibrary("renderdoc");
    }
}

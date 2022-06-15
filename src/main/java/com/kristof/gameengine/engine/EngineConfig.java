package com.kristof.gameengine.engine;

import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;

public class EngineConfig {
    enum RENDER_MODE {TRADITIONAL, RAY_TRACING}

    private static final String RENDER_GROUP_LABEL = "render";
    private static final String PHYSICS_GROUP_LABEL = "physics";
    private static final String RENDER_MODE_TRADITIONAL_LABEL = "traditional";
    private static final String RENDER_MODE_RAY_TRACING_LABEL = "raytracing";

    final boolean isFullScreen;
    final RENDER_MODE renderMode;
    final float fieldOfViewDeg;
    final boolean isShadowsEnabled;
    final boolean isShadowVolumesVisible;
    final boolean isPostProcessingEnabled;

    final boolean isAvatarPowerGravityPullEnabled;
    final boolean isDragEnabled;
    final boolean isEyeIdleMovementEnabled;
    final boolean isGravityOn;

    public EngineConfig(String iniFilePath) throws IOException {
        final Ini ini = new Ini(new File(iniFilePath));
        isFullScreen = ini.get(RENDER_GROUP_LABEL, "fullscreen", boolean.class);
        final String renderModeLabel = ini.get(RENDER_GROUP_LABEL, "mode", String.class);
        renderMode = switch (renderModeLabel) {
            case RENDER_MODE_RAY_TRACING_LABEL -> RENDER_MODE.RAY_TRACING;
            case RENDER_MODE_TRADITIONAL_LABEL -> RENDER_MODE.TRADITIONAL;
            default -> throw new IllegalArgumentException("Invalid render mode in .ini: " + renderModeLabel);
        };
        fieldOfViewDeg = ini.get(RENDER_GROUP_LABEL, "fieldOfViewDeg", float.class);
        isShadowsEnabled = ini.get(RENDER_GROUP_LABEL, "shadowsEnabled", boolean.class);
        isShadowVolumesVisible = ini.get(RENDER_GROUP_LABEL, "shadowVolumesVisible", boolean.class);
        isPostProcessingEnabled = ini.get(RENDER_GROUP_LABEL, "postProcessingEnabled", boolean.class);

        isAvatarPowerGravityPullEnabled = ini.get(PHYSICS_GROUP_LABEL, "avatarPowerGravityPullEnabled",
                boolean.class);
        isDragEnabled = ini.get(PHYSICS_GROUP_LABEL, "dragEnabled", boolean.class);
        isEyeIdleMovementEnabled = ini.get(PHYSICS_GROUP_LABEL, "eyeIdleMovementEnabled", boolean.class);
        isGravityOn = ini.get(PHYSICS_GROUP_LABEL, "gravityEnabled", boolean.class);
    }
}

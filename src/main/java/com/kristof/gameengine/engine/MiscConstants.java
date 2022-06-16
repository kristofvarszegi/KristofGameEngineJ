package com.kristof.gameengine.engine;

import com.kristof.gameengine.util.Vector3fExt;

public class MiscConstants {
    static final String WINDOW_TITLE = "Space Fly";
    static final float SCREEN_FILL_RATIO = 0.5f;
    public static final int FORCED_FPS = 60;
    static final float TIME_STEP_S = 1f / (float) FORCED_FPS;
    public static final float INFINITY = 10000f;//1000000000f;
    static final float PROJECTION_NEAR = 0.1f;
    static final float PROJECTION_FAR = 1.1f * INFINITY;
    static final float FWD_CONTROL_V = 10f;
    static final float SIDE_CONTROL_V = 10f;
    static final float VERT_CONTROL_V = 10f;
    static final float DIRECTION_CONTROL_FACTOR = 0.5f;
    static final float POLAR_VIEW_SPEED = 0.01f;
    static final float AZIMUTH_VIEW_SPEED = 0.01f;
    static final float MOUSE_D_TRIM = 50;
    static final float EYE_IDLE_FACTOR = 0.02f;
    static final float EYE_FOLLOW_FACTOR = 0.7f;
    static final float EYE_ROT_FOLLOW_FACTOR = 0.99f;
    static final float V_DRIVE_FACTOR = 100000000000000f;
    static final float TPS_DISTANCE_Y = 0.5f;
    static final float TPS_DISTANCE_Z = 4.5f;
    static final Vector3fExt RESET_POSITION = new Vector3fExt(0, 2f, 0);
    static final float DASH_DISTANCE = 4f;
    static final float EYE_IDLE_ANGULAR_FREQUENCY_RAD_PER_NS = 0.000000003f;

    enum TEXTURE_ASSET_KEYS {
        BLUE, BROKEN, SPACE_CLOUDS, DIAGONAL, MANHOLE, MTL_FLOOR02, MTL_TRIM01, REDBRICK, WHITE
    }
}

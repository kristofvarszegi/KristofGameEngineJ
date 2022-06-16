package com.kristof.gameengine.engine;

import com.kristof.gameengine.object3d.Object3d;
import com.kristof.gameengine.shadow.ShadowVolumeBO;

import java.util.List;
import java.util.Vector;

public class Scene {
    Object3d avatar;
    Object3d skyBox;
    final List<Object3d> staticObjects;
    final List<Object3d> inertObjects;
    ShadowVolumeBO staticShadowVolumeBO;

    Scene() {
        staticObjects = new Vector<>();
        inertObjects = new Vector<>();
    }

    void destroy() {
        avatar.destroyShadowVolume();
        // TODO destroy all shadow volumes
    }
}

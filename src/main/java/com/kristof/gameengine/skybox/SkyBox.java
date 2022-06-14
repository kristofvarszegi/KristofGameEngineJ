package com.kristof.gameengine.skybox;

import com.kristof.gameengine.shadow.ShadowVolume;
import com.kristof.gameengine.util.ColorVector;
import com.kristof.gameengine.util.Material;
import com.kristof.gameengine.util.Vector3fExt;
import com.kristof.gameengine.object3d.Object3d;
import com.kristof.gameengine.object3d.Object3dBO;

import java.util.List;

public class SkyBox extends Object3d {
    public SkyBox(Vector3fExt position, int colorMapTexIndex, int normalMapTexIndex) {
        super(position, 0, 0, 0, Vector3fExt.UNIT_VECTOR, Vector3fExt.NULL_VECTOR,
                Vector3fExt.NULL_VECTOR, ColorVector.NULL_COLOR, Material.EMISSIVE,
                colorMapTexIndex, normalMapTexIndex);   // TODO don't require normalMapTexIndex from outside
    }

    @Override
    protected Object3dBO getGlBufferObject() {
        return SkyBoxBO.getInstance();
    }

    @Override
    public float getCollisionRadius() {
        return 0;
    }

    @Override
    public float getVolume() {
        return 0;
    }

    @Override
    public Vector3fExt[] applyCollisionTo(Object3d obj2) {
        return new Vector3fExt[0];
    }

    @Override
    public List<Vector3fExt> getShadowVertices(Vector3fExt lightParam, ShadowVolume.LIGHT_PARAM_TYPE lightType) {
        return null;
    }
}

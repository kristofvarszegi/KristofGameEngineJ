package com.kristof.gameengine.screen;

import com.kristof.gameengine.shadow.ShadowVolume;
import com.kristof.gameengine.util.ColorVector;
import com.kristof.gameengine.util.Material;
import com.kristof.gameengine.util.Vector3fExt;
import com.kristof.gameengine.object3d.Object3d;
import com.kristof.gameengine.object3d.Object3dBO;

import java.util.List;

public class ScreenQuad extends Object3d {
    public ScreenQuad() {
        super(Vector3fExt.NULL_VECTOR, Vector3fExt.PI / 2f, 0, 0, Vector3fExt.UNIT_VECTOR,
                Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.RED, Material.BLACK_BODY,
                -1, -1);
    }

    @Override
    protected Object3dBO getGlBufferObject() {
        return ScreenQuadBO.getInstance();
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
        return (new Vector3fExt[]{Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR});
    }

    @Override
    public List<Vector3fExt> getShadowVertices(Vector3fExt lightParam, ShadowVolume.LIGHT_PARAM_TYPE lightType) {
        throw new RuntimeException("Shadow vertices N/A for a screen quad");
    }
}

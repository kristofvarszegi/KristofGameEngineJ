package com.kristof.gameengine.object3d;

import com.kristof.gameengine.shadow.ShadowVolume;
import com.kristof.gameengine.util.ColorVector;
import com.kristof.gameengine.util.Material;
import com.kristof.gameengine.util.Vector3fExt;

import java.util.List;
import java.util.Vector;

public class DummyObject3d extends Object3d {
    public DummyObject3d() {
        super(Vector3fExt.NULL_VECTOR, 0, 0, 0, Vector3fExt.UNIT_VECTOR,
                Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.NULL_COLOR, Material.BLACK_BODY,
                -1, -1);
    }

    @Override
    public Object3dBO getPrototype() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Vector3fExt[] applyCollisionTo(Object3d obj2) {
        return (new Vector3fExt[]{Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR});
    }

    @Override
    public List<Vector3fExt> getShadowVertices(Vector3fExt lightParam, ShadowVolume.LIGHT_PARAM_TYPE lightType) {
        return new Vector<>();
    }

    @Override
    public float getCollisionRadius() {
        return 0;
    }

    @Override
    public float getVolume() {
        return 0;
    }
}

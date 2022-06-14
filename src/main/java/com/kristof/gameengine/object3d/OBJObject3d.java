package com.kristof.gameengine.object3d;

import com.kristof.gameengine.shadow.ShadowVolume;
import com.kristof.gameengine.util.ColorVector;
import com.kristof.gameengine.util.Material;
import com.kristof.gameengine.util.Vector3fExt;

import java.util.List;

public class OBJObject3d extends Object3d {
    public OBJObject3dBO bufferObject;

    public OBJObject3d(Vector3fExt position, float polarAngle, float azimuthAngle, float rollAngle,
                       Vector3fExt velocity, Vector3fExt force, ColorVector color, Material material,
                       int colorMapTexIndex, int normalMapTexIndex, String fileName, float size) {
        super(position, polarAngle, azimuthAngle, rollAngle, new Vector3fExt(size), velocity, force, color,
                material, colorMapTexIndex, normalMapTexIndex);
        bufferObject = new OBJObject3dBO(fileName, 1f, size);
    }

    @Override
    public Object3dBO getPrototype() {
        return bufferObject;
    }

    @Override
    public float getCollisionRadius() {
        return (scale.getCoordAverage() * bufferObject.getSize());
    }

    @Override
    public float getVolume() {
        float r = this.getCollisionRadius() * bufferObject.getSize();
        return (4f * r * r * r * Vector3fExt.PI / 3f);
    }

    @Override
    public Vector3fExt[] applyCollisionTo(Object3d obj2) {
        return bufferObject.applyCollision(obj2, scale.getCoordAverage(), modelMatrix, rotationMatrix);
    }

    @Override
    public List<Vector3fExt> getShadowVertices(Vector3fExt lightParam, ShadowVolume.LIGHT_PARAM_TYPE lightType) {
        return bufferObject.getShadowVertices(lightParam, lightType, modelMatrix, rotationMatrix);
    }
}

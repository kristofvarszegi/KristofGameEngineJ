package com.kristof.gameengine.objobject;

import com.kristof.gameengine.object3d.Object3d;
import com.kristof.gameengine.object3d.Object3dBO;
import com.kristof.gameengine.shadow.ShadowVolume;
import com.kristof.gameengine.util.ColorVector;
import com.kristof.gameengine.util.Material;
import com.kristof.gameengine.util.Vector3fExt;

import java.util.List;

public class OBJObject3d extends Object3d {
    public OBJObject3dBO glBufferObject;

    public OBJObject3d(Vector3fExt position, float polarAngle, float azimuthAngle, float rollAngle,
                       Vector3fExt velocity, Vector3fExt force, ColorVector color, Material material,
                       int colorMapTexIndex, int normalMapTexIndex, String fileName, float size) {
        super(position, polarAngle, azimuthAngle, rollAngle, new Vector3fExt(size), velocity, force, color,
                material, colorMapTexIndex, normalMapTexIndex);
        glBufferObject = new OBJObject3dBO(fileName, 1f, size);
    }

    @Override
    protected Object3dBO getGlBufferObject() {
        return glBufferObject;
    }

    @Override
    public float getCollisionRadius() {
        return (scale.getCoordAverage() * glBufferObject.getSize());
    }

    @Override
    public float getVolume() {
        float r = this.getCollisionRadius() * glBufferObject.getSize();
        return (4f * r * r * r * Vector3fExt.PI / 3f);
    }

    @Override
    public Vector3fExt[] applyCollisionTo(Object3d obj2) {
        return glBufferObject.applyCollision(obj2, scale.getCoordAverage(), modelMatrix, rotationMatrix);
    }

    @Override
    public List<Vector3fExt> getShadowVertices(Vector3fExt lightParam, ShadowVolume.LIGHT_PARAM_TYPE lightType) {
        return glBufferObject.getShadowVertices(lightParam, lightType, modelMatrix, rotationMatrix);
    }
}

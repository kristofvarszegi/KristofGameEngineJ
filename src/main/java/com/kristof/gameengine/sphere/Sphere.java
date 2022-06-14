package com.kristof.gameengine.sphere;

import com.kristof.gameengine.util.ColorVector;
import com.kristof.gameengine.util.Material;
import com.kristof.gameengine.util.Vector3fExt;
import com.kristof.gameengine.object3d.Object3d;
import com.kristof.gameengine.object3d.Object3dBO;
import com.kristof.gameengine.shadow.ShadowVolume;

import java.util.List;
import java.util.Vector;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Sphere extends Object3d {
    private final float radius;

    public Sphere(Vector3fExt position, float polarAngle, float azimuthAngle, float rollAngle, Vector3fExt velocity,
                  Vector3fExt force, ColorVector color, Material material, int colorMapTexIndex, int normalMapTexIndex,
                  float radius) {
        super(position, polarAngle, azimuthAngle, rollAngle, new Vector3fExt(radius, radius, radius), velocity, force,
                color, material, colorMapTexIndex, normalMapTexIndex);
        this.radius = radius;
    }

    @Override
    public float getCollisionRadius() {
        return radius;
    }

    @Override
    public float getVolume() {
        return 4f * radius * radius * radius * Vector3fExt.PI / 3f;
    }

    public static float getVolume(float R) {
        return (4f / 3f) * Vector3fExt.PI * R * R * R;
    }

    @Override
    public Vector3fExt[] applyCollisionTo(Object3d obj2) {
        Vector3fExt[] retVal;
        final Vector3fExt diffObjSphere = Vector3fExt.substract(obj2.getPosition(), position);    // Pointing from position to incoming object
        final Vector3fExt locNormal = diffObjSphere.getNormalized();
        final float nComp = diffObjSphere.getLength() - radius;
        final float collDistance = nComp - (obj2.getCollisionRadius() + GAP_SIZE);
        if (collDistance >= 0) {    // if NOT touching
            return null;
        }
        retVal = calculateCollisionResults(obj2, locNormal, collDistance, diffObjSphere.getWithLength(radius));
        return retVal;
    }

    @Override
    public List<Vector3fExt> getShadowVertices(Vector3fExt lightParam, ShadowVolume.LIGHT_PARAM_TYPE lightType) {
        final Vector<Vector3fExt> shVxPos = new Vector<>();
        final Vector3fExt locNormal = lightParam.getThisMinus(position).getNormalized();
        final Vector3fExt locRight;
        if (locNormal.getThisMinus(Vector3fExt.Y_UNIT_VECTOR).getLengthSquare() < Vector3fExt.EQUALITY_RANGE) {
            locRight = Vector3fExt.X_UNIT_VECTOR;
        } else {
            locRight = (new Vector3fExt(locNormal.getZ(), 0, -locNormal.getX())).getNormalized();
        }
        final Vector3fExt locUp = locNormal.getCrossProductWith(locRight).getNormalized();
        final float dPhi = 2f * Vector3fExt.PI / (float) SphereBO.NUM_VERTICES_EW;
        for (int i = 0; i < SphereBO.NUM_VERTICES_EW; i++) {
            shVxPos.add(Vector3fExt.add(
                            locRight.getMultipliedBy(radius * (float) cos(i * dPhi)),
                            locUp.getMultipliedBy(radius * (float) sin(i * dPhi)))
                    .add(position));
        }
        return shVxPos;
    }

    @Override
    public Object3dBO getPrototype() {
        return sSpherePrototype;
    }

    @Override
    public String toString() {
        return (this.getClass().getSimpleName() + " r = " + radius + ", pos: " + position);
    }
}

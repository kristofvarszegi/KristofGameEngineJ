package com.kristof.gameengine.heightmap;

import com.kristof.gameengine.util.ColorVector;
import com.kristof.gameengine.util.Material;
import com.kristof.gameengine.util.Vector3fExt;
import com.kristof.gameengine.object3d.Object3d;
import com.kristof.gameengine.object3d.Object3dBO;
import com.kristof.gameengine.plate.Plate;
import com.kristof.gameengine.shadow.ShadowVolume;

import java.util.List;
import java.util.Vector;

public class HeightMapSeamless extends Object3d {
    private final float aSize;
    private final float bSize;
    private final float hMax;

    public HeightMapSeamless(Vector3fExt center, float polarAngle, float azimuthAngle, float rollAngle,
                             Vector3fExt velocity, Vector3fExt force, ColorVector color, Material material,
                             int colorMapTexIndex, int normalMapTexIndex, float a, float b, float h) {
        super(center, polarAngle, azimuthAngle, rollAngle, new Vector3fExt(a, h, b), velocity, force,
                color, material, colorMapTexIndex, normalMapTexIndex);
        aSize = a;
        bSize = b;
        hMax = h;
    }

    @Override
    protected Object3dBO getGlBufferObject() {
        return HeightMapSeamlessBO.getInstance();
    }

    @Override
    public float getCollisionRadius() {
        return (float) Math.max(hMax, Math.max(aSize, bSize));
    }

    @Override
    public float getVolume() {
        return aSize * bSize * Plate.PLATE_THICKNESS;
    }

    @Override
    public Vector3fExt[] applyCollisionTo(Object3d obj2) {
        Vector3fExt[] retVal = null;

        // Calculate distance from the plain
        final Vector3fExt rawDistance = obj2.getPosition().getThisMinus(position);
        final Vector3fExt plainTangent = Vector3fExt.X_UNIT_VECTOR.getMultipliedBy(rotationMatrix);
        final Vector3fExt plainBitangent = Vector3fExt.Z_UNIT_VECTOR.getReverse().getMultipliedBy(rotationMatrix);
		final float xComp = rawDistance.getDotProductWith(plainTangent);
        final float yComp = rawDistance.getDotProductWith(plainBitangent);
        if (Math.abs(xComp) > aSize / 2f || Math.abs(yComp) > bSize / 2f) {
            return null;
        } else {
            // TODO calc local height
            final Vector3fExt locN = Vector3fExt.normalFuncWithNeighbors(xComp, yComp, HeightFunction.PLAIN_FUNC);
            locN.DescartesToOpenGL();
            float nComponent = rawDistance.getDotProductWith(locN);
            if (nComponent < 0) {
                locN.reverseDirection();
                nComponent *= -1f;
            }
            final float collDistance = nComponent - obj2.getCollisionRadius();// - GAP_SIZE;
            final Vector3fExt collPoint = rawDistance.getProjectedToSurface(    // in world space
                    position,
                    Vector3fExt.Y_UNIT_VECTOR.getMultipliedBy(rotationMatrix)).getSumWith(position);
            if (collDistance < 0) {    // if colliding
                retVal = calculateCollisionResults(obj2, locN, collDistance, collPoint);
            }
            return retVal;
        }
    }

    @Override
    public List<Vector3fExt> getShadowVertices(Vector3fExt lightParam, ShadowVolume.LIGHT_PARAM_TYPE lightType) {
        final List<Vector3fExt> shVs = new Vector<>();
        shVs.add(new Vector3fExt());
        return shVs;
    }
}

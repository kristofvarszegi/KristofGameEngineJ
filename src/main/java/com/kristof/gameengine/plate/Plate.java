package com.kristof.gameengine.plate;

import com.kristof.gameengine.object3d.Object3d;
import com.kristof.gameengine.rectangle.Rectangle;
import com.kristof.gameengine.util.ColorVector;
import com.kristof.gameengine.util.Material;
import com.kristof.gameengine.util.Matrix4f;
import com.kristof.gameengine.util.Vector3fExt;

public abstract class Plate extends Object3d {
    public static float PLATE_THICKNESS = 0.0005f;

    protected Vector3fExt normal = Vector3fExt.Y_UNIT_VECTOR.getCopy();
    protected Vector3fExt right = Vector3fExt.X_UNIT_VECTOR.getCopy();
    protected Vector3fExt up = Vector3fExt.Z_UNIT_VECTOR.getReverse();
    protected int id = -1;
    protected int[] neighborIDs = new int[]{-1, -1, -1, -1};

    public Plate(Vector3fExt position, float polarAngle, float azimuthAngle, float rollAngle, Vector3fExt scale,
                 Vector3fExt velocity, Vector3fExt force, ColorVector color, Material material, int colorMapTexIndex,
                 int normalMapTexIndex) {
        super(position, polarAngle, azimuthAngle, rollAngle, scale, velocity, force, color, material, colorMapTexIndex,
                normalMapTexIndex);
    }

    public Plate(Vector3fExt position, Matrix4f rotationMatrix, Vector3fExt scale, Vector3fExt velocity,
                 Vector3fExt force, ColorVector color, Material material, int colorMapTexIndex,
                 int normalMapTexIndex) {
        super(position, rotationMatrix, scale, velocity, force, color, material, colorMapTexIndex, normalMapTexIndex);
    }

    public Vector3fExt getNormal() {
        return normal.getCopy();
    }

    public Vector3fExt getRight() {
        return right.getCopy();
    }

    public Vector3fExt getUp() {
        return up.getCopy();
    }

    public int getId() {
        return id;
    }

    public int[] getNeighborIDs() {
        return neighborIDs;
    }

    public void setId(int newId) {
        id = newId;
    }

    public void setNeighborIds(int[] newNeighborIds) {
        int maxL = Math.min(neighborIDs.length, newNeighborIds.length);
        System.arraycopy(newNeighborIds, 0, neighborIDs, 0, maxL);
    }

    public void setNeighborIds(int nId1, int nId2, int nId3) {
        neighborIDs[0] = nId1;
        neighborIDs[1] = nId2;
        neighborIDs[2] = nId3;
    }

    public void setNeighborIds(int nId1, int nId2, int nId3, int nId4) {
        neighborIDs[0] = nId1;
        neighborIDs[1] = nId2;
        neighborIDs[2] = nId3;
        neighborIDs[3] = nId4;
    }

    public void transform(Matrix4f modelMatrix, Matrix4f rotationMatrix) {
        normal.multiplyBy(rotationMatrix);
        right.multiplyBy(rotationMatrix);
        up = normal.getCrossProductWith(right);
        position.multiplyBy(modelMatrix);
        transformPlateAttribsBy(modelMatrix, rotationMatrix);    //position, polarAngle, azimuthAngle, rollAngle, scale);
    }

    @Override
    public Vector3fExt[] applyCollisionTo(Object3d obj2) {
        // TODO variable collision displacement on edges
        if (obj2.getPosition().getThisMinus(getBasePoint()).getLengthSquare()
                > (getAverageSize() + obj2.getCollisionRadius()) * (getAverageSize() + obj2.getCollisionRadius())) {
            return null;
        }

        final Vector3fExt[] retVal;
		final Vector3fExt rawDistance = obj2.getPosition().getThisMinus(getBasePoint());//position);
        float normalComponent = rawDistance.getDotProductWith(normal);
        final Vector3fExt locNormal = normal.getCopy();
        if (normalComponent < 0) {
            locNormal.reverseDirection();
            normalComponent *= -1f;
        }

        float collDistance = normalComponent - (obj2.getCollisionRadius() + GAP_SIZE);
        if (collDistance >= 0) {    // If NOT touching the Triangle's plain
            return null;
        }
        if (!isOnPlain(obj2.getPosition())) {    // If NOT above the Triangle
            return null;
        }
        retVal = calculateCollisionResults(obj2, locNormal, collDistance,
                rawDistance.getProjectedToSurface(getBasePoint(), normal).getSumWith(getBasePoint()));
        return retVal;
    }

    public boolean isFacingInwardVector(Vector3fExt inwardVector) {
        return normal.getDotProductWith(inwardVector) < 0;
    }

    public boolean isNeighbor(int possibleNeighborId) {
        for (final int neighborID : neighborIDs) {
            if (neighborID == possibleNeighborId) return true;
        }
        return false;
    }

    public abstract Vector3fExt getVertex(int i);

    public abstract float getAverageSize();

    public abstract Vector3fExt getBasePoint();

    public abstract boolean isOnPlain(Vector3fExt point);

    public abstract void transformPlateAttribsBy(Matrix4f modelMatrix, Matrix4f rotationMatrix);

    public abstract Vector3fExt[] getCommonEdgeWith(Rectangle face2);
}

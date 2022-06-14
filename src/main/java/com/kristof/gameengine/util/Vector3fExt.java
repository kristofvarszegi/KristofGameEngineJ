package com.kristof.gameengine.util;

import com.kristof.gameengine.heightmap.HeightFunction;
import com.kristof.gameengine.object3d.Object3d;

import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;

public class Vector3fExt {
    public static final int NUM_COORDINATES = 3;
    public static final Vector3fExt NULL_VECTOR = new Vector3fExt(0f, 0f, 0f);
    public static final Vector3fExt X_UNIT_VECTOR = new Vector3fExt(1f, 0, 0);
    public static final Vector3fExt Y_UNIT_VECTOR = new Vector3fExt(0, 1f, 0);
    public static final Vector3fExt Z_UNIT_VECTOR = new Vector3fExt(0, 0, 1f);
    public static final Vector3fExt UNIT_VECTOR = new Vector3fExt(1f, 1f, 1f);
    public static final Vector3fExt INFINITY = new Vector3fExt(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    public static float NEIGHBOR_DISTANCE = 0.001f;
    public static final float PI = (float) Math.PI;
    public static final float EQUALITY_RANGE = 0.0001f;//(float) Math.pow(10, 12) * Float.MIN_VALUE;
    public static final float EQUALITY_RANGE_SQ = EQUALITY_RANGE * EQUALITY_RANGE;

    private final float[] coords;

    public Vector3fExt() {
        coords = new float[]{0f, 0f, 0f};
    }

    public Vector3fExt(float xCoord, float yCoord, float zCoord) {
        coords = new float[]{xCoord, yCoord, zCoord};
    }

    public Vector3fExt(float[] coords) {
        this.coords = new float[3];
        if (coords.length >= 3) {
            System.arraycopy(coords, 0, this.coords, 0, 3);
        } else {
            this.coords[0] = this.coords[1] = this.coords[2] = 0;
        }
    }

    public Vector3fExt(Vector3fExt vr2) {
        coords = new float[3];
        for (int i = 0; i < 3; i++) {
            coords[i] = vr2.getCoord(i);
        }
    }

    public Vector3fExt(float length1D) {
        coords = new float[3];
        for (int i = 0; i < 3; i++) {
            coords[i] = length1D;
        }
    }

    public Vector3fExt(Vector3fExt direction, float magnitude) {
        this(direction);
        this.setLength(magnitude);
    }

    public Vector3fExt(float polarAngle, float azimuthAngle, float length, Vector3fExt zAxis, Vector3fExt xAxis) {
        this();
        setByPolarCoords(polarAngle, azimuthAngle, length, zAxis, xAxis);
    }

    public Vector3fExt(float polarAngle, float azimuthAngle, Vector3fExt zAxis, Vector3fExt xAxis) {
        this(polarAngle, azimuthAngle, 1f, zAxis, xAxis);
    }

    public Vector3fExt(Matrix4f sourceMatrix) {
        coords = new float[3];
        coords[0] = sourceMatrix.m00;
        coords[1] = sourceMatrix.m01;
        coords[2] = sourceMatrix.m02;
    }

    public float getX() {
        return coords[0];
    }

    public float getY() {
        return coords[1];
    }

    public float getZ() {
        return coords[2];
    }

    public float getCoord(int dimIndex) {
        if (dimIndex < 0 || dimIndex > 2) {
            throw new IllegalArgumentException("Dim index out of bounds: " + dimIndex);
        }
        return coords[dimIndex];
    }

    public float[] getAsArray3() {
        final float[] rA = new float[3];
        System.arraycopy(coords, 0, rA, 0, 3);
        return rA;
    }

    public float[] getAsArray4() {
        final float[] rA = new float[4];
        System.arraycopy(coords, 0, rA, 0, 3);
        rA[3] = 1f;
        return rA;
    }

    public Vector3f getAsVector3f() {
        return new Vector3f(coords[0], coords[1], coords[2]);
    }

    public Matrix4f getAsMatrix4f() {
        final Matrix4f retMx = new Matrix4f();
        retMx.m00 = coords[0];
        retMx.m01 = coords[1];
        retMx.m02 = coords[2];
        retMx.m03 = 1f;

        retMx.m10 = 0;
        retMx.m11 = 0;
        retMx.m12 = 0;
        retMx.m13 = 0;

        retMx.m20 = 0;
        retMx.m21 = 0;
        retMx.m22 = 0;
        retMx.m23 = 0;

        retMx.m30 = 0;
        retMx.m31 = 0;
        retMx.m32 = 0;
        retMx.m33 = 0;

        return retMx;
    }

    public Vector3fExt getCopy() {
        return new Vector3fExt(this);
    }

    public Vector3fExt getXY() {
        return new Vector3fExt(coords[0], coords[1], 0);
    }

    public Vector3fExt getXZ() {
        return new Vector3fExt(coords[0], 0, coords[2]);
    }

    public Vector3fExt getYZ() {
        return new Vector3fExt(0, coords[1], coords[2]);
    }

    public float getCoordAverage() {
        return (coords[0] + coords[1] + coords[2]) / 3f;
    }

    public static float getAngle(Vector3fExt vr1, Vector3fExt vr2) {
        return (float) acos(Vector3fExt.dotProduct(vr1.getNormalized(), vr2.getNormalized()));
    }

    public Vector3fExt getRotatedBy(float polarAngle, float azimuthAngle, float rollAngle, Vector3fExt zAxis, Vector3fExt xAxis) {
        Vector3fExt rotatedVr = new Vector3fExt(this);
        rotatedVr.rotateBy(polarAngle, azimuthAngle, zAxis, xAxis);    // TODO rollAngle
        return rotatedVr;
    }

    public void setByPolarCoords(float polarAngle, float azimuthAngle, float length, Vector3fExt zAxis, Vector3fExt xAxis) {
        this.reset();
        add(zAxis.getWithLength((float) cos(polarAngle) * length));
        add(xAxis.getWithLength((float) sin(polarAngle) * (float) cos(azimuthAngle) * length));
        add(zAxis.getCrossProductWith(xAxis).getWithLength((float) sin(polarAngle) * (float) sin(azimuthAngle) * length));
    }

    public void reset() {
        Arrays.fill(coords, 0);
    }

    public boolean isAnyCoordZero() {
        if (coords[0] == 0) return true;
        if (coords[1] == 0) return true;
        if (coords[2] == 0) return true;
        return false;
    }

    public boolean isAnyCoordLessThan(float limit) {
        for (final float coord : coords) {
            if (coord < limit) return true;
        }
        return false;
    }

    public boolean equals(Object obj2) {    // Default implementation checks full identity, but floats need an epsilon range
        if (obj2 instanceof Vector3fExt) {
            final Vector3fExt svr2 = (Vector3fExt) obj2;
            if (Math.abs(coords[0] - svr2.getX()) > EQUALITY_RANGE) return false;
            if (Math.abs(coords[1] - svr2.getY()) > EQUALITY_RANGE) return false;
            if (Math.abs(coords[2] - svr2.getZ()) > EQUALITY_RANGE) return false;
            return true;
        } else {
            return false;
        }
    }

    public static boolean arrayEquals(Vector3fExt[] sVrA1, Vector3fExt[] sVrA2) {
        if (sVrA1.length != sVrA2.length) return false;
        for (int i = 0; i < sVrA1.length; i++) {
            if (!sVrA1[i].equals(sVrA2[i])) return false;
        }
        return true;
    }

    public static boolean hasCommonVertex(Vector3fExt[] sVrA1, Vector3fExt[] sVrA2) {
        for (final Vector3fExt vector3FExt : sVrA1) {
            for (final Vector3fExt fM : sVrA2) {
                if (vector3FExt.equals(fM)) return true;
            }
        }
        return false;
    }

    public void setX(float x) {
        coords[0] = x;
    }

    public void setY(float y) {
        coords[1] = y;
    }

    public void setZ(float z) {
        coords[2] = z;
    }

    public Vector3fExt getRounded(int decimals) {
        final Vector3fExt rvr = new Vector3fExt();
        final float rd = (float) Math.pow(10, decimals);
        for (int i = 0; i < coords.length; i++) {
            rvr.setCoord(i, Math.round(coords[i] * rd) / rd);
        }
        return rvr;
    }

    public float getLengthRounded(int decimals) {
        final float rd = (float) Math.pow(10, decimals);
        return Math.round((getLength() * rd) / rd);
    }

    public Vector3fExt getReverse() {
        final Vector3fExt revVr = new Vector3fExt(this);
        revVr.reverseDirection();
        return revVr;
    }

    public Vector3fExt getReflectedFrom(Vector3fExt reflPlainNormal, float normalReflectionFactor,
                                        float surfaceFrictionCoefficent) {
        final Vector3fExt rPN = reflPlainNormal.getNormalized();
        final float nComp = this.getDotProductWith(rPN);
        final Vector3fExt ruVr = this.getThisMinus(reflPlainNormal.getMultipliedBy(nComp));
        ruVr.multiply(surfaceFrictionCoefficent);
        return ruVr.getSumWith(reflPlainNormal.getMultipliedBy(-nComp * normalReflectionFactor));
    }

    public Vector3fExt getMultipliedBy(float multM) {
        return Vector3fExt.multiply(this, multM);
    }

    public Vector3fExt getWithLength(float len) {
        return this.getNormalized().getMultipliedBy(len);
    }

    public Vector3fExt getMultipliedBy(Matrix4f multM) {
        final Vector3fExt multVr = new Vector3fExt(this);
        multVr.multiplyBy(multM);
        return multVr;
    }

    public float getAngleWith(Vector3fExt v2) {
        return (float) Math.acos(getNormalized().getDotProductWith(v2.getNormalized()));
    }

    /**
     * Returns the 3D point projected onto the plain represented by the parameters, in surface space.
     *
     * @param surfacePoint  a point of the surface
     * @param surfaceNormal a normal vector of the surface
     * @return The 3D point projected onto the plain represented by the parameters, in surface space.
     */
    public Vector3fExt getProjectedToSurface(Vector3fExt surfacePoint, Vector3fExt surfaceNormal) {
        final Vector3fExt rawDistance = getThisMinus(surfacePoint);
        final float normalComp = rawDistance.getDotProductWith(surfaceNormal.getNormalized());
        return getThisMinus(surfaceNormal.getNormalized().getMultipliedBy(normalComp));
    }

    public void setCoord(int i, float c) {
        if (i < 3 && i >= 0) coords[i] = c;
    }

    public Vector3fExt add(Vector3fExt svr) {
        for (int i = 0; i < 3; i++) {
            coords[i] += svr.getCoord(i);
        }
        return this;
    }

    public Vector3fExt add(float plusX, float plusY, float plusZ) {
        coords[0] += plusX;
        coords[1] += plusY;
        coords[2] += plusZ;
        return this;
    }

    public Vector3fExt getSumWith(Vector3fExt svr2) {
        return Vector3fExt.add(this, svr2);
    }

    public Vector3fExt getSumWith(float x2, float y2, float z2) {
        return Vector3fExt.add(this, new Vector3fExt(x2, y2, z2));
    }

    public Vector3fExt getThisMinus(Vector3fExt svr2) {
        return Vector3fExt.substract(this, svr2);
    }

    public float getDistanceSquare(Vector3fExt svr2) {
        return Vector3fExt.substract(this, svr2).getLengthSquare();
    }

    public float getDistance(Vector3fExt svr2) {
        return Vector3fExt.substract(this, svr2).getLength();
    }

    public Vector3fExt addX(float plusX) {
        coords[0] += plusX;
        return this;
    }

    public Vector3fExt addY(float plusY) {
        coords[1] += plusY;

        return this;
    }

    public Vector3fExt addZ(float plusZ) {
        coords[2] += plusZ;

        return this;
    }

    public void substract(Vector3fExt svr) {
        for (int i = 0; i < 3; i++) {
            coords[i] -= svr.getCoord(i);
        }
    }

    public void substractFromLength(float sub) {
        setLength(getLength() - sub);
    }

    @Override
    public String toString() {
        return "(" + coords[0] + ", " + coords[1] + ", " + coords[2] + ")";
    }

    public float getLength() {
        return (float) (sqrt(coords[0] * coords[0] + coords[1] * coords[1] + coords[2] * coords[2]));
    }

    public float getLengthSquare() {
        return coords[0] * coords[0] + coords[1] * coords[1] + coords[2] * coords[2];
    }

    public static float vectorAbs(float[] svr) {
        return (float) sqrt(svr[0] * svr[0] + svr[1] * svr[1] + svr[2] * svr[2]);
    }

    public static float vectorAbs(float x, float y, float z) {
        return (float) sqrt(x * x + y * y + z * z);
    }

    public static float vectorAbs(Vector3fExt svr) {
        return (float) sqrt(svr.getX() * svr.getX() + svr.getY() * svr.getY() + svr.getZ() * svr.getZ());
    }

    public float getPolarAngle(Vector3fExt zAxis) {
        return (float) Math.acos(this.getNormalized().getDotProductWith(zAxis.getNormalized()));
    }

    public float getAzimuthAngle(Vector3fExt zAxis, Vector3fExt xAxis) {
        final Vector3fExt xAxisN = xAxis.getNormalized();
        final Vector3fExt zAxisN = zAxis.getNormalized();
        final Vector3fExt yAxisN = zAxis.getCrossProductWith(xAxis);
        final Vector3fExt thisN = this.getNormalized();
        final float zComp = thisN.getDotProductWith(zAxisN);
        final Vector3fExt thisNXY = thisN.getThisMinus(zAxisN.getMultipliedBy(zComp));
        return (float) Math.atan2(thisNXY.getDotProductWith(yAxisN), thisNXY.getDotProductWith(xAxisN));
    }

    public void DescartesToOpenGL() {
        final float oldY = coords[1];
        coords[1] = coords[2];
        coords[2] = -oldY;
    }

    public void OpenGLToDescartes() {
        final float oldY = -coords[2];
        coords[2] = coords[1];
        coords[1] = oldY;
    }

    public void normalize() {
        final float va = getLength();
        if (va != 0) {
            for (int i = 0; i < 3; i++) {
                coords[i] /= va;
            }
        }
    }

    public Vector3fExt getNormalized() {
        final Vector3fExt retVr = new Vector3fExt(this);
        retVr.normalize();
        return retVr;
    }

    /**
     * Multiplies each coordinate of the vector by "scale".
     *
     * @param scale The scaling parameter.
     */
    public void multiply(float scale) {
        for (int i = 0; i < 3; i++) {
            coords[i] *= scale;
        }
    }

    /**
     * Multiplies each coordinate of the vector by the corresponding coordinate of parameter "scale".
     *
     * @param scale The scaling vector.
     */
    public void multiply(Vector3fExt scale) {
        for (int i = 0; i < 3; i++) {
            coords[i] *= scale.getCoord(i);
        }
    }

    public void multiplyBy(Matrix4f multMatrix) {
        final Matrix4f resultMx = new Matrix4f();
        Matrix4f.mul(multMatrix, this.getAsMatrix4f(), resultMx);
        coords[0] = resultMx.m00;
        coords[1] = resultMx.m01;
        coords[2] = resultMx.m02;
    }

    public void multiplyX(float scX) {
        coords[0] *= scX;
    }

    public void multiplyY(float scY) {
        coords[1] *= scY;
    }

    public void multiplyZ(float scZ) {
        coords[2] *= scZ;
    }

    public void reverseDirection() {
        for (int i = 0; i < 3; i++) {
            coords[i] *= -1f;
        }
    }

    public void reflectFrom(Vector3fExt reflPlainNormal) {
        final Vector3fExt rPN = reflPlainNormal.getNormalized();
        final float nComp = this.getDotProductWith(rPN);
        this.add(rPN.getMultipliedBy(-2f * nComp));
    }

    public static Vector3fExt multiply(Vector3fExt svr, float sc) {
        final Vector3fExt rvr = new Vector3fExt();
        for (int i = 0; i < 3; i++) {
            rvr.setCoord(i, sc * svr.getCoord(i));
        }
        return rvr;
    }

    public static Vector3fExt multiply(Vector3fExt svr, Matrix4f multMatrix) {
        final Vector3fExt rvr = new Vector3fExt(svr);
        rvr.multiplyBy(multMatrix);
        return rvr;
    }

    public static Vector3fExt add(Vector3fExt svr1, Vector3fExt svr2) {
        final Vector3fExt rvr = new Vector3fExt();
        for (int i = 0; i < 3; i++) {
            rvr.setCoord(i, svr1.getCoord(i) + svr2.getCoord(i));
        }
        return rvr;
    }

    public static Vector3fExt add(Vector3fExt svr1, Vector3fExt svr2, Vector3fExt svr3) {
        final Vector3fExt rvr = new Vector3fExt();
        for (int i = 0; i < 3; i++) {
            rvr.setCoord(i, svr1.getCoord(i) + svr2.getCoord(i) + svr3.getCoord(i));
        }
        return rvr;
    }

    public static Vector3fExt sum(List<Vector3fExt> vrList) {
        final Vector3fExt sumVr = new Vector3fExt();
        for (final Vector3fExt vector3FExt : vrList) {
            sumVr.add(vector3FExt);
        }
        return sumVr;
    }

    public static Vector3fExt sum(Vector3fExt svr1, Vector3fExt svr2, Vector3fExt svr3) {
        final Vector3fExt sumVr = new Vector3fExt();
        sumVr.add(svr1);
        sumVr.add(svr2);
        sumVr.add(svr3);
        return sumVr;
    }

    public static Vector3fExt substract(Vector3fExt svr1, Vector3fExt svr2) {
        final Vector3fExt rvr = new Vector3fExt();
        for (int i = 0; i < 3; i++) {
            rvr.setCoord(i, svr1.getCoord(i) - svr2.getCoord(i));
        }
        return rvr;
    }

    public static Vector3fExt substract(float x1, float y1, float z1, float x2, float y2, float z2) {
        final Vector3fExt rvr = new Vector3fExt();
        rvr.setX(x1 - x2);
        rvr.setY(y1 - y2);
        rvr.setZ(z1 - z2);
        return rvr;
    }

    public static float dotProduct(Vector3fExt svr1, Vector3fExt svr2) {
        return svr1.getX() * svr2.getX() + svr1.getY() * svr2.getY() + svr1.getZ() * svr2.getZ();
    }

    public float getDotProductWith(Vector3fExt svr2) {
        return coords[0] * svr2.getX() + coords[1] * svr2.getY() + coords[2] * svr2.getZ();
    }

    public static Vector3fExt crossProduct(Vector3fExt svr1, Vector3fExt svr2) {
        final Vector3fExt resVr = new Vector3fExt();
        resVr.setX(svr1.getY() * svr2.getZ() - svr1.getZ() * svr2.getY());
        resVr.setY(-svr1.getX() * svr2.getZ() + svr1.getZ() * svr2.getX());
        resVr.setZ(svr1.getX() * svr2.getY() - svr1.getY() * svr2.getX());
        return resVr;
    }

    public Vector3fExt getCrossProductWith(Vector3fExt svr2) {
        final Vector3fExt resVr = new Vector3fExt();
        resVr.setX(coords[1] * svr2.getZ() - coords[2] * svr2.getY());
        resVr.setY(-coords[0] * svr2.getZ() + coords[2] * svr2.getX());
        resVr.setZ(coords[0] * svr2.getY() - coords[1] * svr2.getX());
        return resVr;
    }

    public static float mixedProduct(Vector3fExt svr1, Vector3fExt svr2, Vector3fExt svr3) {
        return Vector3fExt.dotProduct(svr1, Vector3fExt.crossProduct(svr2, svr3));
    }

    public static Vector3fExt reverseDirection(Vector3fExt svr) {
        return new Vector3fExt(Vector3fExt.multiply(svr, -1));
    }

    public static Vector3fExt normalize(Vector3fExt svr) {
        final Vector3fExt nvr = new Vector3fExt(svr);
        nvr.normalize();
        return nvr;
    }

    public static float absoluteDistance(Vector3fExt svr1, Vector3fExt svr2) {
        return vectorAbs(Vector3fExt.substract(svr1, svr2));
    }

    public static float absoluteDistanceSquare(Vector3fExt svr1, Vector3fExt svr2) {
        return Vector3fExt.substract(svr1, svr2).getLengthSquare();
    }

    public static float absoluteDistance(Object3d o1, Object3d o2) {
        return vectorAbs(Vector3fExt.substract(o1.getPosition(), o2.getPosition()));
    }

    public static Vector3fExt average(Vector3fExt svr1, Vector3fExt svr2) {
        return new Vector3fExt(0.5f * (svr1.getX() + svr2.getX()), 0.5f * (svr1.getY() + svr2.getY()),
                0.5f * (svr1.getZ() + svr2.getZ()));
    }

    public static Vector3fExt average(Vector3fExt svr1, Vector3fExt svr2, Vector3fExt svr3) {
        return Vector3fExt.sum(svr1, svr2, svr3).getMultipliedBy(1f / 3f);
    }

    public static Vector3fExt average(Vector3fExt svr1, Vector3fExt svr2, Vector3fExt svr3, Vector3fExt svr4) {
        return Vector3fExt.sum(svr1, svr2, svr3).getSumWith(svr4).getMultipliedBy(0.25f);
    }

    public static Vector3fExt average(List<Vector3fExt> svrs) {
        if (svrs.size() == 0) return (new Vector3fExt());
        final Vector3fExt avVr = sum(svrs);
        avVr.divideAbs((float) svrs.size());
        return avVr;
    }

    public Vector3fExt setLength(float sc) {
        normalize();
        for (int i = 0; i < 3; i++) {
            coords[i] *= sc;
        }
        return this;
    }

    public void divideAbs(float den) {
        for (int i = 0; i < 3; i++) {
            coords[i] /= den;
        }
    }

    public static void normalize(float[] svr) {
        final float va = vectorAbs(svr);
        for (int i = 0; i < 3; i++) {
            svr[i] /= va;
        }
    }

    public void trim(float minLength) {
        if (vectorAbs(this) < minLength) {
            coords[0] = 0;
            coords[1] = 0;
            coords[2] = 0;
        }
    }

    public static Vector3fExt randomVector(float abs) {
        final Vector3fExt rVr = new Vector3fExt((float) Math.random() - 0.5f,
                (float) Math.random() - 0.5f, (float) Math.random() - 0.5f);
        rVr.setLength(abs);
        return rVr;
    }

    public void rotateBy(float polarAngle, float azimuthAngle, Vector3fExt zAxis, Vector3fExt xAxis) {
        if (polarAngle == 0 && azimuthAngle == 0) return;
        final Matrix4f rotMx = new Matrix4f();
        final Vector3fExt yAxis = zAxis.getCrossProductWith(xAxis);
        rotMx.rotate(azimuthAngle, zAxis.getAsVector3f());
        rotMx.rotate(polarAngle, yAxis.getReverse().getAsVector3f());
        this.multiplyBy(rotMx);
    }

    public void rotateBy(float angle, Vector3fExt axis) {
        if (angle == 0) return;
        final Matrix4f rotMx = new Matrix4f();
        rotMx.rotate(angle, axis.getAsVector3f());
        this.multiplyBy(rotMx);
    }

    public static Vector3fExt rotatingVectorXZ(float seed, float ampl, Vector3fExt offset) {
        return new Vector3fExt(ampl * (float) sin(seed) + offset.getX(), offset.getY(),
                ampl * (float) cos(seed) + offset.getZ());
    }

    public static Vector3fExt normalFuncWithNeighbors(float x, float y, HeightFunction hMFunc) {
        final float leftZ = hMFunc.height(x - NEIGHBOR_DISTANCE, y);
        final float rightZ = hMFunc.height(x + NEIGHBOR_DISTANCE, y);
        final float dZX = rightZ - leftZ;
        final Vector3fExt xDerVr = (new Vector3fExt(2 * NEIGHBOR_DISTANCE, 0, dZX)).getNormalized();
        final float backZ = hMFunc.height(x, y - NEIGHBOR_DISTANCE);
        final float frontZ = hMFunc.height(x, y + NEIGHBOR_DISTANCE);
        final float dZY = frontZ - backZ;
        final Vector3fExt yDerVr = (new Vector3fExt(0, 2 * NEIGHBOR_DISTANCE, dZY)).getNormalized();
        return xDerVr.getCrossProductWith(yDerVr).getNormalized();
    }

    public boolean isInOnePlain(List<Vector3fExt> points) {
        if (points.size() <= 2) {
            return true;
        } else {
            final Vector3fExt center = Vector3fExt.average(points);
            final Vector3fExt normal = Vector3fExt.crossProduct(points.get(0).getThisMinus(center),
                    points.get(1).getThisMinus(center)).getNormalized();
            for (int i = 2; i < points.size(); i++) {
                if (points.get(i).getThisMinus(center).getDotProductWith(normal) > EQUALITY_RANGE) return false;
            }
            return true;
        }
    }

    public void transformToPositiveCoords() {
        for (int i = 0; i < coords.length; i++) {
            if (coords[i] < 0) coords[i] *= -1f;
        }
    }
}

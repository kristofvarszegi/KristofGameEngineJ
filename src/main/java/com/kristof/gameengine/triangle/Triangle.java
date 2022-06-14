package com.kristof.gameengine.triangle;

import com.kristof.gameengine.rectangle.Rectangle;
import com.kristof.gameengine.util.ColorVector;
import com.kristof.gameengine.util.Material;
import com.kristof.gameengine.util.Matrix4f;
import com.kristof.gameengine.util.Vector3fExt;
import com.kristof.gameengine.object3d.Object3dBO;
import com.kristof.gameengine.plate.Plate;

import com.kristof.gameengine.shadow.ShadowVolume;

import java.util.List;
import java.util.Vector;

public class Triangle extends Plate {
    // Vertices in CCW order. The 1st and the 2nd holds the biggest side of the triangle.
    private Vector3fExt vertex1;
    private Vector3fExt vertex2;
    private Vector3fExt vertex3;
    private float baseLength = 0;
    private float baseLengthLeftHalf = 0;
    private float baseLengthRightHalf = 0;
    private float height = 0;
    private final TriangleBO bufferObject;

    /*
     * Constructors
     *
     * Vertices given in world space, then transformed into Triangle position space
     */

    public Triangle(float polarAngle, float azimuthAngle, float rollAngle, Vector3fExt velocity, Vector3fExt force,
                    ColorVector color, Material material, int colorMapTexIndex, int normalMapTexIndex,
                    Vector3fExt vertex1, Vector3fExt vertex2, Vector3fExt vertex3) {
        super(new Vector3fExt(), polarAngle, azimuthAngle, rollAngle, Vector3fExt.UNIT_VECTOR, velocity, force,
                color, material, colorMapTexIndex, normalMapTexIndex);  // TODO scale
        final float d12 = vertex2.getThisMinus(vertex1).getLengthSquare();
        final float d23 = vertex3.getThisMinus(vertex2).getLengthSquare();
        final float d31 = vertex1.getThisMinus(vertex3).getLengthSquare();
        final float maxD = Math.max(d12, Math.max(d23, d31));
        if (maxD == d12) {
            this.vertex1 = vertex1.getCopy();
            this.vertex2 = vertex2.getCopy();
            this.vertex3 = vertex3.getCopy();
        } else {
            if (maxD == d23) {
                this.vertex1 = vertex2.getCopy();
                this.vertex2 = vertex3.getCopy();
                this.vertex3 = vertex1.getCopy();
            } else {
                if (maxD == d31) {
                    this.vertex1 = vertex3.getCopy();
                    this.vertex2 = vertex1.getCopy();
                    this.vertex3 = vertex2.getCopy();
                }
            }
        }

        normal = Vector3fExt.crossProduct(vertex2.getThisMinus(vertex1), vertex3.getThisMinus(vertex1)).getNormalized();
        right = this.vertex2.getThisMinus(this.vertex1).getNormalized();
        up = normal.getCrossProductWith(right);

        baseLength = this.vertex2.getThisMinus(this.vertex1).getLength();
        baseLengthLeftHalf = this.vertex3.getThisMinus(this.vertex1).getDotProductWith(right);
        baseLengthRightHalf = baseLength - baseLengthLeftHalf;
        height = this.vertex3.getThisMinus(this.vertex1).getDotProductWith(up);
        position = new Vector3fExt();

        bufferObject = new TriangleBO(this.vertex1, this.vertex2, this.vertex3);
    }

    public Triangle(Matrix4f rotationMatrix, Vector3fExt velocity, Vector3fExt force, ColorVector color,
                    Material material, int colorMapTexIndex, int normalMapTexIndex, Vector3fExt vertex1,
                    Vector3fExt vertex2, Vector3fExt vertex3) {
        super(new Vector3fExt(), rotationMatrix, Vector3fExt.UNIT_VECTOR, velocity, force, color, material,
                colorMapTexIndex, normalMapTexIndex);   // TODO scale
        final float d12 = vertex2.getThisMinus(vertex1).getLengthSquare();
        final float d23 = vertex3.getThisMinus(vertex2).getLengthSquare();
        final float d31 = vertex1.getThisMinus(vertex3).getLengthSquare();
        final float maxD = Math.max(d12, Math.max(d23, d31));
        if (maxD == d12) {
            this.vertex1 = vertex1.getCopy();
            this.vertex2 = vertex2.getCopy();
            this.vertex3 = vertex3.getCopy();
        } else {
            if (maxD == d23) {
                this.vertex1 = vertex2.getCopy();
                this.vertex2 = vertex3.getCopy();
                this.vertex3 = vertex1.getCopy();
            } else {
                if (maxD == d31) {
                    this.vertex1 = vertex3.getCopy();
                    this.vertex2 = vertex1.getCopy();
                    this.vertex3 = vertex2.getCopy();
                }
            }
        }

        normal = Vector3fExt.crossProduct(vertex2.getThisMinus(vertex1), vertex3.getThisMinus(vertex1)).getNormalized();
        right = this.vertex2.getThisMinus(this.vertex1).getNormalized();
        up = normal.getCrossProductWith(right);

        baseLength = this.vertex2.getThisMinus(this.vertex1).getLength();
        baseLengthLeftHalf = this.vertex3.getThisMinus(this.vertex1).getDotProductWith(right);
        baseLengthRightHalf = baseLength - baseLengthLeftHalf;
        height = this.vertex3.getThisMinus(this.vertex1).getDotProductWith(up);
        position = new Vector3fExt();

        bufferObject = new TriangleBO(this.vertex1, this.vertex2, this.vertex3);
    }

    public Triangle(Triangle tr2) {    // Deep copy ctor
        super(tr2.getPosition(), tr2.getRotationMatrix(), tr2.getScale(), tr2.getVelocity(), tr2.getForce(),
                tr2.getColor(), tr2.getMaterial(), tr2.getColorMapTexIndex(), tr2.getNormalMapTexIndex());
        vertex1 = tr2.getVertex1();
        vertex2 = tr2.getVertex2();
        vertex3 = tr2.getVertex3();

        normal = tr2.getNormal();
        right = tr2.getRight();
        up = normal.getCrossProductWith(right);

        baseLength = vertex2.getThisMinus(vertex1).getLength();
        baseLengthLeftHalf = vertex3.getThisMinus(vertex1).getDotProductWith(right);
        baseLengthRightHalf = baseLength - baseLengthLeftHalf;
        height = vertex3.getThisMinus(vertex1).getDotProductWith(up);
        position = vertex1.getSumWith(right.getMultipliedBy(baseLengthLeftHalf));

        bufferObject = new TriangleBO(vertex1, vertex2, vertex3);
    }

    public Vector3fExt getVertex1() {
        return vertex1.getCopy();
    }

    public Vector3fExt getVertex2() {
        return vertex2.getCopy();
    }

    public Vector3fExt getVertex3() {
        return vertex3.getCopy();
    }

    public float getAverageSize() {
        return Math.max(baseLengthLeftHalf, Math.max(baseLengthRightHalf, height));
    }

    @Override
    public float getCollisionRadius() {
        return Math.max(height, Math.max(baseLengthLeftHalf, baseLengthRightHalf));
    }

    @Override
    public float getVolume() {
        return (baseLength * height * 0.5f * PLATE_THICKNESS);
    }

    public Vector3fExt getBasePoint() {
        return vertex1.getSumWith(right.getMultipliedBy(baseLengthLeftHalf));
    }

    public Triangle getCopy() {
        return new Triangle(rotationMatrix, velocity, force, color, material, colorMapTexIndex, normalMapTexIndex,
                vertex1.getCopy(), vertex2.getCopy(), vertex3.getCopy());
    }

    public Triangle getTransformed(Matrix4f modelMatrix, Matrix4f rotationMatrix) {
        final Triangle retTr = new Triangle(this);    // Deep copy constructor
        retTr.transform(modelMatrix, rotationMatrix);
        return retTr;
    }

    public void calculateMetrics() {
        baseLength = vertex2.getThisMinus(vertex1).getLength();
        baseLengthLeftHalf = vertex3.getThisMinus(vertex1).getDotProductWith(right);
        baseLengthRightHalf = baseLength - baseLengthLeftHalf;
        height = vertex3.getThisMinus(vertex1).getDotProductWith(up);
    }

    public float upComponent(float rightComponent) {
        if (rightComponent < 0 && rightComponent > -baseLengthLeftHalf) {    // On left side of triangle base
            return (height + rightComponent * height / baseLengthLeftHalf);    // + as rC is negative
        }
        if (rightComponent >= 0 && rightComponent < baseLengthRightHalf) {    // On right side of triangle base
            return (height + rightComponent * (-height / baseLengthRightHalf));
        }
        return 0;
    }

    /**
     * Returns true if 3D point p2 is above/below the Triangle, if the reference plain is
     * that of the Triangle. Returns false otherwise.
     *
     * @param p2 The 3D point in world space.
     * @return true if 3D point p2 is above/below the Triangle, if the reference plain is
     * that of the Triangle; false otherwise.
     */
    @Override
    public boolean isOnPlain(Vector3fExt p2) {    // p2 in world space
        final Vector3fExt basePoint = vertex1.getSumWith(right.getMultipliedBy(baseLengthLeftHalf));
        final Vector3fExt p2Projected = p2.getProjectedToSurface(basePoint, normal);
        final Vector3fExt diffP2FromTrPosInPlain = p2Projected.getThisMinus(basePoint);

        // Is it on the triangle base section?
        final float rightComp = diffP2FromTrPosInPlain.getDotProductWith(right);    // TODO model transformation
        final boolean isOnBase = (rightComp >= -baseLengthLeftHalf) && (rightComp <= baseLengthRightHalf);

        // Is it inside triangle area considering h?
        final float upComp = diffP2FromTrPosInPlain.getDotProductWith(up);
        final boolean isAboveBaseAndBelowH = (upComp >= 0) && (upComp <= upComponent(rightComp));
        return isOnBase && isAboveBaseAndBelowH;
    }

    @Override
    public void transformPlateAttribsBy(Matrix4f modelMatrix, Matrix4f rotationMatrix) {
        // TODO
        vertex1.multiplyBy(modelMatrix);
        vertex2.multiplyBy(modelMatrix);
        vertex3.multiplyBy(modelMatrix);
        calculateMetrics();
    }

    @Override
    public Vector3fExt[] getCommonEdgeWith(Rectangle face2) {
        return null;  // TODO
    }

    @Override
    public List<Vector3fExt> getShadowVertices(Vector3fExt lightParam, ShadowVolume.LIGHT_PARAM_TYPE lightType) {
        final List<Vector3fExt> shVxPos = new Vector<>();
        shVxPos.add(vertex1.getSumWith(position));
        shVxPos.add(vertex2.getSumWith(position));
        shVxPos.add(vertex3.getSumWith(position));
        return shVxPos;
    }

    @Override
    public Object3dBO getPrototype() {
        return bufferObject;
    }

    @Override
    public String toString() {
        return ("Triangle v1: " + vertex1.getSumWith(position) + ", v2: " + vertex2.getSumWith(position) + ", v3: "
                + vertex3.getSumWith(position));
    }

    @Override
    public Vector3fExt getVertex(int i) {
        return switch (i) {
            case 0 -> vertex1;
            case 1 -> vertex2;
            case 2 -> vertex3;
            default -> throw new IllegalArgumentException("Triangle vertex index out of bounds: " + i);
        };
    }
}

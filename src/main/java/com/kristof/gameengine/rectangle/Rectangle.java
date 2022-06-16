package com.kristof.gameengine.rectangle;

import com.kristof.gameengine.object3d.Object3dBO;
import com.kristof.gameengine.plate.Plate;
import com.kristof.gameengine.shadow.ShadowVolume;
import com.kristof.gameengine.util.ColorVector;
import com.kristof.gameengine.util.Material;
import com.kristof.gameengine.util.Matrix4f;
import com.kristof.gameengine.util.Vector3fExt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Vector;

import static com.kristof.gameengine.shadow.ShadowVolume.LIGHT_PARAM_TYPE.LIGHT_DIRECTION;

public class Rectangle extends Plate {
    private static final Logger LOGGER = LogManager.getLogger(Rectangle.class);

    private final float aSize;
    private final float bSize;
    private RectangleBO glBufferObject;

    /*
     * Constructors
     *
     * Vertices given in world space, then transformed into Rectangle position space
     */

    public Rectangle(Vector3fExt position, float polarAngle, float azimuthAngle, float rollAngle,
                     Vector3fExt velocity, Vector3fExt force, ColorVector color, Material material,
                     int colorMapTexIndex, int normalMapTexIndex, float aSide, float bSide, boolean isGhost) {
        super(position, polarAngle, azimuthAngle, rollAngle, Vector3fExt.UNIT_VECTOR, velocity, force, color, material,
                colorMapTexIndex, normalMapTexIndex);
        // TODO scale
        aSize = aSide;
        bSize = bSide;
        calculateModelMatrix();
        normal.multiplyBy(rotationMatrix);
        right.multiplyBy(rotationMatrix);
        up = Vector3fExt.crossProduct(normal, right);
        if (!isGhost) glBufferObject = new RectangleBO(aSide, bSide);
    }

    public Rectangle(Vector3fExt position, Matrix4f rotationMatrix, Vector3fExt velocity, Vector3fExt force,
                     ColorVector color, Material material, int colorMapTexIndex, int normalMapTexIndex, float aSide,
                     float bSide, boolean isGhost) {
        super(position, rotationMatrix, Vector3fExt.UNIT_VECTOR, velocity, force, color, material, colorMapTexIndex,
                normalMapTexIndex);
        // TODO scale
        aSize = aSide;
        bSize = bSide;
        calculateModelMatrix();
        normal.multiplyBy(this.rotationMatrix);
        right.multiplyBy(this.rotationMatrix);
        up = Vector3fExt.crossProduct(normal, right);
        if (!isGhost) glBufferObject = new RectangleBO(aSide, bSide);
    }

    public Rectangle(Rectangle rect2, boolean isGhost) {    // Deep copy ctor
        super(rect2.getPosition(), rect2.getRotationMatrix(), rect2.getScale(), rect2.getVelocity(), rect2.getForce(),
                rect2.getColor(), rect2.getMaterial(), rect2.getColorMapTexIndex(), rect2.getNormalMapTexIndex());
        aSize = rect2.getASize();
        bSize = rect2.getBSize();
        normal = rect2.getNormal();
        right = rect2.getRight();
        up = rect2.getUp();
        id = rect2.getId();
        neighborIDs = new int[4];
        int[] rect2NIDs = rect2.getNeighborIDs();
        System.arraycopy(rect2NIDs, 0, neighborIDs, 0, 4);
        if (!isGhost) glBufferObject = new RectangleBO(aSize, aSize);
    }

    public float getASize() {
        return aSize;
    }

    public float getBSize() {
        return bSize;
    }

    @Override
    protected Object3dBO getGlBufferObject() {
        return glBufferObject;
    }

    @Override
    public float getCollisionRadius() {
        return (float) Math.max(aSize, bSize);
    }

    @Override
    public float getVolume() {
        return (aSize * bSize * PLATE_THICKNESS);
    }

    public Rectangle getCopy() {
        return new Rectangle(position, rotationMatrix, velocity, force, color, material,
                colorMapTexIndex, normalMapTexIndex, aSize, bSize, true);
    }

    public Vector3fExt getBasePoint() {
        return position;
    } // TODO

    public Rectangle getTransformed(Matrix4f modelMatrix, Matrix4f rotationMatrix) {
        final Rectangle retRect = new Rectangle(this, true);
        retRect.transform(modelMatrix, rotationMatrix);
        return retRect;
    }

    @Override
    public Vector3fExt getVertex(int i) {
        float ap2 = 0.5f * aSize;
        float bp2 = 0.5f * bSize;
        return switch (i) {
            case 0 -> position.getSumWith(right.getMultipliedBy(-ap2).getSumWith(up.getMultipliedBy(-bp2)));
            case 1 -> position.getSumWith(right.getMultipliedBy(ap2).getSumWith(up.getMultipliedBy(-bp2)));
            case 2 -> position.getSumWith(right.getMultipliedBy(ap2).getSumWith(up.getMultipliedBy(bp2)));
            case 3 -> position.getSumWith(right.getMultipliedBy(-ap2).getSumWith(up.getMultipliedBy(bp2)));
            default -> Vector3fExt.NULL_VECTOR.getCopy();
        };
    }

    public float getAverageSize() {
        return Math.max(aSize, bSize);
    }

    /**
     * Returns true if 3D point p2 is above/below the Rectangle, if the reference plain is
     * that of the Rectangle. Returns false otherwise.
     *
     * @param p2 The 3D point in world space.
     * @return true if 3D point p2 is above/below the Rectangle, if the reference plain is
     * that of the Rectangle; false otherwise.
     */
    @Override
    public boolean isOnPlain(Vector3fExt p2) {    // p2 in world space
        Vector3fExt diffP2RectPos = p2.getThisMinus(position);
        float ap2 = 0.5f * aSize;
        float bp2 = 0.5f * bSize;
        float rightComp = diffP2RectPos.getDotProductWith(right);
        boolean isOnASide = (rightComp >= -ap2) && (rightComp <= ap2);
        float upComp = diffP2RectPos.getDotProductWith(up);    // TODO model transformation
        boolean isOnBSide = (upComp >= -bp2) && (upComp <= bp2);
        return isOnASide && isOnBSide;
    }

    @Override
    public void transformPlateAttribsBy(Matrix4f modelMatrix, Matrix4f rotationMatrix) {
        // TODO
        // Position, vectors done in Plate
    }

    @Override
    public Vector3fExt[] getCommonEdgeWith(Rectangle face2) {
        final Vector3fExt[] commonEdge = new Vector3fExt[]{Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR};
        int currentIndex = 0;
        final List<Vector3fExt> vertexPositions = getShadowVertices(Vector3fExt.NULL_VECTOR, LIGHT_DIRECTION);
        for (final Vector3fExt vertexPosition : vertexPositions) {
            for (int j = 0; j < 4; j++) {    // 4 vertices in a Rectangle
                if (vertexPosition.equals(face2.getVertex(j))) {
                    commonEdge[currentIndex] = vertexPosition.getCopy();
                    currentIndex++;
                }
                if (currentIndex == 2) return commonEdge;
            }
        }
        LOGGER.debug("No common edge found (" + commonEdge[0] + ", " + commonEdge[1] + ")");
        return commonEdge;
    }

    @Override
    public List<Vector3fExt> getShadowVertices(Vector3fExt lightParam, ShadowVolume.LIGHT_PARAM_TYPE lightType) {
        List<Vector3fExt> shVxPos = new Vector<>();
        float ap2 = 0.5f * aSize;
        float bp2 = 0.5f * bSize;
        shVxPos.add(Vector3fExt.add(right.getMultipliedBy(-ap2), up.getMultipliedBy(-bp2)).getSumWith(position));
        shVxPos.add(Vector3fExt.add(right.getMultipliedBy(ap2), up.getMultipliedBy(-bp2)).getSumWith(position));
        shVxPos.add(Vector3fExt.add(right.getMultipliedBy(ap2), up.getMultipliedBy(bp2)).getSumWith(position));
        shVxPos.add(Vector3fExt.add(right.getMultipliedBy(-ap2), up.getMultipliedBy(bp2)).getSumWith(position));
        return shVxPos;
    }

    @Override
    public String toString() {
        return ("Rectangle pos: " + position + ", nor: " + normal + ", rit: " + right + ", a: " + aSize + ", b: "
                + bSize);
    }
}

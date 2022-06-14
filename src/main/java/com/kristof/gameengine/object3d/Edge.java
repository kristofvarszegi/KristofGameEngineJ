package com.kristof.gameengine.object3d;

import com.kristof.gameengine.util.Matrix4f;
import com.kristof.gameengine.util.Vector3fExt;

public class Edge {
    private final Vector3fExt vertex1;
    private final Vector3fExt vertex2;
    private final Vector3fExt faceNormal1;
    private final Vector3fExt faceNormal2;

    public Edge() {
        vertex1 = new Vector3fExt();
        vertex2 = new Vector3fExt();
        faceNormal1 = new Vector3fExt();
        faceNormal2 = new Vector3fExt();
    }

    public Edge(Edge edge2) {
        vertex1 = edge2.getVertex(0);
        vertex2 = edge2.getVertex(1);
        faceNormal1 = edge2.getNormal(0);
        faceNormal2 = edge2.getNormal(1);
    }

    public Edge(Vector3fExt v1, Vector3fExt v2, Vector3fExt n1, Vector3fExt n2) {
        vertex1 = v1.getCopy();
        vertex2 = v2.getCopy();
        faceNormal1 = n1.getCopy();
        faceNormal2 = n2.getCopy();
    }

    public Edge(Vector3fExt v1, Vector3fExt v2, Vector3fExt n1) {
        vertex1 = v1.getCopy();
        vertex2 = v2.getCopy();
        faceNormal1 = n1.getCopy();
        faceNormal2 = new Vector3fExt();
    }

    @Override
    public String toString() {
        return ("v1 = " + vertex1.getRounded(2) + ", v2 = " + vertex2.getRounded(2) + ", n1 = "
                + faceNormal1.getRounded(2) + ", n2= " + faceNormal2.getRounded(2));
    }

    public Vector3fExt getVertex(int index) {
        return switch (index) {
            case 0 -> vertex1.getCopy();
            case 1 -> vertex2.getCopy();
            default -> new Vector3fExt();
        };
    }

    public Vector3fExt getNormal(int index) {
        return switch (index) {
            case 0 -> faceNormal1.getCopy();
            case 1 -> faceNormal2.getCopy();
            default -> new Vector3fExt();
        };
    }

    public boolean hasSameVertices(Edge edge2) {
        return (vertex1.getThisMinus(edge2.getVertex(0)).getLengthSquare() < Vector3fExt.EQUALITY_RANGE_SQ
                || vertex1.getThisMinus(edge2.getVertex(1)).getLengthSquare() < Vector3fExt.EQUALITY_RANGE_SQ)
                && (vertex2.getThisMinus(edge2.getVertex(0)).getLengthSquare() < Vector3fExt.EQUALITY_RANGE_SQ
                || vertex2.getThisMinus(edge2.getVertex(1)).getLengthSquare() < Vector3fExt.EQUALITY_RANGE_SQ);
    }

    public void mergeNormals(Edge edge2) {
        faceNormal2.reset();
        faceNormal2.add(edge2.getNormal(0).getCopy());
    }

    public Edge getScaled(float scale) {
        return (new Edge(vertex1.getMultipliedBy(scale), vertex2.getMultipliedBy(scale), faceNormal1.getCopy(),
                faceNormal2.getCopy()));
    }

    public Edge getTransformed(Matrix4f rotMx) {
        return (new Edge(vertex1.getMultipliedBy(rotMx), vertex2.getMultipliedBy(rotMx), faceNormal1.getCopy(),
                faceNormal2.getCopy()));
    }

    public void transform(Matrix4f modelMx, Matrix4f rotationMx) {
        vertex1.multiplyBy(modelMx);
        vertex2.multiplyBy(modelMx);
        faceNormal1.multiplyBy(rotationMx);
        faceNormal2.multiplyBy(rotationMx);
    }

    public boolean isShadowEdge(Vector3fExt lightPos) {
        final Vector3fExt lToEVr = vertex1.getThisMinus(lightPos);
        if (lToEVr.getDotProductWith(faceNormal1) * lToEVr.getDotProductWith(faceNormal2) < 0) {    // If different sign
            return true;
        }
        if (faceNormal2.getLengthSquare() < Vector3fExt.EQUALITY_RANGE_SQ) {    // For plates
            return true;
        }
        return false;
    }
}

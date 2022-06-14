package com.kristof.gameengine.object3d;

import java.util.List;
import java.util.Vector;

public class OBJFaceIndexData {
    private final short[] vertexIndices;
    private final short[] normalIndices;
    private final short[] texCoordIndices;

    public OBJFaceIndexData() {
        vertexIndices = new short[3];
        vertexIndices[0] = -1;
        vertexIndices[1] = -1;
        vertexIndices[2] = -1;
        normalIndices = new short[3];
        texCoordIndices = new short[3];
    }

    public OBJFaceIndexData(short vertexIndex1, short vertexIndex2, short vertexIndex3, short normalIndex) {
        vertexIndices = new short[3];
        vertexIndices[0] = vertexIndex1;
        vertexIndices[1] = vertexIndex2;
        vertexIndices[2] = vertexIndex3;
        normalIndices = new short[]{normalIndex, normalIndex, normalIndex};
        texCoordIndices = new short[3];
    }

    public OBJFaceIndexData(short vertexIndex1, short vertexIndex2, short vertexIndex3, short normalIndex1,
                            short normalIndex2, short normalIndex3) {
        vertexIndices = new short[3];
        vertexIndices[0] = vertexIndex1;
        vertexIndices[1] = vertexIndex2;
        vertexIndices[2] = vertexIndex3;

        normalIndices = new short[3];
        normalIndices[0] = normalIndex1;
        normalIndices[1] = normalIndex2;
        normalIndices[2] = normalIndex3;

        texCoordIndices = new short[3];
    }

    public OBJFaceIndexData(short vertexIndex1, short vertexIndex2, short vertexIndex3, short normalIndex1,
                            short normalIndex2, short normalIndex3, short texCoordIndex1, short texCoordIndex2,
                            short texCoordIndex3) {
        vertexIndices = new short[3];
        vertexIndices[0] = vertexIndex1;
        vertexIndices[1] = vertexIndex2;
        vertexIndices[2] = vertexIndex3;

        normalIndices = new short[3];
        normalIndices[0] = normalIndex1;
        normalIndices[1] = normalIndex2;
        normalIndices[2] = normalIndex3;

        texCoordIndices = new short[3];
        texCoordIndices[0] = texCoordIndex1;
        texCoordIndices[1] = texCoordIndex2;
        texCoordIndices[2] = texCoordIndex3;
    }

    public short getNormalIndex(int index) {
        return (index >= 0 && index < 3) ? normalIndices[index] : -1;
    }

    public boolean contains(short vertexIndex) {
        for (final short index : vertexIndices) {
            if (index == vertexIndex) return true;
        }
        return false;
    }

    public List<Short> getVertexIndices() {
        List<Short> vertexIndices = new Vector<>();
        vertexIndices.add(this.vertexIndices[0]);
        vertexIndices.add(this.vertexIndices[1]);
        vertexIndices.add(this.vertexIndices[2]);
        return vertexIndices;
    }

    public List<Short> getNormalIndices() {
        List<Short> normalIndices = new Vector<>();
        normalIndices.add(this.normalIndices[0]);
        normalIndices.add(this.normalIndices[1]);
        normalIndices.add(this.normalIndices[2]);
        return normalIndices;
    }

    public List<Short> getTexCoordIndices() {
        List<Short> texCoordIndices = new Vector<>();
        texCoordIndices.add(this.texCoordIndices[0]);
        texCoordIndices.add(this.texCoordIndices[1]);
        texCoordIndices.add(this.texCoordIndices[2]);
        return texCoordIndices;
    }

    public short getNormalOfVertex(int vertexIndex) {
        for (int i = 0; i < vertexIndices.length; i++) {
            if (vertexIndices[i] == vertexIndex) return normalIndices[i];
        }
        return (short) -1;
    }

    public boolean isNeighbor(OBJFaceIndexData face2) {
        int numCommonVertices = 0;
        for (final short vertexIndex : vertexIndices) {
            if (face2.hasVertexIndex(vertexIndex)) numCommonVertices++;
        }
        return numCommonVertices == 2;
    }

    public boolean hasVertexIndex(short vIndex) {
        for (final short vertexIndex : vertexIndices) {
            if (vertexIndex == vIndex) return true;
        }
        return false;
    }
}

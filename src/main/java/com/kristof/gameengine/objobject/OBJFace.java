package com.kristof.gameengine.objobject;

import java.util.List;
import java.util.Vector;

public class OBJFace {
    private final short[] vertexIndices;
    private final short[] normalIndices;
    private final short[] texCoordIndices;

    public OBJFace() {
        vertexIndices = new short[3];
        vertexIndices[0] = -1;
        vertexIndices[1] = -1;
        vertexIndices[2] = -1;
        normalIndices = new short[3];
        texCoordIndices = new short[3];
    }

    public OBJFace(short vertexIndex1, short vertexIndex2, short vertexIndex3, short normalIndex) {
        vertexIndices = new short[3];
        vertexIndices[0] = vertexIndex1;
        vertexIndices[1] = vertexIndex2;
        vertexIndices[2] = vertexIndex3;
        normalIndices = new short[]{normalIndex, normalIndex, normalIndex};
        texCoordIndices = new short[3];
    }

    public OBJFace(short vertexIndex1, short vertexIndex2, short vertexIndex3, short normalIndex1, short normalIndex2,
                   short normalIndex3) {
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

    public OBJFace(short vertexIndex1, short vertexIndex2, short vertexIndex3, short normalIndex1, short normalIndex2,
                   short normalIndex3, short texCoordIndex1, short texCoordIndex2, short texCoordIndex3) {
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
        List<Short> normalIndexList = new Vector<>();
        normalIndexList.add(normalIndices[0]);
        normalIndexList.add(normalIndices[1]);
        normalIndexList.add(normalIndices[2]);
        return normalIndexList;
    }

    public List<Short> getTexCoordIndices() {
        List<Short> texCoordIndexList = new Vector<>();
        texCoordIndexList.add(texCoordIndices[0]);
        texCoordIndexList.add(texCoordIndices[1]);
        texCoordIndexList.add(texCoordIndices[2]);
        return texCoordIndexList;
    }

    public short getNormalOfVertex(int vertexIndex) {
        for (int i = 0; i < vertexIndices.length; i++) {
            if (vertexIndices[i] == vertexIndex) return normalIndices[i];
        }
        return (short) -1;
    }

    public boolean isNeighbor(OBJFace face2) {
        int commonVertexCount = 0;
        for (final short vertexIndex : vertexIndices) {
            if (face2.hasVertexIndex(vertexIndex)) commonVertexCount++;
        }
        return commonVertexCount == 2;
    }

    public boolean hasVertexIndex(short vIndex) {
        for (final short vertexIndex : vertexIndices) {
            if (vertexIndex == vIndex) return true;
        }
        return false;
    }
}

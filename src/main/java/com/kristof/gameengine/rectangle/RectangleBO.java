package com.kristof.gameengine.rectangle;

import com.kristof.gameengine.util.Vector3fExt;
import com.kristof.gameengine.object3d.ByteObject3dBO;

import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Vector;

public class RectangleBO extends ByteObject3dBO {
    private static final int VERTEX_COUNT = 4;

    private float aSize;
    private float bSize;

    public RectangleBO(float aSide, float bSide) {
        super(List.of(aSide, bSide));
    }

    @Override
    protected void saveGeometryParams(List<Object> geometryParams) {
        this.aSize = (float) geometryParams.get(0);
        this.bSize = (float) geometryParams.get(1);
    }

    @Override
    protected void fillVertexAttribLists() {
        float ap2 = 0.5f * aSize;
        float bp2 = 0.5f * bSize;

        positions.add(new Vector3fExt(-ap2, 0, bp2));
        positions.add(new Vector3fExt(ap2, 0, bp2));
        positions.add(new Vector3fExt(ap2, 0, -bp2));
        positions.add(new Vector3fExt(-ap2, 0, -bp2));

        normals.add(Vector3fExt.Y_UNIT_VECTOR);
        normals.add(Vector3fExt.Y_UNIT_VECTOR);
        normals.add(Vector3fExt.Y_UNIT_VECTOR);
        normals.add(Vector3fExt.Y_UNIT_VECTOR);

        tangents.add(Vector3fExt.X_UNIT_VECTOR);
        tangents.add(Vector3fExt.X_UNIT_VECTOR);
        tangents.add(Vector3fExt.X_UNIT_VECTOR);
        tangents.add(Vector3fExt.X_UNIT_VECTOR);

        texCoords.add(new float[]{0f, 1f});
        texCoords.add(new float[]{1f, 1f});
        texCoords.add(new float[]{1f, 0f});
        texCoords.add(new float[]{0f, 0f});
    }

    protected void fillIndices() {
        indices.add((byte) 0);
        indices.add((byte) 3);
        indices.add((byte) 1);
        indices.add((byte) 2);
    }

    @Override
    protected void drawElements() {
        GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, VERTEX_COUNT, GL11.GL_UNSIGNED_BYTE,0);
    }
}

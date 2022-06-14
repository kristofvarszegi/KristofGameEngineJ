package com.kristof.gameengine.triangle;

import com.kristof.gameengine.util.Vector3fExt;
import com.kristof.gameengine.object3d.ByteObject3dBO;

import org.lwjgl.opengl.GL11;

import java.util.List;

public class TriangleBO extends ByteObject3dBO {
    private static final int VERTEX_COUNT = 3;

    // Vertices in CCW order. The 1st and the 2nd holds the biggest side of the triangle.
    private Vector3fExt triangleVertex1;
    private Vector3fExt triangleVertex2;
    private Vector3fExt triangleVertex3;

    public TriangleBO(Vector3fExt vertex1, Vector3fExt vertex2, Vector3fExt vertex3) {
        super(List.of(vertex1, vertex2, vertex3));
    }

    @Override
    protected void saveGeometryParams(List<Object> geometryParams) {
        triangleVertex1 = (Vector3fExt) geometryParams.get(0);
        triangleVertex2 = (Vector3fExt) geometryParams.get(1);
        triangleVertex3 = (Vector3fExt) geometryParams.get(2);
    }

    @Override
    protected void fillVertexAttribLists() {
        positions.add(triangleVertex1);
        positions.add(triangleVertex2);
        positions.add(triangleVertex3);

        Vector3fExt normal = Vector3fExt.crossProduct(triangleVertex2.getThisMinus(triangleVertex1),
                triangleVertex3.getThisMinus(triangleVertex1)).getNormalized();
        Vector3fExt right = triangleVertex2.getThisMinus(triangleVertex1).getNormalized();
        Vector3fExt up = normal.getCrossProductWith(right);

        normals.add(normal);
        normals.add(normal);
        normals.add(normal);

        tangents.add(right);
        tangents.add(right);
        tangents.add(right);

        float baseLength = triangleVertex2.getThisMinus(triangleVertex1).getLength();
        float baseLengthLeftHalf = triangleVertex3.getThisMinus(triangleVertex1).getDotProductWith(right);
        float height = triangleVertex3.getThisMinus(triangleVertex1).getDotProductWith(up);

        texCoords.add(new float[]{0f, 1f});
        texCoords.add(new float[]{1f, 1f});
        texCoords.add(new float[]{baseLengthLeftHalf / baseLength, Math.max(1f - height / baseLength, 0f)});
    }

    protected void fillIndices() {
        indices.add((byte) 0);
        indices.add((byte) 1);
        indices.add((byte) 2);
    }

    @Override
    protected void drawElements() {
        GL11.glDrawElements(GL11.GL_TRIANGLES, VERTEX_COUNT, GL11.GL_UNSIGNED_BYTE, 0);
    }
}

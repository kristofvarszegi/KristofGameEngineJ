package com.kristof.gameengine.polyface;

import com.kristof.gameengine.util.Vector3fExt;
import com.kristof.gameengine.object3d.ByteObject3dBO;

import org.lwjgl.opengl.GL11;

import java.util.List;

public class PolyFaceBO extends ByteObject3dBO {
    public PolyFaceBO(List<Vector3fExt> vertexPositions) {
        super(vertexPositions);
    }

    @Override
    protected void fillVertexAttribLists() {
        // Positions already done in ByteObject3dBO ctor
        Vector3fExt normal = Vector3fExt.crossProduct(
                positions.get(1).getThisMinus(positions.get(0)),
                positions.get(2).getThisMinus(positions.get(0))).getNormalized();
        Vector3fExt right = positions.get(1).getThisMinus(positions.get(0)).getNormalized();

        for (int i = 0; i < positions.size(); i++) {
            normals.add(normal);
            tangents.add(right);
            texCoords.add(new float[]{0.5f, 0.5f});    // TODO
        }
    }

    @Override
    protected void fillIndices() {
        for (int i = 0; i < positions.size(); i++) {
            indices.add((byte) i);
        }
        indices.add((byte) 1);
    }

    @Override
    protected void drawElements() {
        GL11.glDrawElements(GL11.GL_TRIANGLE_FAN, indices.size(), GL11.GL_UNSIGNED_BYTE, 0);
    }
}

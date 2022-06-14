package com.kristof.gameengine.screen;

import com.kristof.gameengine.object3d.Object3dBO;
import com.kristof.gameengine.util.Vector3fExt;
import com.kristof.gameengine.object3d.ByteObject3dBO;

import org.lwjgl.opengl.GL11;

public class ScreenQuadBO extends ByteObject3dBO {
    private static volatile ScreenQuadBO instance;

    public static Object3dBO getInstance() {
        if (instance == null) {
            synchronized (ScreenQuadBO.class) {
                if (instance == null) {
                    instance = new ScreenQuadBO();
                }
            }
        }
        return instance;
    }

    @Override
    protected void fillVertexAttribLists() {
        positions.add(new Vector3fExt(-1f, -1f, -1f));    // left-bottom vertex
        positions.add(new Vector3fExt(1f, -1f, -1f));    // right-bottom vertex
        positions.add(new Vector3fExt(1f, 1f, -1f));    // right-top vertex
        positions.add(new Vector3fExt(-1f, 1f, -1f));    // left-top vertex

        normals.add(new Vector3fExt(0, 0, 1f));
        normals.add(new Vector3fExt(0, 0, 1f));
        normals.add(new Vector3fExt(0, 0, 1f));
        normals.add(new Vector3fExt(0, 0, 1f));

        tangents.add(new Vector3fExt(1f, 0, 0));
        tangents.add(new Vector3fExt(1f, 0, 0));
        tangents.add(new Vector3fExt(1f, 0, 0));
        tangents.add(new Vector3fExt(1f, 0, 0));

        texCoords.add(new float[]{0, 0});
        texCoords.add(new float[]{1, 0});
        texCoords.add(new float[]{1, 1});
        texCoords.add(new float[]{0, 1});
    }

    protected void fillIndices() {
        indices.add((byte) 0);
        indices.add((byte) 1);
        indices.add((byte) 2);
        indices.add((byte) 0);
        indices.add((byte) 2);
        indices.add((byte) 3);
    }

    @Override
    protected void drawElements() {
        GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_BYTE, 0);
    }
}

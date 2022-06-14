package com.kristof.gameengine.skybox;

import com.kristof.gameengine.object3d.Object3dBO;
import com.kristof.gameengine.util.Vector3fExt;
import com.kristof.gameengine.object3d.ByteObject3dBO;

import org.lwjgl.opengl.GL11;

import static com.kristof.gameengine.engine.EngineMiscConstants.INFINITY;

public class SkyBoxBO extends ByteObject3dBO {
    private static final float SIDE = 0.9f * INFINITY;
    private static final float TEX_EPSILON = 0.0008f;

    private static volatile SkyBoxBO instance;

    public static Object3dBO getInstance() {
        if (instance == null) {
            synchronized (SkyBoxBO.class) {
                if (instance == null) {
                    instance = new SkyBoxBO();
                }
            }
        }
        return instance;
    }

    @Override
    protected void fillVertexAttribLists() {   // TODO with cubemap
        float sp2 = 0.5f * SIDE;

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, sp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, sp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, sp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -sp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -sp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -sp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -sp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, sp2)));


        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, sp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, sp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, sp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -sp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -sp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -sp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -sp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, sp2)));

        // Top & bottom
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -sp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -sp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, sp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, sp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, sp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, sp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -sp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, sp2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -sp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -sp2)));

        normals.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());
        normals.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getReverse());

        normals.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        normals.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.Z_UNIT_VECTOR);
        tangents.add(Vector3fExt.Z_UNIT_VECTOR);

        normals.add(Vector3fExt.Z_UNIT_VECTOR);
        normals.add(Vector3fExt.Z_UNIT_VECTOR);
        tangents.add(Vector3fExt.X_UNIT_VECTOR);
        tangents.add(Vector3fExt.X_UNIT_VECTOR);

        normals.add(Vector3fExt.X_UNIT_VECTOR);
        normals.add(Vector3fExt.X_UNIT_VECTOR);
        tangents.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());

        normals.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());
        normals.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getReverse());

        normals.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        normals.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.Z_UNIT_VECTOR);
        tangents.add(Vector3fExt.Z_UNIT_VECTOR);

        normals.add(Vector3fExt.Z_UNIT_VECTOR);
        normals.add(Vector3fExt.Z_UNIT_VECTOR);
        tangents.add(Vector3fExt.X_UNIT_VECTOR);
        tangents.add(Vector3fExt.X_UNIT_VECTOR);

        normals.add(Vector3fExt.X_UNIT_VECTOR);
        normals.add(Vector3fExt.X_UNIT_VECTOR);
        tangents.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());

        normals.add(Vector3fExt.Y_UNIT_VECTOR.getReverse());
        normals.add(Vector3fExt.Y_UNIT_VECTOR.getCopy());
        normals.add(Vector3fExt.Y_UNIT_VECTOR.getReverse());
        normals.add(Vector3fExt.Y_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getCopy());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getReverse());

        normals.add(Vector3fExt.Y_UNIT_VECTOR);
        normals.add(Vector3fExt.Y_UNIT_VECTOR);
        normals.add(Vector3fExt.Y_UNIT_VECTOR);
        normals.add(Vector3fExt.Y_UNIT_VECTOR);
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getReverse());

        // Top rim
        texCoords.add(new float[]{0.5f, 1f / 3f + TEX_EPSILON});

        texCoords.add(new float[]{0.25f, 1f / 3f + TEX_EPSILON});
        texCoords.add(new float[]{0.25f, 1f / 3f + TEX_EPSILON});

        texCoords.add(new float[]{0, 1f / 3f + TEX_EPSILON});
        texCoords.add(new float[]{1f, 1f / 3f + TEX_EPSILON});

        texCoords.add(new float[]{0.75f, 1f / 3f + TEX_EPSILON});
        texCoords.add(new float[]{0.75f, 1f / 3f + TEX_EPSILON});

        texCoords.add(new float[]{0.5f, 1f / 3f + TEX_EPSILON});

        // Bottom rim
        texCoords.add(new float[]{0.5f, 2f / 3f - TEX_EPSILON});

        texCoords.add(new float[]{0.25f, 2f / 3f - TEX_EPSILON});
        texCoords.add(new float[]{0.25f, 2f / 3f - TEX_EPSILON});

        texCoords.add(new float[]{0, 2f / 3f - TEX_EPSILON});
        texCoords.add(new float[]{1f, 2f / 3f - TEX_EPSILON});

        texCoords.add(new float[]{0.75f, 2f / 3f - TEX_EPSILON});
        texCoords.add(new float[]{0.75f, 2f / 3f - TEX_EPSILON});

        texCoords.add(new float[]{0.5f, 2f / 3f - TEX_EPSILON});

        // Top
        texCoords.add(new float[]{0.5f - TEX_EPSILON, TEX_EPSILON});
        texCoords.add(new float[]{0.25f + TEX_EPSILON, TEX_EPSILON});
        texCoords.add(new float[]{0.5f - TEX_EPSILON, 1f / 3f + TEX_EPSILON});
        texCoords.add(new float[]{0.25f + TEX_EPSILON, 1f / 3f + TEX_EPSILON});

        // Bottom
        texCoords.add(new float[]{0.5f - TEX_EPSILON, 2f / 3f - TEX_EPSILON});
        texCoords.add(new float[]{0.25f + TEX_EPSILON, 2f / 3f - TEX_EPSILON});
        texCoords.add(new float[]{0.5f - TEX_EPSILON, 1f - TEX_EPSILON});
        texCoords.add(new float[]{0.25f + TEX_EPSILON, 1f - TEX_EPSILON});
    }

    @Override
    protected void fillIndices() {
        indices.add((byte) 0);
        indices.add((byte) 8);
        indices.add((byte) 1);
        indices.add((byte) 9);
        indices.add((byte) 9);

        indices.add((byte) 2);
        indices.add((byte) 2);
        indices.add((byte) 10);
        indices.add((byte) 3);
        indices.add((byte) 11);
        indices.add((byte) 11);

        indices.add((byte) 4);
        indices.add((byte) 4);
        indices.add((byte) 12);
        indices.add((byte) 5);
        indices.add((byte) 13);
        indices.add((byte) 13);

        indices.add((byte) 6);
        indices.add((byte) 6);
        indices.add((byte) 14);
        indices.add((byte) 7);
        indices.add((byte) 15);
        indices.add((byte) 15);

        indices.add((byte) 16);
        indices.add((byte) 16);
        indices.add((byte) 18);
        indices.add((byte) 17);
        indices.add((byte) 19);
        indices.add((byte) 19);

        indices.add((byte) 20);
        indices.add((byte) 20);
        indices.add((byte) 22);
        indices.add((byte) 21);
        indices.add((byte) 23);
    }

    @Override
    protected void drawElements() {
        GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, indices.size(), GL11.GL_UNSIGNED_BYTE, 0);
    }
}
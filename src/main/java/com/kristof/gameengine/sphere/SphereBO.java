package com.kristof.gameengine.sphere;

import com.kristof.gameengine.cuboid.CuboidBO;
import com.kristof.gameengine.object3d.Object3dBO;
import com.kristof.gameengine.util.Vector3fExt;
import com.kristof.gameengine.object3d.ShortObject3dBO;

import com.kristof.gameengine.util.VertexConstants;
import org.lwjgl.opengl.GL11;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class SphereBO extends ShortObject3dBO {
    private static final float DEFAULT_RADIUS = 1f;
    private static final int DETAIL_LEVEL = 16;    // 50 is nice; 24 is the best compromise so far
    public static final int NUM_VERTICES_NS = DETAIL_LEVEL + 1;
    public static final int NUM_VERTICES_EW = DETAIL_LEVEL * 2;

    private static volatile SphereBO instance;

    public static Object3dBO getInstance() {
        if (instance == null) {
            synchronized (SphereBO.class) {
                if (instance == null) {
                    instance = new SphereBO();
                }
            }
        }
        return instance;
    }

    @Override
    public void fillVertexAttribLists() {
        float theta;
        float phi;
        Vector3fExt posTemp;

        // Top vertex
        posTemp = new Vector3fExt(0, DEFAULT_RADIUS, 0);
        positions.add(posTemp);
        texCoords.add(new float[]{0.5f, 0});
        tangents.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());

        for (int i = 1; i < (NUM_VERTICES_NS - 2 + 1); i++) {
            theta = (float) i * Vector3fExt.PI / ((float) NUM_VERTICES_NS - 2 + 1);
            for (int j = 0; j < NUM_VERTICES_EW; j++) {
                phi = (float) j * 2 * Vector3fExt.PI / ((float) NUM_VERTICES_EW - 1);
                posTemp = new Vector3fExt(
                        DEFAULT_RADIUS * (float) sin(theta) * (float) cos(phi),
                        DEFAULT_RADIUS * (float) cos(theta),
                        -DEFAULT_RADIUS * (float) sin(theta) * (float) sin(phi));
                positions.add(posTemp);
			    tangents.add(Vector3fExt.Y_UNIT_VECTOR.getCrossProductWith(posTemp));
                texCoords.add(new float[]{phi / (2 * Vector3fExt.PI), theta / Vector3fExt.PI});
            }
        }

        // Bottom vertex
        posTemp = new Vector3fExt(0, -DEFAULT_RADIUS, 0);
        positions.add(posTemp);
        texCoords.add(new float[]{0.5f, 1f});
        tangents.add(Vector3fExt.Z_UNIT_VECTOR);
        for (final Vector3fExt position : positions) {
            normals.add(position.getNormalized());
        }
    }

    protected void fillIndices() {
        // Top cover - TRIANGLE_FAN
        for (int i = 0; i < (NUM_VERTICES_EW + 1); i++) {
            indices.add((short) i);
        }

        // Top cover - vertex for last triangle
        indices.add((short) 1);

        // Mantle bands - TRIANGLE_STRIP
        for (int i = 0; i < (NUM_VERTICES_NS - 3); i++) {
            // Repeat first vertex for degenerate triangles
            if (i != 0) indices.add((short) (i * NUM_VERTICES_EW + 1));

            // Strips
            for (int j = 0; j < NUM_VERTICES_EW; j++) {
                indices.add((short) (i * NUM_VERTICES_EW + 1 + j));
                indices.add((short) ((i + 1) * NUM_VERTICES_EW + 1 + j));
            }

            // Last triangles' vertices
            indices.add((short) (i * NUM_VERTICES_EW + 1));
            indices.add((short) ((i + 1) * NUM_VERTICES_EW + 1));

            // Repeat last vertex for degenerate triangles
            if (i != NUM_VERTICES_NS - 4) indices.add((short) ((i + 1) * NUM_VERTICES_EW + 1));
        }

        // Bottom cover - TRIANGLE_FAN
        for (int i = 0; i < (NUM_VERTICES_EW + 1); i++) {
            indices.add((short) (vertexCount - (i + 1)));
        }

        // Last triangle vertex
        indices.add((short) (vertexCount - 2));
    }

    @Override
    protected void drawElements() {
        GL11.glDrawElements(GL11.GL_TRIANGLE_FAN,
                (NUM_VERTICES_EW + 2),
                GL11.GL_UNSIGNED_SHORT,
                0);
        GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP,
                ((NUM_VERTICES_EW + 1) * 2 * (NUM_VERTICES_NS - 3) + (NUM_VERTICES_NS - 4) * 2),    // vxs in a strip * #strips + degenerate vxs
                GL11.GL_UNSIGNED_SHORT,
                (NUM_VERTICES_EW + 2) * VertexConstants.BYTES_PER_SHORT);
        GL11.glDrawElements(GL11.GL_TRIANGLE_FAN,
                (NUM_VERTICES_EW + 2),
                GL11.GL_UNSIGNED_SHORT,
                ((NUM_VERTICES_EW + 2) + (NUM_VERTICES_EW + 1) * 2 * (NUM_VERTICES_NS - 3) + (NUM_VERTICES_NS - 4) * 2) * VertexConstants.BYTES_PER_SHORT);
    }
}

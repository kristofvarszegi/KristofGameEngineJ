package com.kristof.gameengine.cuboid;

import com.kristof.gameengine.util.Vector3fExt;
import com.kristof.gameengine.object3d.ByteObject3dBO;

import org.lwjgl.opengl.GL11;

public class CuboidBO extends ByteObject3dBO {
	private static final float X_SIDE = 1;
    private static final float Y_SIDE = 1;
    private static final float Z_SIDE = 1;

    public CuboidBO() {
    }

    @Override
    protected void fillVertexAttribLists() {
        float ap2 = 0.5f * X_SIDE;
        float bp2 = 0.5f * Y_SIDE;
        float cp2 = 0.5f * Z_SIDE;

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, cp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, cp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, cp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -cp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -cp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -cp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -cp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, cp2)));


        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, cp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, cp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, cp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -cp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -cp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -cp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -cp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, cp2)));


        // top & bottom
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -cp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -cp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, cp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, cp2)));

        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, cp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, cp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, -ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -cp2)));
        positions.add(Vector3fExt.add(Vector3fExt.multiply(Vector3fExt.X_UNIT_VECTOR, ap2), Vector3fExt.multiply(Vector3fExt.Y_UNIT_VECTOR, -bp2), Vector3fExt.multiply(Vector3fExt.Z_UNIT_VECTOR, -cp2)));

        normals.add(Vector3fExt.Z_UNIT_VECTOR.getCopy());
        normals.add(Vector3fExt.Z_UNIT_VECTOR.getCopy());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getCopy());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getCopy());

        normals.add(Vector3fExt.X_UNIT_VECTOR.getCopy());
        normals.add(Vector3fExt.X_UNIT_VECTOR.getCopy());
        tangents.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());

        normals.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());
        normals.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getReverse());

        normals.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        normals.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.Z_UNIT_VECTOR.getCopy());
        tangents.add(Vector3fExt.Z_UNIT_VECTOR.getCopy());


        normals.add(Vector3fExt.Z_UNIT_VECTOR.getCopy());
        normals.add(Vector3fExt.Z_UNIT_VECTOR.getCopy());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getCopy());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getCopy());

        normals.add(Vector3fExt.X_UNIT_VECTOR.getCopy());
        normals.add(Vector3fExt.X_UNIT_VECTOR.getCopy());
        tangents.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());

        normals.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());
        normals.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getReverse());

        normals.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        normals.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.Z_UNIT_VECTOR.getCopy());
        tangents.add(Vector3fExt.Z_UNIT_VECTOR.getCopy());

        normals.add(Vector3fExt.Y_UNIT_VECTOR.getCopy());
        normals.add(Vector3fExt.Y_UNIT_VECTOR.getCopy());
        normals.add(Vector3fExt.Y_UNIT_VECTOR.getCopy());
        normals.add(Vector3fExt.Y_UNIT_VECTOR.getCopy());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getCopy());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getCopy());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getCopy());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getCopy());

        normals.add(Vector3fExt.Y_UNIT_VECTOR.getReverse());
        normals.add(Vector3fExt.Y_UNIT_VECTOR.getReverse());
        normals.add(Vector3fExt.Y_UNIT_VECTOR.getReverse());
        normals.add(Vector3fExt.Y_UNIT_VECTOR.getReverse());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getCopy());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getCopy());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getCopy());
        tangents.add(Vector3fExt.X_UNIT_VECTOR.getCopy());

        // top rim
        texCoords.add(new float[]{0, 0});

        texCoords.add(new float[]{1f, 0});
        texCoords.add(new float[]{0, 0});

        texCoords.add(new float[]{1f, 0});
        texCoords.add(new float[]{0, 0});

        texCoords.add(new float[]{1f, 0});
        texCoords.add(new float[]{0, 0});

        texCoords.add(new float[]{1f, 0});

        // bottom rim
        texCoords.add(new float[]{0, 1f});

        texCoords.add(new float[]{1f, 1f});
        texCoords.add(new float[]{0, 1f});

        texCoords.add(new float[]{1f, 1f});
        texCoords.add(new float[]{0, 1f});

        texCoords.add(new float[]{1f, 1f});
        texCoords.add(new float[]{0, 1f});

        texCoords.add(new float[]{1f, 1f});

        // top
        texCoords.add(new float[]{0, 0});
        texCoords.add(new float[]{1f, 0});
        texCoords.add(new float[]{0, 1f});
        texCoords.add(new float[]{1f, 1f});

        // bottom
        texCoords.add(new float[]{0, 0});
        texCoords.add(new float[]{1f, 0});
        texCoords.add(new float[]{0, 1f});
        texCoords.add(new float[]{1f, 1f});
    }

    @Override
    protected void fillIndices() {
        // triangle strip
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

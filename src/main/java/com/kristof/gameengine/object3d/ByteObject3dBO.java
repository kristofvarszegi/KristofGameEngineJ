package com.kristof.gameengine.object3d;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Vector;

public abstract class ByteObject3dBO extends Object3dBO {
    protected final List<Byte> indices;

    public ByteObject3dBO() {
        this(new Vector<>());
    }

    public ByteObject3dBO(List<Object> geometryParams) {
        indices = new Vector<>();
        saveGeometryParams(geometryParams);
        fillVertexAttribLists();
        createAndFillVertexAttribArray();
        createAndFillVertexAttribBuffers();
        fillIndices();
        createAndFillIndexBuffer();
    }

    protected void saveGeometryParams(List<Object> geometryParams) {
    }

    @Override
    protected void createAndFillIndexBuffer() {
        final byte[] indexArray = new byte[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indexArray[i] = indices.get(i);
        }
        ByteBuffer indexBuffer = BufferUtils.createByteBuffer(indices.size());
        indexBuffer.put(indexArray);
        indexBuffer.flip();

        // Create a new VBO for the indices and select it (bind)
        iboIndex = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, iboIndex);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);

        // Deselect (bind to 0) the VBO
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}

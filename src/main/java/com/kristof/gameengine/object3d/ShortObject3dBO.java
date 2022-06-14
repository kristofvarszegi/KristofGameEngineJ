package com.kristof.gameengine.object3d;

import com.kristof.gameengine.shadow.ShadowVolume;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.nio.ShortBuffer;
import java.util.List;
import java.util.Vector;

public abstract class ShortObject3dBO extends Object3dBO {
    protected List<Short> indices = new Vector<>();

    protected static final float A_SIZE = 1;
    protected static final float B_SIZE = 1;

    public ShortObject3dBO() {
        fillVertexAttribLists();
        createAndFillVertexAttribArray();
        createAndFillVertexAttribBuffers();
        fillIndices();
        createAndFillIndexBuffer();
    }

    public ShortObject3dBO(ShadowVolume shadowVolumeData) {
        this.shadowVolume = shadowVolumeData;
        fillVertexAttribLists();
        createAndFillVertexAttribArray();
        createAndFillVertexAttribBuffers();
        fillIndices();
        createAndFillIndexBuffer();
    }

    public ShortObject3dBO(String fileName, float normScale, float scale) {
        super(fileName, normScale, scale);
        fillVertexAttribLists();
        createAndFillVertexAttribArray();
        createAndFillVertexAttribBuffers();
        fillIndices();
        createAndFillIndexBuffer();
    }

    @Override
    protected void createAndFillIndexBuffer() {
        final short[] indexArray = new short[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indexArray[i] = indices.get(i);
        }
        ShortBuffer indexBuffer = BufferUtils.createShortBuffer(indices.size());
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

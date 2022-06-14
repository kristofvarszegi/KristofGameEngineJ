package com.kristof.gameengine.object3d;

import com.kristof.gameengine.shadow.ShadowVolume;
import com.kristof.gameengine.util.Vector3fExt;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Vector;

public abstract class ByteObject3dBO extends Object3dBO {
    protected final List<Byte> indices;
    private byte[] indexArray;

    // TODO general Object array param
    // For Triangles. Vertices in CCW order. The 1st and the 2nd holds the biggest side of the triangle.
    protected Vector3fExt triangleVertex1 = new Vector3fExt();
    protected Vector3fExt triangleVertex2 = new Vector3fExt();
    protected Vector3fExt triangleVertex3 = new Vector3fExt();

    // For Rectangles.
    protected float rectASide = 0;
    protected float rectBSide = 0;

    public ByteObject3dBO() {
        indices = new Vector<>();
        fillVertexAttribLists();
        setUpVertexAttribArray();
        setUpVertexAttribBuffers();
        fillIndices();
        toIndexArray(indices);
        setUpIndexBuffer();
    }

    public ByteObject3dBO(ShadowVolume shadowVolumeData) {
        this.shadowVolumeData = shadowVolumeData;
        indices = new Vector<>();
        fillVertexAttribLists();
        setUpVertexAttribArray();
        setUpVertexAttribBuffers();
        fillIndices();
        toIndexArray(indices);
        setUpIndexBuffer();
    }

    public ByteObject3dBO(Vector3fExt vertex1, Vector3fExt vertex2, Vector3fExt vertex3) {    // For triangles
        indices = new Vector<>();
        triangleVertex1 = vertex1.getCopy();
        triangleVertex2 = vertex2.getCopy();
        triangleVertex3 = vertex3.getCopy();
        fillVertexAttribLists();
        setUpVertexAttribArray();
        setUpVertexAttribBuffers();
        fillIndices();
        toIndexArray(indices);
        setUpIndexBuffer();
    }

    public ByteObject3dBO(float aSide, float bSide) {    // For rectangles
        indices = new Vector<>();
        rectASide = aSide;
        rectBSide = bSide;
        fillVertexAttribLists();
        setUpVertexAttribArray();
        setUpVertexAttribBuffers();
        fillIndices();
        toIndexArray(indices);
        setUpIndexBuffer();
    }

    public ByteObject3dBO(List<Vector3fExt> vertexPositions) {    // For PolyFaces
        indices = new Vector<>();
        if (vertexPositions.size() <= 2) {
            positions.add(Vector3fExt.X_UNIT_VECTOR);
            positions.add(Vector3fExt.Z_UNIT_VECTOR.getReverse());
            positions.add(Vector3fExt.X_UNIT_VECTOR.getReverse());
        } else {
            positions.add(Vector3fExt.average(vertexPositions));
            positions.addAll(vertexPositions);
        }
        fillVertexAttribLists();
        setUpVertexAttribArray();
        setUpVertexAttribBuffers();
        fillIndices();
        toIndexArray(indices);
        setUpIndexBuffer();
    }

    private void toIndexArray(List<Byte> toSer) {
        indexArray = new byte[toSer.size()];
        for (int i = 0; i < toSer.size(); i++) {
            indexArray[i] = toSer.get(i);
        }
    }

    @Override
    protected void setUpIndexBuffer() {
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

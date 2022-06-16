package com.kristof.gameengine.object3d;

import com.kristof.gameengine.shadow.ShadowVolume;
import com.kristof.gameengine.util.Material;
import com.kristof.gameengine.util.Matrix4f;
import com.kristof.gameengine.util.Vector3fExt;
import com.kristof.gameengine.util.VertexConstants;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Vector;

import static com.kristof.gameengine.engine.GLConstants.*;
import static com.kristof.gameengine.util.Utils.exitOnGLError;

public abstract class Object3dBO {
    protected static final int POS_VA_INDEX = 0;
    protected static final int NOR_VA_INDEX = 1;
    private static final int TEX_VA_INDEX = 2;
    private static final int TAN_VA_INDEX = 3;

    protected String fileName = "filename uninitialized";
    protected float normScale = 1f;
    protected float size = 1f;

    protected ShadowVolume shadowVolume;

    protected int vaoIndex = 0;
    protected int vboIndex = 0;
    protected int iboIndex = 0;

    protected List<Vector3fExt> positions;
    protected List<Vector3fExt> normals;
    protected List<Vector3fExt> tangents;
    protected List<float[]> texCoords;

    protected FloatBuffer matrix44Buffer;
    private float[] vertexAttribArray;
    protected int vertexCount;

    protected Object3dBO() {
        positions = new Vector<>();
        normals = new Vector<>();
        tangents = new Vector<>();
        texCoords = new Vector<>();
        matrix44Buffer = BufferUtils.createFloatBuffer(16);
    }

    public Object3dBO(String fileName, float normScale, float scale) {
        this();
        this.fileName = fileName;
        this.normScale = normScale;
        size = scale;
    }

    public List<Vector3fExt> getPositions() {
        return (new Vector<>(positions));
    }

    public float getSize() {
        return size;
    }

    protected void createAndFillVertexAttribBuffers() {
        // Sending data to OpenGL requires the usage of (flipped) byte buffers
        final FloatBuffer vertexAttribBuffer = BufferUtils.createFloatBuffer(vertexAttribArray.length);
        vertexAttribBuffer.put(vertexAttribArray);
        vertexAttribBuffer.flip();

        // Create a new Vertex Array Object in memory and select it (bind)
        // A VAO can have up to 16 attributes (VBO's) assigned to it by default
        vaoIndex = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoIndex);

        // Create a new Vertex Buffer Object in memory and select it (bind)
        // A VBO is a collection of vectors which in this case resemble the location of each vertex
        vboIndex = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboIndex);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexAttribBuffer, GL15.GL_STATIC_DRAW);
        exitOnGLError("Object3dBO constructor - bufferdata vbo");

        // Put the VBO in the attributes list at pre-defined mIndexList
        GL20.glVertexAttribPointer(POS_VA_INDEX, VertexConstants.POS_ELEMENT_NUM, GL11.GL_FLOAT, false, VertexConstants.STRIDE_IN_BYTES_PNTT, 0);
        GL20.glVertexAttribPointer(NOR_VA_INDEX, VertexConstants.NOR_ELEMENT_NUM, GL11.GL_FLOAT, false, VertexConstants.STRIDE_IN_BYTES_PNTT,
                VertexConstants.POS_ELEMENT_NUM * VertexConstants.BYTES_PER_FLOAT);
        GL20.glVertexAttribPointer(TEX_VA_INDEX, VertexConstants.TEX_ELEMENT_NUM, GL11.GL_FLOAT, false, VertexConstants.STRIDE_IN_BYTES_PNTT,
                (VertexConstants.POS_ELEMENT_NUM + VertexConstants.NOR_ELEMENT_NUM) * VertexConstants.BYTES_PER_FLOAT);
        GL20.glVertexAttribPointer(TAN_VA_INDEX, VertexConstants.TAN_ELEMENT_NUM, GL11.GL_FLOAT, false, VertexConstants.STRIDE_IN_BYTES_PNTT,
                (VertexConstants.POS_ELEMENT_NUM + VertexConstants.NOR_ELEMENT_NUM + VertexConstants.TEX_ELEMENT_NUM) * VertexConstants.BYTES_PER_FLOAT);
        exitOnGLError("Object3dBO ctor - vertexAttribPointer vbo");

        // Deselect (bind to 0) the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        exitOnGLError("Object3dBO ctor - unbinding vbo");

        // Deselect (bind to 0) the VAO
        GL30.glBindVertexArray(0); // vaoIndex
    }

    public void destroy() {
        // Delete the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vboIndex);

        // Delete the IBO
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(iboIndex);

        // Delete the VAO
        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoIndex);
    }

    private void toPositionArray(List<Vector3fExt> toSer) {
        for (int i = 0; i < vertexCount; i++) {
            vertexAttribArray[VertexConstants.STRIDE_IN_ELEMENTS_PNTT * i] = toSer.get(i).getX();
            vertexAttribArray[VertexConstants.STRIDE_IN_ELEMENTS_PNTT * i + 1] = toSer.get(i).getY();
            vertexAttribArray[VertexConstants.STRIDE_IN_ELEMENTS_PNTT * i + 2] = toSer.get(i).getZ();
        }
    }

    private void toNormalArray(List<Vector3fExt> toSer) {
        for (int i = 0; i < vertexCount; i++) {
            vertexAttribArray[VertexConstants.STRIDE_IN_ELEMENTS_PNTT * i + VertexConstants.POS_ELEMENT_NUM] = toSer.get(i).getX();
            vertexAttribArray[VertexConstants.STRIDE_IN_ELEMENTS_PNTT * i + VertexConstants.POS_ELEMENT_NUM + 1] = toSer.get(i).getY();
            vertexAttribArray[VertexConstants.STRIDE_IN_ELEMENTS_PNTT * i + VertexConstants.POS_ELEMENT_NUM + 2] = toSer.get(i).getZ();
        }
    }

    private void toTexCoordArray(List<float[]> toSer) {
        for (int i = 0; i < vertexCount; i++) {
            vertexAttribArray[VertexConstants.STRIDE_IN_ELEMENTS_PNTT * i + VertexConstants.POS_ELEMENT_NUM + VertexConstants.NOR_ELEMENT_NUM] = toSer.get(i)[0];
            vertexAttribArray[VertexConstants.STRIDE_IN_ELEMENTS_PNTT * i + VertexConstants.POS_ELEMENT_NUM + VertexConstants.NOR_ELEMENT_NUM + 1] = toSer.get(i)[1];
        }
    }

    private void toTangentArray(List<Vector3fExt> toSer) {
        for (int i = 0; i < vertexCount; i++) {
            vertexAttribArray[VertexConstants.STRIDE_IN_ELEMENTS_PNTT * i + VertexConstants.POS_ELEMENT_NUM + VertexConstants.NOR_ELEMENT_NUM + VertexConstants.TEX_ELEMENT_NUM] = toSer.get(i).getX();
            vertexAttribArray[VertexConstants.STRIDE_IN_ELEMENTS_PNTT * i + VertexConstants.POS_ELEMENT_NUM + VertexConstants.NOR_ELEMENT_NUM + VertexConstants.TEX_ELEMENT_NUM + 1] = toSer.get(i).getY();
            vertexAttribArray[VertexConstants.STRIDE_IN_ELEMENTS_PNTT * i + VertexConstants.POS_ELEMENT_NUM + VertexConstants.NOR_ELEMENT_NUM + VertexConstants.TEX_ELEMENT_NUM + 2] = toSer.get(i).getZ();
        }
    }

    protected void createAndFillVertexAttribArray() {
        vertexCount = positions.size();
        vertexAttribArray = new float[vertexCount * VertexConstants.STRIDE_IN_ELEMENTS_PNTT];
        toPositionArray(positions);
        toNormalArray(normals);
        toTexCoordArray(texCoords);
        toTangentArray(tangents);
    }

    private void loadUniforms(int[] programUniformIndices, Matrix4f viewMatrix, Matrix4f projectionMatrix,
                              Object3d object3d) {
		if (programUniformIndices[U_COLORMAP_MAP] >= 0 && object3d.getColorMapTexIndex() >= 0) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + COLORMAP_TEXTURE_UNIT);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, object3d.getColorMapTexIndex());
            GL20.glUniform1i(programUniformIndices[U_COLORMAP_MAP], COLORMAP_TEXTURE_UNIT);
            exitOnGLError("in " + this.getClass().getSimpleName()
                    + " at Object3dBO.loadUniforms(), colormap");
        }
        if (programUniformIndices[U_NORMALMAP_MAP] >= 0 && object3d.getNormalMapTexIndex() >= 0) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + NORMALMAP_TEXTURE_UNIT);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, object3d.getNormalMapTexIndex());
            GL20.glUniform1i(programUniformIndices[U_NORMALMAP_MAP], NORMALMAP_TEXTURE_UNIT);
            exitOnGLError("in " + this.getClass().getSimpleName()
                    + " at Object3dBO.loadUniforms(), normalmap");
        }

        matrix44Buffer = BufferUtils.createFloatBuffer(16);

        if (programUniformIndices[U_MMATRIX_MAP] >= 0) {
            object3d.getModelMatrix().store(matrix44Buffer);
            matrix44Buffer.flip();
            GL20.glUniformMatrix4fv(programUniformIndices[U_MMATRIX_MAP], false, matrix44Buffer);
            exitOnGLError("in " + this.getClass().getSimpleName()
                    + " at Object3dBO.loadUniforms(), u_MMatrix");
        }

        if (programUniformIndices[U_VMATRIX_MAP] >= 0) {
            viewMatrix.store(matrix44Buffer);
            matrix44Buffer.flip();
            GL20.glUniformMatrix4fv(programUniformIndices[U_VMATRIX_MAP], false, matrix44Buffer);
            exitOnGLError("in " + this.getClass().getSimpleName()
                    + " at Object3dBO.loadUniforms(), u_VMatrix");
        }

        if (programUniformIndices[U_PMATRIX_MAP] >= 0) {
            projectionMatrix.store(matrix44Buffer);
            matrix44Buffer.flip();
            GL20.glUniformMatrix4fv(programUniformIndices[U_PMATRIX_MAP], false, matrix44Buffer);
            exitOnGLError("in " + this.getClass().getSimpleName()
                    + " at Object3dBO.loadUniforms(), u_PMatrix");
        }

        if (programUniformIndices[U_RMATRIX_MAP] >= 0) {
            object3d.getRotationMatrix().store(matrix44Buffer);
            matrix44Buffer.flip();
            GL20.glUniformMatrix4fv(programUniformIndices[U_RMATRIX_MAP], false, matrix44Buffer);
            exitOnGLError("in " + this.getClass().getSimpleName()
                    + " at Object3dBO.loadUniforms(), u_RMatrix");
        }

        if (programUniformIndices[U_MATERIAL_MAP] >= 0) {
            Material lps = object3d.getMaterial();
            GL20.glUniform4f(programUniformIndices[U_MATERIAL_MAP], lps.getDiffuse(), lps.getSpecular(),
                    lps.getShininess(), lps.getEmissive());
            exitOnGLError("in " + this.getClass().getSimpleName()
                    + " at Object3dBO.loadUniforms(), u_Material");
        }

        exitOnGLError("in " + this.getClass().getSimpleName() + " at Object3dBO.loadUniforms()");
    }

    public void render(int[] programUniformIndices, Matrix4f viewMatrix, Matrix4f projectionMatrix,
                       Object3d object3d) {    // TODO check if gl is initialized
        exitOnGLError("in " + this.getClass().getSimpleName() + " at Object3dBO.render() start");
        loadUniforms(programUniformIndices, viewMatrix, projectionMatrix, object3d);

        // Bind to the VAO that has all the information about the vertices
        GL30.glBindVertexArray(vaoIndex);
        GL20.glEnableVertexAttribArray(POS_VA_INDEX);
        GL20.glEnableVertexAttribArray(NOR_VA_INDEX);
        GL20.glEnableVertexAttribArray(TEX_VA_INDEX);
        GL20.glEnableVertexAttribArray(TAN_VA_INDEX);
        exitOnGLError("in " + this.getClass().getSimpleName() + " at binding vertex array");

        // Bind to the index VBO that has all the information about the order of the vertices
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, iboIndex);

        // Draw the vertices
        drawElements();

        // Put everything back to default (deselect)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL20.glDisableVertexAttribArray(POS_VA_INDEX);
        GL20.glDisableVertexAttribArray(NOR_VA_INDEX);
        GL20.glDisableVertexAttribArray(TEX_VA_INDEX);
        GL20.glDisableVertexAttribArray(TAN_VA_INDEX);
        GL30.glBindVertexArray(0);
    }

    /**
     * Fills positions, normals, tangents, and texCoords
     */
    protected abstract void fillVertexAttribLists();    // To build: positions, normals, tangents, texcoords

    /**
     * Fills indices
     */
    protected abstract void fillIndices();

    protected abstract void createAndFillIndexBuffer();

    protected abstract void drawElements();
}

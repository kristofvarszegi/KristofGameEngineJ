package com.kristof.gameengine.shadow;

import com.kristof.gameengine.util.Vector3fExt;
import com.kristof.gameengine.object3d.ByteObject3dBO;

import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Vector;

import static com.kristof.gameengine.engine.EngineMiscConstants.INFINITY;
import static com.kristof.gameengine.object3d.Object3d.GAP_SIZE;

public class ShadowVolumeBO extends ByteObject3dBO {
    private Vector3fExt lightParam;
    private List<Vector3fExt> rimVertexPositions;
    private List<Integer> tokenSizes;

    public ShadowVolumeBO(ShadowVolume shadowVolumeData) {
        super(shadowVolumeData);
        lightParam = new Vector3fExt();
        // TODO check list consistency
    }

    public int getTokenCount() {
        return tokenSizes.size();
    }

    @Override
    protected void fillVertexAttribLists() {
        lightParam = shadowVolumeData.getLightPosition();
        rimVertexPositions = shadowVolumeData.getRimVertexPositions();
        tokenSizes = shadowVolumeData.getTokenSizes();
        if (rimVertexPositions.size() > 0) {
            if (tokenSizes.size() > 1) {
                setUpRimVertexListTokenAttribLists();
            } else {
                setUpRimVertexListAttribLists();
            }
        }
    }

    protected void setUpRimVertexListTokenAttribLists() {
        final Vector3fExt zGap = Vector3fExt.substract(shadowVolumeData.getPosition(), lightParam)
                .getNormalized().getMultipliedBy(GAP_SIZE);
        List<Vector3fExt> rimVertexPositionsTemp;
        int tokenSizeSumSoFar = 0;
        for (final Integer tokenSize : tokenSizes) {    // Loop for a part shadow
            rimVertexPositionsTemp = new Vector<>();
            for (int j = 0; j < tokenSize; j++) {   // Create the subset of small rim vertices
                rimVertexPositionsTemp.add(rimVertexPositions.get(tokenSizeSumSoFar + j));
            }

            // Add front cap
            positions.add(Vector3fExt.average(rimVertexPositionsTemp).getSumWith(zGap));
            for (final Vector3fExt vector3FExt : rimVertexPositionsTemp) {
                positions.add(vector3FExt.getSumWith(zGap));
            }

            // Extrude center and silhouette vertices
            final Vector3fExt extrCVr = Vector3fExt.substract(shadowVolumeData.getPosition(), lightParam);
            extrCVr.setLength(INFINITY);
            positions.add(extrCVr);

            Vector3fExt extrVr;
            for (final Vector3fExt vector3FExt : rimVertexPositionsTemp) {
                extrVr = Vector3fExt.add(vector3FExt, lightParam.getReverse());
                extrVr.setLength(INFINITY);
                positions.add(extrVr);
            }
            tokenSizeSumSoFar += tokenSize;
        }

        // Building normals just to be able to make the shape visible for debugging
        for (int i = 0; i < positions.size(); i++) {
            normals.add(Vector3fExt.Y_UNIT_VECTOR);
        }

        for (int i = 0; i < positions.size(); i++) {
            tangents.add(Vector3fExt.X_UNIT_VECTOR);
        }

        for (int i = 0; i < positions.size(); i++) {
            texCoords.add(new float[]{0f, 0f});
        }
    }

    protected void setUpRimVertexListAttribLists() {
        // Build shadow volume vertex position array - front cap
        final Vector3fExt zGap = Vector3fExt.substract(shadowVolumeData.getPosition(), lightParam)
                .getNormalized().getMultipliedBy(GAP_SIZE);
        positions.add(Vector3fExt.average(rimVertexPositions).getSumWith(zGap));
        for (final Vector3fExt rimVertexPosition : rimVertexPositions) {
            positions.add(rimVertexPosition.getSumWith(zGap));
        }

        // Extrude silhouette vertices & center
        final Vector3fExt extrCVr = shadowVolumeData.getPosition().getSumWith(
                Vector3fExt.substract(shadowVolumeData.getPosition(), lightParam)
                        .getNormalized().getMultipliedBy(INFINITY));
        positions.add(extrCVr);

        Vector3fExt extrVr;
        for (int i = 0; i < rimVertexPositions.size(); i++) {
            extrVr = Vector3fExt.substract(positions.get(i + 1), lightParam);
            extrVr.setLength(INFINITY);
            positions.add(positions.get(i + 1).getSumWith(extrVr));
        }
        
        // Building normals just to be able to make the shape visible for debugging
        for (int i = 0; i < positions.size(); i++) {
            normals.add(Vector3fExt.Y_UNIT_VECTOR);
        }
        for (int i = 0; i < positions.size(); i++) {
            tangents.add(Vector3fExt.X_UNIT_VECTOR);
        }
        for (int i = 0; i < positions.size(); i++) {
            texCoords.add(new float[]{0f, 0f});
        }
    }

    protected void fillIndices() {
        if (tokenSizes.size() > 1) {
            int vertexCountSoFar = 0;
            int vertexCountTemp;
            for (final Integer tokenSize : tokenSizes) {
                vertexCountTemp = (byte) (2 * tokenSize + 2);

                // front cap - triangle fan
                for (int j = 0; j < vertexCountTemp / 2; j++) {
                    indices.add((byte) (j + vertexCountSoFar));
                }
                indices.add((byte) (1 + vertexCountSoFar));

                // mantle - triangle strip
                for (int j = 0; j < (vertexCountTemp / 2 - 1); j++) {
                    indices.add((byte) (j + 1 + vertexCountSoFar));
                    indices.add((byte) (vertexCountTemp / 2 + j + 1 + vertexCountSoFar));
                }
                // closing mantle strip
                indices.add((byte) (1 + vertexCountSoFar));
                indices.add((byte) (vertexCountTemp / 2 + 1 + vertexCountSoFar));

                // back cap - triangle fan, should be in reverse order (normal pointing away from light source) for correct culling
                indices.add((byte) (vertexCountTemp / 2 + vertexCountSoFar));    // back cap center
                for (int j = 0; j < (vertexCountTemp / 2 - 1); j++) {
                    indices.add((byte) (vertexCountTemp - (j + 1) + vertexCountSoFar));
                }
                indices.add((byte) (vertexCountTemp - 1 + vertexCountSoFar));

                vertexCountSoFar += vertexCountTemp;
            }
        } else {    // Single shadow volume (tokenSizes size = 1)
            // front cap - triangle fan
            for (int i = 0; i < vertexCount / 2; i++) {
                indices.add((byte) i);
            }
            indices.add((byte) 1);

            // mantle - triangle strip
            for (int i = 0; i < (vertexCount / 2 - 1); i++) {
                indices.add((byte) (i + 1));
                indices.add((byte) (vertexCount / 2 + i + 1));
            }
            // closing mantle strip
            indices.add((byte) 1);
            indices.add((byte) (vertexCount / 2 + 1));

            // back cap - triangle fan, should be in reverse order (normal pointing away from light source) for correct culling
            indices.add((byte) (vertexCount / 2));    // back cap center
            for (int i = 0; i < (vertexCount / 2 - 1); i++) {
                indices.add((byte) (vertexCount - (i + 1)));
            }
            indices.add((byte) (vertexCount - 1));

        }
    }

    /*public static List<Vector3fExt> orderVerticesCCW(Vector3fExt pOV) {
        final List<Vector3fExt> vsCCW = new Vector<>();
        // TODO
        return vsCCW;
    }*/

    protected void drawElements() {
        GL11.glCullFace(GL11.GL_FRONT);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 0x0, 0xff);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_INCR, GL11.GL_KEEP);

        if (tokenSizes.size() > 1) {
            renderComplexShadowVolume();
        } else {
            renderSingleShadowVolume();
        }

        GL11.glCullFace(GL11.GL_BACK);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 0x0, 0xff);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_DECR, GL11.GL_KEEP);

        if (tokenSizes.size() > 1) {
            renderComplexShadowVolume();
        } else {
            renderSingleShadowVolume();
        }
    }

    private void renderComplexShadowVolume() {
        int indexCountSoFar = 0;
        int vertexCountTemp = 0;
        for (final Integer tokenSize : tokenSizes) {
            vertexCountTemp = (byte) (2 * tokenSize + 2);

            GL11.glDrawElements(GL11.GL_TRIANGLE_FAN,
                    (vertexCountTemp / 2 + 1),
                    GL11.GL_UNSIGNED_BYTE,
                    indexCountSoFar);
            indexCountSoFar += (vertexCountTemp / 2 + 1);

            GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP,
                    vertexCountTemp,    // #vxs in a strip
                    GL11.GL_UNSIGNED_BYTE,
                    indexCountSoFar);
            indexCountSoFar += vertexCountTemp;

            GL11.glDrawElements(GL11.GL_TRIANGLE_FAN,
                    (vertexCountTemp / 2 + 1),
                    GL11.GL_UNSIGNED_BYTE,
                    indexCountSoFar);
            indexCountSoFar += (vertexCountTemp / 2 + 1);
        }
    }

    private void renderSingleShadowVolume() {
        GL11.glDrawElements(GL11.GL_TRIANGLE_FAN,
                (vertexCount / 2 + 1),
                GL11.GL_UNSIGNED_BYTE,
                0);
        GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP,
                vertexCount,    // #vxs in a strip
                GL11.GL_UNSIGNED_BYTE,
                (vertexCount / 2 + 1));
        GL11.glDrawElements(GL11.GL_TRIANGLE_FAN,
                (vertexCount / 2 + 1),
                GL11.GL_UNSIGNED_BYTE,
                ((vertexCount / 2 + 1) + (vertexCount)));
    }
}

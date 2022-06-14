package com.kristof.gameengine.shadow;

import com.kristof.gameengine.util.ColorVector;
import com.kristof.gameengine.util.Material;
import com.kristof.gameengine.util.Vector3fExt;
import com.kristof.gameengine.object3d.Object3d;
import com.kristof.gameengine.object3d.Object3dBO;

import java.util.List;
import java.util.Vector;

public class ShadowVolume extends Object3d {
    private final Vector3fExt lightParam;
    private final LIGHT_PARAM_TYPE lightType;
    private List<Vector3fExt> shadowVertices;
    private List<Integer> tokenSizes;

    public enum LIGHT_PARAM_TYPE {LIGHT_DIRECTION, LIGHT_POSITION}

    public ShadowVolume(Vector3fExt lightParam, LIGHT_PARAM_TYPE lightType) {
        super(Vector3fExt.NULL_VECTOR, 0, 0, 0, Vector3fExt.UNIT_VECTOR,
                Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.NEXUS_BLUE, Material.EMISSIVE,
                -1, -1);
        this.lightParam = new Vector3fExt(lightParam);
        this.lightType = lightType;
        shadowVertices = new Vector<>();
        tokenSizes = new Vector<>();
    }

    public ShadowVolume(Object3d obj, Vector3fExt lightParam, LIGHT_PARAM_TYPE lightType) {
        super(Vector3fExt.NULL_VECTOR, 0, 0, 0, Vector3fExt.UNIT_VECTOR,
                Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.NEXUS_BLUE, Material.EMISSIVE,
                -1, -1);
        this.lightParam = new Vector3fExt(lightParam);
        this.lightType = lightType;
        shadowVertices = obj.getShadowVertices(lightParam, lightType);
        tokenSizes = new Vector<>();
        tokenSizes.add(shadowVertices.size());
    }

    public ShadowVolume(List<Vector3fExt> shadowVertices, Vector3fExt position, Vector3fExt lightParam,
                        LIGHT_PARAM_TYPE lightType) {
        super(position, 0, 0, 0, Vector3fExt.UNIT_VECTOR, Vector3fExt.NULL_VECTOR,
                Vector3fExt.NULL_VECTOR, ColorVector.NEXUS_BLUE, Material.EMISSIVE, -1,
                -1);
        this.lightParam = new Vector3fExt(lightParam);
        this.lightType = lightType;
        tokenSizes = new Vector<>();
        if (shadowVertices != null) {
            this.shadowVertices = new Vector<>(shadowVertices);
            tokenSizes.add(shadowVertices.size());
        } else {
            this.shadowVertices = new Vector<>();
        }
    }

    public ShadowVolume(List<Vector3fExt> shadowVertices, Vector3fExt position, Vector3fExt lightParam,
                        LIGHT_PARAM_TYPE lightType, List<Integer> tokenSizes) {
        super(position, 0, 0, 0, Vector3fExt.UNIT_VECTOR, Vector3fExt.NULL_VECTOR,
                Vector3fExt.NULL_VECTOR, ColorVector.NEXUS_BLUE, Material.EMISSIVE, -1,
                -1);
        // TODO check input consistency
        this.lightParam = new Vector3fExt(lightParam);
        this.lightType = lightType;
        this.shadowVertices = new Vector<>(shadowVertices);
        this.tokenSizes = new Vector<>(tokenSizes);
    }

    public void addData(List<Vector3fExt> shadowVertexList) {
        if (shadowVertexList.size() > 2) {
            this.shadowVertices.addAll(shadowVertexList);
            tokenSizes.add(shadowVertexList.size());
        }
    }

    public Vector3fExt getPosition() {
        return position.getCopy();
    }

    public Vector3fExt getLightPosition() {
        return lightParam.getCopy();
    }

    public LIGHT_PARAM_TYPE getLightType() {
        return lightType;
    }

    public List<Vector3fExt> getRimVertexPositions() {
        return new Vector<>(shadowVertices);
    }

    public List<Integer> getTokenSizes() {
        return tokenSizes;
    }

    public int getTokenCount() {
        return tokenSizes.size();
    }

    public List<Vector3fExt> getShadowVertices() {
        return shadowVertices;
    }

    public int getShadowVertexCount() {
        return shadowVertices.size();
    }

    @Override
    public float getCollisionRadius() {
        return 0;
    }

    @Override
    public float getVolume() {
        return 0;
    }

    public void setTokenSizes(List<Integer> tokenSizes) {
        this.tokenSizes = tokenSizes;
    }

    public void setShadowVertices(List<Vector3fExt> shadowVertices) {
        this.shadowVertices = shadowVertices;
    }

    public void add(List<Vector3fExt> shadowVertices, List<Integer> tokenSizes) {
        this.shadowVertices.addAll(shadowVertices);
        this.tokenSizes.addAll(tokenSizes);
    }

    public void add(ShadowVolume shVData) {
        this.shadowVertices.addAll(shVData.getShadowVertices());
        this.tokenSizes.addAll(shVData.getTokenSizes());
    }

    @Override
    public Object3dBO getPrototype() {
        throw new RuntimeException("getPrototype N/A for shadow volume");
    }

    @Override
    public Vector3fExt[] applyCollisionTo(Object3d obj2) {
        throw new RuntimeException("Collision N/A for shadow volume");
    }

    @Override
    public List<Vector3fExt> getShadowVertices(Vector3fExt lightParam, LIGHT_PARAM_TYPE lightType) {
        throw new RuntimeException("Shadow vertices N/A for shadow volume");
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("ShadowVolume: ");
        int vxIndex = 0;
        for (final Integer tokenSize : tokenSizes) {
            str.append("\ntoken size: ").append(tokenSize);
            for (int j = 0; j < tokenSize; j++) {
                str.append(", ").append(shadowVertices.get(vxIndex + j));
            }
            vxIndex += tokenSize;
        }
        return str.toString();
    }
}

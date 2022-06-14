package com.kristof.gameengine.polyface;

import com.kristof.gameengine.object3d.Object3d;
import com.kristof.gameengine.object3d.Object3dBO;
import com.kristof.gameengine.plate.Plate;
import com.kristof.gameengine.shadow.ShadowVolume;
import com.kristof.gameengine.triangle.Triangle;
import com.kristof.gameengine.util.*;

import java.util.List;
import java.util.Vector;

public class PolyFace extends Object3d {
    private final List<Vector3fExt> vertexPositions;
    private final Vector3fExt normal;
    private final List<Triangle> triangles;
    private int id;
    private int[] neighborIDs;
    private PolyFaceBO glBufferObject;

    /*
     * Constructors
     *
     * Vertex positions
     * 	given in world space, remaining in that
     * 	ordered in CCW
     */

    public PolyFace(int id, Vector3fExt vertex1, Vector3fExt vertex2, Vector3fExt vertex3, Vector3fExt vertex4,
                    int colorMapTexIndex, int normalMapTexIndex) {    // CCW if normal it is pointed to my eye
        super(Vector3fExt.NULL_VECTOR, 0, 0, 0, Vector3fExt.UNIT_VECTOR,
                Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.MAGENTA, Material.PEARL, colorMapTexIndex,
                normalMapTexIndex);
        vertexPositions = new Vector<>();
        triangles = new Vector<>();
        this.id = id;

        // TODO check if vertex positions are in one common plane
        // TODO order vertices in ccw
        vertexPositions.add(vertex1.getCopy());
        vertexPositions.add(vertex2.getCopy());
        vertexPositions.add(vertex3.getCopy());
        vertexPositions.add(vertex4.getCopy());

        normal = Vector3fExt.crossProduct(vertex2.getThisMinus(vertex1), vertex4.getThisMinus(vertex1)).getNormalized();

        Vector3fExt averagePos = Vector3fExt.average(vertex1, vertex2, vertex3, vertex4);
        triangles.add(new Triangle(0, 0, 0, Vector3fExt.NULL_VECTOR,
                Vector3fExt.NULL_VECTOR, ColorVector.MAGENTA, Material.PEARL, -1, -1,
                averagePos, vertex1, vertex2));
        triangles.add(new Triangle(0, 0, 0, Vector3fExt.NULL_VECTOR,
                Vector3fExt.NULL_VECTOR, ColorVector.MAGENTA, Material.PEARL, -1, -1,
                averagePos, vertex2, vertex3));
        triangles.add(new Triangle(0, 0, 0, Vector3fExt.NULL_VECTOR,
                Vector3fExt.NULL_VECTOR, ColorVector.MAGENTA, Material.PEARL, -1, -1,
                averagePos, vertex3, vertex4));
        triangles.add(new Triangle(0, 0, 0, Vector3fExt.NULL_VECTOR,
                Vector3fExt.NULL_VECTOR, ColorVector.MAGENTA, Material.PEARL, -1, -1,
                averagePos, vertex4, vertex1));

        neighborIDs = new int[]{-1, -1, -1, -1};

        glBufferObject = new PolyFaceBO(vertexPositions);
    }

    public PolyFace(int id, List<Vector3fExt> verticesCCW, int colorMapTexIndex, int normalMapTexIndex) {    // CCW if normal it is pointed to my eye
        super(Vector3fExt.NULL_VECTOR, 0, 0, 0, Vector3fExt.UNIT_VECTOR,
                Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.MAGENTA, Material.PEARL, colorMapTexIndex,
                normalMapTexIndex);

        vertexPositions = new Vector<>();
        triangles = new Vector<>();
        this.id = id;

        // TODO check if vertexPositions are in one common plane
        // TODO order vertices in ccw
        normal = Vector3fExt.crossProduct(verticesCCW.get(1).getThisMinus(verticesCCW.get(0)),
                verticesCCW.get(verticesCCW.size() - 1).getThisMinus(verticesCCW.get(0))).getNormalized();
        if (verticesCCW.size() > 2) {
            for (final Vector3fExt vector3fExt : verticesCCW) {
                vertexPositions.add(vector3fExt.getCopy());
            }
            final Vector3fExt averagePos = Vector3fExt.average(verticesCCW);
            for (int i = 0; i < verticesCCW.size() - 1; i++) {
                triangles.add(new Triangle(0, 0, 0, Vector3fExt.NULL_VECTOR,
                        Vector3fExt.NULL_VECTOR, ColorVector.MAGENTA, Material.PEARL, -1,
                        -1, averagePos, verticesCCW.get(i), verticesCCW.get(i + 1)));
            }
            triangles.add(new Triangle(0, 0, 0, Vector3fExt.NULL_VECTOR,
                    Vector3fExt.NULL_VECTOR, ColorVector.MAGENTA, Material.PEARL, -1, -1,
                    averagePos, verticesCCW.get(verticesCCW.size() - 1), verticesCCW.get(0)));
            neighborIDs = new int[vertexPositions.size()];
            glBufferObject = new PolyFaceBO(vertexPositions);
        }
    }

    public PolyFace(PolyFace pf2) {    // Deep copy ctor
        super(Vector3fExt.average(pf2.getVertexPositions()), pf2.getRotationMatrix(), pf2.getScale(), pf2.getVelocity(),
                pf2.getForce(), pf2.getColor(), pf2.getMaterial(), pf2.getColorMapTexIndex(),
                pf2.getNormalMapTexIndex());
        this.id = pf2.getId();
        vertexPositions = new Vector<>();
        for (int i = 0; i < pf2.getVertexPositionCount(); i++) {
            vertexPositions.add(pf2.getVertexPosition(i).getCopy());
        }
        normal = pf2.getNormal().getCopy();
        triangles = new Vector<>();
        for (int i = 0; i < pf2.getVertexPositionCount(); i++) {
            triangles.add(pf2.getTriangle(i).getCopy());
        }
        neighborIDs = new int[pf2.getNeighborCount()];
        for (int i = 0; i < neighborIDs.length; i++) {
            neighborIDs[i] = pf2.getNeighborId(i);
        }
        glBufferObject = new PolyFaceBO(vertexPositions);
    }

    @Override
    protected Object3dBO getGlBufferObject() {
        return glBufferObject;
    }

    @Override
    public float getCollisionRadius() {
        return getMaxRadius();
    }

    @Override
    public float getVolume() {
        float r = getMaxRadius();
        return (r * r * Vector3fExt.PI * Plate.PLATE_THICKNESS);
    }

    public Vector3fExt getVertexPosition(int index) {
        return vertexPositions.get(index);
    }

    public Triangle getTriangle(int index) {
        return triangles.get(index);
    }

    public float getMaxRadius() {
        float maxR = 0;
        Vector3fExt center = Vector3fExt.average(vertexPositions);
        for (final Vector3fExt vertexPosition : vertexPositions) {
            if (vertexPosition.getThisMinus(center).getLengthSquare() > maxR * maxR) {
                maxR = vertexPosition.getThisMinus(center).getLength();
            }
        }
        return maxR;
    }

    public void setId(int newId) {
        id = newId;
    }

    public void setNeighborIds(int[] newNeighborIds) {
        int maxL = Math.min(neighborIDs.length, newNeighborIds.length);
        System.arraycopy(newNeighborIds, 0, neighborIDs, 0, maxL);
    }

    public void setNeighborIds(int nId1, int nId2, int nId3) {
        neighborIDs[0] = nId1;
        neighborIDs[1] = nId2;
        neighborIDs[2] = nId3;
    }

    public void setNeighborIds(int nId1, int nId2, int nId3, int nId4) {
        neighborIDs[0] = nId1;
        neighborIDs[1] = nId2;
        neighborIDs[2] = nId3;
        neighborIDs[3] = nId4;
    }

    public int getId() {
        return id;
    }

    public Vector3fExt getNormal() {
        return normal.getCopy();
    }

    public Vector3fExt getVertex(int index) {
        if (index < vertexPositions.size()) {
            return vertexPositions.get(index).getCopy();
        } else {
            return vertexPositions.get(0).getCopy();
        }
    }

    public int getNeighborId(int index) {
        if (index < neighborIDs.length) {
            return neighborIDs[index];
        } else {
            return neighborIDs[0];
        }
    }

    public List<Vector3fExt> getVertexPositions() {
        return new Vector<>(vertexPositions);
    }

    public Vector3fExt[] getCommonEdge(PolyFace face2) {
        final Vector3fExt[] commonEdge = new Vector3fExt[2];
        int currentIndex = 0;
        for (final Vector3fExt vertexPosition : vertexPositions) {
            for (int j = 0; j < face2.getVertexPositionCount(); j++) {
                if (vertexPosition.equals(face2.getVertex(j))) {
                    commonEdge[currentIndex] = vertexPosition.getCopy();
                    currentIndex++;
                }
                if (currentIndex == 2) return commonEdge;
            }
        }
        return commonEdge;
    }

    public int getVertexPositionCount() {
        return vertexPositions.size();
    }

    public int getNeighborCount() {
        return neighborIDs.length;
    }

    public PolyFace getScaled(float scale) {
        final List<Vector3fExt> scaledVertices = new Vector<>();
        for (final Vector3fExt vertexPosition : vertexPositions) {
            scaledVertices.add(vertexPosition.getMultipliedBy(scale));
        }
        PolyFace scaledFace = new PolyFace(id, scaledVertices, colorMapTexIndex, normalMapTexIndex);
        scaledFace.setNeighborIds(neighborIDs);
        return scaledFace;
    }

    public PolyFace getTransformed(Matrix4f modelMatrix, Matrix4f rotationMatrix) {
        final PolyFace retFace = new PolyFace(this);    // Deep copy constructor
        retFace.transform(modelMatrix, rotationMatrix);
        return retFace;
    }

    public boolean isFacingInwardVector(Vector3fExt inwardVector) {
        return normal.getDotProductWith(inwardVector) < 0;
    }

    public boolean isNeighbor(int possibleNeighborId) {
        for (final int neighborID : neighborIDs) {
            if (neighborID == possibleNeighborId) return true;
        }
        return false;
    }

    public void translate(Vector3fExt transVr) {
        for (final Vector3fExt vertexPosition : vertexPositions) {
            vertexPosition.add(transVr);
        }
    }

    public void transform(Matrix4f modelMatrix, Matrix4f rotationMatrix) {
        for (final Vector3fExt vertexPosition : vertexPositions) {
            vertexPosition.multiplyBy(modelMatrix);
        }
        normal.multiplyBy(rotationMatrix);
        for (final Triangle triangle : triangles) {
            triangle.transform(modelMatrix, rotationMatrix);
        }
    }

    @Override
    public Vector3fExt[] applyCollisionTo(Object3d obj2) {  // TODO revive
        Vector3fExt[] retVal;
        for (final Triangle triangle : triangles) {
            retVal = triangle.applyCollisionTo(obj2);
            if (retVal != null) return retVal;
        }
        return null;

		/*// Calculate distance from the plain
		position = Vector3fExt.average(getVertexPositions());
		final Vector3fExt rawDistance = obj2.getPosition().getThisMinus(position);
		final float normalComponent = rawDistance.getDotProductWith(normal);
		final Vector3fExt projectedPos = rawDistance.getProjectedToSurface(normal, position);

		if (Math.abs(normalComponent) > obj2.getAverageSize()) {	// If object is not touching the plain of the polygon
			System.out.println("Object is NOT touching the plain of the polygon"); 
			return null;
		}
		System.out.println("Object IS touching the plain of the polygon");
		
		if (!isOnPlain(projectedPos)) {	// if object is touching the plain of the face, but it is not above the polygon
			System.out.println("Object is NOT above the polygon");
			return null;
		}
		System.out.println("Object IS above the polygon");
		
		// Collision case
		System.out.println("PolyFace collison case");
		final float collisionGap = obj2.getAverageSize() - normalComponent + GAP_SIZE;

		final Vector3fExt obj2V = obj2.getVelocity();
		final Vector3fExt obj2VRefl = obj2V.getReflectedFrom(normal);
		
		final Vector3fExt collForce = obj2VRefl.getThisMinus(obj2V).getMultipliedBy((1f/TIMESTEP) * obj2.getMass())
		    .getThisMinus(obj2.getForce());
				
		final Vector3fExt collPosition = obj2.getPosition().getSumWith(normal.getMultipliedBy(Math.abs(collisionGap)));
		final Vector3fExt collVelocity = obj2VRefl.getMultipliedBy(DEFAULT_COLLISION_REFLECTION_FACTOR);
		if (collVelocity.getLengthSquare() < COLLISION_V_TRIM_SQ) collVelocity.reset();
		
		retVal = new Vector3fExt[3];
		retVal[0] = collPosition;
		retVal[1] = collVelocity;
		retVal[2] = collForce;
		return retVal;*/
    }

    private boolean isOnPlain(Vector3fExt p2) {    // p2 in world space; TODO revive
        for (final Triangle triangle : triangles) {
            if (triangle.isOnPlain(p2)) return true;
        }
        return false;

        // Local radius method - approximate solution
		/*Vector3fExt centerPos = Vector3fExt.average(vertexPositions);
		Vector3fExt p2Projected = p2.getProjectedToSurface(centerPos, normal);
		Vector3fExt diffP2C = p2Projected.getThisMinus(centerPos);
		float distP2C = diffP2C.getLength(), distV1C, distV2C;
		diffP2C.normalize();
		Vector3fExt diffV1C, diffV2C;
		float cosAngle1, cosAngle2, cosAngleV2V1, localRadius;
		for (int i = 0; i < vertexPositions.size() - 1; i++) {
			
			diffV1C = vertexPositions.get(i).getThisMinus(centerPos);
			diffV2C = vertexPositions.get(i+1).getThisMinus(centerPos);
			distV1C = diffV1C.getLength();
			distV2C = diffV2C.getLength();
			diffV1C.normalize();
			diffV2C.normalize();
			
			cosAngle1 = diffV1C.getDotProductWith(diffP2C);
			cosAngle2 = diffV2C.getDotProductWith(diffP2C);
			cosAngleV2V1 = diffV1C.getDotProductWith(diffV2C);

			if (cosAngle1 > cosAngleV2V1 && cosAngle2 > cosAngleV2V1) {	// If point is on the sector
				localRadius = (float) (distV1C + (Math.acos(cosAngle1) / Math.acos(cosAngleV2V1)) * (distV2C - distV1C));
				if( distP2C <= localRadius ) return true;
			}
		}
		
		// Last minus first
		diffV1C = vertexPositions.get(vertexPositions.size()-1).getThisMinus(centerPos);
		diffV2C = vertexPositions.get(0).getThisMinus(centerPos);
		distV1C = diffV1C.getLength();
		distV2C = diffV2C.getLength();
		diffV1C.normalize();
		diffV2C.normalize();
		
		cosAngle1 = diffV1C.getDotProductWith(diffP2C);
		cosAngle2 = diffV2C.getDotProductWith(diffP2C);
		cosAngleV2V1 = diffV1C.getDotProductWith(diffV2C);

		if (cosAngle1 > cosAngleV2V1 && cosAngle2 > cosAngleV2V1) {	// If point is on the sector
			localRadius = (float) (distV1C + (Math.acos(cosAngle1) / Math.acos(cosAngleV2V1)) * (distV2C - distV1C));
			if( distP2C <= localRadius ) return true;
		}
		
		return false;*/

        // Sub-triangle side band method
		/*Vector3fExt centerPos = Vector3fExt.average(vertexPositions);
		Vector3fExt p2Projected = p2.getProjectedToSurface(centerPos, normal);
		Vector3fExt diffP2CSq = p2Projected.getThisMinus(centerPos).getNormalized();
		Vector3fExt diffV1CSq, diffV2CSq, diffV1P2Sq, diffV2P2Sq;
		for (int i = 0; i < vertexPositions.size() - 1; i++) {
			diffV1CSq = vertexPositions.get(i).getThisMinus(centerPos).getNormalized();
			diffV2CSq = vertexPositions.get(i+1).getThisMinus(centerPos).getNormalized();
			diffV1P2Sq = vertexPositions.get(i).getThisMinus(p2Projected).getNormalized();
			diffV2P2Sq = vertexPositions.get(i+1).getThisMinus(p2Projected).getNormalized();
			
			if (diffV1CSq.getDotProductWith(diffP2CSq) > 0	// cos should be >0 to be on the band
				&& diffV2CSq.getDotProductWith(diffP2CSq) > 0
				&& diffV1CSq.getDotProductWith(diffV1P2Sq) > 0
				&& diffV2CSq.getDotProductWith(diffV2P2Sq) > 0) {
				...
			}
		}*/

        // Center distance method
		/*Vector3fExt centerPos = Vector3fExt.average(vertexPositions);
		Vector3fExt p2Projected = p2.getProjectedToSurface(centerPos, normal);
		float diffP2CSq = p2Projected.getThisMinus(centerPos).getLengthSquare();
		float diffV1CSq, diffV2CSq, diffV1P2Sq, diffV2P2Sq;
		for (int i = 0; i < vertexPositions.size() - 1; i++) {
			diffV1CSq = vertexPositions.get(i).getThisMinus(centerPos).getLengthSquare();
			diffV2CSq = vertexPositions.get(i+1).getThisMinus(centerPos).getLengthSquare();
			diffV1P2Sq = vertexPositions.get(i).getThisMinus(p2Projected).getLengthSquare();
			diffV2P2Sq = vertexPositions.get(i+1).getThisMinus(p2Projected).getLengthSquare();
			if (diffV2P2Sq < diffV2CSq && diffP2CSq < diffV2CSq)
			...
		}
		...*/

        // Difference vector angle method
		/*Vector3fExt center = Vector3fExt.average(vertexPositions);
		Vector3fExt sectionVector, cDiffVector, p2DiffVector;
		float cSAngle, p2CAngle;
		//Vector3fExt intersectPoint;
		//Line cP2Line = new Line(center, p2);
		for (int i = 0; i < (vertexPositions.size() - 1); i++) {
			sectionVector = Vector3fExt.substract(vertexPositions.get(i+1), vertexPositions.get(i)).getNormalized();
			cDiffVector = center.getThisMinus(vertexPositions.get(i)).getNormalized();
			p2DiffVector = Vector3fExt.substract(p2, vertexPositions.get(i)).getNormalized();
			
			cSAngle = sectionVector.getAngleWith(cDiffVector);
			if (cSAngle > 0.5f*Vector3fExt.PI) cSAngle = Vector3fExt.PI - cSAngle;
			p2CAngle = p2DiffVector.getAngleWith(cDiffVector);
			
			if (p2CAngle > cSAngle) return false;
		}
		
		sectionVector = Vector3fExt.substract( vertexPositions.get(0), vertexPositions.get(vertexPositions.size()-1) ).getNormalized();
		cDiffVector = center.getThisMinus( vertexPositions.get(vertexPositions.size()-1) ).getNormalized();
		p2DiffVector = Vector3fExt.substract( p2, vertexPositions.get(vertexPositions.size()-1) ).getNormalized();
		
		cSAngle = sectionVector.getAngleWith(cDiffVector);
		if (cSAngle > 0.5f*Vector3fExt.PI) cSAngle = Vector3fExt.PI - cSAngle;
		p2CAngle = p2DiffVector.getAngleWith(cDiffVector);
		
		if (p2CAngle > cSAngle) return false;
		
		return true;*/

        // Line intersection method
		/*for (int i = 0; i < vertexPositions.size() - 1; i++) {
			intersectPoint = (new Line(vertexPositions.get(i+1),
			    vertexPositions.get(i)).getIntersectionPointWith(cP2Line);
			if (p2.getThisMinus(center).getLengthSquare() < p2.getThisMinus(intersectionPoint)
					|| p2.getThisMinus(center).getLengthSquare() < center.getThisMinus(intersectionPoint)) {
				return false;
			}
		}
		
		intersectPoint = getIntersectionPoint(new Line(vertexPositions.get(vertexPositions.size() - 1),
		    vertexPositions.get(0)), cP2Line);
		if (p2.getThisMinus(center).getLengthSquare() < p2.getThisMinus(intersectionPoint)
				|| p2.getThisMinus(center).getLengthSquare() < center.getThisMinus(intersectionPoint)) {
			return false;
		}*/
    }

    @Override
    public List<Vector3fExt> getShadowVertices(Vector3fExt lightParam, ShadowVolume.LIGHT_PARAM_TYPE lightType) {
        return getVertexPositions();
    }

    @Override
    public String toString() {
        final StringBuilder retStr = new StringBuilder("PolyFace id " + id + " vertices:");
        retStr.append("\n").append(Utils.toString(vertexPositions, Utils.TOSTRING_CLASS_NAMES.VECTOR3FEXT, 99,
                2));
        retStr.append("\n triangles: ");
        for (int i = 0; i < triangles.size(); i++) {
            retStr.append("\n").append(i).append(": ").append(triangles.get(i));
        }
        return retStr.toString();
    }
}

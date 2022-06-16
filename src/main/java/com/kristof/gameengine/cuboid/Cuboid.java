package com.kristof.gameengine.cuboid;

import com.kristof.gameengine.object3d.Object3d;
import com.kristof.gameengine.object3d.Object3dBO;
import com.kristof.gameengine.rectangle.Rectangle;
import com.kristof.gameengine.shadow.ShadowVolume;
import com.kristof.gameengine.util.ColorVector;
import com.kristof.gameengine.util.Material;
import com.kristof.gameengine.util.Vector3fExt;

import java.util.List;
import java.util.Vector;

public class Cuboid extends Object3d {
    private final float xSize;
    private final float ySize;
    private final float zSize;
    private final float diagonalLength;
    private final List<Rectangle> faces = new Vector<>();

    public Cuboid(Vector3fExt position, float polarAngle, float azimuthAngle, float rollAngle, Vector3fExt velocity,
                  Vector3fExt force, ColorVector color, Material material, int colorMapTexIndex, int normalMapTexIndex,
                  float a, float b, float c) {
        super(position, polarAngle, azimuthAngle, rollAngle, new Vector3fExt(a, b, c), velocity, force,
                color, material, colorMapTexIndex, normalMapTexIndex);
        xSize = a;
        ySize = b;
        zSize = c;
        diagonalLength = (new Vector3fExt(0.5f * a, 0.5f * b, 0.5f * c)).getLength();

        Rectangle tmpFace;
        float ap2 = 0.5f;
        float bp2 = 0.5f;
        float cp2 = 0.5f;    // size will do the scaling

        // Front face
        tmpFace = new Rectangle(new Vector3fExt(0, 0, cp2),0.5f * Vector3fExt.PI,
                1.5f * Vector3fExt.PI, 0.5f * Vector3fExt.PI, Vector3fExt.NULL_VECTOR,
                Vector3fExt.NULL_VECTOR, ColorVector.MAGENTA, Material.PLASTIC, -1, -1, a,
                c, true);
        tmpFace.setId(1);
        tmpFace.setNeighborIds(2, 5, 4, 6);
        faces.add(tmpFace);

        // Right face
        tmpFace = new Rectangle(new Vector3fExt(ap2, 0, 0), 0.5f * Vector3fExt.PI, 0,
                0.5f * Vector3fExt.PI, Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.MAGENTA,
                Material.PLASTIC, -1, -1, b, c, true);
        tmpFace.setId(2);
        tmpFace.setNeighborIds(3, 5, 1, 6);
        faces.add(tmpFace);

        // Back face
        tmpFace = new Rectangle(new Vector3fExt(0, 0, -cp2), 0.5f * Vector3fExt.PI,
                0.5f * Vector3fExt.PI, 0.5f * Vector3fExt.PI, Vector3fExt.NULL_VECTOR,
                Vector3fExt.NULL_VECTOR, ColorVector.MAGENTA, Material.PLASTIC, -1, -1, a, c, true);
        tmpFace.setId(3);
        tmpFace.setNeighborIds(4, 5, 2, 6);
        faces.add(tmpFace);

        // Left face
        tmpFace = new Rectangle(new Vector3fExt(-ap2, 0, 0), 0.5f * Vector3fExt.PI, Vector3fExt.PI,
                0.5f * Vector3fExt.PI, Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.MAGENTA,
                Material.PLASTIC, -1, -1, b, c, true);
        tmpFace.setId(4);
        tmpFace.setNeighborIds(1, 5, 3, 6);
        faces.add(tmpFace);

        // Top face
        tmpFace = new Rectangle(new Vector3fExt(0, bp2, 0), 0, 0, 0,
                Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.MAGENTA, Material.PLASTIC,
                -1, -1, a, c, true);
        tmpFace.setId(5);
        tmpFace.setNeighborIds(2, 3, 4, 1);
        faces.add(tmpFace);

        // Bottom face
        tmpFace = new Rectangle(new Vector3fExt(0, -bp2, 0), Vector3fExt.PI, 0, 0,
                Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.MAGENTA, Material.PLASTIC,
                -1, -1, a, c, true);
        tmpFace.setId(6);
        tmpFace.setNeighborIds(2, 1, 4, 3);
        faces.add(tmpFace);
    }

    @Override
    protected Object3dBO getGlBufferObject() {
        return CuboidBO.getInstance();
    }

    @Override
    public float getCollisionRadius() {
        return diagonalLength;
    }

    @Override
    public float getVolume() {
        return xSize * ySize * zSize;
    }

    @Override
    public Vector3fExt[] applyCollisionTo(Object3d obj2) {
        Vector3fExt[] retVal;
        for (final Rectangle face : faces) {
            retVal = face.getTransformed(modelMatrix, rotationMatrix).applyCollisionTo(obj2);
            if (retVal != null) return retVal;
        }
        return null;
    }

    @Override
    public List<Vector3fExt> getShadowVertices(Vector3fExt lightParam, ShadowVolume.LIGHT_PARAM_TYPE lightType) {   // TODO fix geometry
        // Transform faces with model matrix
        final List<Rectangle> facesTransformed = new Vector<>();
        Rectangle fTemp;
        for (final Rectangle face : faces) {
            fTemp = new Rectangle(face, true);
            fTemp.transform(modelMatrix, rotationMatrix);
            facesTransformed.add(fTemp);
        }

        // Get shadow edges
        final List<Vector3fExt[]> shadowEdgesMixed = new Vector<>();
        Vector3fExt diffLToVx = new Vector3fExt();    // vector pointing from light to a face vertex
        final List<Vector3fExt> diffsLToVx = new Vector<>();
        //int facingCounter = 0;
        for (int i = 0; i < facesTransformed.size(); i++) {
            final Rectangle currentFace = facesTransformed.get(i);
            diffLToVx = switch (lightType) {
                case LIGHT_DIRECTION -> lightParam.getCopy();
                case LIGHT_POSITION -> currentFace.getVertex(0).getThisMinus(lightParam);
            };
            diffsLToVx.add(diffLToVx);
            if (currentFace.isFacingInwardVector(diffLToVx)) {    // check a facing face
                //facingCounter++;
                //for( int j = 0 ; j < currentFace.getNumNeighbors() ; j++ ) {	// check every neighbor for isFacing
                for (int k = 0; k < facesTransformed.size(); k++) {
                    if (k != i) {
                        //facingCounter++;
                        final Rectangle otherFace = facesTransformed.get(k);
                        //if( currentFace.isNeighbor( otherFace.getId() ) && !otherFace.isFacingInwardVector( diffLToVx ) ) {
                        if (currentFace.isNeighbor(otherFace.getId())) {
                            //facingCounter++;
                            diffLToVx = switch (lightType) {
                                case LIGHT_DIRECTION -> lightParam.getCopy();
                                case LIGHT_POSITION -> otherFace.getVertex(0).getThisMinus(lightParam);
                            };
                            if (!otherFace.isFacingInwardVector(diffLToVx)) {
                                //facingCounter++;
                                //LOGGER.debug("Neighboring faces:\n" + currentFace.getId() + " - " + currentFace + ",\n" + otherFace.getId() + " - " + otherFace + "\n");
                                shadowEdgesMixed.add(currentFace.getCommonEdgeWith(otherFace));    // TODO fix
                            }
                        }
                    }
                }
                //}
            }
        }
        // now we have all the shadow edges mixed

        //LOGGER.debug("shadowEdgesMixed:\n" + CWBRenderer.toString(shadowEdgesMixed, TOSTRING_CLASS_NAMES.VECTOR3FM_ARRAY, 1, 2));
        final List<Vector3fExt> faceNormals = new Vector<>();
        for (final Rectangle rectangle : facesTransformed) {
            faceNormals.add(rectangle.getNormal());
        }

        // Sort shadow edges
		/*
		List<Vector3fM[]> shadowEdgesSequenced = new Vector<List>();
		Vector3fM[] currentEdge;
		for (int i = 0; i < shadowEdgesMixed.size(); i++) {
			if (shadowEdgesSequenced.size() == 0) {
				shadowEdgesSequenced.add(shadowEdgesMixed.get(i));
				//Log.e("shadowEdgesSeqd", SRSurfaceView.VectorVector3fMArrayToString(shadowEdgesSequenced, 10, 10));
				//Log.e("shadowEdgesMixed", SRSurfaceView.VectorVector3fMArrayToString(shadowEdgesMixed, 10, 10));
			} else {
				// check if edge is already in or not
				currentEdge = shadowEdgesMixed.get(i);
				boolean isAlreadyIn = false;
				//Log.e("currentEdge", SRSurfaceView.arrayToString(currentEdge, 10));
				for( int j = 0 ; j < shadowEdgesSequenced.size() ; j++ ) {
					//Log.e("shadowEdgesSeqd", SRSurfaceView.VectorVector3fMArrayToString(shadowEdgesSequenced, 10, 10));
					if( Vector3fM.arrayEquals(shadowEdgesSequenced.get(j), currentEdge) ) {
						isAlreadyIn = true;
					}
				}
				
				if( !isAlreadyIn ) {	// if current edge is not in yet, put it in its place in the sequence
					// look for its connecting edges
					for( int j = 0 ; j < shadowEdgesSequenced.size() ; j++ ) {
						if( Vector3fM.hasCommonVertex(shadowEdgesSequenced.get(j), currentEdge) ) {	// if found connecting edge, insert current edge
							shadowEdgesSequenced.add(j+1, currentEdge);
							j++;
						} else {	// no connecting edge found => insert it to end
							shadowEdgesSequenced.add(currentEdge);
						}
					}
				}
			}
		}// now we have all the shadow edges in sequence
		*/

        // Get shadow vertices
        final List<Vector3fExt> shVsMixed = new Vector<>();
        Vector3fExt currentVertex;
        boolean isAlreadyIn = false;
		/*if( ! Vector3fM.hasCommonVertex(shadowEdgesSequenced.get(0), shadowEdgesSequenced.get( shadowEdgesSequenced.size() - 1 ) ) ) {
			// if not closed, there is an error => return empty vertex set
			//return new Vector<Vector3fM>();
		} else {*/

        // Gather vertices without duplicates
        //for( int i = 0 ; i < shadowEdgesSequenced.size() ; i++ ) {
        for (final Vector3fExt[] vector3FExts : shadowEdgesMixed) {
            for (int j = 0; j < 2; j++) {    // 2 vertices per edge
                //currentVertex = shadowEdgesSequenced.get(i)[j];
                currentVertex = vector3FExts[j];
                isAlreadyIn = false;
                for (Vector3fExt vector3FExt : shVsMixed) {
                    //LOGGER.debug("shadowEdgesMixed.get(i)[j]: " + shadowEdgesMixed.get(i)[j]);
                    if (vector3FExt.equals(currentVertex)) {
                        isAlreadyIn = true;
                        break;
                    }
                }
                if (!isAlreadyIn) {
                    shVsMixed.add(currentVertex);
                }
            }
        }
        //}
        // Now we have all the shadow vertices mixed

        // Sort vertices in CCW from the point of light source
        final Vector3fExt diffCToL = switch (lightType) {
            case LIGHT_DIRECTION -> lightParam.getReverse();
            case LIGHT_POSITION -> lightParam.getThisMinus(position).getNormalized();
        };

        final List<Vector3fExt> shVsProjd = new Vector<>();
        final Vector3fExt fCRight, fCUp;

        if (diffCToL.equals(Vector3fExt.Y_UNIT_VECTOR)) {
            fCRight = diffCToL.getCrossProductWith(Vector3fExt.Z_UNIT_VECTOR.getReverse()).getNormalized();
        } else {
            fCRight = diffCToL.getCrossProductWith(Vector3fExt.Y_UNIT_VECTOR).getNormalized();
        }
        fCUp = diffCToL.getCrossProductWith(fCRight).getNormalized();

        Vector3fExt pVrTemp;
        for (final Vector3fExt vector3FExt : shVsMixed) {
            pVrTemp = vector3FExt.getThisMinus(position);
            shVsProjd.add(new Vector3fExt(pVrTemp.getDotProductWith(fCRight), pVrTemp.getDotProductWith(fCUp), 0));
			/*shVsProjd.add( Vector3fM.add(
					fCRight.getMultipliedBy( pVrTemp.getDotProductWith(fCRight) ),
					fCUp.getMultipliedBy( pVrTemp.getDotProductWith(fCUp) ) ) );*/
        }

        final List<Float> angles = new Vector<>();
        //angles.add(0f); no need, we check the min for the first not shPos1 vertex
        final Vector3fExt shPosMixed1 = shVsMixed.get(0);
        final Vector3fExt shPosProjd1 = shVsProjd.get(0);

        float mxProd, dotProd, crossProdSq, angle1;
        for (int i = 1; i < shVsProjd.size(); i++) {
            //crossProdSq = shVsProjd.get(i).getCrossProductWith(shPosProjd1).getVectorAbsSquare();
            //if( crossProdSq < Vector3fM.EQUALITY_RANGE_SQ ) {	// opposite vectors (identical is not possible since there are no duplicates)
            if (shVsProjd.get(i).getSumWith(shPosProjd1).getLengthSquare() < Vector3fExt.EQUALITY_RANGE_SQ) {
                angle1 = Vector3fExt.PI;
            } else {
                mxProd = Vector3fExt.mixedProduct(Vector3fExt.Z_UNIT_VECTOR, shVsProjd.get(i), shPosProjd1);
                if (mxProd < 0) {
                    angle1 = (float) Math.acos(Vector3fExt.dotProduct(shVsProjd.get(i).getNormalized(), shPosProjd1.getNormalized()));
                } else {    // mxProd > 0; pointing downwards
                    angle1 = 2 * Vector3fExt.PI - (float) Math.acos(Vector3fExt.dotProduct(shVsProjd.get(i).getNormalized(), shPosProjd1.getNormalized()));
                    //angle1 = 0.25f*FLOAT_PI - (float)Math.acos( Vector3fM.dotProduct(shVsProjd.get(i).getNormalized(), shPosProjd1.getNormalized()) );
                }
            }
            //if( Math.abs(angle1) < Vector3fM.EQUALITY_RANGE )angle1 = FLOAT_PI;	// TO DO find why does 0 angle occur and write the code normally
            angles.add(angle1);
        }

        // Convert to degrees for display
        final List<Float> anglesInDegrees = new Vector<>();
        for (final Float angle : angles) {
            anglesInDegrees.add((float) Math.toDegrees(angle));
        }

        // Sort vertices according to angles
        final List<Vector3fExt> shVsCCW = new Vector<>();
        try {
            shVsCCW.add(shVsMixed.get(0));
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        while (angles.size() != 0) {
            int minIndex = 0;
            for (int i = 0; i < angles.size(); i++) {
                if (angles.get(i) < angles.get(minIndex)) {
                    minIndex = i;
                }
            }
            // Now we have the index of the smallest angle
            shVsCCW.add(shVsMixed.remove(minIndex + 1));
            angles.remove(minIndex);
        }

		/*// Transform by model matrix
		List<Vector3fM> shVsTransd = new Vector<>();
		for( int i = 0 ; i < shVsCCW.size() ; i++ ) {
			shVsTransd.add( shVsCCW.get(i).getMultipliedBy(modelMatrix) );
		}*/

		/*if(shVsMixed.size() == 0) {
			Log.e("shadowEdgesMixed.size()", "" + shadowEdgesMixed.size() );
			//Log.e("shadowEdgesSequenced.size()", "" + shadowEdgesSequenced.size() );
		}*/

        if (shVsCCW.size() > 0) {
            return shVsCCW;
            //return shVsTransd;
            //return shVsMixed;
        } else {
            final List<Vector3fExt> shVs2 = new Vector<>();
            shVs2.add(new Vector3fExt(-1f, 1f, 1f));
            shVs2.add(new Vector3fExt(1f, 1f, 1f));
            shVs2.add(new Vector3fExt(0, 1f, -1f));
            return shVs2;
        }
    }
}

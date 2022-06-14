package com.kristof.gameengine.heightmap;

import com.kristof.gameengine.object3d.ShortObject3dBO;
import com.kristof.gameengine.util.Vector3fExt;

import org.lwjgl.opengl.GL11;

public class HeightMapSeamlessBO extends ShortObject3dBO {
    protected static final int A_DETAIL = 20, B_DETAIL = 20;    // Number of tiles

    protected float a, b;
    protected HeightFunction hMFunc;

    public HeightMapSeamlessBO() {
    }

    @Override
    public void fillVertexAttribLists() {
        a = A_SIZE;
        b = B_SIZE;
        //hMFunc = HeightFunc.SIN_COS_FUNC; // TODO to init param
        hMFunc = HeightFunction.PLAIN_FUNC;

        float h, xTemp, yTemp;
        final float apd = a / ((float) A_DETAIL), bpd = b / ((float) B_DETAIL);
        Vector3fExt posTemp, norTemp;
        int rowInsertNum = 0;

        for (int i = 0; i < (B_DETAIL + 1); i++) {
            if ((i != 0) && (i != B_DETAIL)) {    // Repeat inner vertices for different texture coords
                rowInsertNum = 2;
            } else {
                rowInsertNum = 1;
            }
            for (int k = 0; k < rowInsertNum; k++) {
                for (int j = 0; j < (A_DETAIL + 1); j++) {
                    xTemp = -0.5f * a + j * apd;
                    yTemp = -0.5f * b + i * bpd;
                    h = 10f * hMFunc.height(xTemp, yTemp);
                    posTemp = Vector3fExt.add(
                            Vector3fExt.X_UNIT_VECTOR.getMultipliedBy(xTemp),
                            Vector3fExt.Y_UNIT_VECTOR.getMultipliedBy(h),
                            Vector3fExt.Z_UNIT_VECTOR.getMultipliedBy(-yTemp));
                    positions.add(posTemp);
                    norTemp = Vector3fExt.normalFuncWithNeighbors(xTemp, yTemp, hMFunc);
                    norTemp = new Vector3fExt(norTemp.getX(), norTemp.getZ(), -norTemp.getY());
                    normals.add(norTemp);
                    tangents.add(Vector3fExt.X_UNIT_VECTOR);
                    if ((j != 0) && (j != A_DETAIL)) {    // repeat inner vertices for different texture coords
                        positions.add(posTemp);
                        normals.add(Vector3fExt.Y_UNIT_VECTOR);
                        tangents.add(Vector3fExt.X_UNIT_VECTOR);
                    }
                }
            }
        }

        final int vertexCountPerRow = 2 * A_DETAIL;//2*(detailLevel - 1) + 2;
        final int vertexCountPerCol = 2 * B_DETAIL;
        for (int i = 0; i < vertexCountPerCol; i++) {
            for (int j = 0; j < vertexCountPerRow; j++) {
                texCoords.add(new float[]{j % 2, 1f - i % 2});
            }
        }
    }

    protected void fillIndices() {
        /*
         * #tiles in a row/col: detailLevel
         * #vertices in a row/col: detailLevel + 1
         */
        int vertexCountPerRow = 2 * A_DETAIL;//2*(detailLevel - 1) + 2;
        //int vertexCountPerCol = 2 * B_DETAIL;
        for (int i = 0; i < B_DETAIL; i++) {    // numVerticesPerRC - 1
            for (int j = 0; j < vertexCountPerRow; j++) {
                if (((j % 2) == 0) && !(j == 0 && i == 0)) {
                    indices.add((short) ((2 * i * vertexCountPerRow) + j));// + (2*i* B_DETAIL) ) );
                }
                indices.add((short) ((2 * i * vertexCountPerRow) + j));// + (2*i* B_DETAIL)) );
                indices.add((short) (((2 * i + 1) * vertexCountPerRow) + j));//+ (2*i* B_DETAIL) ) );
                if ((j % 2) == 1 && !(j == (vertexCountPerRow - 1) && i == (B_DETAIL - 1))) {
                    indices.add((short) (((2 * i + 1) * vertexCountPerRow) + j));//+ (2*i* B_DETAIL) ) );
                }
            }
        }
    }

    @Override
    protected void drawElements() {
        GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, indices.size(), GL11.GL_UNSIGNED_SHORT, 0);
    }

    public int getShadowVertexCount() {
        return 4;
    }

	/*@Override TODO revive
	public ArrayList<Vector3fM> getShadowVertices(Vector3fM lightPos) {

		ArrayList<Vector3fM> shVxPos = new ArrayList<Vector3fM>();
		float ap2 = 0.5f*a;
		
		shVxPos.add( Vector3fM.add(
				right.getMultipliedBy(-ap2),
				up.getMultipliedBy(-ap2) ) );
		shVxPos.add( Vector3fM.add(
				right.getMultipliedBy(ap2),
				up.getMultipliedBy(-ap2) ) );
		shVxPos.add( Vector3fM.add(
				right.getMultipliedBy(ap2),
				up.getMultipliedBy(ap2) ) );
		shVxPos.add( Vector3fM.add(
				right.getMultipliedBy(-ap2),
				up.getMultipliedBy(ap2) ) );
		
		return shVxPos;
	}*/

	/*@Override TODO revive
	public void applyCollisionAfterRFilter(Actor3D act2) {
		
		//message = "beginning collision detection";
		//message = message + 'I';
		
		Vector3fM diffObjectPlane = Vector3fM.substract(act2.getCenter(), center); 
		float objCollR = act2.getCollisionRadius();
		
		float normalComp = Vector3fM.dotProduct( diffObjectPlane, normal);
		float rightComp = Vector3fM.dotProduct( diffObjectPlane, right);
		float upComp = Vector3fM.dotProduct( diffObjectPlane, up);
		
		float locH = hMFunc.heightFunc(rightComp, upComp);
		
		boolean cond1 = Math.abs(normalComp - locH) <= (objCollR);
				
		boolean cond2 = Math.abs(rightComp) < (0.5f*a);
			
		boolean cond3 = Math.abs(upComp) < (0.5f*b);

				
		if( cond1  && cond2 && cond3 ) {
				
			float vNormalComp = Vector3fM.dotProduct( act2.getOldVelocity(), normal);
			//message = "vNormalComp = " + vNormalComp;
			message = "HMap collided";
			
			// lying/rolling on the surface case
			if( Math.abs(vNormalComp) < Object3DSpace.COLLISION_V_THRESHOLD ){
				
				act2.setMidAir(false);
				
				// stopping object
				act2.resetOldVelocity();
							
				// applying counter-force caused by surface
				Vector3fM f2 = act2.getForce();
				float fNormalComp = Vector3fM.dotProduct(f2, normal);
				if(fNormalComp < 0) {
					f2.substract( Vector3fM.multiply(normal, fNormalComp) );
					act2.setForce(f2);
				}
						
						
				// put it back in exact proper just-touch distance from surface 
				if(normalComp >= locH) {
					act2.translate( Vector3fM.multiply(normal, (objCollR - normalComp + locH - Object3DSpace.GAP_SIZE) ) );
				} else {
					act2.translate( Vector3fM.multiply(normal.getReverse(), (objCollR + normalComp - locH - Object3DSpace.GAP_SIZE) ) );
				}
				

				//message = message + 'I';
				Messenger.collisionMessage = "surfacing/rolling detected";
						
				return;
			}
					
					
					
				// velocity refraction case
					
				float ovrn = Vector3fM.dotProduct(act2.getOldVelocity(), normal);
				float ovrr = Vector3fM.dotProduct(act2.getOldVelocity(), right);
				float ovru = Vector3fM.dotProduct(act2.getOldVelocity(), up);
					
				Vector3fM cVNormalComp = Vector3fM.multiply( normal, -ovrn*Object3DSpace.COLLISION_ELASTICITY );
					
				Vector3fM collV = Vector3fM.add(
						cVNormalComp,
						Vector3fM.multiply( right, ovrr ),
						Vector3fM.multiply( up, ovru ) );
					
					
				// put it back in just-touch distance from surface
				if(normalComp >= locH) {
					act2.translate( Vector3fM.multiply(normal, (objCollR - normalComp + locH - Object3DSpace.GAP_SIZE) ) );
				} else {
					act2.translate( Vector3fM.multiply(normal.getReverse(), (objCollR + normalComp - locH - Object3DSpace.GAP_SIZE) ) );
				}
					
					
				// setting collision velocity
				act2.setOldVelocity(collV);
				
					
					
				//message = message + "1";
				Messenger.collisionMessage = "v collision detected";
					
				return;
			}
				
				
			//message = ""
					//+ "no collision detected";
					//+ "collided, F = " + obj2.getForce().getRounded(2)
					//+ "\ncollV = " + collV.getRounded(2);


	}*/
}

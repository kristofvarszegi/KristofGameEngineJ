package com.kristof.gameengine.object3d;

import com.kristof.gameengine.shadow.ShadowVolume;
import com.kristof.gameengine.triangle.Triangle;
import com.kristof.gameengine.util.ColorVector;
import com.kristof.gameengine.util.Material;
import com.kristof.gameengine.util.Matrix4f;
import com.kristof.gameengine.util.Vector3fExt;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class OBJObject3dBO extends ShortObject3dBO {
    private List<Triangle> triangleFaces;
    private List<Edge> edges;
    private boolean isSmooth = false;   // TODO to init params

    public OBJObject3dBO(String fileName, float normScale, float scale) {
        super(fileName, normScale, scale);
    }

    @Override
    protected void fillVertexAttribLists() {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(fileName));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }

        // Get the line count
        int lineCount = -1;
        try {
            LineNumberReader lnr = new LineNumberReader(new FileReader(new File(fileName)));
            lnr.skip(Long.MAX_VALUE);
            lineCount = lnr.getLineNumber();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        List<Vector3fExt> positionsFromOBJ = new Vector<>();
        List<Vector3fExt> normalsFromOBJ = new Vector<>();
        List<float[]> texCoordsFromOBJ = new Vector<>();
        List<OBJFaceIndexData> faces = new Vector<>();
        List<Vector3fExt> faceNormals = new Vector<>();

        String line;
        String[] tokens, fTokens1, fTokens2, fTokens3;
        Vector3fExt posTemp;
        OBJFaceIndexData faceTemp;

        final long startTime = System.nanoTime();
        int currentLine = 0;

        int progress = 0;
        try {
            while ((line = br.readLine()) != null) {
                currentLine++;

                int totalSize = positionsFromOBJ.size() + normalsFromOBJ.size() + faces.size();
                progress = (int) (100f * ((float) totalSize / lineCount));//25211f));

                tokens = line.split("[ ]");
                if (line.startsWith("v ")) {    // vertex position
		            posTemp = new Vector3fExt(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), -Float.parseFloat(tokens[3]));
                    positionsFromOBJ.add(posTemp);
                } else {    // normal
                    if (line.startsWith("vn ")) {
                        normalsFromOBJ.add(new Vector3fExt(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]),
                                Float.parseFloat(tokens[3])));
                    } else {    // triangular face indices
                        if (line.startsWith("vt ")) {    // texture coordinate
                            final float[] txCTemp = new float[2];
                            txCTemp[0] = Float.parseFloat(tokens[1]);
                            txCTemp[1] = 1f - Float.parseFloat(tokens[2]);
                            texCoordsFromOBJ.add(txCTemp);
                        } else {
                            if (line.startsWith("s ")) {    // s is the flag of smooth/not smooth shading
                                if (Objects.equals(tokens[1], "on")) isSmooth = true;
                            } else {
                                if (line.startsWith("f ")) {    // at this point we have all the positionsFromOBJ and normals
									fTokens1 = tokens[1].split("[//]");    // results in 3 tokens where middle one is "" (empty string)
                                    fTokens2 = tokens[2].split("[//]");
                                    fTokens3 = tokens[3].split("[//]");
                                    faceTemp = new OBJFaceIndexData(
                                            (short) (Short.parseShort(fTokens1[0]) - 1),    // position indices
                                            (short) (Short.parseShort(fTokens2[0]) - 1),
                                            (short) (Short.parseShort(fTokens3[0]) - 1),
                                            (short) (Short.parseShort(fTokens1[2]) - 1),    // normal indices
                                            (short) (Short.parseShort(fTokens2[2]) - 1),
                                            (short) (Short.parseShort(fTokens3[2]) - 1),
                                            (short) (Short.parseShort(fTokens1[1]) - 1),    // texture indices
                                            (short) (Short.parseShort(fTokens2[1]) - 1),
                                            (short) (Short.parseShort(fTokens3[1]) - 1));
                                    faces.add(faceTemp);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Fill texture coordinates as dummy for now
		/*for( int i = 0 ; i < positionsFromOBJ.size() ; i++ ) {
			//tangents.add(Vector3fM.NULL_VECTOR);
			texCoords.add(new float[2]);
		}*/

        // Build v a lists by faces
        List<Short> vis, nis, tcis;
        Vector3fExt tanTemp;
        for (final OBJFaceIndexData face : faces) {
            // TODO check list sizes
            vis = face.getVertexIndices();
            nis = face.getNormalIndices();
            tcis = face.getTexCoordIndices();
            positions.add(positionsFromOBJ.get(vis.get(0)).getMultipliedBy(normScale));
            positions.add(positionsFromOBJ.get(vis.get(1)).getMultipliedBy(normScale));
            positions.add(positionsFromOBJ.get(vis.get(2)).getMultipliedBy(normScale));

            normals.add(normalsFromOBJ.get(nis.get(0)));
            normals.add(normalsFromOBJ.get(nis.get(1)));
            normals.add(normalsFromOBJ.get(nis.get(2)));

            tanTemp = Vector3fExt.substract(positions.get(positions.size() - 2), positions.get(positions.size() - 3)).getXZ().getNormalized();
            tangents.add(tanTemp);
            tangents.add(tanTemp);
            tangents.add(tanTemp);

            texCoords.add(texCoordsFromOBJ.get(tcis.get(0)));
            texCoords.add(texCoordsFromOBJ.get(tcis.get(1)));
            texCoords.add(texCoordsFromOBJ.get(tcis.get(2)));
        }

        // Get vertex normals
        short normalIndexTemp;
        boolean isFaceFound;
        for (int i = 0; i < positionsFromOBJ.size(); i++) {    // for all vertices
            isFaceFound = false;
            for (int j = 0; j < faces.size() && !isFaceFound; j++) {    // run through all faces
                normalIndexTemp = faces.get(j).getNormalOfVertex(i);
                if (normalIndexTemp >= 0) {
                    normals.add(normalsFromOBJ.get(normalIndexTemp));
                    isFaceFound = true;
                }
            }

            progress = (int) (100f * (float) (i + 1) / (float) positionsFromOBJ.size());
        }

        // Get edges
        final List<Edge> doubleEdges = new Vector<Edge>();
        triangleFaces = new Vector<Triangle>();

        // Get edges of all faces
        for (int i = 0; i < faces.size(); i++) {    // for all index trios (indices.size() / 3)
            faceTemp = faces.get(i);
            List<Short> vIs = faceTemp.getVertexIndices();
            List<Short> nIs = faceTemp.getNormalIndices();
            Vector3fExt faceNormal = Vector3fExt.crossProduct(
                    positionsFromOBJ.get(vIs.get(1)).getThisMinus(positionsFromOBJ.get(vIs.get(0))),
                    positionsFromOBJ.get(vIs.get(2)).getThisMinus(positionsFromOBJ.get(vIs.get(0)))).getNormalized();

            triangleFaces.add(new Triangle(0, 0, 0, Vector3fExt.NULL_VECTOR,
                    Vector3fExt.NULL_VECTOR, ColorVector.CYAN, Material.CHROME, -1, -1,
                    positionsFromOBJ.get(vIs.get(0)), positionsFromOBJ.get(vIs.get(1)),
                    positionsFromOBJ.get(vIs.get(2))));

            doubleEdges.add(new Edge(positionsFromOBJ.get(vIs.get(0)), positionsFromOBJ.get(vIs.get(1)), faceNormal));
            doubleEdges.add(new Edge(positionsFromOBJ.get(vIs.get(1)), positionsFromOBJ.get(vIs.get(2)), faceNormal));
            doubleEdges.add(new Edge(positionsFromOBJ.get(vIs.get(2)), positionsFromOBJ.get(vIs.get(0)), faceNormal));

            progress = (int) (100f * (float) (i + 1) / (float) faces.size());
        }

        // Merge Edges
        final List<Edge> mergedEdges = new Vector<>();
        Edge edgeTemp = new Edge();
        boolean isEdgeMerged = false;
        while (doubleEdges.size() > 0) {
            edgeTemp = doubleEdges.remove(0);
            isEdgeMerged = false;
            for (int j = 0; j < doubleEdges.size() && !isEdgeMerged; j++) {
                if (edgeTemp.hasSameVertices(doubleEdges.get(j))) {
                    edgeTemp.mergeNormals(doubleEdges.remove(j));
                    mergedEdges.add(edgeTemp);
                    isEdgeMerged = true;
                }
            }
        }
        edges = new Vector<>(mergedEdges);

        // Get max radius
        posTemp = new Vector3fExt();
        for (final Vector3fExt position : positions) {
            if (position.getLengthSquare() > posTemp.getLengthSquare()) {
                posTemp = position.getCopy();
            }
        }

        size = posTemp.getLength();

        final long endTime = System.nanoTime();

        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void fillIndices() {
        for (short i = 0; i < positions.size(); i++) {
            indices.add(i);
        }
    }

    @Override
    protected void drawElements() {
        GL11.glDrawElements(GL11.GL_TRIANGLES, indices.size(), GL11.GL_UNSIGNED_SHORT, 0);
    }

    public Vector3fExt[] applyCollision(Object3d obj2, float scale, Matrix4f modelMatrix, Matrix4f rotationMatrix) { //Vector3fM position,	float polarAngle, float azimuthAngle, float rollAngle, Vector3fM scale) {
        if (obj2.getPosition().getThisMinus(Vector3fExt.NULL_VECTOR.getMultipliedBy(modelMatrix)).getLengthSquare()
                > (size * scale + obj2.getCollisionRadius()) * (size * scale + obj2.getCollisionRadius())) {
            return null;
        }    // TODO find out scale metrics

        Vector3fExt[] retVal;

        for (final Triangle triangleFace : triangleFaces) {
            retVal = triangleFace.getTransformed(modelMatrix, rotationMatrix).applyCollisionTo(obj2);
            if (retVal != null) return retVal;
        }

        // Testing with top
        return null;
    }

    public List<Vector3fExt> getShadowVertices(Vector3fExt lightParam, ShadowVolume.LIGHT_PARAM_TYPE lightType,
                                                 Matrix4f modelMatrix, Matrix4f rotationMatrix) {   // TODO revive
        // Transform edges with model matrix
        final List<Edge> edgesTransformed = new Vector<>();
        Edge eTemp;
        for (final Edge value : edges) {
            eTemp = new Edge(value);
            eTemp.transform(modelMatrix, rotationMatrix);
            edgesTransformed.add(eTemp);
        }

        final List<Edge> shadowEdgesMixed = new Vector<>();
        for (final Edge value : edgesTransformed) {
            eTemp = value;
            if (eTemp.isShadowEdge(lightParam)) {
                shadowEdgesMixed.add(eTemp);
            }
        }

        // Get shadow vertices
        final List<Vector3fExt> shVsMixed = new Vector<>();
        Vector3fExt currentVertex;
        boolean isAlreadyIn;

        // Gather vertices without duplicates
        for (final Edge edge : shadowEdgesMixed) {
            for (int j = 0; j < 2; j++) {    // 2 vertices per edge
                currentVertex = edge.getVertex(j);
                isAlreadyIn = false;
                for (final Vector3fExt vector3FExt : shVsMixed) {
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
        // Now we have all the shadow vertices mixed

        // Sort vertices in CCW from the point of light source
        final Vector3fExt diffCToL = lightParam.getThisMinus(Vector3fExt.NULL_VECTOR.getMultipliedBy(modelMatrix)).getNormalized();
        final List<Vector3fExt> shVsProjd = new Vector<>();
        Vector3fExt fCRight, fCUp;
        if (diffCToL.equals(Vector3fExt.Y_UNIT_VECTOR)) {
            fCRight = diffCToL.getCrossProductWith(Vector3fExt.Z_UNIT_VECTOR.getReverse()).getNormalized();
        } else {
            fCRight = diffCToL.getCrossProductWith(Vector3fExt.Y_UNIT_VECTOR).getNormalized();
        }
        fCUp = diffCToL.getCrossProductWith(fCRight).getNormalized();

        Vector3fExt pVrTemp;
        for (final Vector3fExt vector3FExt : shVsMixed) {
            pVrTemp = vector3FExt.getThisMinus(Vector3fExt.NULL_VECTOR.getMultipliedBy(modelMatrix));
            shVsProjd.add(new Vector3fExt(pVrTemp.getDotProductWith(fCRight), pVrTemp.getDotProductWith(fCUp),
                    0));
        }

        final List<Float> angles = new Vector<>();
        Vector3fExt shPosMixed1 = Vector3fExt.NULL_VECTOR.getCopy();
        Vector3fExt shPosProjd1 = Vector3fExt.NULL_VECTOR.getCopy();

        try {
            shPosMixed1 = shVsMixed.get(0);
            shPosProjd1 = shVsProjd.get(0);
        } catch (IndexOutOfBoundsException e) {
            // TODO handle
        }

        float mxProd, dotProd, crossProdSq, angle1;

        for (int i = 1; i < shVsProjd.size(); i++) {
            if (shVsProjd.get(i).getSumWith(shPosProjd1).getLengthSquare() < Vector3fExt.EQUALITY_RANGE_SQ) {
                angle1 = Vector3fExt.PI;
            } else {
                mxProd = Vector3fExt.mixedProduct(Vector3fExt.Z_UNIT_VECTOR, shVsProjd.get(i), shPosProjd1);
                if (mxProd < 0) {
                    angle1 = (float) Math.acos(Vector3fExt.dotProduct(shVsProjd.get(i).getNormalized(),
                            shPosProjd1.getNormalized()));
                } else {    // mxProd > 0; pointing downwards
                    angle1 = 2 * Vector3fExt.PI - (float) Math.acos(Vector3fExt.dotProduct(shVsProjd.get(i).getNormalized(),
                            shPosProjd1.getNormalized()));
                }
            }
            angles.add(angle1);
        }

        // Convert to degrees for display
        final List<Float> anglesInDegrees = new Vector<>();
        for (final Float angle : angles) {
            anglesInDegrees.add((float) Math.toDegrees(angle));
        }

        // Sort vertices according to angles
        List<Vector3fExt> shVsCCW = new Vector<>();
        try {
            shVsCCW.add(shVsMixed.get(0));
        } catch (IndexOutOfBoundsException e) {
            // TODO handle
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

        if (shVsCCW.size() > 0) {
            return shVsCCW;
        } else {
            final List<Vector3fExt> shVs2 = new Vector<>();
            shVs2.add(new Vector3fExt(-1f, 1f, 1f));
            shVs2.add(new Vector3fExt(1f, 1f, 1f));
            shVs2.add(new Vector3fExt(0, 1f, -1f));
            return shVs2;
        }
    }
}

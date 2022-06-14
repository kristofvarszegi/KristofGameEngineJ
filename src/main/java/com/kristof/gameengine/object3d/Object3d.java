package com.kristof.gameengine.object3d;

import com.kristof.gameengine.skybox.SkyBoxBO;
import com.kristof.gameengine.util.ColorVector;
import com.kristof.gameengine.util.Material;
import com.kristof.gameengine.util.Matrix4f;
import com.kristof.gameengine.util.Vector3fExt;
import com.kristof.gameengine.cuboid.CuboidBO;
import com.kristof.gameengine.heightmap.HeightMapSeamlessBO;

import com.kristof.gameengine.screen.ScreenQuadBO;
import com.kristof.gameengine.shadow.ShadowVolume;
import com.kristof.gameengine.shadow.ShadowVolumeBO;
import com.kristof.gameengine.sphere.SphereBO;

import java.util.List;
import java.util.Vector;

import static com.kristof.gameengine.engine.EngineMiscConstants.FORCED_FPS;
import static com.kristof.gameengine.util.Utils.exitOnGLError;

public abstract class Object3d {
    public static final float TIMESTEP = 1f / (float) FORCED_FPS;
    public static final float GAP_SIZE = 0.0001f;//0.000001f;
    public static final float GRAVITATIONAL_CONSTANT = 6.67384f * (float) Math.pow(10, -11);
    public static final float GRAVITY_ACCEL_EARTH = 9.80665f;    // m / s^2
    public static final float DEFAULT_STIFFNESS = (float) Math.pow(10, 15);
    public static final float DEFAULT_VISCOSITY = 3.66116f * (float) Math.pow(10, 14);
    public static final float CHILD_STIFFNESS = (float) Math.pow(10, 15);//2500;// beta^2 = 4mk critical damping
    public static final float CHILD_VISCOSITY = 3.66116f * (float) Math.pow(10, 14);//100;// T = 2m / beta
    public static final float DEFAULT_COLLISION_REFL_FACTOR = 0.3f;
    public static final float DEFAULT_SURFACE_FRICTION_COEFF = 0.5f;
    public static final float MIN_V_TRIM_SQ = 0.001f * 0.001f;
    public static final float COLLISION_V_TRIM = 0.25f;//1.5f;
    public static final float COLLISION_V_TRIM_SQ = COLLISION_V_TRIM * COLLISION_V_TRIM;
    public static final float COLLISION_VISCOSITY = 2 * 10000000000000000f;

    public enum PROTOTYPE_NAMES {CUBOID, H_MAP_S, HAND, SKYBOX, SPHERE, SCREENQUAD, OBJ_TEST, OBJ_BLOB}

    protected Vector3fExt position;
    protected Vector3fExt velocity;
    protected Vector3fExt force;
    protected Vector3fExt prevPosition;
    protected Vector3fExt prevVelocity;
    protected Vector3fExt prevForce;
    protected Vector3fExt centerAttrib;
    protected Vector3fExt scale;
    protected float angularVelocity;
    protected float prevAngularVelocity;
    protected Vector3fExt prevTorque;
    protected Vector3fExt rotationAxis;
    protected ColorVector color;
    protected Material material;

    protected ShadowVolumeBO shadowVolumeBO;

    protected Matrix4f modelMatrix;
    protected Matrix4f rotationMatrix;
    protected Matrix4f prevModelMatrix;

    protected int colorMapTexIndex;
    protected int normalMapTexIndex;

    private final List<Object3d> childObjects;  // TODO fix position

    /**
     * Indicates if this object is fixed in position and rotation or not.
     */
    protected boolean isFixedInSpace;

    /**
     * Indicates if the shadow of this object is enabled. Does not indicate for child objects.
     */
    protected boolean isShadowEnabled;

    protected boolean isCollisionPVSet;

    protected static CuboidBO sCuboidPrototype;
    protected static HeightMapSeamlessBO sHMapSPrototype;
    protected static ScreenQuadBO sScreenQuadPrototype;
    protected static SkyBoxBO sSkyBoxPrototype;
    protected static SphereBO sSpherePrototype;
    protected static OBJObject3dBO sHandPrototype;
    protected static OBJObject3dBO sOBJTestPrototype;
    protected static OBJObject3dBO sOBJBlobPrototype;

    private Object3d(Vector3fExt position, Vector3fExt scale, Vector3fExt velocity, Vector3fExt force,
                     ColorVector color, Material material, int colorMapTexIndex, int normalMapTexIndex) {
        this.position = position;
        this.velocity = velocity;
        this.force = force;

        this.colorMapTexIndex = colorMapTexIndex;
        this.normalMapTexIndex = normalMapTexIndex;
        prevPosition = new Vector3fExt(position);
        centerAttrib = this.position.getCopy();

        if (!scale.isAnyCoordZero()) {
            if (!scale.isAnyCoordLessThan(0)) {
                this.scale = new Vector3fExt(scale);
            } else {
                this.scale = new Vector3fExt(scale);
                this.scale.transformToPositiveCoords();
            }
        } else {
            this.scale = new Vector3fExt(1f, 1f, 1f);
        }
        this.color = new ColorVector(color);
        this.material = new Material(material);

        prevVelocity = new Vector3fExt();
        prevForce = new Vector3fExt();
        prevTorque = new Vector3fExt();
        rotationAxis = new Vector3fExt();

        modelMatrix = new Matrix4f();
        rotationMatrix = new Matrix4f();
        prevModelMatrix = new Matrix4f();

        isFixedInSpace = false;
        isShadowEnabled = true;
        isCollisionPVSet = false;

        childObjects = new Vector<>();
    }

    public Object3d(Vector3fExt position, float polarAngle, float azimuthAngle, float rollAngle, Vector3fExt scale,
                    Vector3fExt velocity, Vector3fExt force, ColorVector color, Material material,
                    int colorMapTexIndex, int normalMapTexIndex) {
        this(position, scale, velocity, force, color, material, colorMapTexIndex, normalMapTexIndex);
        rotationMatrix.rotate(azimuthAngle, Vector3fExt.Y_UNIT_VECTOR.getAsVector3f());
        rotationMatrix.rotate(polarAngle, Vector3fExt.Z_UNIT_VECTOR.getReverse().getAsVector3f());
        rotationMatrix.rotate(rollAngle, Vector3fExt.Y_UNIT_VECTOR.getAsVector3f());
    }

    public Object3d(Vector3fExt position, Matrix4f rotationMatrix, Vector3fExt scale, Vector3fExt velocity,
                    Vector3fExt force, ColorVector color, Material material, int colorMapTexIndex,
                    int normalMapTexIndex) {
        this(position, scale, velocity, force, color, material, colorMapTexIndex, normalMapTexIndex);
        Matrix4f.mul(rotationMatrix, this.rotationMatrix, this.rotationMatrix);
    }

    public int getColorMapTexIndex() {
        return colorMapTexIndex;
    }

    public int getNormalMapTexIndex() {
        return normalMapTexIndex;
    }

    public ColorVector getColor() {
        return color.getCopy();
    }

    public Vector3fExt getPosition() {
        return position.getCopy();
    }

    public Vector3fExt getScale() {
        return scale.getCopy();
    }

    public Vector3fExt getVelocity() {
        return velocity.getCopy();
    }

    public Vector3fExt getPrevVelocity() {
        return prevVelocity.getCopy();
    }

    public Vector3fExt getForce() {
        return force.getCopy();
    }

    public float getMass() {
        return (getVolume() * material.getMassDensity());
    }//mMass;}

    public float getMomentOfInertia() {
        return (getMass() * scale.getCoordAverage() * scale.getCoordAverage());
    }

    public Vector3fExt getCenterAttrib() {
        return centerAttrib.getCopy();
    }

    public Material getMaterial() {
        return material;
    }

    public boolean isFixed() {
        return isFixedInSpace;
    }

    public void setColorMapTexIndex(int newCMapTexI) {
        colorMapTexIndex = newCMapTexI;
    }

    public void setNormalMapTexIndex(int newNMapTexI) {
        normalMapTexIndex = newNMapTexI;
    }

    public void setPosition(Vector3fExt newPos) {
        position = new Vector3fExt(newPos);
    }

    public void setVelocity(Vector3fExt newVelocity) {
        velocity = new Vector3fExt(newVelocity);
    }

    public void setCenterAttrib(Vector3fExt newCenterAttrib) {
        centerAttrib = new Vector3fExt(newCenterAttrib);
    }

    public void resetForce() {
        force = new Vector3fExt();
    }

    /**
     * Fixes or releases this object in position and rotation.
     *
     * @param isFixedInSpace Flag indicating if this object is being fixed in position and rotation.
     */
    public void setFixedInSpace(boolean isFixedInSpace) {
        this.isFixedInSpace = isFixedInSpace;
    }

    /**
     * Enables or disables the shadow of this object. Does not affect child objects.
     *
     * @param isShadowEnabled Flag indicating if this object's shadow is being enabled or disabled.
     */
    public void setShadowEnabled(boolean isShadowEnabled) {
        this.isShadowEnabled = isShadowEnabled;
    }

    public void collisionPVSet() {
        isCollisionPVSet = true;
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public Matrix4f getRotationMatrix() {
        return rotationMatrix;
    }

    public Matrix4f getPrevModelMatrix() {
        return prevModelMatrix;
    }

    public static void setPrototype(PROTOTYPE_NAMES name, Object3dBO prototype) {
        switch (name) {
            case CUBOID -> sCuboidPrototype = (CuboidBO) prototype;
            case SCREENQUAD -> sScreenQuadPrototype = (ScreenQuadBO) prototype;
            case SKYBOX -> sSkyBoxPrototype = (SkyBoxBO) prototype;
            case SPHERE -> sSpherePrototype = (SphereBO) prototype;
            case H_MAP_S -> sHMapSPrototype = (HeightMapSeamlessBO) prototype;
            case HAND -> sHandPrototype = (OBJObject3dBO) prototype;
            case OBJ_TEST -> sOBJTestPrototype = (OBJObject3dBO) prototype;
            case OBJ_BLOB -> sOBJBlobPrototype = (OBJObject3dBO) prototype;
        }
    }

    public void calculateModelMatrix() {
        prevModelMatrix = new Matrix4f(modelMatrix);
        modelMatrix = new Matrix4f();
        modelMatrix.translate(position.getAsVector3f());
        Matrix4f.mul(modelMatrix, rotationMatrix, modelMatrix);
        modelMatrix.scale(scale.getAsVector3f());
    }

    /*public void addChildObject(Object3d newChild) {
        childObjects.add(newChild);
    }*/

    /*public void addChildObjects(List<Object3d> newChildren) {
        childObjects.addAll(newChildren);
    }*/

    public void translateWith(Vector3fExt translation) {
        position.add(translation);
    }

    public void moveTo(Vector3fExt targetPos, float displacementFactor) {
        position = new Vector3fExt(position.getSumWith(targetPos.getThisMinus(position).getMultipliedBy(displacementFactor)));
    }

    public void moveBy(Vector3fExt displacementVr) {
        position.add(displacementVr);
    }

    public void updatePrevPosition() {
        prevPosition = position.getCopy();
    }

    public void render(int[] programUniformIndices, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        final Object3dBO pt = getPrototype();
        exitOnGLError("in " + this.getClass().getSimpleName() + " at getting object BO prototype");
        if (pt == null) {
            System.out.println(this.getClass().getSimpleName() + " prototype not initialized");
        } else {
            pt.render(programUniformIndices, viewMatrix, projectionMatrix, this);
        }
        for (final Object3d childObject : childObjects) {
            childObject.render(programUniformIndices, viewMatrix, projectionMatrix);
        }
    }

    public void addForce(Vector3fExt plusForce) {
        force.add(plusForce);
    }

    public void addMediumForce(float viscosity) {
        force.add(velocity.getMultipliedBy(-viscosity));
    }

    public void addSpringForce(Vector3fExt springCenter, float stiffness) {
        force.add(position.getThisMinus(springCenter).getMultipliedBy(-stiffness));
    }

    public void addDampedSpringForce(Vector3fExt springCenter, float stiffness, float viscosity) {
        force.add(position.getThisMinus(springCenter).getMultipliedBy(-stiffness));
        force.add(velocity.getMultipliedBy(-viscosity));
    }

    public void addGravitationalForce(Vector3fExt otherCenter, float otherMass) {
        Vector3fExt distance = otherCenter.getThisMinus(position);
        force.add(distance.getWithLength(GRAVITATIONAL_CONSTANT * this.getMass() * otherMass / distance.getLengthSquare()));
    }

    public void addVDriveForce(Vector3fExt vTarget, float vDriveFactor) {
        force.add(vTarget.getThisMinus(velocity).getMultipliedBy(vDriveFactor));
    }

    public void addVDriveForce(Vector3fExt vTarget) {
        force.add(vTarget.getThisMinus(velocity).getMultipliedBy(this.getMass() / TIMESTEP));
    }

    /**
     * Adds force to the subject object according to the collision with parameter obj2.
     *
     * @param obj2 The obstacle object.
     */
    public void addCollisionForceBy(Object3d obj2) {
        if (!isCollisionFilterPass(this, obj2)) return;
        Vector3fExt[] collisionData = obj2.applyCollisionTo(this);
        if (collisionData != null) {
            position = collisionData[0].getCopy();
            velocity = collisionData[1].getCopy();
            force.reset();
            angularVelocity = collisionData[3].getLength();
            rotationAxis = collisionData[3].getNormalized();
            isCollisionPVSet = true;
            if (!obj2.isFixed()) {
                Vector3fExt[] revCollData = new Vector3fExt[collisionData.length];
                revCollData[1] = collisionData[1].getReverse();    // TODO calculate by mass
                revCollData[3] = collisionData[3].getReverse();
                obj2.setCollisionValues(revCollData);
            }
        }

        // Set post-collision values of child objects
        for (final Object3d childObject : childObjects) {
            collisionData = obj2.applyCollisionTo(childObject);
            if (collisionData != null) {
                childObject.setPosition(collisionData[0].getCopy());
                childObject.setVelocity(collisionData[1].getCopy());
                childObject.resetForce();
                childObject.collisionPVSet();
            }
        }

        // Apply parent - child collision
        for (final Object3d childObject : childObjects) {
            collisionData = this.applyCollisionTo(childObject);
            if (collisionData != null) {
                childObject.setPosition(collisionData[0].getCopy());
                childObject.setVelocity(collisionData[1].getCopy());
                childObject.resetForce();
                childObject.collisionPVSet();
            }
        }
    }

    /**
     * Sets post-collision values of this object. Recommended for setting counter-force effects.
     *
     * @param collisionData The post-collision values of this object.
     */
    public void setCollisionValues(Vector3fExt[] collisionData) {
        if (collisionData != null) {
            velocity = collisionData[1].getCopy();
            force.reset();
            angularVelocity = collisionData[3].getLength();
            rotationAxis = collisionData[3].getNormalized();
            isCollisionPVSet = true;
        }
    }

    public boolean isCollisionFilterPass(Object3d obj1, Object3d obj2) {
        final float objDistSquare = Vector3fExt.substract(obj1.getPosition(), obj2.getPosition()).getLengthSquare();
        final float objCollRadSumSquare = (obj1.getCollisionRadius() + obj2.getCollisionRadius())
                * (obj1.getCollisionRadius() + obj2.getCollisionRadius());
        return !(objDistSquare > objCollRadSumSquare);
    }

    public void update(Vector3fExt postTranslation) {
        if (!isCollisionPVSet) {    // If moving freely without collision.
            position = prevPosition.getSumWith(prevVelocity.getMultipliedBy(TIMESTEP));
            velocity = prevVelocity.getSumWith(prevForce.getMultipliedBy((1f / this.getMass()) * TIMESTEP));
            final Matrix4f tempMx = new Matrix4f();
            tempMx.rotate(prevAngularVelocity * TIMESTEP, rotationAxis.getNormalized().getAsVector3f());
            Matrix4f.mul(tempMx, rotationMatrix, rotationMatrix);
        } else {    // If had collision. Collision attributes already set by this point.
            isCollisionPVSet = false;
        }

        // Post-force updates
        position.add(postTranslation);

        // Save this round's values
        prevPosition = new Vector3fExt(position);
        prevVelocity = new Vector3fExt(velocity);
        prevForce = new Vector3fExt(force);
        prevAngularVelocity = angularVelocity;

        force.reset();

        calculateModelMatrix();

        // Update child objects
        this.addChildSpringForce();
        for (final Object3d childObject : childObjects) {
            childObject.update(Vector3fExt.NULL_VECTOR);
        }
    }

    public void addChildSpringForce() {
        for (final Object3d childObject : childObjects) {
            final Vector3fExt relPos = childObject.getPosition();
            final Matrix4f tempRMx = new Matrix4f();
            tempRMx.rotate(Vector3fExt.Z_UNIT_VECTOR.getReverse().getAngleWith(velocity),
                    Vector3fExt.Z_UNIT_VECTOR.getReverse().getCrossProductWith(velocity).getNormalized().getAsVector3f());
            relPos.multiplyBy(tempRMx);
            childObject.addDampedSpringForce(position.getSumWith(relPos), CHILD_STIFFNESS, CHILD_VISCOSITY);
        }
    }

    public void renderDynamicShadow(int[] programUniformIndices, Matrix4f viewMatrix, Matrix4f projectionMatrix,
                                    Vector3fExt lightPos) {
        if (isShadowEnabled) {
            if (shadowVolumeBO != null) {
                shadowVolumeBO.destroy();
            }
            shadowVolumeBO = new ShadowVolumeBO(new ShadowVolume(getShadowVertices(lightPos, ShadowVolume.LIGHT_PARAM_TYPE.LIGHT_DIRECTION),
                    position, lightPos, ShadowVolume.LIGHT_PARAM_TYPE.LIGHT_DIRECTION));
            shadowVolumeBO.render(programUniformIndices, viewMatrix, projectionMatrix, new DummyObject3d());
        }
        for (final Object3d childObject : childObjects) {
            childObject.renderDynamicShadow(programUniformIndices, viewMatrix, projectionMatrix, lightPos);
        }
    }

    public void destroyShadowVolume() {
        if (shadowVolumeBO != null) shadowVolumeBO.destroy();
        for (final Object3d childObject : childObjects) {
            childObject.destroyShadowVolume();
        }
    }

    /**
     * Calculates the post-collision values of parameter obj2 colliding with this object.
     *
     * @param obj2              The object of which the post-collision values are calculated.
     * @param localNormal       The local normal vector of the collision arrangement.
     * @param collisionDistance The overlap size between the colliding objects.
     * @param collisionPoint    The point of collision.
     * @return The post-collision values of parameter obj2 colliding with this object.
     */
    public Vector3fExt[] calculateCollisionResults(Object3d obj2, Vector3fExt localNormal, float collisionDistance,
                                                   Vector3fExt collisionPoint) {
        final Vector3fExt obj2V = obj2.getVelocity();
        final Vector3fExt obj2VRefl = obj2V.getReflectedFrom(localNormal, DEFAULT_COLLISION_REFL_FACTOR,
                1f - DEFAULT_SURFACE_FRICTION_COEFF);
        final Vector3fExt collisionForce = obj2VRefl.getThisMinus(obj2V).getMultipliedBy((1f / TIMESTEP) * obj2.getMass()).getThisMinus(obj2.getForce());

        final Vector3fExt collisionNewPosition = obj2.getPosition().getSumWith(localNormal.getMultipliedBy(Math.abs(collisionDistance) + GAP_SIZE));
        if (obj2VRefl.getLengthSquare() < COLLISION_V_TRIM_SQ) obj2VRefl.reset();

        final Vector3fExt torqueR = collisionPoint.getThisMinus(obj2.getPosition());
        final Vector3fExt torqueF = obj2.getVelocity().getWithLength(-obj2.getForce().getLength() * (1f - DEFAULT_SURFACE_FRICTION_COEFF));
        final Vector3fExt collisionRotVelocity = torqueR.getCrossProductWith(torqueF).getWithLength(obj2VRefl.getLength() / obj2.getCollisionRadius());

        final Vector3fExt[] retVal = new Vector3fExt[4];
        retVal[0] = collisionNewPosition;
        retVal[1] = obj2VRefl;
        retVal[2] = collisionForce;
        retVal[3] = collisionRotVelocity;
        return retVal;
    }

    public abstract Object3dBO getPrototype();

    public abstract float getCollisionRadius();

    public abstract float getVolume();

    public abstract Vector3fExt[] applyCollisionTo(Object3d obj2);

    public abstract List<Vector3fExt> getShadowVertices(Vector3fExt lightParam,
                                                        ShadowVolume.LIGHT_PARAM_TYPE lightType);
}

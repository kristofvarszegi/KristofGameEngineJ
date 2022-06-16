package com.kristof.gameengine.engine;

import com.kristof.gameengine.util.Matrix4f;
import com.kristof.gameengine.util.Vector3f;
import com.kristof.gameengine.util.Vector3fExt;

import org.lwjgl.BufferUtils;

import java.awt.*;
import java.nio.FloatBuffer;

import static com.kristof.gameengine.engine.MiscConstants.PROJECTION_FAR;
import static com.kristof.gameengine.engine.MiscConstants.PROJECTION_NEAR;
import static java.lang.Math.tan;

public class Kinematics {
    Vector3fExt lookDirection;
    Vector3fExt rightDirection;
    final Vector3fExt upVector;
    float lookPolarAngle;
    float lookAzimuthAngle;
    float targetPolarAngle;
    float targetAzimuthAngle;
    final Vector3fExt velocityTarget;
    final Vector3fExt dashTarget;
    final Vector3fExt eyeVelocity;
    Matrix4f projectionMatrix;
    Matrix4f viewMatrix;
    final Matrix4f prevVPMatrix;
    final FloatBuffer matrix44Buffer;
    final Vector3fExt eyePosition;
    Vector3fExt eyePosTemp;
    Vector3fExt lightPosition;
    Vector3fExt prevLightPosition;

    Kinematics() {
        eyePosition = new Vector3fExt(0, 0, 0);
        eyePosTemp = new Vector3fExt(0, 0, 0);
        lightPosition = new Vector3fExt();
        prevLightPosition = new Vector3fExt();
        lookDirection = new Vector3fExt(0, 0, -1f);
        rightDirection = new Vector3fExt(1f, 0, 0);
        upVector = Vector3fExt.Y_UNIT_VECTOR;
        lookPolarAngle = Vector3fExt.PI / 2f;
        lookAzimuthAngle = 0;
        targetPolarAngle = Vector3fExt.PI / 2f;
        targetAzimuthAngle = 0;
        velocityTarget = new Vector3fExt();
        dashTarget = new Vector3fExt();
        eyeVelocity = new Vector3fExt();

        matrix44Buffer = BufferUtils.createFloatBuffer(16);
        prevVPMatrix = new Matrix4f();
    }

    void setUpViewMatrix() {
        viewMatrix = new Matrix4f();
        viewMatrix.rotate(lookPolarAngle - Vector3fExt.PI / 2f, new Vector3f(1f, 0, 0));
        viewMatrix.rotate(lookAzimuthAngle, new Vector3f(0, -1f, 0));
        viewMatrix.translate(eyePosition.getReverse().getAsVector3f());
    }

    void setUpProjectionMatrix(Config config, Dimension screenDimension) {
        projectionMatrix = new Matrix4f();
        final float fieldOfView = config.fieldOfViewDeg;
        final float aspectRatio = (float) screenDimension.width / (float) screenDimension.height;
        final float nearPlaneDistance = PROJECTION_NEAR;//0f;
        final float farPlaneDistance = PROJECTION_FAR;//20000f;
        final float yScale = 1f / (float) tan(Math.toRadians(fieldOfView / 2f));
        final float xScale = yScale / aspectRatio;
        final float frustumLength = farPlaneDistance - nearPlaneDistance;
        projectionMatrix.m00 = xScale;
        projectionMatrix.m11 = yScale;
        projectionMatrix.m22 = -((farPlaneDistance + nearPlaneDistance) / frustumLength);
        projectionMatrix.m23 = -1f;
        projectionMatrix.m32 = -((2f * nearPlaneDistance * farPlaneDistance) / frustumLength);
        projectionMatrix.m33 = 0f;
    }
}

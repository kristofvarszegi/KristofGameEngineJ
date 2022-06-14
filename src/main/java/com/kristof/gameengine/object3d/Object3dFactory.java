package com.kristof.gameengine.object3d;

import com.kristof.gameengine.cuboid.Cuboid;
import com.kristof.gameengine.dummyobject.DummyObject3d;
import com.kristof.gameengine.heightmap.HeightMapSeamless;
import com.kristof.gameengine.rectangle.Rectangle;
import com.kristof.gameengine.skybox.SkyBox;
import com.kristof.gameengine.sphere.Sphere;
import com.kristof.gameengine.util.ColorVector;
import com.kristof.gameengine.util.Material;
import com.kristof.gameengine.util.Matrix4f;
import com.kristof.gameengine.util.Vector3fExt;

public class Object3dFactory {
    public static Object3d createCuboid(Vector3fExt position, float polarAngle, float azimuthAngle, float rollAngle,
                                        Vector3fExt velocity, Vector3fExt force, ColorVector color, Material material,
                                        int colorMapTexIndex, int normalMapTexIndex, float a, float b, float c) {
        return new Cuboid(position, polarAngle, azimuthAngle, rollAngle, velocity, force, color, material,
                colorMapTexIndex, normalMapTexIndex, a, b, c);
    }

    public static Object3d createDummyObject3d() {
        return new DummyObject3d();
    }

    public static Object3d createHeightMapSeamless(Vector3fExt center, float polarAngle, float azimuthAngle,
                                                   float rollAngle, Vector3fExt velocity, Vector3fExt force,
                                                   ColorVector color, Material material, int colorMapTexIndex,
                                                   int normalMapTexIndex, float a, float b, float h) {
        return new HeightMapSeamless(center, polarAngle, azimuthAngle, rollAngle, velocity,
                force, color, material, colorMapTexIndex, normalMapTexIndex, a, b, h);
    }

    public static Object3d createRectangle(Vector3fExt position, float polarAngle, float azimuthAngle, float rollAngle,
                                           Vector3fExt velocity, Vector3fExt force, ColorVector color,
                                           Material material, int colorMapTexIndex, int normalMapTexIndex, float aSide,
                                           float bSide, boolean isGhost) {
        return new Rectangle(position, polarAngle, azimuthAngle, rollAngle, velocity, force, color, material,
                colorMapTexIndex, normalMapTexIndex, aSide, bSide, isGhost);
    }

    public static Object3d createRectangle(Vector3fExt position, Matrix4f rotationMatrix, Vector3fExt velocity, Vector3fExt force,
                                           ColorVector color, Material material, int colorMapTexIndex, int normalMapTexIndex, float aSide,
                                           float bSide, boolean isGhost) {
        return new Rectangle(position, rotationMatrix, velocity, force, color, material, colorMapTexIndex,
                normalMapTexIndex, aSide, bSide, isGhost);
    }

    public static Object3d createSkyBox(Vector3fExt position, int colorMapTexIndex, int normalMapTexIndex) {
        return new SkyBox(position, colorMapTexIndex, normalMapTexIndex);
    }

    public static Object3d createSphere(Vector3fExt position, float polarAngle, float azimuthAngle, float rollAngle,
                                        Vector3fExt velocity, Vector3fExt force, ColorVector color, Material material,
                                        int colorMapTexIndex, int normalMapTexIndex, float radius) {
        return new Sphere(position, polarAngle, azimuthAngle, rollAngle, velocity, force, color, material,
                colorMapTexIndex, normalMapTexIndex, radius);
    }
}

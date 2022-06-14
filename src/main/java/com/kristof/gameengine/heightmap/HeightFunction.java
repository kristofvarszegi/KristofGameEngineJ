package com.kristof.gameengine.heightmap;

import com.kristof.gameengine.util.Vector3fExt;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public interface HeightFunction {
    float height(float x, float y);

    Vector3fExt normal(float x, float y);

    HeightFunction PLAIN_FUNC = new HeightFunction() {
        public float height(float x, float y) {
            return 0;
        }

        public Vector3fExt normal(float x, float y) {
            return Vector3fExt.Y_UNIT_VECTOR;
        }
    };

    HeightFunction RANDOM_FUNC = new HeightFunction() {
        public float height(float x, float y) {
            return (float) Math.random();
        }

        public Vector3fExt normal(float x, float y) {
            return Vector3fExt.Y_UNIT_VECTOR;
        }
    };

    HeightFunction SIN_COS_FUNC = new HeightFunction() {
        public float height(float x, float y) {
            return 0.1f * (float) cos(10f * x) * (float) sin(10f * y);
        }   // TODO find the clean way to parametrize this

        public Vector3fExt normal(float x, float y) {
            return Vector3fExt.Y_UNIT_VECTOR;
        }
    };
}

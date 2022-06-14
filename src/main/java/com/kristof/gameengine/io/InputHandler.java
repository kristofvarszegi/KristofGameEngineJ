package com.kristof.gameengine.io;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import org.lwjgl.glfw.GLFWKeyCallback;

public class InputHandler extends GLFWKeyCallback {
    public static final boolean[] sKeys = new boolean[65536];

    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
            glfwSetWindowShouldClose(window, true);
        sKeys[key] = action != GLFW_RELEASE;
    }

    public static boolean isKeyDown(int keyId) {
        return sKeys[keyId];
    }
}

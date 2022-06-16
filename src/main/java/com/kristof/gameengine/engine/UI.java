package com.kristof.gameengine.engine;

import com.kristof.gameengine.util.InputHistoryElement;

import java.awt.*;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;

public class UI {
    long window;
    Dimension screenDimension;
    long firstStartTime;
    long newLoopStartTime;
    long prevLoopEndTime;
    double prevMouseX = 0;
    double prevMouseY = 0;
    final List<InputHistoryElement> inputHistory;   // TODO move to model layer
    OutputStream outStream;

    boolean isOutputStreamSet = false;
    boolean isMouseLocked = false;
    boolean isVTargetSet = false;
    boolean isDashTargetSet = false;

    UI() {
        inputHistory = new Vector<>();
        inputHistory.add(new InputHistoryElement(InputHistoryElement.START, System.nanoTime()));
    }

    void destroy() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
    }
}

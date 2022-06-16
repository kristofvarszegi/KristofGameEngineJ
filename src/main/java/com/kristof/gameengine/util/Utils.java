package com.kristof.gameengine.util;

import com.kristof.gameengine.rectangle.Rectangle;
import com.kristof.gameengine.triangle.Triangle;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.stackPush;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;

public class Utils {
    public enum TOSTRING_CLASS_NAMES {
        VECTOR3FEXT, VECTOR3FEXT_ARRAY, FLOAT_ARRAY, INTEGER,
        SHORT, BYTE, TRIANGLE, RECTANGLE
    }

    private static final Logger LOGGER = LogManager.getLogger(Utils.class);

    public static int loadShader(String filename, int type) {
        final StringBuilder shaderSourceStr = new StringBuilder();
        int shaderId = 0;
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSourceStr.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            final String message = "Could not read file: " + filename;
            LOGGER.error(message);
            throw new RuntimeException(message);
        }

        shaderId = glCreateShader(type);
        glShaderSource(shaderId, shaderSourceStr);
        glCompileShader(shaderId);
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            String shaderTypeString = "ERROR";
            if (type == GL_VERTEX_SHADER) {
                shaderTypeString = "vertex";
            }
            if (type == GL_FRAGMENT_SHADER) {
                shaderTypeString = "fragment";
            }
            final String message = "Could not compile " + shaderTypeString + " shader " + filename;
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
        exitOnGLError("Renderer.loadShader()");
        return shaderId;
    }

    public static void exitOnGLError(String errorMessage) {
        final int errorValue = glGetError();
        if (errorValue != GL_NO_ERROR) {
            final String message = "GL Error: " + errorMessage + "(" + errorValue + ")";
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
    }

    public static Dimension getScreenDimensionFromGlfw(long window) {
        // Get the thread stack and push a new frame. The stack frame will be popped automatically.
        try (MemoryStack stack = stackPush()) {
            final IntBuffer pWidth = stack.mallocInt(1);
            final IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            final GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            return new Dimension(vidmode.width(), vidmode.height());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void takeScreenshot(Dimension screenDim) {
        GL11.glReadBuffer(GL11.GL_FRONT);
        final int width = (int) screenDim.getWidth();
        final int height = (int) screenDim.getHeight();
        final int bpp = 3; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
        final ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);

        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        final String timeAndDate = dateFormat.format(new Date());

        final File file = new File("screenshot_" + timeAndDate + ".png"); // The file to save to.
        final String format = "PNG"; // Example: "PNG" or "JPG"
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int i = (x + (width * y)) * bpp;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }

        try {
            ImageIO.write(image, format, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String toString(List<?> objects, TOSTRING_CLASS_NAMES classType, int lb, int roundingDigits) {
        StringBuilder aStr = new StringBuilder();
        for (int i = 0; i < objects.size(); i++) {
            if ((lb != 0) && (i % lb == 0) && (i != 0)) {
                aStr.append("\n");
            }
            switch (classType) {
                case VECTOR3FEXT -> aStr.append(", ").append(((Vector3fExt) objects.get(i)).getRounded(roundingDigits));
                case VECTOR3FEXT_ARRAY -> {
                    Vector3fExt[] tempVA = ((Vector3fExt[]) objects.get(i));
                    for (final Vector3fExt vector3FExt : tempVA) {
                        aStr.append(", ").append(vector3FExt.getRounded(roundingDigits));
                    }
                }
                case FLOAT_ARRAY -> aStr.append(", {").append(((float[]) objects.get(i))[0]).append(", ").append(((float[]) objects.get(i))[1]).append("}");
                case INTEGER -> aStr.append(", ").append((Integer) objects.get(i));
                case SHORT -> aStr.append(", ").append((Short) objects.get(i));
                case BYTE -> aStr.append(", ").append((Byte) objects.get(i));
                case TRIANGLE -> aStr.append(", ").append((Triangle) objects.get(i));
                case RECTANGLE -> aStr.append(", ").append((Rectangle) objects.get(i));
            }

        }
        return aStr.toString();
    }

    public static void writeInputHistoryToFile(List<InputHistoryElement> inputHistory, String fileName) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
            for (InputHistoryElement e : inputHistory) {
                out.write(e.getTimeStamp() + " " + e.getEventId());
                out.newLine();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String toString(int[] arr, int lb) {
        final StringBuilder aStr = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if ((lb != 0) && (i % lb == 0) && (i != 0)) {
                aStr.append("\n");
            }
            aStr.append(arr[i]).append(", ");
        }
        return aStr.toString();
    }
}

package com.kristof.gameengine.engine;

import static com.kristof.gameengine.shadow.ShadowVolume.LIGHT_PARAM_TYPE.LIGHT_POSITION;
import static com.kristof.gameengine.engine.GLConstants.*;
import static com.kristof.gameengine.engine.MiscConstants.*;
import static com.kristof.gameengine.util.Utils.*;

import com.kristof.gameengine.io.InputHandler;
import com.kristof.gameengine.object3d.Object3d;
import com.kristof.gameengine.object3d.Object3dFactory;
import com.kristof.gameengine.screen.ScreenQuad;
import com.kristof.gameengine.shadow.ShadowVolume;
import com.kristof.gameengine.shadow.ShadowVolumeBO;
import com.kristof.gameengine.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.Callback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.lang.Math.sin;

import java.awt.*;
import java.io.*;
import java.nio.DoubleBuffer;

public class Engine {
    private static final Logger LOGGER = LogManager.getLogger(Engine.class);

    private final Config config;
    private final GlBufferStore glBufferStore;
    private final Kinematics kinematics;
    private final ScreenQuad postProcessQuad;
    private final Scene scene;
    private final ScreenQuad shadowQuad;
    private final UI ui;

    private Callback debugMessageCallback;

    public Engine(String iniFilePath) throws IOException {
        kinematics = new Kinematics();
        scene = new Scene();
        config = new Config(iniFilePath);
        ui = new UI();

        initGL();
        exitOnGLError("initGL()");

        glBufferStore = new GlBufferStore(config, ui);

        kinematics.setUpProjectionMatrix(config, ui.screenDimension);
        kinematics.setUpViewMatrix();

        postProcessQuad = new ScreenQuad();
        shadowQuad = new ScreenQuad();

        initWorld();
    }

    public long getFirstStartTime() {
        return ui.firstStartTime;
    }

    public void setOutputStream(OutputStream out) {
        this.ui.outStream = out;
        ui.isOutputStreamSet = true;
    }

    public boolean hasOutputStream() {
        return ui.outStream != null;
    }

    private void initGL() {
        GLFWErrorCallback.createPrint(System.err).set();    // TODO find the canonical way to direct this to Log4j

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints(); // Optional. The current ui.window hints are already the default.
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // The ui.window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // The ui.window will be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        final DisplayMode mode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getDisplayMode();
        ui.screenDimension = new Dimension(mode.getWidth(), mode.getHeight());
        if (config.isFullScreen) {
            ui.window = glfwCreateWindow((int) ui.screenDimension.getWidth(), (int) ui.screenDimension.getHeight(), WINDOW_TITLE,
                    glfwGetPrimaryMonitor(), NULL);
        } else {
            ui.window = glfwCreateWindow((int) ui.screenDimension.getWidth(), (int) ui.screenDimension.getHeight(), WINDOW_TITLE,
                    NULL, NULL);
        }
        if (ui.window == NULL)
            throw new RuntimeException("Failed to create the GLFW ui.window");
        glfwSetKeyCallback(ui.window, new InputHandler());
        glfwSetWindowPos(ui.window, 0, 0);    // TODO only if ui.windowed
        glfwSetInputMode(ui.window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        glfwMakeContextCurrent(ui.window);
        // This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed
        // externally. LWJGL detects the context that is current in the current thread, creates the ContextCapabilities
        // instance and makes the OpenGL bindings available for use.
        GL.createCapabilities();
        debugMessageCallback = GLUtil.setupDebugMessageCallback();

        glViewport(0, 0, (int) ui.screenDimension.getWidth(), (int) ui.screenDimension.getHeight());  // Map the internal OpenGL coordinate system to the entire screen
        glClearColor(0f, 1f, 1f, 0f);
        glDisable(GL11.GL_CULL_FACE);
        glEnable(GL11.GL_DEPTH_TEST);
        glfwSwapInterval(1);    // Enable v-sync
        glfwShowWindow(ui.window);
    }

    public void initWorld() {   // TODO from scene assets
        scene.skyBox = Object3dFactory.createSkyBox(kinematics.eyePosition,
                glBufferStore.colorMapIndices.get(TEXTURE_ASSET_KEYS.SPACE_CLOUDS),
                glBufferStore.normalMapIndices.get(TEXTURE_ASSET_KEYS.BLUE));

        /*avatar = Object3dFactory.createCuboid(new Vector3fExt(0, 0, 0), 0, 0,
                0, Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.BLUE, Material.CHROME,
                glBufferIndices.colorMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE), glBufferIndices.normalMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE),
                0.5f, 0.5f, 0.5f);*/
        scene.avatar = Object3dFactory.createSphere(new Vector3fExt(0, 4f, 0), 0, 0,
                0, Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.BLUE, Material.EMISSIVE,
                glBufferStore.colorMapIndices.get(TEXTURE_ASSET_KEYS.WHITE),
                glBufferStore.normalMapIndices.get(TEXTURE_ASSET_KEYS.BLUE), 0.2f);
        scene.avatar.setShadowEnabled(false);

        /*final float childObjectSize = 0.3f;
        avatar.addChildObject(new Cuboid(new Vector3fExt(-2f, 0, 0), 0, 0,
                0, Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.BLUE,
                Material.CHROME, glBufferIndices.colorMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE),
                glBufferIndices.normalMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE), childObjectSize, childObjectSize, childObjectSize));
        avatar.addChildObject(new Cuboid(new Vector3fExt(-1f, -1f, 0), 0, 0,
                0, Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.BLUE,
                Material.CHROME, glBufferIndices.colorMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE),
                glBufferIndices.normalMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE), childObjectSize, childObjectSize, childObjectSize));
        avatar.addChildObject(new Cuboid(new Vector3fExt(1f, -1f, 0), 0, 0,
                0, Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.BLUE,
                Material.CHROME, glBufferIndices.colorMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE),
                glBufferIndices.normalMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE), childObjectSize, childObjectSize, childObjectSize));
        avatar.addChildObject(new Cuboid(new Vector3fExt(2f, 0, 0), 0, 0,
                0, Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.BLUE,
                Material.CHROME, glBufferIndices.colorMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE),
                glBufferIndices.normalMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE), childObjectSize, childObjectSize, childObjectSize));*/

        scene.staticObjects.add(Object3dFactory.createHeightMapSeamless(new Vector3fExt(0, 0, 0),
                0, 0, 0, Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR,
                ColorVector.BLUE, Material.PEARL, glBufferStore.colorMapIndices.get(TEXTURE_ASSET_KEYS.BROKEN),
                glBufferStore.normalMapIndices.get(TEXTURE_ASSET_KEYS.BROKEN), 40f, 40f, 1f));
        /*staticObjects.add(Object3dFactory.createRectangle(new Vector3fExt(0, 0, 0), 0,
                0, (float) Math.toRadians(45), Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR,
                ColorVector.BLUE, Material.PEARL, glBufferIndices.colorMapIndices.get(TEXTURE_ASSET_KEYS.DIAGONAL),
                glBufferIndices.normalMapIndices.get(TEXTURE_ASSET_KEYS.DIAGONAL), 100f, 100f, false));*/

        final int floatingRectCount = 5;
        for (int i = 0; i < floatingRectCount; i++) {
            scene.staticObjects.add(Object3dFactory.createRectangle(
                    new Vector3fExt(-1f, 0.7f, 2f + i * (-2f)),
                    (float) Math.toRadians(60.), (float) Math.toRadians(-20. - 5. * Math.random()),
                    (float) Math.toRadians(90.),
                    Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.BLUE, Material.SILVER,
                    glBufferStore.colorMapIndices.get(TEXTURE_ASSET_KEYS.MTL_FLOOR02),
                    glBufferStore.normalMapIndices.get(TEXTURE_ASSET_KEYS.MTL_FLOOR02), 1.5f, 1.5f,
                    false));
        }

        // 3D cube grid TODO from level assets
        /*final int n = 3;
        final float d = 10f;
        final float a = 4f;
        final float o = d;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    staticObjects.add(Object3dFactory.createCuboid(
                            new Vector3fExt(o - i * d, o + j * d, o - k * d),
                            0, 0, 0,
                            Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR,
                            ColorVector.BLUE, Material.CHROME,
                            glBufferIndices.colorMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE),
                            glBufferIndices.normalMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE),
                            a, a, a));
                }
            }
        }*/

        // Spheres
        final int inertSphereCount = 5;  // TODO from level assets
        final float d = 4f;
        final float h = 2f;
        float r;
        for (int i = 0; i < inertSphereCount; i++) {
            r = 0.2f + (float) (0.3f * Math.random());
            scene.inertObjects.add(Object3dFactory.createSphere(
                    Vector3fExt.randomVector(0.01f).getSumWith(i * d, h, i * d),
                    0, (float) Math.toRadians(120), 0, Vector3fExt.NULL_VECTOR,
                    Vector3fExt.NULL_VECTOR, ColorVector.BLUE, Material.SHINY,
                    glBufferStore.colorMapIndices.get(TEXTURE_ASSET_KEYS.MTL_TRIM01),
                    glBufferStore.normalMapIndices.get(TEXTURE_ASSET_KEYS.MTL_TRIM01), r));
        }

        scene.staticObjects.forEach((final Object3d object3d) -> object3d.setFixedInSpace(true));
    }

    private void readInput() {
        glfwPollEvents();

        // Check if LMB has been pressed
        if (glfwGetMouseButton(ui.window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS) {
            ui.isMouseLocked = true;
        }

        // Check LMB click position
        if (ui.isMouseLocked) {
            // TODO what you do on click   
            ui.isMouseLocked = false;
        }

        final DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
        final DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

        glfwGetCursorPos(ui.window, x, y);
        x.rewind();
        y.rewind();

        double newMouseX = Math.max(0, Math.min(ui.screenDimension.width, x.get()));
        double newMouseY = Math.max(0, Math.min(ui.screenDimension.height, y.get()));

        double dX = newMouseX - ui.prevMouseX;
        double dY = newMouseY - ui.prevMouseY;
        ui.prevMouseX = newMouseX;
        ui.prevMouseY = newMouseY;

        // Place cursor to the other side of the screen if it has exited on a side
        if (newMouseX == ui.screenDimension.width - 1) {
            glfwSetCursorPos(ui.window, 1, newMouseY);
        }
        if (newMouseX == 0) {
            glfwSetCursorPos(ui.window, ui.screenDimension.width - 2, newMouseY);
        }
        if (newMouseY == ui.screenDimension.height - 1) {
            glfwSetCursorPos(ui.window, newMouseX, 1);
        }
        if (newMouseY == 0) {
            glfwSetCursorPos(ui.window, newMouseX, ui.screenDimension.height - 2);
        }

        if (dX < MOUSE_D_TRIM && dY < MOUSE_D_TRIM) {
            kinematics.targetPolarAngle += dY * POLAR_VIEW_SPEED;
            kinematics.targetAzimuthAngle -= dX * AZIMUTH_VIEW_SPEED;
            kinematics.targetPolarAngle = (float) Math.min(Math.PI, Math.max(0, kinematics.targetPolarAngle));
            kinematics.lookPolarAngle += EYE_ROT_FOLLOW_FACTOR
                    * (kinematics.targetPolarAngle - kinematics.lookPolarAngle);
            kinematics.lookAzimuthAngle += EYE_ROT_FOLLOW_FACTOR
                    * (kinematics.targetAzimuthAngle - kinematics.lookAzimuthAngle);
        }

        kinematics.lookDirection = new Vector3fExt(kinematics.lookPolarAngle, kinematics.lookAzimuthAngle,
                Vector3fExt.Y_UNIT_VECTOR, Vector3fExt.Z_UNIT_VECTOR.getReverse());
        kinematics.rightDirection = kinematics.lookDirection.getCrossProductWith(Vector3fExt.Y_UNIT_VECTOR)
                .getNormalized();

        kinematics.velocityTarget.reset();
        ui.isVTargetSet = false;

        // Read Keyboard events
        if (InputHandler.isKeyDown(GLFW_KEY_W)) {
            kinematics.velocityTarget.add(kinematics.lookDirection.getMultipliedBy(FWD_CONTROL_V));
            ui.isVTargetSet = true;
            ui.inputHistory.add(new InputHistoryElement(InputHistoryElement.KEY_FWD, ui.newLoopStartTime));
        }
        if (InputHandler.isKeyDown(GLFW_KEY_A)) {
            kinematics.velocityTarget.add(kinematics.rightDirection.getMultipliedBy(-SIDE_CONTROL_V));
            ui.isVTargetSet = true;
            ui.inputHistory.add(new InputHistoryElement(InputHistoryElement.KEY_LEFT, ui.newLoopStartTime));
        }
        if (InputHandler.isKeyDown(GLFW_KEY_S)) {
            kinematics.velocityTarget.add(kinematics.lookDirection.getMultipliedBy(-FWD_CONTROL_V));
            ui.isVTargetSet = true;
            ui.inputHistory.add(new InputHistoryElement(InputHistoryElement.KEY_BP, ui.newLoopStartTime));
        }
        if (InputHandler.isKeyDown(GLFW_KEY_D)) {
            kinematics.velocityTarget.add(kinematics.rightDirection.getMultipliedBy(SIDE_CONTROL_V));
            ui.isVTargetSet = true;
            ui.inputHistory.add(new InputHistoryElement(InputHistoryElement.KEY_RIGHT, ui.newLoopStartTime));
        }
        if (InputHandler.isKeyDown(GLFW_KEY_SPACE)) {
            kinematics.velocityTarget.add(kinematics.upVector.getMultipliedBy(VERT_CONTROL_V));
            ui.isVTargetSet = true;
            ui.inputHistory.add(new InputHistoryElement(InputHistoryElement.KEY_SPACE, ui.newLoopStartTime));
        }
        if (InputHandler.isKeyDown(GLFW_KEY_C)) {
            kinematics.velocityTarget.add(kinematics.upVector.getMultipliedBy(-VERT_CONTROL_V));
            ui.isVTargetSet = true;
        }
        if (InputHandler.isKeyDown(GLFW_KEY_X)) {
            kinematics.velocityTarget.reset();
            ui.isVTargetSet = true;
            scene.avatar.setVelocity(Vector3fExt.NULL_VECTOR);
        }
        /*if (InputHandler.isKeyDown(GLFW_KEY_R)) { // TODO fit to speed-based pos updating
            avatar.setPosition(RESET_POSITION);
            avatar.resetForce();
        }*/
        if (InputHandler.isKeyDown(GLFW_KEY_F)) {
            kinematics.dashTarget.add(scene.avatar.getPosition().getSumWith(kinematics.lookDirection
                    .getWithLength(DASH_DISTANCE)));
            ui.isDashTargetSet = true;
        }
        if (InputHandler.isKeyDown(GLFW_KEY_P)) {
            takeScreenshot(ui.screenDimension);
        }
        if (ui.isOutputStreamSet) {
            if (InputHandler.isKeyDown(GLFW_KEY_W)) {
                try {
                    ui.outStream.write(GLFW_KEY_W);
                    LOGGER.debug(GLFW_KEY_W + " written to serial port.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (InputHandler.isKeyDown(GLFW_KEY_A)) {
                try {
                    ui.outStream.write(GLFW_KEY_A);
                    LOGGER.debug(GLFW_KEY_A + " written to serial port.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (InputHandler.isKeyDown(GLFW_KEY_S)) {
                try {
                    ui.outStream.write(GLFW_KEY_S);
                    LOGGER.debug(GLFW_KEY_S + " written to serial port.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (InputHandler.isKeyDown(GLFW_KEY_D)) {
                try {
                    ui.outStream.write(GLFW_KEY_D);
                    LOGGER.debug(GLFW_KEY_D + " written to serial port.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateEye() {
        kinematics.eyePosTemp = scene.avatar.getPosition().getSumWith(kinematics.lookDirection.getReverse()
                        .getMultipliedBy(TPS_DISTANCE_Z))
                .getSumWith(Vector3fExt.Y_UNIT_VECTOR.getMultipliedBy(TPS_DISTANCE_Y));
        kinematics.eyePosition.add(kinematics.eyePosTemp.getThisMinus(kinematics.eyePosition)
                .getMultipliedBy(EYE_FOLLOW_FACTOR));
        if (config.isEyeIdleMovementEnabled) {
            kinematics.eyePosition.add((new Vector3fExt(0,
                    (float) sin(EYE_IDLE_ANGULAR_FREQUENCY_RAD_PER_NS * System.nanoTime()), 0))
                    .getMultipliedBy(EYE_IDLE_FACTOR));
        }
    }

    private void updateWorld() {
        scene.avatar.resetForce();
        if (ui.isVTargetSet) scene.avatar.addVDriveForce(kinematics.velocityTarget, V_DRIVE_FACTOR);
        if (ui.isDashTargetSet) scene.avatar.addSpringForce(kinematics.dashTarget, Object3d.DEFAULT_STIFFNESS);
        //avatar.addForce(kinematics.dashTarget.getThisMinus(avatar.getPosition()).getWithLength(1000000000000000f));
        if (config.isGravityOn) {
            scene.avatar.addForce(new Vector3fExt(0,
                    -Object3d.GRAVITY_ACCEL_EARTH * scene.avatar.getMass(), 0));
        }
        if (config.isDragEnabled) {
            scene.avatar.addDragForce(Object3d.DEFAULT_VISCOSITY);
        }

        // Apply collision between inert objects
        scene.inertObjects.forEach((final Object3d object3d1) -> {
            scene.inertObjects.forEach((final Object3d object3d2) -> {
                if (!object3d1.equals(object3d2)) {
                    object3d1.addCollisionForceBy(object3d2);
                }
            });
        });

        // Apply avatar's attributes according to collision
        scene.staticObjects.forEach((final Object3d object3d) -> scene.avatar.addCollisionForceBy(object3d));
        scene.inertObjects.forEach((final Object3d object3d) -> scene.avatar.addCollisionForceBy(object3d));

        // TODO from scene description
        kinematics.prevLightPosition = kinematics.lightPosition;
        //kinematics.lightPosition = Vector3fExt.rotatingVectorXZ(0.0000000006f * System.nanoTime(), 30f, new Vector3fExt(0, 40f, 0));
        //kinematics.lightPosition = new Vector3fExt(0f, 100f, 0f);
        kinematics.lightPosition = scene.avatar.getPosition();

        scene.inertObjects.forEach((final Object3d inertObject) -> {
            inertObject.resetForce();
            if (config.isGravityOn) {
                inertObject.addForce(new Vector3fExt(0,
                        -Object3d.GRAVITY_ACCEL_EARTH * inertObject.getMass(), 0));
            }
            scene.staticObjects.forEach((final Object3d staticObject) -> {
                inertObject.addCollisionForceBy(staticObject);
            });
            inertObject.addCollisionForceBy(scene.avatar);    // TODO make counter-forces in one round

            if (InputHandler.isKeyDown(GLFW_KEY_G)) {
                inertObject.addSpringForce(scene.avatar.getPosition(), Object3d.DEFAULT_STIFFNESS);
                if (config.isDragEnabled) {
                    inertObject.addDragForce(Object3d.DEFAULT_VISCOSITY);
                }
                if (config.isAvatarPowerGravityPullEnabled) {
                    inertObject.addGravitationalForce(scene.avatar.getPosition(), scene.avatar.getMass());
                }
            }
            inertObject.update(Vector3fExt.NULL_VECTOR);
        });

        // Update object properties according to the calculated forces
        final Vector3fExt dashVr = ui.isDashTargetSet ? kinematics.lookDirection.getWithLength(DASH_DISTANCE)
                : Vector3fExt.NULL_VECTOR;
        scene.skyBox.setPosition(kinematics.eyePosition);
        scene.skyBox.calculateModelMatrix();
        scene.avatar.update(dashVr);  // TODO add collision when dashing
        scene.staticObjects.forEach((final Object3d staticObject) -> staticObject.update(Vector3fExt.NULL_VECTOR));

        ui.isDashTargetSet = false;
        kinematics.dashTarget.reset();
    }

    public void run() {
        ui.firstStartTime = System.currentTimeMillis();
        while (!glfwWindowShouldClose(ui.window)) {
            executeOneCycle();
        }
        destroy();
    }

    private void executeOneCycle() {
        ui.newLoopStartTime = System.nanoTime();
        readInput();
        updateWorld();
        updateEye();
        kinematics.setUpProjectionMatrix(config, ui.screenDimension);
        kinematics.setUpViewMatrix();
        render();

        final long loopTime = System.nanoTime() - ui.newLoopStartTime;
        final long framesPerSec = Math.round(1000000000. / ((double) loopTime));
        if (System.nanoTime() - ui.prevLoopEndTime > 0.1 * 1000000000) {
            LOGGER.debug("fps: " + framesPerSec + ", T = " + 0.000001 * loopTime + " ms");
        }
        ui.prevLoopEndTime = System.nanoTime();
        Matrix4f.mul(kinematics.viewMatrix, kinematics.projectionMatrix, kinematics.prevVPMatrix);
    }

    private void render() {
        switch (config.renderMode) {
            case RAY_TRACING -> {
                kinematics.lightPosition = new Vector3fExt(0f, 100f, 0f);  // TODO fix for light source within object

                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                glUseProgram(glBufferStore.rayTracingProgramIndex);
                glUniform3f(glBufferStore.rayTracingUniformIndices[U_EYEPOSITION_MAP], kinematics.eyePosTemp.getX(),
                        kinematics.eyePosTemp.getY(), kinematics.eyePosTemp.getZ());
                glUniform3f(glBufferStore.rayTracingUniformIndices[U_LIGHTPOSITION_MAP],
                        kinematics.lightPosition.getX(),
                        kinematics.lightPosition.getY(), kinematics.lightPosition.getZ());
                glUniform3f(glBufferStore.rayTracingUniformIndices[U_LOOKDIR_MAP], kinematics.lookDirection.getX(),
                        kinematics.lookDirection.getY(), kinematics.lookDirection.getZ());
                glUniform3f(glBufferStore.rayTracingUniformIndices[U_RIGHTDIR_MAP], kinematics.rightDirection.getX(),
                        kinematics.rightDirection.getY(), kinematics.rightDirection.getZ());
                glUniform3f(glBufferStore.rayTracingUniformIndices[U_AVATARPOS_MAP],
                        scene.avatar.getPosition().getX(), scene.avatar.getPosition().getY(),
                        scene.avatar.getPosition().getZ());
                glUniform2f(glBufferStore.rayTracingUniformIndices[U_SCREENDIM_MAP],
                        (float) ui.screenDimension.getWidth(), (float) ui.screenDimension.getHeight());
                glActiveTexture(GL_TEXTURE0 + NORMALMAP_TEXTURE_UNIT);
                glBindTexture(GL_TEXTURE_2D, scene.avatar.getNormalMapTexIndex());
                final int normalMapUnifIndex = glGetUniformLocation(glBufferStore.rayTracingProgramIndex,
                        NORMAL_MAP_LABEL);
                glUniform1i(normalMapUnifIndex, NORMALMAP_TEXTURE_UNIT);
                postProcessQuad.render(glBufferStore.ignorerUniformIndices, kinematics.viewMatrix,
                        kinematics.projectionMatrix);
                exitOnGLError("at rendering ray tracing screen quad.");
                glUseProgram(0);
                exitOnGLError("glUseProgram detach in ray tracing render cycle");
                glfwSwapBuffers(ui.window);
            }
            case TRADITIONAL -> {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
                if (config.isPostProcessingEnabled)
                    preparePostProcessing();   // TODO DoF as a separate, 1st GPU pass
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                glUseProgram(glBufferStore.skyBoxProgramIndex);
                scene.skyBox.render(glBufferStore.skyBoxUniformIndices, kinematics.viewMatrix,
                        kinematics.projectionMatrix);
                glUseProgram(glBufferStore.defaultProgramIndex);
                exitOnGLError("in " + this.getClass().getSimpleName()
                        + " at glUseProgram(glBufferIndices.defaultProgramIndex)");
                glUniform3f(glBufferStore.defaultUniformIndices[U_EYEPOSITION_MAP], kinematics.eyePosTemp.getX(),
                        kinematics.eyePosTemp.getY(), kinematics.eyePosTemp.getZ());
                glUniform3f(glBufferStore.defaultUniformIndices[U_LIGHTPOSITION_MAP], kinematics.lightPosition.getX(),
                        kinematics.lightPosition.getY(), kinematics.lightPosition.getZ());
                renderWorldObjects(glBufferStore.defaultUniformIndices);
                if (config.isShadowsEnabled) renderShadows();
                if (config.isPostProcessingEnabled) postProcess();
                glUseProgram(0);
                exitOnGLError("glUseProgram detach in render cycle");
                glfwSwapBuffers(ui.window);
            }
            default -> throw new IllegalArgumentException("Invalid render mode: " + config.renderMode);
        }
    }

    private void renderShadows() {
        glUseProgram(glBufferStore.defaultProgramIndex);
        exitOnGLError("in " + this.getClass().getSimpleName()
                + " at glUseProgram() at drawing shadow volumes");

        if (!config.isShadowVolumesVisible) {
            glColorMask(false, false, false, false);
        }
        glDepthMask(false);
        glEnable(GL_CULL_FACE);

        glClearStencil(0);
        glClear(GL_STENCIL_BUFFER_BIT);
        glEnable(GL_STENCIL_TEST);

        glEnable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(0.0f, 100.0f);

        renderShadowVolumes(glBufferStore.defaultUniformIndices, kinematics.viewMatrix, kinematics.projectionMatrix,
                kinematics.lightPosition, LIGHT_POSITION);   // TODO with bare color shader

        glDisable(GL_POLYGON_OFFSET_FILL);
        glDisable(GL_CULL_FACE);
        glColorMask(true, true, true, true);
        glDepthMask(true);

        glStencilFunc(GL_NOTEQUAL, 0x0, 0xff);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);

        glDepthFunc(GL_LEQUAL);

        glUseProgram(glBufferStore.monoScreenProgramIndex);
        exitOnGLError("glUseProgram - at switching to monoScreenProgram");

        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shadowQuad.render(glBufferStore.ignorerUniformIndices, kinematics.viewMatrix, kinematics.projectionMatrix);

        glDisable(GL_STENCIL_TEST);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    private void preparePostProcessing() {
        GL30.glBindFramebuffer(GL_FRAMEBUFFER, glBufferStore.velocityFrameBuffer1Index);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(glBufferStore.velocityMap1ProgramIndex);
        //renderWorldObjects(glBufferIndices.velocityMap1ProgramIndex, glBufferIndices.velocityMap1UniformIndices); TODO fix shader performance

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, glBufferStore.velocityFrameBuffer2Index);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(glBufferStore.velocityMap2ProgramIndex);
        //renderWorldObjects(glBufferIndices.velocityMap1ProgramIndex, glBufferIndices.velocityMap1UniformIndices); TODO fix shader performance

        // Rendering scene into the framebuffer TODO uncomment after fixing shader performance
        //glViewport(0, 0, ui.screenDimension.width, ui.screenDimension.height); // Render on the whole framebuffer, complete from the lower left corner to the upper right
        //glBindTexture(GL_TEXTURE_2D, 0);
        GL30.glBindFramebuffer(GL_FRAMEBUFFER, glBufferStore.postProcessFrameBufferIndex);
        //GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBufferIndex);

        exitOnGLError("binding framebuffer in cycle");
    }

    private void postProcess() {
        glActiveTexture(GL_TEXTURE0 + DEPTH_MAP_TEXTURE_UNIT);
        glBindTexture(GL_TEXTURE_2D, glBufferStore.depthTexIndex);
        glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, 0, 0, ui.screenDimension.width,
                ui.screenDimension.height, 0);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(glBufferStore.postProcessProgramIndex);

        // Bind rendered texture
        glActiveTexture(GL_TEXTURE0 + POSTPROCESS_MAP_TEXTURE_UNIT);
        glBindTexture(GL_TEXTURE_2D, glBufferStore.postProcessTexIndex);
        glUniform1i(glBufferStore.postProcessMapUnifIndex, POSTPROCESS_MAP_TEXTURE_UNIT);

        // Bind depth texture
        glActiveTexture(GL_TEXTURE0 + DEPTH_MAP_TEXTURE_UNIT);
        glBindTexture(GL_TEXTURE_2D, glBufferStore.depthTexIndex);
        glUniform1i(glBufferStore.depthMapUnifIndex, DEPTH_MAP_TEXTURE_UNIT);

        // Bind kinematics.eyeVelocity maps
        glActiveTexture(GL_TEXTURE0 + VELOCITY_MAP1_TEXTURE_UNIT);
        glBindTexture(GL_TEXTURE_2D, glBufferStore.velocityTex1Index);
        glUniform1i(glBufferStore.velocityMap1UnifIndex, VELOCITY_MAP1_TEXTURE_UNIT);
        glActiveTexture(GL_TEXTURE0 + VELOCITY_MAP2_TEXTURE_UNIT);
        glBindTexture(GL_TEXTURE_2D, glBufferStore.velocityTex2Index);
        glUniform1i(glBufferStore.velocityMap2UnifIndex, VELOCITY_MAP2_TEXTURE_UNIT);

        final Matrix4f VPMatrixInv = new Matrix4f();
        Matrix4f.mul(kinematics.viewMatrix, kinematics.projectionMatrix, VPMatrixInv);
        VPMatrixInv.invert();
        VPMatrixInv.store(kinematics.matrix44Buffer);
        kinematics.matrix44Buffer.flip();
        glUniformMatrix4fv(glBufferStore.vpMatrixInvIndex, false, kinematics.matrix44Buffer);

        kinematics.prevVPMatrix.store(kinematics.matrix44Buffer);
        kinematics.matrix44Buffer.flip();
        glUniformMatrix4fv(glBufferStore.prevVPMatrixIndex, false, kinematics.matrix44Buffer);

        glUniform3f(glBufferStore.eyeVelocityIndex, kinematics.eyeVelocity.getX(), kinematics.eyeVelocity.getY(),
                kinematics.eyeVelocity.getZ());
        glUniform3f(glBufferStore.lookVectorIndex, kinematics.lookDirection.getX(), kinematics.lookDirection.getY(),
                kinematics.lookDirection.getZ());

        postProcessQuad.render(glBufferStore.postProcessUniformIndices, kinematics.viewMatrix,
                kinematics.projectionMatrix);
    }

    private void renderWorldObjects(int[] uniformIndices) {
        scene.staticObjects.forEach((final Object3d staticObject) -> staticObject.render(uniformIndices,
                kinematics.viewMatrix, kinematics.projectionMatrix));
        scene.inertObjects.forEach((final Object3d inertObject) -> inertObject.render(uniformIndices,
                kinematics.viewMatrix, kinematics.projectionMatrix));
        scene.avatar.render(uniformIndices, kinematics.viewMatrix, kinematics.projectionMatrix);
    }

    private void renderShadowVolumes(int[] uniformIndices, Matrix4f vMatrix, Matrix4f pMatrix, Vector3fExt lightParam,
                                     ShadowVolume.LIGHT_PARAM_TYPE lightType) {
        if (scene.staticShadowVolumeBO == null || !kinematics.lightPosition.equals(kinematics.prevLightPosition)) {
            final ShadowVolume staticShadowVolume = new ShadowVolume(lightParam, lightType);
            scene.staticObjects.forEach((final Object3d staticObject) -> staticShadowVolume.addData(
                    staticObject.getShadowVertices(lightParam, lightType)));
            scene.staticShadowVolumeBO = new ShadowVolumeBO(staticShadowVolume);
        }
        scene.staticShadowVolumeBO.render(uniformIndices, vMatrix, pMatrix, Object3dFactory.createDummyObject3d());

        scene.inertObjects.forEach((final Object3d inertObject) -> inertObject.renderDynamicShadow(uniformIndices,
                vMatrix, pMatrix, lightParam));

        scene.avatar.renderDynamicShadow(uniformIndices, vMatrix, pMatrix, lightParam);
    }

    private void destroy() {    // TODO find a good design for distributing which field is responsible for which part of the GL
        scene.destroy();
        glBufferStore.destroy();
        ui.destroy();

        glfwTerminate();
        glfwSetErrorCallback(null).free();
        debugMessageCallback.free();
    }
}

package com.kristof.gameengine.engine;

import static com.kristof.gameengine.shadow.ShadowVolume.LIGHT_PARAM_TYPE.LIGHT_POSITION;
import static com.kristof.gameengine.engine.EngineGLConstants.*;
import static com.kristof.gameengine.engine.EngineMiscConstants.*;

import com.kristof.gameengine.cuboid.Cuboid;
import com.kristof.gameengine.cuboid.CuboidBO;
import com.kristof.gameengine.heightmap.HeightMapSeamless;
import com.kristof.gameengine.heightmap.HeightMapSeamlessBO;
import com.kristof.gameengine.io.InputHandler;
import com.kristof.gameengine.object3d.DummyObject3d;
import com.kristof.gameengine.object3d.OBJObject3dBO;
import com.kristof.gameengine.object3d.Object3d;
import com.kristof.gameengine.object3d.Object3dBO;
import com.kristof.gameengine.screen.ScreenQuad;
import com.kristof.gameengine.screen.ScreenQuadBO;
import com.kristof.gameengine.shadow.ShadowVolume;
import com.kristof.gameengine.shadow.ShadowVolumeBO;
import com.kristof.gameengine.skybox.SkyBox;
import com.kristof.gameengine.skybox.SkyBoxBO;
import com.kristof.gameengine.sphere.Sphere;
import com.kristof.gameengine.sphere.SphereBO;
import com.kristof.gameengine.util.*;

import static com.kristof.gameengine.util.Utils.*;
import static java.lang.Math.sin;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.Callback;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

import static java.lang.Math.tan;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.List;

public class Engine {
    private final List<Object3dBO> prototypes;
    private OBJObject3dBO objBlobBO;    // TODO fix support

    private Object3d avatar;
    private SkyBox skyBox;
    private final List<Object3d> staticObjects;
    private final List<Object3d> inertObjects;
    private ShadowVolumeBO staticShadowVolumeBO;

    // Program objects and fields
    private final ScreenQuad postProcessQuad;
    private final ScreenQuad shadowQuad;
    private int defaultProgramIndex;
    private int skyBoxProgramIndex;
    private int monoScreenProgramIndex;
    private int velocityMap1ProgramIndex;
    private int velocityMap2ProgramIndex;
    private int postProcessProgramIndex;
    private int rayTracingProgramIndex;
    private final int[] defaultUniformIndices;
    private final int[] skyBoxUniformIndices;
    private final int[] ignorerUniformIndices;
    private final int[] velocityMap1UniformIndices;
    private final int[] velocityMap2UniformIndices;
    private final int[] postProcessUniformIndices;
    private final int[] rayTracingUniformIndices;
    private int postProcessMapUnifIndex;
    private int depthMapUnifIndex;
    private int velocityMap1UnifIndex;
    private int velocityMap2UnifIndex;
    private int vpMatrixInvIndex;
    private int prevVPMatrixIndex;
    private int eyeVelocityIndex;
    private int lookVectorIndex;
    private final HashMap<TEXTURE_ASSET_KEYS, Integer> colorMapIndices;
    private final HashMap<TEXTURE_ASSET_KEYS, Integer> normalMapIndices;
    private int postProcessTexIndex;
    private int depthTexIndex;
    private int velocityTex1Index;
    private int velocityTex2Index;
    private int postProcessFrameBufferIndex;
    private int depthBufferIndex;
    private int stencilBufferIndex;
    private int depthStencilBufferIndex;
    private int velocityFrameBuffer1Index;
    private int velocityDepthBuffer1Index;
    private int velocityFrameBuffer2Index;
    private int velocityDepthBuffer2Index;

    // Camera related fields
    private Vector3fExt lookDirection;
    private Vector3fExt rightDirection;
    private final Vector3fExt upVector;
    private float lookPolarAngle;
    private float lookAzimuthAngle;
    private float targetPolarAngle;
    private float targetAzimuthAngle;
    private final Vector3fExt velocityTarget;
    private final Vector3fExt dashTarget;
    private final Vector3fExt eyeVelocity;
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private final Matrix4f prevVPMatrix;
    private final FloatBuffer matrix44Buffer;

    private final Vector3fExt eyePosition;
    private Vector3fExt eyePosTemp;
    private Vector3fExt lightPosition;

    // UI fields
    private long window;
    Dimension screenDimension;
    private long firstStartTime;
    private long newLoopStartTime;
    private long prevLoopEndTime;
    private double prevMouseX = 0;
    private double prevMouseY = 0;
    private final List<InputHistoryElement> inputHistory;
    private OutputStream outStream;

    // TODO with config file
    private final boolean isRayTracingModeEnabled = false;
    private boolean isOutputStreamSet = false;
    private boolean isMouseLocked = false;
    private boolean isVTargetSet = false;
    private boolean isDashTargetSet = false;
    private final boolean isGravityOn = true;
    private final boolean isFullscreen = true;
    private final boolean isPostProcessingEnabled = true;
    private final boolean isShadowsEnabled = true;
    private final boolean isShadowVolumesVisible = false;
    private final boolean isEyeIdleMovementEnabled = true;

    private Callback debugProc;

    public Engine() {
        defaultUniformIndices = new int[16];
        Arrays.fill(defaultUniformIndices, -1);
        skyBoxUniformIndices = new int[16];
        Arrays.fill(skyBoxUniformIndices, -1);
        ignorerUniformIndices = new int[16];
        Arrays.fill(ignorerUniformIndices, -1);
        velocityMap1UniformIndices = new int[16];
        velocityMap2UniformIndices = new int[16];
        Arrays.fill(velocityMap1UniformIndices, -1);
        Arrays.fill(velocityMap2UniformIndices, -1);
        postProcessUniformIndices = new int[16];
        Arrays.fill(postProcessUniformIndices, -1);
        rayTracingUniformIndices = new int[32];
        Arrays.fill(rayTracingUniformIndices, -1);

        eyePosition = new Vector3fExt(0, 0, 0);
        eyePosTemp = new Vector3fExt(0, 0, 0);
        lightPosition = new Vector3fExt();
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

        staticObjects = new Vector<>();
        inertObjects = new Vector<>();

        prototypes = new Vector<>();

        initGL();
        exitOnGLError("renderer constructor - initOpenGL()");
        inputHistory = new Vector<>();
        inputHistory.add(new InputHistoryElement(InputHistoryElement.START, System.nanoTime()));

        loadShaders();
        exitOnGLError("renderer constructor - loadShaders()");

        // Load built-in textures
        colorMapIndices = new HashMap<>();
        normalMapIndices = new HashMap<>();
        loadBuiltInTextures();
        exitOnGLError("renderer constructor - loadTextures()");

        if (isPostProcessingEnabled) {
            setUpDepthOfFieldBuffer();
            setUpVelocityBuffers();
        }

        // Setting up matrices
        matrix44Buffer = BufferUtils.createFloatBuffer(16);
        setUpProjectionMatrix();
        setUpViewMatrix();
        prevVPMatrix = new Matrix4f();

        initObject3dPrototypes();

        postProcessQuad = new ScreenQuad();
        shadowQuad = new ScreenQuad();

        initWorld();
    }

    public long getFirstStartTime() {
        return firstStartTime;
    }

    public void setOutputStream(OutputStream out) {
        this.outStream = out;
        isOutputStreamSet = true;
    }

    public boolean hasOutputStream() {
        return outStream != null;
    }

    private void initGL() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure window
        glfwDefaultWindowHints(); // Optional. The current window hints are already the default.
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // The window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // The window will be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        final DisplayMode mode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        screenDimension = new Dimension(mode.getWidth(), mode.getHeight());
        //final long monitor = glfwGetPrimaryMonitor();   // Fullscreen TODO from config
        final long monitor = NULL;  // Windowed
        window = glfwCreateWindow((int) screenDimension.getWidth(), (int) screenDimension.getHeight(), WINDOW_TITLE,
                monitor, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
        glfwSetKeyCallback(window, new InputHandler());
        glfwSetWindowPos(window, 0, 0);    // TODO only if windowed
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        glfwMakeContextCurrent(window);
        // This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed
        // externally. LWJGL detects the context that is current in the current thread, creates the ContextCapabilities
        // instance and makes the OpenGL bindings available for use.
        GL.createCapabilities();
        debugProc = GLUtil.setupDebugMessageCallback();

        glViewport(0, 0, (int) screenDimension.getWidth(), (int) screenDimension.getHeight());  // Map the internal OpenGL coordinate system to the entire screen
        glClearColor(0f, 1f, 1f, 0f);
        glDisable(GL11.GL_CULL_FACE);
        glEnable(GL11.GL_DEPTH_TEST);
        glfwSwapInterval(1);    // Enable v-sync
        glfwShowWindow(window);
    }

    private void loadShaders() {
        loadDefaultShaders();
        loadSkyBoxShaders();    // TODO shadow color via uniform
        loadMonoScreenShaders();
        if (isPostProcessingEnabled) {
            loadPostProcessShaders();
            loadVelocityMapShaders();
        }
        if (isRayTracingModeEnabled) loadRayTracingShaders();
        exitOnGLError("loadShaders()");
    }

    // TODO merge into one single shader loader method
    private void loadDefaultShaders() {
        final int vertexShaderIndex = loadShader("res/shaders/default_shader.vert", GL_VERTEX_SHADER);
        final int fragmentShaderIndex = loadShader("res/shaders/default_shader.frag", GL_FRAGMENT_SHADER);
        defaultProgramIndex = glCreateProgram();
        glAttachShader(defaultProgramIndex, vertexShaderIndex);
        glAttachShader(defaultProgramIndex, fragmentShaderIndex);

        glBindAttribLocation(defaultProgramIndex, POS_VA_SHADER_INDEX, POS_VA_LABEL);
        glBindAttribLocation(defaultProgramIndex, NOR_VA_SHADER_INDEX, NOR_VA_LABEL);
        glBindAttribLocation(defaultProgramIndex, TEX_VA_SHADER_INDEX, TEX_VA_LABEL);
        glBindAttribLocation(defaultProgramIndex, TAN_VA_SHADER_INDEX, TAN_VA_LABEL);

        glLinkProgram(defaultProgramIndex);
        glValidateProgram(defaultProgramIndex);

        exitOnGLError("loadDefaultShaders()");

        defaultUniformIndices[U_MMATRIX_MAP] = glGetUniformLocation(defaultProgramIndex, M_MATRIX_LABEL);
        defaultUniformIndices[U_VMATRIX_MAP] = glGetUniformLocation(defaultProgramIndex, V_MATRIX_LABEL);
        defaultUniformIndices[U_PMATRIX_MAP] = glGetUniformLocation(defaultProgramIndex, P_MATRIX_LABEL);
        defaultUniformIndices[U_RMATRIX_MAP] = glGetUniformLocation(defaultProgramIndex, R_MATRIX_LABEL);
        defaultUniformIndices[U_MATERIAL_MAP] = glGetUniformLocation(defaultProgramIndex, MATERIAL_LABEL);
        defaultUniformIndices[U_EYEPOSITION_MAP] = glGetUniformLocation(defaultProgramIndex, EYE_POS_LABEL);
        defaultUniformIndices[U_LIGHTPOSITION_MAP] = glGetUniformLocation(defaultProgramIndex, LIGHT_POS_LABEL);
        defaultUniformIndices[U_COLORMAP_MAP] = glGetUniformLocation(defaultProgramIndex, COLOR_MAP_LABEL);
        defaultUniformIndices[U_NORMALMAP_MAP] = glGetUniformLocation(defaultProgramIndex, NORMAL_MAP_LABEL);
    }

    private void loadSkyBoxShaders() {
        final int vertexShaderIndex = loadShader("res/shaders/default_shader.vert", GL_VERTEX_SHADER);
        final int fragmentShaderIndex = loadShader("res/shaders/skybox_shader.frag", GL_FRAGMENT_SHADER);
        skyBoxProgramIndex = glCreateProgram();
        glAttachShader(skyBoxProgramIndex, vertexShaderIndex);
        glAttachShader(skyBoxProgramIndex, fragmentShaderIndex);

        glBindAttribLocation(skyBoxProgramIndex, POS_VA_SHADER_INDEX, POS_VA_LABEL);
        glBindAttribLocation(skyBoxProgramIndex, NOR_VA_SHADER_INDEX, NOR_VA_LABEL);
        glBindAttribLocation(skyBoxProgramIndex, TEX_VA_SHADER_INDEX, TEX_VA_LABEL);
        glBindAttribLocation(skyBoxProgramIndex, TAN_VA_SHADER_INDEX, TAN_VA_LABEL);

        glLinkProgram(skyBoxProgramIndex);
        glValidateProgram(skyBoxProgramIndex);

        exitOnGLError("loadSkyBoxShaders()");

        skyBoxUniformIndices[U_MMATRIX_MAP] = glGetUniformLocation(skyBoxProgramIndex, M_MATRIX_LABEL);
        skyBoxUniformIndices[U_VMATRIX_MAP] = glGetUniformLocation(skyBoxProgramIndex, V_MATRIX_LABEL);
        skyBoxUniformIndices[U_PMATRIX_MAP] = glGetUniformLocation(skyBoxProgramIndex, P_MATRIX_LABEL);
        skyBoxUniformIndices[U_RMATRIX_MAP] = glGetUniformLocation(skyBoxProgramIndex, R_MATRIX_LABEL);
        skyBoxUniformIndices[U_MATERIAL_MAP] = glGetUniformLocation(skyBoxProgramIndex, MATERIAL_LABEL);
        skyBoxUniformIndices[U_COLORMAP_MAP] = glGetUniformLocation(skyBoxProgramIndex, COLOR_MAP_LABEL);
    }

    private void loadMonoScreenShaders() {
        final int vertexShaderIndex = loadShader("res/shaders/screenquad_shader.vert", GL_VERTEX_SHADER);
        final int fragmentShaderIndex = loadShader("res/shaders/shadow_shader.frag", GL_FRAGMENT_SHADER);
        monoScreenProgramIndex = glCreateProgram();
        glAttachShader(monoScreenProgramIndex, vertexShaderIndex);
        glAttachShader(monoScreenProgramIndex, fragmentShaderIndex);

        glBindAttribLocation(monoScreenProgramIndex, POS_VA_SHADER_INDEX, POS_VA_LABEL);
        glBindAttribLocation(monoScreenProgramIndex, NOR_VA_SHADER_INDEX, NOR_VA_LABEL);
        glBindAttribLocation(monoScreenProgramIndex, TEX_VA_SHADER_INDEX, TEX_VA_LABEL);
        glBindAttribLocation(monoScreenProgramIndex, TAN_VA_SHADER_INDEX, TAN_VA_LABEL);

        glLinkProgram(monoScreenProgramIndex);
        glValidateProgram(monoScreenProgramIndex);
    }

    private void loadPostProcessShaders() {
        final int vertexShaderIndex = loadShader("res/shaders/screenquad_shader.vert", GL_VERTEX_SHADER);
        final int fragmentShaderIndex = loadShader("res/shaders/post_process_shader.frag", GL_FRAGMENT_SHADER);

        postProcessProgramIndex = glCreateProgram();
        glAttachShader(postProcessProgramIndex, vertexShaderIndex);
        glAttachShader(postProcessProgramIndex, fragmentShaderIndex);

        glBindAttribLocation(postProcessProgramIndex, POS_VA_SHADER_INDEX, POS_VA_LABEL);
        glBindAttribLocation(postProcessProgramIndex, NOR_VA_SHADER_INDEX, NOR_VA_LABEL);
        glBindAttribLocation(postProcessProgramIndex, TEX_VA_SHADER_INDEX, TEX_VA_LABEL);
        glBindAttribLocation(postProcessProgramIndex, TAN_VA_SHADER_INDEX, TAN_VA_LABEL);

        glLinkProgram(postProcessProgramIndex);
        glValidateProgram(postProcessProgramIndex);

        vpMatrixInvIndex = glGetUniformLocation(postProcessProgramIndex, VP_MATRIX_INV_LABEL);
        prevVPMatrixIndex = glGetUniformLocation(postProcessProgramIndex, PREV_VP_MATRIX_LABEL);
        eyeVelocityIndex = glGetUniformLocation(postProcessProgramIndex, EYE_VELOCITY_LABEL);
        lookVectorIndex = glGetUniformLocation(postProcessProgramIndex, LOOK_DIRECTION_LABEL);
        postProcessMapUnifIndex = glGetUniformLocation(postProcessProgramIndex, POST_PROCESS_MAP_LABEL);
        depthMapUnifIndex = glGetUniformLocation(postProcessProgramIndex, DEPTH_MAP_LABEL);
        velocityMap1UnifIndex = glGetUniformLocation(postProcessProgramIndex, VELOCITY_MAP1_LABEL);
        velocityMap2UnifIndex = glGetUniformLocation(postProcessProgramIndex, VELOCITY_MAP2_LABEL);
    }

    private void loadVelocityMapShaders() {
        // 1st shader
        int vertexShaderIndex = loadShader("res/shaders/velocitymap1_shader.vert", GL_VERTEX_SHADER);
        int fragmentShaderIndex = loadShader("res/shaders/velocitymap_shader.frag", GL_FRAGMENT_SHADER);

        velocityMap1ProgramIndex = glCreateProgram();
        exitOnGLError("glCreateProgram() velocityMapProgram1");
        glAttachShader(velocityMap1ProgramIndex, vertexShaderIndex);
        glAttachShader(velocityMap1ProgramIndex, fragmentShaderIndex);
        exitOnGLError("glAttachShaderProgram() velocityMapProgram1");

        glBindAttribLocation(velocityMap1ProgramIndex, POS_VA_SHADER_INDEX, POS_VA_LABEL);
        glBindAttribLocation(velocityMap1ProgramIndex, NOR_VA_SHADER_INDEX, NOR_VA_LABEL);
        glBindAttribLocation(velocityMap1ProgramIndex, TEX_VA_SHADER_INDEX, TEX_VA_LABEL);
        glBindAttribLocation(velocityMap1ProgramIndex, TAN_VA_SHADER_INDEX, TAN_VA_LABEL);

        glLinkProgram(velocityMap1ProgramIndex);
        exitOnGLError("glLinkProgram() velocityMapProgram1");
        glValidateProgram(velocityMap1ProgramIndex);

        velocityMap1UniformIndices[U_MMATRIX_MAP] = glGetUniformLocation(velocityMap1ProgramIndex, M_MATRIX_LABEL);
        velocityMap1UniformIndices[U_VMATRIX_MAP] = glGetUniformLocation(velocityMap1ProgramIndex, V_MATRIX_LABEL);
        velocityMap1UniformIndices[U_PMATRIX_MAP] = glGetUniformLocation(velocityMap1ProgramIndex, P_MATRIX_LABEL);
        velocityMap1UniformIndices[U_PREV_MMATRIX_MAP] = glGetUniformLocation(velocityMap1ProgramIndex, PREV_M_MATRIX_LABEL);

        // 2nd shader
        vertexShaderIndex = loadShader("res/shaders/velocitymap2_shader.vert", GL_VERTEX_SHADER);
        fragmentShaderIndex = loadShader("res/shaders/velocitymap_shader.frag", GL_FRAGMENT_SHADER);
        velocityMap2ProgramIndex = glCreateProgram();
        glAttachShader(velocityMap2ProgramIndex, vertexShaderIndex);
        glAttachShader(velocityMap2ProgramIndex, fragmentShaderIndex);

        glBindAttribLocation(velocityMap2ProgramIndex, POS_VA_SHADER_INDEX, POS_VA_LABEL);
        glBindAttribLocation(velocityMap2ProgramIndex, NOR_VA_SHADER_INDEX, NOR_VA_LABEL);
        glBindAttribLocation(velocityMap2ProgramIndex, TEX_VA_SHADER_INDEX, TEX_VA_LABEL);
        glBindAttribLocation(velocityMap2ProgramIndex, TAN_VA_SHADER_INDEX, TAN_VA_LABEL);

        glLinkProgram(velocityMap2ProgramIndex);
        glValidateProgram(velocityMap2ProgramIndex);

        velocityMap2UniformIndices[U_MMATRIX_MAP] = glGetUniformLocation(velocityMap2ProgramIndex, M_MATRIX_LABEL);
        velocityMap2UniformIndices[U_VMATRIX_MAP] = glGetUniformLocation(velocityMap2ProgramIndex, V_MATRIX_LABEL);
        velocityMap2UniformIndices[U_PMATRIX_MAP] = glGetUniformLocation(velocityMap2ProgramIndex, P_MATRIX_LABEL);
        velocityMap2UniformIndices[U_RMATRIX_MAP] = glGetUniformLocation(velocityMap2ProgramIndex, R_MATRIX_LABEL);
        velocityMap2UniformIndices[U_PREV_MMATRIX_MAP] = glGetUniformLocation(velocityMap2ProgramIndex, PREV_M_MATRIX_LABEL);
    }

    private void loadRayTracingShaders() {
        final int vertexShaderIndex = loadShader("res/shaders/screenquad_shader.vert", GL_VERTEX_SHADER);
        final int fragmentShaderIndex = loadShader("res/shaders/raytracing_shader.frag", GL_FRAGMENT_SHADER);

        rayTracingProgramIndex = glCreateProgram();
        glAttachShader(rayTracingProgramIndex, vertexShaderIndex);
        glAttachShader(rayTracingProgramIndex, fragmentShaderIndex);

        glBindAttribLocation(rayTracingProgramIndex, POS_VA_SHADER_INDEX, POS_VA_LABEL);
        glBindAttribLocation(rayTracingProgramIndex, NOR_VA_SHADER_INDEX, NOR_VA_LABEL);
        glBindAttribLocation(rayTracingProgramIndex, TEX_VA_SHADER_INDEX, TEX_VA_LABEL);
        glBindAttribLocation(rayTracingProgramIndex, TAN_VA_SHADER_INDEX, TAN_VA_LABEL);

        glLinkProgram(rayTracingProgramIndex);
        glValidateProgram(rayTracingProgramIndex);

        exitOnGLError("loadRayTracingShaders()");

        rayTracingUniformIndices[U_EYEPOSITION_MAP] =
                glGetUniformLocation(rayTracingProgramIndex, EYE_POS_LABEL);
        rayTracingUniformIndices[U_LIGHTPOSITION_MAP] =
                glGetUniformLocation(rayTracingProgramIndex, LIGHT_POS_LABEL);
        rayTracingUniformIndices[U_LOOKDIR_MAP] =
                glGetUniformLocation(rayTracingProgramIndex, LOOK_DIRECTION_LABEL);
        rayTracingUniformIndices[U_RIGHTDIR_MAP] =
                glGetUniformLocation(rayTracingProgramIndex, RIGHT_DIRECTION_LABEL);
        rayTracingUniformIndices[U_AVATARPOS_MAP] =
                glGetUniformLocation(rayTracingProgramIndex, AVATAR_POS_LABEL);
        rayTracingUniformIndices[U_SCREENDIM_MAP] =
                glGetUniformLocation(rayTracingProgramIndex, SCREEN_DIM_LABEL);
    }

    private void loadBuiltInTextures() {    // TODO from config
        colorMapIndices.put(TEXTURE_ASSET_KEYS.BLUE,
                loadPNGTexture("res/textures/blue_normalmap.png", GL_TEXTURE0 + COLORMAP_TEXTURE_UNIT));
        colorMapIndices.put(TEXTURE_ASSET_KEYS.BROKEN,
                loadPNGTexture("res/textures/ft_broken01_c.png", GL_TEXTURE0 + COLORMAP_TEXTURE_UNIT));
        colorMapIndices.put(TEXTURE_ASSET_KEYS.SPACE_CLOUDS,
                loadPNGTexture("res/textures/spaceclouds_colormap.png", GL_TEXTURE0 + COLORMAP_TEXTURE_UNIT));
        colorMapIndices.put(TEXTURE_ASSET_KEYS.DIAGONAL,
                loadPNGTexture("res/textures/ft_diagonal01_c.png", GL_TEXTURE0 + COLORMAP_TEXTURE_UNIT));
        colorMapIndices.put(TEXTURE_ASSET_KEYS.MANHOLE,
                loadPNGTexture("res/textures/manhole_256x256_colormap.png", GL_TEXTURE0 + COLORMAP_TEXTURE_UNIT));
        colorMapIndices.put(TEXTURE_ASSET_KEYS.MTL_FLOOR02,
                loadPNGTexture("res/textures/mtl_floor02_c.png", GL_TEXTURE0 + COLORMAP_TEXTURE_UNIT));
        colorMapIndices.put(TEXTURE_ASSET_KEYS.MTL_TRIM01,
                loadPNGTexture("res/textures/mtl_trim01_c.png", GL_TEXTURE0 + COLORMAP_TEXTURE_UNIT));

        normalMapIndices.put(TEXTURE_ASSET_KEYS.BROKEN,
                loadPNGTexture("res/textures/ft_broken01_n.png", GL_TEXTURE0 + COLORMAP_TEXTURE_UNIT));
        normalMapIndices.put(TEXTURE_ASSET_KEYS.BLUE,
                loadPNGTexture("res/textures/blue_normalmap.png", GL_TEXTURE0 + COLORMAP_TEXTURE_UNIT));
        normalMapIndices.put(TEXTURE_ASSET_KEYS.DIAGONAL,
                loadPNGTexture("res/textures/ft_diagonal01_n.png", GL_TEXTURE0 + COLORMAP_TEXTURE_UNIT));
        normalMapIndices.put(TEXTURE_ASSET_KEYS.MANHOLE,
                loadPNGTexture("res/textures/manhole_256x256_normalmap.png", GL_TEXTURE0 + NORMALMAP_TEXTURE_UNIT));
        normalMapIndices.put(TEXTURE_ASSET_KEYS.MTL_FLOOR02,
                loadPNGTexture("res/textures/mtl_floor02_n.png", GL_TEXTURE0 + NORMALMAP_TEXTURE_UNIT));
        normalMapIndices.put(TEXTURE_ASSET_KEYS.MTL_TRIM01,
                loadPNGTexture("res/textures/mtl_trim01_n.png", GL_TEXTURE0 + NORMALMAP_TEXTURE_UNIT));
    }

    private int loadPNGTexture(String filename, int textureUnit) {
        final ByteBuffer buf;
        final int imageWidth;
        final int imageHeight;
        try {
            InputStream in = new FileInputStream(filename);
            PNGDecoder decoder = new PNGDecoder(in);
            imageWidth = decoder.getWidth();
            imageHeight = decoder.getHeight();
            buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
            decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
            buf.flip();
            in.close();

            // Create a new texture object in memory and bind it
            int texId = glGenTextures();
            glActiveTexture(textureUnit);
            glBindTexture(GL_TEXTURE_2D, texId);

            // All RGB bytes are aligned to each other and each component is 1 byte
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            // Upload the texture data and generate mip maps (for scaling)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, imageWidth, imageHeight, 0,
                    GL_RGBA, GL_UNSIGNED_BYTE, buf);
            glGenerateMipmap(GL_TEXTURE_2D);

            // Setup the ST coordinate system
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Setup what to do when the texture has to be scaled
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER,
                    GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR_MIPMAP_LINEAR);

            // Unbind
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, 0);

            exitOnGLError("loadPNGTexture");

            return texId;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read " + filename + ": " + e.getMessage());
        }
    }

    private void setUpDepthOfFieldBuffer() {
        postProcessTexIndex = glGenTextures();
        depthTexIndex = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, postProcessTexIndex);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, screenDimension.width, screenDimension.height, 0, GL_RGB,
                GL_UNSIGNED_BYTE, (ByteBuffer) null);

        glBindTexture(GL_TEXTURE_2D, depthTexIndex);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, GL14.GL_COMPARE_R_TO_TEXTURE);
        glTexParameteri(GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, screenDimension.width, screenDimension.height, 0,
                GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE, (ByteBuffer) null);//texBuffer);

        glBindTexture(GL_TEXTURE_2D, 0);

        postProcessFrameBufferIndex = glGenFramebuffers();

        glBindFramebuffer(GL_FRAMEBUFFER, postProcessFrameBufferIndex);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, postProcessTexIndex, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTexIndex, 0);

        // Binding depth buffer TODO fix with DoF
        /*depthBufferIndex = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthBufferIndex);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, screenDimension.width, screenDimension.height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBufferIndex);
        */

        // Binding stencil buffer TODO fix with DoF
        /*stencilBufferIndex = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, stencilBufferIndex);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_STENCIL_INDEX, screenDimension.width, screenDimension.height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, GL_RENDERBUFFER, stencilBufferIndex);
        */

        depthStencilBufferIndex = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthStencilBufferIndex);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_STENCIL, screenDimension.width, screenDimension.height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthStencilBufferIndex);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthStencilBufferIndex);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Framebuffer incomplete");
            System.exit(-1);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void setUpVelocityBuffers() {
        // 1st eyeVelocity buffer
        velocityTex1Index = glGenTextures();
        velocityFrameBuffer1Index = glGenFramebuffers();
        velocityDepthBuffer1Index = glGenRenderbuffers();

        glBindTexture(GL_TEXTURE_2D, velocityTex1Index);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, screenDimension.width, screenDimension.height, 0, GL_RGB, GL_UNSIGNED_BYTE, (ByteBuffer) null);//texBuffer);
        glBindTexture(GL_TEXTURE_2D, 0);

        glBindRenderbuffer(GL_RENDERBUFFER, velocityDepthBuffer1Index);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, screenDimension.width, screenDimension.height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        glBindFramebuffer(GL_FRAMEBUFFER, velocityFrameBuffer1Index);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, velocityTex1Index, 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, velocityDepthBuffer1Index);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Framebuffer incomplete");
            System.exit(-1);
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // 2nd eyeVelocity buffer
        velocityTex2Index = glGenTextures();
        velocityFrameBuffer2Index = glGenFramebuffers();
        velocityDepthBuffer2Index = glGenRenderbuffers();

        glBindTexture(GL_TEXTURE_2D, velocityTex2Index);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, screenDimension.width, screenDimension.height, 0, GL_RGB, GL_UNSIGNED_BYTE, (ByteBuffer) null);//texBuffer);
        glBindTexture(GL_TEXTURE_2D, 0);

        glBindRenderbuffer(GL_RENDERBUFFER, velocityDepthBuffer2Index);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, screenDimension.width, screenDimension.height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        glBindFramebuffer(GL_FRAMEBUFFER, velocityFrameBuffer2Index);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, velocityTex2Index, 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, velocityDepthBuffer2Index);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("2nd velocitybuffer incomplete");
            System.exit(-1);
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void initObject3dPrototypes() {
        final CuboidBO cuboidBO = new CuboidBO();
        Object3d.setPrototype(Object3d.PROTOTYPE_NAMES.CUBOID, cuboidBO);
        prototypes.add(cuboidBO);

        final SphereBO sphereBO = new SphereBO();
        Object3d.setPrototype(Object3d.PROTOTYPE_NAMES.SPHERE, sphereBO);
        prototypes.add(sphereBO);

        final SkyBoxBO skyBoxBO = new SkyBoxBO();
        Object3d.setPrototype(Object3d.PROTOTYPE_NAMES.SKYBOX, skyBoxBO);
        prototypes.add(skyBoxBO);

        final ScreenQuadBO screenQuadBO = new ScreenQuadBO();
        Object3d.setPrototype(Object3d.PROTOTYPE_NAMES.SCREENQUAD, screenQuadBO);
        prototypes.add(screenQuadBO);

        final HeightMapSeamlessBO heightMapSeamlessBO = new HeightMapSeamlessBO();
        Object3d.setPrototype(Object3d.PROTOTYPE_NAMES.H_MAP_S, heightMapSeamlessBO);
        prototypes.add(heightMapSeamlessBO);
    }

    private void setUpViewMatrix() {
        viewMatrix = new Matrix4f();
        viewMatrix.rotate(lookPolarAngle - Vector3fExt.PI / 2f, new Vector3f(1f, 0, 0));
        viewMatrix.rotate(lookAzimuthAngle, new Vector3f(0, -1f, 0));
        viewMatrix.translate(eyePosition.getReverse().getAsVector3f());
    }

    private void setUpProjectionMatrix() {
        projectionMatrix = new Matrix4f();
        final float fieldOfView = 60f;  // TODO from config
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

    public void initWorld() {   // TODO from scene assets
        skyBox = new SkyBox(eyePosition, colorMapIndices.get(TEXTURE_ASSET_KEYS.SPACE_CLOUDS),
                normalMapIndices.get(TEXTURE_ASSET_KEYS.BLUE));

        /*avatar = new Cuboid(new Vector3fExt(0, 0, 0), 0, 0, 0, false,
                Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.BLUE, Material.CHROME,
                colorMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE), normalMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE),
                0.5f, 0.5f, 0.5f);*/
        avatar = new Sphere(new Vector3fExt(0, 4f, 0), 0, 0, 0,
                Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.BLUE, Material.PEARL,
                colorMapIndices.get(TEXTURE_ASSET_KEYS.MTL_TRIM01),
                normalMapIndices.get(TEXTURE_ASSET_KEYS.MTL_TRIM01), 0.4f);

        /*final float childObjectSize = 0.3f;
        avatar.addChildObject(new Cuboid(new Vector3fExt(-2f, 0, 0), 0, 0,
                0, Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.BLUE,
                Material.CHROME, colorMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE),
                normalMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE), childObjectSize, childObjectSize, childObjectSize));
        avatar.addChildObject(new Cuboid(new Vector3fExt(-1f, -1f, 0), 0, 0,
                0, Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.BLUE,
                Material.CHROME, colorMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE),
                normalMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE), childObjectSize, childObjectSize, childObjectSize));
        avatar.addChildObject(new Cuboid(new Vector3fExt(1f, -1f, 0), 0, 0,
                0, Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.BLUE,
                Material.CHROME, colorMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE),
                normalMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE), childObjectSize, childObjectSize, childObjectSize));
        avatar.addChildObject(new Cuboid(new Vector3fExt(2f, 0, 0), 0, 0,
                0, Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.BLUE,
                Material.CHROME, colorMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE),
                normalMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE), childObjectSize, childObjectSize, childObjectSize));*/

        staticObjects.add(new HeightMapSeamless(new Vector3fExt(0, -5f, 0), 0,
                0, 0, Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.BLUE,
                Material.PEARL, colorMapIndices.get(TEXTURE_ASSET_KEYS.BROKEN),
                normalMapIndices.get(TEXTURE_ASSET_KEYS.BROKEN), 40f, 40f, 1f));

        /*staticObjects.add(new Rectangle(new Vector3fExt(0, 0, 0), 0, 0,
                (float) Math.toRadians(45), Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR, ColorVector.BLUE,
                Material.PEARL, colorMapIndices.get(TEXTURE_ASSET_KEYS.CRACKED),
                normalMapIndices.get(TEXTURE_ASSET_KEYS.CRACKED), 100f, 100f, false));*/

        // 3D cube grid TODO from level assets
        /*final int n = 3;
        final float d = 10f;
        final float a = 4f;
        final float o = d;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    staticObjects.add(new Cuboid(new Vector3fExt(o - i * d, o + j * d, o - k * d),
                            0, 0, 0,
                            Vector3fExt.NULL_VECTOR, Vector3fExt.NULL_VECTOR,
                            ColorVector.BLUE, Material.CHROME,
                            colorMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE),
                            normalMapIndices.get(TEXTURE_ASSET_KEYS.MANHOLE),
                            a, a, a));
                }
            }
        }*/

        // Spheres
        final int n = 5;  // TODO from level assets
        final float d = 4f;
        final float h = 2f;
        float r;
        for (int i = 0; i < n; i++) {
            r = 0.2f + (float) (0.3f * Math.random());
            inertObjects.add(new Sphere(Vector3fExt.randomVector(0.01f).getSumWith(i * d, h, i * d),
                    0, (float) Math.toRadians(120), 0, Vector3fExt.NULL_VECTOR,
                    Vector3fExt.NULL_VECTOR, ColorVector.BLUE, Material.SILVER,
                    colorMapIndices.get(TEXTURE_ASSET_KEYS.MTL_TRIM01),
                    normalMapIndices.get(TEXTURE_ASSET_KEYS.MTL_TRIM01), r));
        }

        for (final Object3d staticObject : staticObjects) {
            staticObject.setFixedInSpace(true);
        }
    }

    private void readInput() {
        glfwPollEvents();

        // Check if LMB has been pressed
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS) {
            isMouseLocked = true;
        }

        // Check LMB click position
        if (isMouseLocked) {
            // TODO what you do on click   
            isMouseLocked = false;
        }

        final DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
        final DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

        glfwGetCursorPos(window, x, y);
        x.rewind();
        y.rewind();

        double newMouseX = Math.max(0, Math.min(screenDimension.width, x.get()));
        double newMouseY = Math.max(0, Math.min(screenDimension.height, y.get()));

        double dX = newMouseX - prevMouseX;
        double dY = newMouseY - prevMouseY;
        prevMouseX = newMouseX;
        prevMouseY = newMouseY;

        // Place cursor to the other side of the screen if it has exited on a side
        if (newMouseX == screenDimension.width - 1) {
            glfwSetCursorPos(window, 1, newMouseY);
        }
        if (newMouseX == 0) {
            glfwSetCursorPos(window, screenDimension.width - 2, newMouseY);
        }
        if (newMouseY == screenDimension.height - 1) {
            glfwSetCursorPos(window, newMouseX, 1);
        }
        if (newMouseY == 0) {
            glfwSetCursorPos(window, newMouseX, screenDimension.height - 2);
        }

        if (dX < MOUSE_D_TRIM && dY < MOUSE_D_TRIM) {
            targetPolarAngle += dY * POLAR_VIEW_SPEED;
            targetAzimuthAngle -= dX * AZIMUTH_VIEW_SPEED;
            targetPolarAngle = (float) Math.min(Math.PI, Math.max(0, targetPolarAngle));
            lookPolarAngle += EYE_ROT_FOLLOW_FACTOR * (targetPolarAngle - lookPolarAngle);
            lookAzimuthAngle += EYE_ROT_FOLLOW_FACTOR * (targetAzimuthAngle - lookAzimuthAngle);
        }

        lookDirection = new Vector3fExt(lookPolarAngle, lookAzimuthAngle, Vector3fExt.Y_UNIT_VECTOR, Vector3fExt.Z_UNIT_VECTOR.getReverse());
        rightDirection = lookDirection.getCrossProductWith(Vector3fExt.Y_UNIT_VECTOR).getNormalized();

        velocityTarget.reset();
        isVTargetSet = false;

        // Read Keyboard events
        if (InputHandler.isKeyDown(GLFW_KEY_W)) {
            velocityTarget.add(lookDirection.getMultipliedBy(FWD_CONTROL_V));
            isVTargetSet = true;
            inputHistory.add(new InputHistoryElement(InputHistoryElement.KEY_FWD, newLoopStartTime));
        }
        if (InputHandler.isKeyDown(GLFW_KEY_S)) {
            velocityTarget.add(lookDirection.getMultipliedBy(-FWD_CONTROL_V));
            isVTargetSet = true;
            inputHistory.add(new InputHistoryElement(InputHistoryElement.KEY_BP, newLoopStartTime));
        }
        if (InputHandler.isKeyDown(GLFW_KEY_D)) {
            velocityTarget.add(rightDirection.getMultipliedBy(SIDE_CONTROL_V));
            isVTargetSet = true;
            inputHistory.add(new InputHistoryElement(InputHistoryElement.KEY_RIGHT, newLoopStartTime));
        }
        if (InputHandler.isKeyDown(GLFW_KEY_A)) {
            velocityTarget.add(rightDirection.getMultipliedBy(-SIDE_CONTROL_V));
            isVTargetSet = true;
            inputHistory.add(new InputHistoryElement(InputHistoryElement.KEY_LEFT, newLoopStartTime));
        }
        if (InputHandler.isKeyDown(GLFW_KEY_SPACE)) {
            velocityTarget.add(upVector.getMultipliedBy(VERT_CONTROL_V));
            isVTargetSet = true;
            inputHistory.add(new InputHistoryElement(InputHistoryElement.KEY_SPACE, newLoopStartTime));
        }

        if (InputHandler.isKeyDown(GLFW_KEY_C)) {
            velocityTarget.add(upVector.getMultipliedBy(-VERT_CONTROL_V));
            isVTargetSet = true;
        }
        /*if (InputHandler.isKeyDown(GLFW_KEY_R)) { // TODO fit to speed-based pos updating
            avatar.setPosition(RESET_POSITION);
            avatar.resetForce();
        }*/
        if (InputHandler.isKeyDown(GLFW_KEY_F)) {
            dashTarget.add(avatar.getPosition().getSumWith(lookDirection.getWithLength(DASH_DISTANCE)));
            isDashTargetSet = true;
        }
        if (InputHandler.isKeyDown(GLFW_KEY_P)) {
            takeScreenshot(screenDimension);
        }
        if (isOutputStreamSet) {
            if (InputHandler.isKeyDown(GLFW_KEY_W)) {
                try {
                    outStream.write(GLFW_KEY_W);
                    System.out.println(GLFW_KEY_W + " written to serial port.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (InputHandler.isKeyDown(GLFW_KEY_A)) {
                try {
                    outStream.write(GLFW_KEY_A);
                    System.out.println(GLFW_KEY_A + " written to serial port.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (InputHandler.isKeyDown(GLFW_KEY_S)) {
                try {
                    outStream.write(GLFW_KEY_S);
                    System.out.println(GLFW_KEY_S + " written to serial port.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (InputHandler.isKeyDown(GLFW_KEY_D)) {
                try {
                    outStream.write(GLFW_KEY_D);
                    System.out.println(GLFW_KEY_D + " written to serial port.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (InputHandler.isKeyDown(GLFW_KEY_M)) {
                try {
                    outStream.write(GLFW_KEY_M);
                    System.out.println(GLFW_KEY_M + " written to serial port.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateEye() {
        eyePosTemp = avatar.getPosition().getSumWith(lookDirection.getReverse().getMultipliedBy(TPS_DISTANCE_Z))
                .getSumWith(Vector3fExt.Y_UNIT_VECTOR.getMultipliedBy(TPS_DISTANCE_Y));
        eyePosition.add(eyePosTemp.getThisMinus(eyePosition).getMultipliedBy(EYE_FOLLOW_FACTOR));
        if (isEyeIdleMovementEnabled) {
            eyePosition.add((new Vector3fExt(0,
                    (float) sin(EYE_IDLE_ANGULAR_FREQUENCY_RAD_PER_NS * System.nanoTime()), 0))
                    .getMultipliedBy(EYE_IDLE_FACTOR));
        }
    }

    private void updateWorld() {
        avatar.resetForce();
        if (isVTargetSet) avatar.addVDriveForce(velocityTarget, V_DRIVE_FACTOR);
        if (isDashTargetSet) avatar.addSpringForce(dashTarget, Object3d.DEFAULT_STIFFNESS);
        avatar.addForce(dashTarget.getThisMinus(avatar.getPosition()).getWithLength(1000000000000000f));
        if (isGravityOn) avatar.addForce(new Vector3fExt(0,
                -Object3d.GRAVITY_ACCEL_EARTH * avatar.getMass(), 0));
        avatar.addMediumForce(Object3d.DEFAULT_VISCOSITY);

        // Apply collision between inert objects
        for (int i = 0; i < inertObjects.size(); i++) {
            for (int j = 0; j < inertObjects.size(); j++) {
                if (i != j) {
                    inertObjects.get(i).addCollisionForceBy(inertObjects.get(j));
                }
            }
        }

        // Apply avatar's attributes according to collision
        for (final Object3d staticObject : staticObjects) {
            avatar.addCollisionForceBy(staticObject);
        }
        for (final Object3d inertObject : inertObjects) {
            avatar.addCollisionForceBy(inertObject);
        }

        //lightPosition = Vector3fExt.rotatingVectorXZ(0.0000000006f * System.nanoTime(), 30f, new Vector3fExt(0, 40f, 0));
        lightPosition = new Vector3fExt(0f, 100f, 0f);    // TODO from scene description
        //lightPosition = avatar.getPosition();

        for (final Object3d inertObject : inertObjects) {
            inertObject.resetForce();

            if (isGravityOn)
                inertObject.addForce(new Vector3fExt(0, -Object3d.GRAVITY_ACCEL_EARTH * inertObject.getMass(), 0));

            for (final Object3d staticObject : staticObjects) {
                inertObject.addCollisionForceBy(staticObject);
            }

            inertObject.addCollisionForceBy(avatar);    // TODO make counter-forces in one round

            if (InputHandler.isKeyDown(GLFW_KEY_G)) {
                inertObject.addSpringForce(avatar.getPosition(), Object3d.DEFAULT_STIFFNESS);
                inertObject.addMediumForce(Object3d.DEFAULT_VISCOSITY);
                //inertObjects.get(i).addGravitationalForce(player.getCenter(), player.getMass());  // TODO from game config
            }
            //inertObjects.get(i).addSpringForce(player.getStiffness(), player.getCenter());    // TODO from game config
            inertObject.update(Vector3fExt.NULL_VECTOR);
        }

        // Update object properties according to the calculated forces
        final Vector3fExt dashVr = isDashTargetSet ? lookDirection.getWithLength(DASH_DISTANCE) : Vector3fExt.NULL_VECTOR;
        skyBox.setPosition(eyePosition);
        skyBox.calculateModelMatrix();
        avatar.update(dashVr);  // TODO add collision
        for (final Object3d staticObject : staticObjects) {
            staticObject.update(Vector3fExt.NULL_VECTOR);    //new Matrix4f());
        }

        isDashTargetSet = false;
        dashTarget.reset();
    }

    public void run() {
        firstStartTime = System.currentTimeMillis();
        while (!glfwWindowShouldClose(window)) {
            executeOneCycle();
        }
        destroy();
    }

    private void executeOneCycle() {
        newLoopStartTime = System.nanoTime();
        readInput();
        updateWorld();
        updateEye();
        setUpProjectionMatrix();
        setUpViewMatrix();
        render();

        final long loopTime = System.nanoTime() - newLoopStartTime;
        final long framesPerSec = Math.round(1000000000. / ((double) loopTime));
        if (System.nanoTime() - prevLoopEndTime > 0.1 * 1000000000) {
            System.out.println("fps: " + framesPerSec + ", T = " + 0.000001 * loopTime + " ms");
        }
        prevLoopEndTime = System.nanoTime();
        Matrix4f.mul(viewMatrix, projectionMatrix, prevVPMatrix);
    }

    private void render() {
        if (isRayTracingModeEnabled) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glUseProgram(rayTracingProgramIndex);
            glUniform3f(rayTracingUniformIndices[U_EYEPOSITION_MAP], eyePosTemp.getX(), eyePosTemp.getY(), eyePosTemp.getZ());
            glUniform3f(rayTracingUniformIndices[U_LIGHTPOSITION_MAP], lightPosition.getX(), lightPosition.getY(), lightPosition.getZ());
            glUniform3f(rayTracingUniformIndices[U_LOOKDIR_MAP], lookDirection.getX(), lookDirection.getY(), lookDirection.getZ());
            glUniform3f(rayTracingUniformIndices[U_RIGHTDIR_MAP], rightDirection.getX(), rightDirection.getY(), rightDirection.getZ());
            glUniform3f(rayTracingUniformIndices[U_AVATARPOS_MAP], avatar.getPosition().getX(), avatar.getPosition().getY(), avatar.getPosition().getZ());
            glUniform2f(rayTracingUniformIndices[U_SCREENDIM_MAP], (float) screenDimension.getWidth(), (float) screenDimension.getHeight());

            glActiveTexture(GL_TEXTURE0 + NORMALMAP_TEXTURE_UNIT);
            glBindTexture(GL_TEXTURE_2D, avatar.getNormalMapTexIndex());
            final int normalMapUnifIndex = glGetUniformLocation(rayTracingProgramIndex, NORMAL_MAP_LABEL);
            glUniform1i(normalMapUnifIndex, NORMALMAP_TEXTURE_UNIT);

            postProcessQuad.render(ignorerUniformIndices, viewMatrix, projectionMatrix);
            exitOnGLError("at rendering ray tracing screen quad.");

            glUseProgram(0);
            exitOnGLError("glUseProgram detach in ray tracing render cycle");

            glfwSwapBuffers(window);
        } else {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            if (isPostProcessingEnabled) preparePostProcessing();   // TODO DoF as a separate, 1st GPU pass

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(skyBoxProgramIndex);
            skyBox.render(skyBoxUniformIndices, viewMatrix, projectionMatrix);

            glUseProgram(defaultProgramIndex);
            exitOnGLError("in " + this.getClass().getSimpleName() + " at glUseProgram(defaultProgramIndex)");
            glUniform3f(defaultUniformIndices[U_EYEPOSITION_MAP], eyePosTemp.getX(), eyePosTemp.getY(), eyePosTemp.getZ());
            glUniform3f(defaultUniformIndices[U_LIGHTPOSITION_MAP], lightPosition.getX(), lightPosition.getY(), lightPosition.getZ());

            renderWorldObjects(defaultUniformIndices);

            if (isShadowsEnabled) renderShadows();
            if (isPostProcessingEnabled) postProcess();

            glUseProgram(0);
            exitOnGLError("glUseProgram detach in render cycle");

            glfwSwapBuffers(window);
        }
    }

    private void renderShadows() {
        glUseProgram(defaultProgramIndex);
        exitOnGLError("in " + this.getClass().getSimpleName() + " at glUseProgram() at drawing shadow volumes");

        if (!isShadowVolumesVisible) {
            glColorMask(false, false, false, false);
        }
        glDepthMask(false);
        glEnable(GL_CULL_FACE);

        glClearStencil(0);
        glClear(GL_STENCIL_BUFFER_BIT);
        glEnable(GL_STENCIL_TEST);

        glEnable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(0.0f, 100.0f);

        renderShadowVolumes(defaultUniformIndices, viewMatrix, projectionMatrix, lightPosition, LIGHT_POSITION);   // TODO with bare color shader

        glDisable(GL_POLYGON_OFFSET_FILL);
        glDisable(GL_CULL_FACE);
        glColorMask(true, true, true, true);
        glDepthMask(true);

        glStencilFunc(GL_NOTEQUAL, 0x0, 0xff);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);

        glDepthFunc(GL_LEQUAL);

        glUseProgram(monoScreenProgramIndex);
        exitOnGLError("glUseProgram - at switching to monoScreenProgram");

        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shadowQuad.render(ignorerUniformIndices, viewMatrix, projectionMatrix);

        glDisable(GL_STENCIL_TEST);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    private void preparePostProcessing() {
        GL30.glBindFramebuffer(GL_FRAMEBUFFER, velocityFrameBuffer1Index);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(velocityMap1ProgramIndex);
        //renderWorldObjects(velocityMap1ProgramIndex, velocityMap1UniformIndices); TODO fix shader performance

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, velocityFrameBuffer2Index);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(velocityMap2ProgramIndex);
        //renderWorldObjects(velocityMap1ProgramIndex, velocityMap1UniformIndices); TODO fix shader performance

        // Rendering scene into the framebuffer TODO uncomment after fixing shader performance
        //glViewport(0, 0, screenDimension.width, screenDimension.height); // Render on the whole framebuffer, complete from the lower left corner to the upper right
        //glBindTexture(GL_TEXTURE_2D, 0);
        GL30.glBindFramebuffer(GL_FRAMEBUFFER, postProcessFrameBufferIndex);
        //GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBufferIndex);

        exitOnGLError("binding framebuffer in cycle");
    }

    private void postProcess() {
        glActiveTexture(GL_TEXTURE0 + DEPTH_MAP_TEXTURE_UNIT);
        glBindTexture(GL_TEXTURE_2D, depthTexIndex);
        glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, 0, 0, screenDimension.width, screenDimension.height, 0);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(postProcessProgramIndex);

        // Bind rendered texture
        glActiveTexture(GL_TEXTURE0 + POSTPROCESS_MAP_TEXTURE_UNIT);
        glBindTexture(GL_TEXTURE_2D, postProcessTexIndex);
        glUniform1i(postProcessMapUnifIndex, POSTPROCESS_MAP_TEXTURE_UNIT);

        // Bind depth texture
        glActiveTexture(GL_TEXTURE0 + DEPTH_MAP_TEXTURE_UNIT);
        glBindTexture(GL_TEXTURE_2D, depthTexIndex);
        glUniform1i(depthMapUnifIndex, DEPTH_MAP_TEXTURE_UNIT);

        // Bind eyeVelocity maps
        glActiveTexture(GL_TEXTURE0 + VELOCITY_MAP1_TEXTURE_UNIT);
        glBindTexture(GL_TEXTURE_2D, velocityTex1Index);
        glUniform1i(velocityMap1UnifIndex, VELOCITY_MAP1_TEXTURE_UNIT);
        glActiveTexture(GL_TEXTURE0 + VELOCITY_MAP2_TEXTURE_UNIT);
        glBindTexture(GL_TEXTURE_2D, velocityTex2Index);
        glUniform1i(velocityMap2UnifIndex, VELOCITY_MAP2_TEXTURE_UNIT);

        final Matrix4f VPMatrixInv = new Matrix4f();
        Matrix4f.mul(viewMatrix, projectionMatrix, VPMatrixInv);
        VPMatrixInv.invert();
        VPMatrixInv.store(matrix44Buffer);
        matrix44Buffer.flip();
        glUniformMatrix4fv(vpMatrixInvIndex, false, matrix44Buffer);

        prevVPMatrix.store(matrix44Buffer);
        matrix44Buffer.flip();
        glUniformMatrix4fv(prevVPMatrixIndex, false, matrix44Buffer);

        glUniform3f(eyeVelocityIndex, eyeVelocity.getX(), eyeVelocity.getY(), eyeVelocity.getZ());
        glUniform3f(lookVectorIndex, lookDirection.getX(), lookDirection.getY(), lookDirection.getZ());

        postProcessQuad.render(postProcessUniformIndices, viewMatrix, projectionMatrix);
    }

    private void renderWorldObjects(int[] uniformIndices) {
        for (final Object3d staticObject : staticObjects) {
            staticObject.render(uniformIndices, viewMatrix, projectionMatrix);
        }
        for (final Object3d inertObject : inertObjects) {
            inertObject.render(uniformIndices, viewMatrix, projectionMatrix);
        }
        avatar.render(uniformIndices, viewMatrix, projectionMatrix);
    }

    private void renderShadowVolumes(int[] uniformIndices, Matrix4f vMatrix, Matrix4f pMatrix, Vector3fExt lightParam,
                                     ShadowVolume.LIGHT_PARAM_TYPE lightType) {
        if (staticShadowVolumeBO == null) {
            final ShadowVolume staticShadowVolumeData = new ShadowVolume(lightParam, lightType);
            for (final Object3d staticObject : staticObjects) {
                staticShadowVolumeData.addData(staticObject.getShadowVertices(lightParam, lightType));
            }
            staticShadowVolumeBO = new ShadowVolumeBO(staticShadowVolumeData);
        }
        staticShadowVolumeBO.render(uniformIndices, vMatrix, pMatrix, new DummyObject3d());

        for (final Object3d inertObject : inertObjects) {
            inertObject.renderDynamicShadow(uniformIndices, vMatrix, pMatrix, lightParam);
        }

        avatar.renderDynamicShadow(uniformIndices, vMatrix, pMatrix, lightParam);
    }

    private void destroy() {
        for (final Object3dBO prototype : prototypes) {
            prototype.destroy();
        }

        avatar.destroyShadowVolume();
        // TODO destroy all shadow volumes

        // Delete shaders
        glUseProgram(0);
        glDeleteProgram(defaultProgramIndex);
        glDeleteProgram(postProcessProgramIndex);
        glDeleteProgram(velocityMap1ProgramIndex);
        glDeleteProgram(velocityMap2ProgramIndex);

        // TODO delete all other programs too

        // Delete textures
        for (Map.Entry<TEXTURE_ASSET_KEYS, Integer> entry : colorMapIndices.entrySet()) {
            glDeleteTextures(entry.getValue());
        }
        for (Map.Entry<TEXTURE_ASSET_KEYS, Integer> entry : normalMapIndices.entrySet()) {
            glDeleteTextures(entry.getValue());
        }
        glDeleteTextures(postProcessTexIndex);
        glDeleteTextures(depthTexIndex);

        exitOnGLError("Destroying renderer");

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
        debugProc.free();
    }
}
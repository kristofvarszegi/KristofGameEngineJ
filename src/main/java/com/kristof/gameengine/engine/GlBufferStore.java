package com.kristof.gameengine.engine;

import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.kristof.gameengine.engine.Config.RENDER_MODE.RAY_TRACING;
import static com.kristof.gameengine.engine.GLConstants.*;
import static com.kristof.gameengine.engine.GLConstants.NORMAL_MAP_LABEL;
import static com.kristof.gameengine.util.Utils.*;
import static com.kristof.gameengine.util.Utils.loadShader;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;

public class GlBufferStore {
    private static final Logger LOGGER = LogManager.getLogger(GlBufferStore.class);

    int defaultProgramIndex;
    int skyBoxProgramIndex;
    int monoScreenProgramIndex;
    int velocityMap1ProgramIndex;
    int velocityMap2ProgramIndex;
    int postProcessProgramIndex;
    int rayTracingProgramIndex;
    final int[] defaultUniformIndices;
    final int[] skyBoxUniformIndices;
    final int[] ignorerUniformIndices;
    final int[] velocityMap1UniformIndices;
    final int[] velocityMap2UniformIndices;
    final int[] postProcessUniformIndices;
    final int[] rayTracingUniformIndices;
    int postProcessMapUnifIndex;
    int depthMapUnifIndex;
    int velocityMap1UnifIndex;
    int velocityMap2UnifIndex;
    int vpMatrixInvIndex;
    int prevVPMatrixIndex;
    int eyeVelocityIndex;
    int lookVectorIndex;
    final HashMap<MiscConstants.TEXTURE_ASSET_KEYS, Integer> colorMapIndices;
    final HashMap<MiscConstants.TEXTURE_ASSET_KEYS, Integer> normalMapIndices;
    int postProcessTexIndex;
    int depthTexIndex;
    int velocityTex1Index;
    int velocityTex2Index;
    int postProcessFrameBufferIndex;
    int depthBufferIndex;
    int stencilBufferIndex;
    int depthStencilBufferIndex;
    int velocityFrameBuffer1Index;
    int velocityDepthBuffer1Index;
    int velocityFrameBuffer2Index;
    int velocityDepthBuffer2Index;

    private GlBufferStore() {
        defaultUniformIndices = null;
        skyBoxUniformIndices = null;
        ignorerUniformIndices = null;
        velocityMap1UniformIndices = null;
        velocityMap2UniformIndices = null;
        postProcessUniformIndices = null;
        rayTracingUniformIndices = null;
        colorMapIndices = null;
        normalMapIndices = null;
    }

    GlBufferStore(Config config, UI ui) {
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

        loadShaders(config);
        exitOnGLError("loadShaders()");

        if (config.isPostProcessingEnabled) {
            setUpDepthOfFieldBuffer(ui.screenDimension);
            setUpVelocityBuffers(ui.screenDimension);
        }

        colorMapIndices = new HashMap<>();
        normalMapIndices = new HashMap<>();
        loadBuiltInTextures();
        exitOnGLError("renderer constructor - loadTextures()");
    }

    private void loadShaders(Config config) {
        loadDefaultShaders();
        loadSkyBoxShaders();    // TODO shadow color via uniform
        loadMonoScreenShaders();
        if (config.isPostProcessingEnabled) {
            loadPostProcessShaders();
            loadVelocityMapShaders();
        }
        if (config.renderMode == RAY_TRACING) loadRayTracingShaders();
        exitOnGLError("loadShaders()");
    }

    // TODO merge into one single shader loader method
    private void loadDefaultShaders() {
        final int vertexShaderIndex = loadShader("src/main/resources/shaders/default_shader.vert", GL_VERTEX_SHADER);
        final int fragmentShaderIndex = loadShader("src/main/resources/shaders/default_shader.frag", GL_FRAGMENT_SHADER);
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
        final int vertexShaderIndex = loadShader("src/main/resources/shaders/default_shader.vert", GL_VERTEX_SHADER);
        final int fragmentShaderIndex = loadShader("src/main/resources/shaders/skybox_shader.frag", GL_FRAGMENT_SHADER);
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
        final int vertexShaderIndex = loadShader("src/main/resources/shaders/screenquad_shader.vert", GL_VERTEX_SHADER);
        final int fragmentShaderIndex = loadShader("src/main/resources/shaders/shadow_shader.frag", GL_FRAGMENT_SHADER);
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
        final int vertexShaderIndex = loadShader("src/main/resources/shaders/screenquad_shader.vert", GL_VERTEX_SHADER);
        final int fragmentShaderIndex = loadShader("src/main/resources/shaders/post_process_shader.frag", GL_FRAGMENT_SHADER);

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
        int vertexShaderIndex = loadShader("src/main/resources/shaders/velocitymap1_shader.vert", GL_VERTEX_SHADER);
        int fragmentShaderIndex = loadShader("src/main/resources/shaders/velocitymap_shader.frag", GL_FRAGMENT_SHADER);

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
        vertexShaderIndex = loadShader("src/main/resources/shaders/velocitymap2_shader.vert", GL_VERTEX_SHADER);
        fragmentShaderIndex = loadShader("src/main/resources/shaders/velocitymap_shader.frag", GL_FRAGMENT_SHADER);
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
        final int vertexShaderIndex = loadShader("src/main/resources/shaders/screenquad_shader.vert", GL_VERTEX_SHADER);
        final int fragmentShaderIndex = loadShader("src/main/resources/shaders/raytracing_shader.frag", GL_FRAGMENT_SHADER);

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

        rayTracingUniformIndices[U_EYEPOSITION_MAP] = glGetUniformLocation(rayTracingProgramIndex, EYE_POS_LABEL);
        rayTracingUniformIndices[U_LIGHTPOSITION_MAP] = glGetUniformLocation(rayTracingProgramIndex, LIGHT_POS_LABEL);
        rayTracingUniformIndices[U_LOOKDIR_MAP] = glGetUniformLocation(rayTracingProgramIndex, LOOK_DIRECTION_LABEL);
        rayTracingUniformIndices[U_RIGHTDIR_MAP] = glGetUniformLocation(rayTracingProgramIndex, RIGHT_DIRECTION_LABEL);
        rayTracingUniformIndices[U_AVATARPOS_MAP] = glGetUniformLocation(rayTracingProgramIndex, AVATAR_POS_LABEL);
        rayTracingUniformIndices[U_SCREENDIM_MAP] = glGetUniformLocation(rayTracingProgramIndex, SCREEN_DIM_LABEL);
    }

    private void setUpDepthOfFieldBuffer(Dimension screenDimension) {
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
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER,
                depthStencilBufferIndex);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, GL_RENDERBUFFER,
                depthStencilBufferIndex);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            final String message = "Framebuffer incomplete";
            LOGGER.error(message);
            throw new RuntimeException(message);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void setUpVelocityBuffers(Dimension screenDimension) {
        // 1st kinematics.eyeVelocity buffer
        velocityTex1Index = glGenTextures();
        velocityFrameBuffer1Index = glGenFramebuffers();
        velocityDepthBuffer1Index = glGenRenderbuffers();

        glBindTexture(GL_TEXTURE_2D, velocityTex1Index);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, screenDimension.width, screenDimension.height, 0, GL_RGB,
                GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glBindTexture(GL_TEXTURE_2D, 0);

        glBindRenderbuffer(GL_RENDERBUFFER, velocityDepthBuffer1Index);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, screenDimension.width, screenDimension.height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        glBindFramebuffer(GL_FRAMEBUFFER, velocityFrameBuffer1Index);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
                velocityTex1Index, 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER,
                velocityDepthBuffer1Index);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            final String message = "Framebuffer incomplete";
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // 2nd kinematics.eyeVelocity buffer
        velocityTex2Index = glGenTextures();
        velocityFrameBuffer2Index = glGenFramebuffers();
        velocityDepthBuffer2Index = glGenRenderbuffers();

        glBindTexture(GL_TEXTURE_2D, velocityTex2Index);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, screenDimension.width, screenDimension.height, 0, GL_RGB,
                GL_UNSIGNED_BYTE, (ByteBuffer) null);//texBuffer);
        glBindTexture(GL_TEXTURE_2D, 0);

        glBindRenderbuffer(GL_RENDERBUFFER, velocityDepthBuffer2Index);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, screenDimension.width, screenDimension.height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        glBindFramebuffer(GL_FRAMEBUFFER, velocityFrameBuffer2Index);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, velocityTex2Index,
                0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER,
                velocityDepthBuffer2Index);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            final String message = "2nd velocity buffer incomplete";
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void loadBuiltInTextures() {    // TODO from config
        final int colorMapTextureUnit = GL_TEXTURE0 + COLORMAP_TEXTURE_UNIT;
        colorMapIndices.put(MiscConstants.TEXTURE_ASSET_KEYS.WHITE,
                loadPNGTexture("src/main/resources/textures/white_colormap.png", colorMapTextureUnit));
        colorMapIndices.put(MiscConstants.TEXTURE_ASSET_KEYS.BROKEN,
                loadPNGTexture("src/main/resources/textures/ft_broken01_c.png", colorMapTextureUnit));
        colorMapIndices.put(MiscConstants.TEXTURE_ASSET_KEYS.SPACE_CLOUDS,
                loadPNGTexture("src/main/resources/textures/spaceclouds_colormap.png", colorMapTextureUnit));
        colorMapIndices.put(MiscConstants.TEXTURE_ASSET_KEYS.DIAGONAL,
                loadPNGTexture("src/main/resources/textures/ft_diagonal01_c.png", colorMapTextureUnit));
        colorMapIndices.put(MiscConstants.TEXTURE_ASSET_KEYS.MANHOLE,
                loadPNGTexture("src/main/resources/textures/manhole_256x256_colormap.png", colorMapTextureUnit));
        colorMapIndices.put(MiscConstants.TEXTURE_ASSET_KEYS.MTL_FLOOR02,
                loadPNGTexture("src/main/resources/textures/mtl_floor02_c.png", colorMapTextureUnit));
        colorMapIndices.put(MiscConstants.TEXTURE_ASSET_KEYS.MTL_TRIM01,
                loadPNGTexture("src/main/resources/textures/mtl_trim01_c.png", colorMapTextureUnit));
        colorMapIndices.put(MiscConstants.TEXTURE_ASSET_KEYS.REDBRICK,
                loadPNGTexture("src/main/resources/textures/redbrick01_c.png", colorMapTextureUnit));

        final int normalMapTextureUnit = GL_TEXTURE0 + NORMALMAP_TEXTURE_UNIT;
        normalMapIndices.put(MiscConstants.TEXTURE_ASSET_KEYS.BLUE,
                loadPNGTexture("src/main/resources/textures/blue_normalmap.png", normalMapTextureUnit));
        normalMapIndices.put(MiscConstants.TEXTURE_ASSET_KEYS.BROKEN,
                loadPNGTexture("src/main/resources/textures/ft_broken01_n.png", normalMapTextureUnit));
        normalMapIndices.put(MiscConstants.TEXTURE_ASSET_KEYS.DIAGONAL,
                loadPNGTexture("src/main/resources/textures/ft_diagonal01_n.png", normalMapTextureUnit));
        normalMapIndices.put(MiscConstants.TEXTURE_ASSET_KEYS.MANHOLE,
                loadPNGTexture("src/main/resources/textures/manhole_256x256_normalmap.png",
                        normalMapTextureUnit));
        normalMapIndices.put(MiscConstants.TEXTURE_ASSET_KEYS.MTL_FLOOR02,
                loadPNGTexture("src/main/resources/textures/mtl_floor02_n.png", normalMapTextureUnit));
        normalMapIndices.put(MiscConstants.TEXTURE_ASSET_KEYS.MTL_TRIM01,
                loadPNGTexture("src/main/resources/textures/mtl_trim01_n.png", normalMapTextureUnit));
        normalMapIndices.put(MiscConstants.TEXTURE_ASSET_KEYS.REDBRICK,
                loadPNGTexture("src/main/resources/textures/redbrick01_n.png", normalMapTextureUnit));
    }

    void destroy() {
        glUseProgram(0);
        glDeleteProgram(defaultProgramIndex);
        glDeleteProgram(postProcessProgramIndex);
        glDeleteProgram(velocityMap1ProgramIndex);
        glDeleteProgram(velocityMap2ProgramIndex);

        // TODO delete all the other programs too

        for (Map.Entry<MiscConstants.TEXTURE_ASSET_KEYS, Integer> entry : colorMapIndices.entrySet()) {
            glDeleteTextures(entry.getValue());
        }
        for (Map.Entry<MiscConstants.TEXTURE_ASSET_KEYS, Integer> entry : normalMapIndices.entrySet()) {
            glDeleteTextures(entry.getValue());
        }
        glDeleteTextures(postProcessTexIndex);
        glDeleteTextures(depthTexIndex);

        exitOnGLError("Destroying GL buffers");
    }
}

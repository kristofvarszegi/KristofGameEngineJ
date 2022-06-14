#version 330 core

uniform sampler2D u_PostProcessMap;
uniform sampler2D u_DepthMap;
uniform sampler2D u_VelocityMap1;
uniform sampler2D u_VelocityMap2;
uniform mat4 u_VPMatrixInv;
uniform mat4 u_PrevVPMatrix;
uniform vec3 u_EyeVelocity;
uniform vec3 u_LookVector;

in vec4 pass_Position;
in vec2 pass_TexCoord;

out vec4 out_Color;

void main(void) {
    vec2 velocity;
    vec2 velocity1;
    vec2 velocity2;
    float vMag;
    vec4 color = vec4(0.0, 0.0, 0.0, 0.0);
    vec4 currentColor;
    vec2 pxCoord;
    const float eyeBlurRadialSampleCount = 0.0;
    const float eyeBlurFragmentSampleCount = 10.0;
    const float object3dSampleCount = 0.0;
    const float vMagTrim = 0.0004;    // Must apply some trim, otherwise the picture would get blurred at low speeds as well.

    // Depth of Field. The farther the object is, the more the surrounding colors are blended in the current color.
    /*float blur_radius = 1.0;
	vec4 color_surrounding = vec4(0.0, 0.0, 0.0, 0.0);
	const int blur_span = 4;
	int num_blur_px = (2 * blur_span + 1);
	num_blur_px = num_blur_px * num_blur_px;
	for (int ix = -blur_span; ix < blur_span + 1; ix++) {
	    for (int iy = -blur_span; iy < blur_span + 1; iy++) {
		    color_surrounding += texture(u_PostProcessMap, pass_TexCoord + vec2(ix*blur_radius, iy*blur_radius));
		}
	}
    //vec4 ce = texture(u_PostProcessMap, pass_TexCoord + vec2(blur_radius, 0.0));
    const float depth_val = clamp(pow(texture(u_DepthMap, pass_TexCoord).x, 64), 0.0, 1.0);    // The bigger the depth value, the farther the object is.
    color = texture(u_PostProcessMap, pass_TexCoord);
    color = (1.0 - depth_val) * color + depth_val * color_surrounding / num_blur_px;*/

    // Eye motion blur - radial
    pxCoord = pass_TexCoord;
    //float eyeV = length(u_EyeVelocity);
    float eyeV = dot(u_EyeVelocity, u_LookVector);
    velocity = 0.0001 * (2.0 * vec2( gl_FragCoord.x/1366, gl_FragCoord.y/768) -1.0);
    for (int i = 0; i < eyeBlurRadialSampleCount; i++) {
        currentColor = texture(u_PostProcessMap, pxCoord);
        color += currentColor;
        pxCoord += velocity * eyeV;
    }

    // Eye motion blur - fragment velocity
    pxCoord = pass_TexCoord;
    float zOverW = texture(u_DepthMap, pxCoord).x;
    //float zOverW = pow(texture(u_DepthMap, pxCoord).x, 128);
    //float zOverW = 1.0;
    vec4 H = vec4(pxCoord.x * 2.0 - 1.0, pxCoord.y * 2.0 - 1.0, zOverW, 1.0);	// position in NDC
    vec4 D = u_VPMatrixInv * H;
    //vec4 worldPos = u_VPMatrixInv * vec4(pxCoord.x * 2.0 - 1.0, pxCoord.y * 2.0 - 1.0, zOverW, 1.0);
    vec4 worldPos = D;// / D.w;
    vec4 currPos = H;
    vec4 prevPos = u_PrevVPMatrix * worldPos;
    //prevPos /= prevPos.w;
    vMag = 0.002;
    velocity = vMag * (currPos - prevPos).xy;
    if (length(velocity) < vMagTrim) velocity = vec2(0.0, 0.0);
    for (int i = 0; i < eyeBlurFragmentSampleCount; i++) {
        currentColor = texture(u_PostProcessMap, pxCoord);
        color += currentColor;
        pxCoord += velocity;
    }

    // Object motion blur
    pxCoord = pass_TexCoord;
    const float blurRadius = 0.008;
    vMag = 0.01;
    velocity1 = vMag * ((2 * texture(u_VelocityMap1, pxCoord).xy) - vec2(1.0, 1.0));
    //velocity2 = vMag * ((2 * texture(u_VelocityMap2, pxCoord).xy) - vec2(1.0, 1.0));
    //velocity = 0.5 * (velocity1 + velocity2);
    velocity = velocity1;
    if (length(velocity) < 0.005) velocity = vec2(0.0, 0.0);
    for (int i = 0; i < object3dSampleCount; i++) {
        currentColor = texture(u_PostProcessMap, pxCoord);
        color += currentColor;
        pxCoord += velocity;
    }

    color = color / (eyeBlurRadialSampleCount + eyeBlurFragmentSampleCount + object3dSampleCount);

    out_Color = color;
    //out_Color = texture(u_PostProcessMap, pass_TexCoord);
    //out_Color = texture(u_PostProcessMap, pass_TexCoord - 0.0005 * vec2(gl_FragCoord.x, gl_FragCoord.y));
    //out_Color = texture(u_PostProcessMap, pass_TexCoord - 0.0005 * vec2(0.5*(gl_FragCoord.x+1.0), 0.5*(gl_FragCoord.y+1.0)));
    //float depthColor = 1.0 - clamp(pow(texture(u_DepthMap, pass_TexCoord).x, 64), 0.0, 1.0);    out_Color = vec4(depthColor, depthColor, depthColor, 1.0);
    //float depthColor = pow(zOverW, 64);	out_Color = vec4(depthColor, depthColor, depthColor, 1.0);
    //out_Color = texture(u_VelocityMap1, pass_TexCoord);
    //out_Color = vec4(0.4, 0.2, 0.6, 1.0);
    //out_Color = 0.25 * (ce + cw + cn + cs);
    //out_Color = vec4(pass_TexCoord.x, 1.0, pass_TexCoord.y, 1.0);
}

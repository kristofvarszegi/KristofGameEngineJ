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
    /*float blurRadius = 1.0;
	vec4 surroundingColor = vec4(0.0, 0.0, 0.0, 0.0);
	const int blurSpan = 4;
	int blurPxCount = (2 * blurSpan + 1);
	blurPxCount = blurPxCount * blurPxCount;
	for (int ix = -blurSpan; ix < blurSpan + 1; ix++) {
	    for (int iy = -blurSpan; iy < blurSpan + 1; iy++) {
		    surroundingColor += texture(u_PostProcessMap, pass_TexCoord + vec2(ix * blurRadius, iy * blurRadius));
		}
	}
    //vec4 ce = texture(u_PostProcessMap, pass_TexCoord + vec2(blurRadius, 0.0));
    const float depthVal = clamp(pow(texture(u_DepthMap, pass_TexCoord).x, 64), 0.0, 1.0);    // The bigger the depth value, the farther the object is.
    color = texture(u_PostProcessMap, pass_TexCoord);
    color = (1.0 - depthVal) * color + depthVal * surroundingColor / blurPxCount;*/

    // Eye motion blur - radial
    pxCoord = pass_TexCoord;
    float eyeV = dot(u_EyeVelocity, u_LookVector);
    velocity = 0.0001 * (2.0 * vec2(gl_FragCoord.x / 1366.0, gl_FragCoord.y/768) - 1.0);
    for (int i = 0; i < eyeBlurRadialSampleCount; i++) {
        currentColor = texture(u_PostProcessMap, pxCoord);
        color += currentColor;
        pxCoord += velocity * eyeV;
    }

    // Eye motion blur - fragment velocity
    pxCoord = pass_TexCoord;
    float zOverW = texture(u_DepthMap, pxCoord).x;
    vec4 H = vec4(pxCoord.x * 2.0 - 1.0, pxCoord.y * 2.0 - 1.0, zOverW, 1.0);	// Position in NDC
    vec4 D = u_VPMatrixInv * H;
    vec4 worldPos = D;
    vec4 currPos = H;
    vec4 prevPos = u_PrevVPMatrix * worldPos;
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
    velocity = velocity1;
    if (length(velocity) < 0.005) velocity = vec2(0.0, 0.0);
    for (int i = 0; i < object3dSampleCount; i++) {
        currentColor = texture(u_PostProcessMap, pxCoord);
        color += currentColor;
        pxCoord += velocity;
    }

    color = color / (eyeBlurRadialSampleCount + eyeBlurFragmentSampleCount + object3dSampleCount);
    out_Color = color;
}

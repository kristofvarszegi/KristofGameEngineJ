#version 150 core

uniform sampler2D u_ColorMap;
uniform sampler2D u_NormalMap;
uniform vec4 u_Color;
uniform vec4 u_Material;
uniform vec3 u_LightPosition;
uniform vec3 u_Velocity;

in vec3 pass_Position;
in vec3 pass_Normal;
in vec2 pass_TexCoord;
in vec3 pass_Tangent;
in vec4 pass_Color;
in vec3 pass_EyePosition;
in vec3 pass_PrevPosition;

out vec4 out_Color;

void main(void) {
    float emiIntensity = u_Material.w;
    vec3 lightPos = u_LightPosition;
    vec3 biTangent = -cross(pass_Normal, pass_Tangent);
    vec2 texCoord = pass_TexCoord;
    vec3 eyeVectorN = normalize(pass_EyePosition - pass_Position);
    vec3 lightVectorN = normalize(lightPos - pass_Position);
    vec4 colorMapPixel = texture(u_ColorMap, texCoord);

    // Parallax mapping
    const float scale = 0.04;
    const float bias = -0.02;
    float h = scale * texture(u_NormalMap, texCoord).a + bias;  // TODO get and use height maps
    texCoord += h * vec2(dot(eyeVectorN, pass_Tangent), -dot(eyeVectorN, biTangent));

    vec3 normalTanSpace = normalize((2.0 * texture(u_NormalMap, texCoord).rgb) - 1.0);
    vec3 normal = normalTanSpace.x * pass_Tangent + normalTanSpace.y * biTangent + normalTanSpace.z * pass_Normal;
	
    // Diffuse lighting
    float diffIntensity = u_Material.x * max(0.0, dot(normal, normalize(lightPos - pass_Position)));

    // Specular lighting
    vec3 halfway = normalize(lightVectorN + eyeVectorN);
    float specIntensity = u_Material.y * pow(max(0.0, dot(normal, halfway)), u_Material.z);

    float totalIntensity = emiIntensity + diffIntensity + specIntensity;

    // Attenuation
    const float k = 0.005;
    float r = length(u_LightPosition - pass_Position);
    float finalIntensity = emiIntensity + ((diffIntensity + specIntensity) / ((1.0 + k * r) * (1.0 + k * r)));

    // Applying intensity levels: (x1, x2, y1, y2)
    //vec4 levels = vec4(0.75, 0.8, 0.3, 1.0);
    //vec4 levels = vec4(0.45, 0.55, 0.2, 0.8);
    //vec2 level1 = vec2(0.45, 0.2);
    //vec2 level2 = vec2(0.55, 0.7);
    //vec2 level3 = vec2(0.7, 1.0);
    //if (finalIntensity < level1.x) {	// Lowest intersity threshold
    //    finalIntensity = (level1.y / level1.x) * finalIntensity;
    //} else {
    //    if(finalIntensity < level2.x) {
    //        finalIntensity = ((level2.y-level1.y) / (level2.x-level1.x)) * (finalIntensity - level1.x) + level1.y;
    //    } else {
    //        if(finalIntensity < level3.x) {
    //            finalIntensity = ((1.0-level2.y) / (1.0-level2.x)) * (finalIntensity - level2.x) + level2.y;
    //            //finalIntensity = ((level3.y-level2.y) / (level3.x-level2.x)) * (finalIntensity - level2.x) + level2.y;
    //        } else {
    //            finalIntensity = 1.0;
    //            //finalIntensity = ((1.0-level2.y) / (1.0-level2.x)) * (finalIntensity - level2.x) + level2.y;
    //        }
    //    }
    //}

    out_Color = finalIntensity * texture(u_ColorMap, texCoord);
}
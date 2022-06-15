#version 150 core

uniform sampler2D u_ColorMap;
uniform sampler2D u_NormalMap;
uniform vec4 u_Material;

in vec3 pass_mPosition;
in vec3 pass_Normal;
in vec2 pass_TexCoord;
in vec3 pass_Tangent;
in vec3 pass_EyePosition;

out vec4 out_Color;

void main(void) {
	out_Color = texture(u_ColorMap, pass_TexCoord);
}
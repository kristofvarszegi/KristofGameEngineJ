#version 150 core

uniform mat4 u_PMatrix;
uniform mat4 u_VMatrix;
uniform mat4 u_MMatrix;
uniform mat4 u_RMatrix;
uniform vec4 u_Color;
uniform vec3 u_EyePosition;
uniform vec3 u_Velocity;

in vec4 in_Position;
in vec4 in_Normal;
in vec2 in_TexCoord;
in vec4 in_Tangent;

out vec3 pass_Position;
out vec3 pass_Normal;
out vec2 pass_TexCoord;
out vec3 pass_Tangent;
out vec4 pass_Color;
out vec3 pass_EyePosition;

void main(void) {
	gl_Position = u_PMatrix * u_VMatrix * u_MMatrix * in_Position;
	const float T = 1.0;
	vec3 d3 = T * u_Velocity;
	vec4 d4 = vec4(d3.x, d3.y, d3.z, 1.0);
	if (dot(u_Velocity, pass_Normal) < 0.0) gl_Position -= d4;

	pass_Position = vec3(u_MMatrix * in_Position);
	pass_Normal = vec3(u_RMatrix * in_Normal);
	pass_TexCoord = in_TexCoord;
	pass_Tangent = vec3(u_RMatrix * in_Tangent);
	pass_Color = u_Color;
	pass_EyePosition = vec3(u_EyePosition);
}
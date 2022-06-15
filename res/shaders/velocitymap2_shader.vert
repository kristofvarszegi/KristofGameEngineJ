#version 150 core

uniform mat4 u_PMatrix;
uniform mat4 u_VMatrix;
uniform mat4 u_MMatrix;
uniform mat4 u_PrevMMatrix;
uniform mat4 u_RMatrix;

in vec4 in_Position;
in vec4 in_Normal;
in vec2 in_TexCoord;
in vec4 in_Tangent;

out vec3 pass_Velocity;

void main(void) {
	vec4 prevPosition = u_PMatrix * u_VMatrix * u_PrevMMatrix * in_Position;
	vec4 currPosition = u_PMatrix * u_VMatrix * u_MMatrix * in_Position;
	pass_Velocity = 0.5 * (vec3(currPosition - prevPosition) + vec3(1.0, 1.0, 1.0));	// transform to [0, 1]
	if (length(pass_Velocity) < 0.01)pass_Velocity = vec3(0.0, 0.0, 0.0);
	gl_Position = vec4(prevPosition);
}
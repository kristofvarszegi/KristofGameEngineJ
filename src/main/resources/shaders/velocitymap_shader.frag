#version 150 core

in vec3 pass_Velocity;

out vec4 out_Color;

void main(void) {
	out_Color = vec4(pass_Velocity.x, pass_Velocity.y, 0.0, 1.0);
}
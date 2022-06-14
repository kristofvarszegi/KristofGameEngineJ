#version 150 core

in vec4 pass_Position;
in vec2 pass_TexCoord;

out vec4 out_Color;

void main(void) {
	out_Color = vec4(0.0, 0.0, 0.0, 0.6);
}
#version 150 core

in vec4 in_Position;
in vec2 in_TexCoord;

out vec4 pass_Position;
out vec2 pass_TexCoord;

void main(void) {
	gl_Position = in_Position;
	pass_Position = in_Position;
	pass_TexCoord = in_TexCoord;
}
#version 330 core

in vec4 vertexColor;

out vec4 pixelColor;

void main() {
	pixelColor = vertexColor;
}
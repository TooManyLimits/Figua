#version 330 core

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec4 inColor;

uniform mat4 ModelViewMat;
uniform mat4 ProjectionMat;

out vec4 vertexColor;

void main() {
	gl_Position = ProjectionMat * ModelViewMat * vec4(inPos, 1.0);
	vertexColor = inColor;
}
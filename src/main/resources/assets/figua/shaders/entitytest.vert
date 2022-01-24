#version 330
layout (location = 0) in vec3 Pos;
layout (location = 1) in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Pos, 1.0);
    vertexColor = Color;
}

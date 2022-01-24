#version 330

in vec2 vertexUV;
in vec3 vertexNormal;
in vec4 vertexColor;

uniform sampler2D MainTexture;

out vec4 FragColor;

void main() {
    FragColor = texture(MainTexture, vertexUV, 0);
    FragColor *= vertexColor;
    FragColor.rgb += vertexNormal * 0.0000001; //So not compiled out
}

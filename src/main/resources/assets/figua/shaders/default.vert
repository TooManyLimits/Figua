#version 330

layout (location = 0) in vec3 Position;
layout (location = 1) in vec4 Color;
layout (location = 2) in vec2 TextureUV;
layout (location = 3) in vec3 Normal;
layout (location = 4) in int TransformIndex;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

uniform sampler2D TransformTexture;

out vec2 vertexUV;
out vec3 vertexNormal;
out vec4 vertexColor;

void main() {
    int x = (TransformIndex * 4) % 256;
    int y = TransformIndex / 64;
    vec4 col1 = texelFetch(TransformTexture, ivec2(x, y), 0);
    vec4 col2 = texelFetch(TransformTexture, ivec2(x+1, y), 0);
    vec4 col3 = texelFetch(TransformTexture, ivec2(x+2, y), 0);
    vec4 col4 = texelFetch(TransformTexture, ivec2(x+3, y), 0);
    mat4 transformMatrix = mat4(col1, col2, col3, col4);
    gl_Position = ProjMat * ModelViewMat * transformMatrix * vec4(Position, 1.0);

    vec4 normal = inverse(transpose(transformMatrix)) * vec4(Normal, 0.0);

    vertexUV = TextureUV;
    vertexNormal = normal.xyz / length(normal);
    vertexColor = Color;
}

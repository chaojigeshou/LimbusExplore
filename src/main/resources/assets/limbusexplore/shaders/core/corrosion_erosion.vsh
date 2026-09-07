#version 150

in vec3 Position;

out vec2 vUV;

void main() {
    // 顶点按 -1..1 传入（全屏 quad），vUV 归到 0..1
    vUV = Position.xy * 0.5 + 0.5;
    gl_Position = vec4(Position.xy, 0.0, 1.0);
}

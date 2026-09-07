#version 150

uniform sampler2D SamplerFlash;
uniform sampler2D SamplerBar;
uniform float Time;
uniform float Intensity;
uniform float Mode;

in vec2 vUV;
out vec4 fragColor;

float hash13(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

// Worley：F1/F2 双最近点距离，细胞边界用 F2-F1 的差
vec2 worley(vec2 p) {
    vec2 cell = floor(p);
    vec2 f = fract(p);
    float d1 = 8.0;
    float d2 = 8.0;
    for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
            vec2 g = cell + vec2(float(i), float(j));
            vec2 o = g + vec2(hash13(g), hash13(g + 13.7)) - f;
            float d = dot(o, o);
            if (d < d1) {
                d2 = d1;
                d1 = d;
            } else if (d < d2) {
                d2 = d;
            }
        }
    }
    return vec2(sqrt(d1), sqrt(d2));
}

void main() {
    vec2 uv = vUV;
    bool flash = Mode > 0.5;

    // 基础图：拉伸全屏（一瞬间图 / 持续图由 Java 端选）
    vec3 base = texture(flash ? SamplerFlash : SamplerBar, uv).rgb;

    // Worley 12x12 细胞，UV 按细胞 id 偏移后再采样 → 图案被细胞扭曲成龟裂纹理
    vec2 w = worley(uv * 12.0);
    float cellEdge = w.y - w.x;
    float cellId = hash13(floor(uv * 12.0));
    vec3 warped = texture(flash ? SamplerFlash : SamplerBar,
                          uv + (vec2(0.06, -0.04)) * (cellId - 0.5)).rgb;

    // 侵蚀从屏幕边缘向中心推进（时间驱动）
    float distC = length(uv - vec2(0.5));
    float invade = 1.0 - smoothstep(0.0, 0.8, distC * (1.0 + Time * 0.9));

    // 一瞬间：边界爆闪（快频）；持续：边界缓慢呼吸闪烁
    float flicker = 0.5 + 0.5 * sin(Time * (flash ? 22.0 : 1.6) + cellId * 6.28318);
    float edge = (1.0 - smoothstep(0.0, 0.14, cellEdge)) * (0.35 + 0.65 * flicker);

    // 一瞬间：中心脉冲环（1.4s 内向外扩散一圈）
    float pulse = 0.0;
    if (flash) {
        float p = clamp(Time / 1.4, 0.0, 1.0);
        pulse = smoothstep(0.05, 0.0, abs(distC - p * 0.7)) * (1.0 - p);
    }

    vec3 violet = vec3(0.55, 0.25, 0.75);
    vec3 color = mix(base, violet, 0.45 * invade);
    color = mix(color, warped, 0.35);
    color *= 0.75 + 0.25 * cellId;                       // 细胞明暗
    color += vec3(0.95, 0.5, 1.0) * edge * invade * Intensity * 0.8;  // 边界紫光
    color += vec3(1.0, 0.8, 1.0) * pulse * Intensity;    // 中心脉冲

    fragColor = vec4(color, 0.40 * Intensity);
}

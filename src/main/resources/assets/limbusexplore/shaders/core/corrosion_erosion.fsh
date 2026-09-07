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

    // 漂移的细胞：坐标随时间移动，整张网格慢速流动
    vec2 wp = uv * 12.0 + vec2(Time * 0.4, Time * 0.2);
    vec2 w = worley(wp);
    float cellEdge = w.y - w.x;
    float cellId = hash13(floor(wp));

    // 注意：sampler2D 不能走三目运算（GLSL 语法禁止），两张图都采样再选
    vec2 warpUV = uv + (vec2(0.06, -0.04)) * (cellId - 0.5);
    vec3 flashImg = texture(SamplerFlash, uv).rgb;
    vec3 barImg = texture(SamplerBar, uv).rgb;
    vec3 warpedFlash = texture(SamplerFlash, warpUV).rgb;
    vec3 warpedBar = texture(SamplerBar, warpUV).rgb;

    vec3 base = flash ? flashImg : barImg;
    vec3 warped = flash ? warpedFlash : warpedBar;

    // 侵蚀从屏幕边缘向中心推进，2.5s 内铺满全屏
    float distC = length(uv - vec2(0.5));
    float invade = 1.0 - smoothstep(0.0, 0.8, distC * (1.0 + clamp(Time, 0.0, 2.5) * 1.3));

    // 一瞬间：边界爆闪；持续：边界缓慢呼吸
    float flicker = 0.5 + 0.5 * sin(Time * (flash ? 22.0 : 3.0) + cellId * 6.28318);
    float edge = (1.0 - smoothstep(0.0, 0.22, cellEdge)) * (0.4 + 0.6 * flicker);

    // 一瞬间：中心脉冲环
    float pulse = 0.0;
    if (flash) {
        float p = clamp(Time / 1.4, 0.0, 1.0);
        pulse = smoothstep(0.05, 0.0, abs(distC - p * 0.7)) * (1.0 - p);
    }

    vec3 violet = vec3(0.55, 0.25, 0.75);
    vec3 color = mix(base, violet, 0.50 * invade);
    color = mix(color, warped, 0.35);
    // 细胞明暗 + 慢慢闪烁（流动感的来源之一）
    color *= (0.60 + 0.40 * cellId) * (0.70 + 0.30 * sin(Time * 2.5 + cellId * 6.28318));
    // 边界紫光，加粗加亮
    color += vec3(0.95, 0.50, 1.0) * edge * invade * Intensity * 1.3;
    color += vec3(1.0, 0.8, 1.0) * pulse * Intensity;

    fragColor = vec4(color, 0.45 * Intensity);
}

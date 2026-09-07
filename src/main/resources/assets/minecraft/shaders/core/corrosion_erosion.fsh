#version 150

uniform sampler2D SamplerFlash;
uniform sampler2D SamplerBar;
uniform float Time;
uniform float Intensity;
uniform float Mode;
uniform vec3 Weight;

in vec2 vUV;
out vec4 fragColor;

float hash13(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

// 整数坐标 → 三维伪随机向量（每角一个梯度方向）
vec3 hash3(vec3 p) {
    return fract(sin(vec3(
        dot(p, vec3(127.1, 311.7, 74.7)),
        dot(p, vec3(269.5, 183.3, 246.1)),
        dot(p, vec3(113.5, 271.9, 124.6)))) * 43758.5453123);
}

// 改进柏林噪声（3D）：Ken Perlin 的 smootherstep 淡入淡出 + 角点梯度，输出 -1..1
float perlin3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    vec3 u = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);
    vec3 g000 = hash3(i) * 2.0 - 1.0;
    vec3 g100 = hash3(i + vec3(1.0, 0.0, 0.0)) * 2.0 - 1.0;
    vec3 g010 = hash3(i + vec3(0.0, 1.0, 0.0)) * 2.0 - 1.0;
    vec3 g110 = hash3(i + vec3(1.0, 1.0, 0.0)) * 2.0 - 1.0;
    vec3 g001 = hash3(i + vec3(0.0, 0.0, 1.0)) * 2.0 - 1.0;
    vec3 g101 = hash3(i + vec3(1.0, 0.0, 1.0)) * 2.0 - 1.0;
    vec3 g011 = hash3(i + vec3(0.0, 1.0, 1.0)) * 2.0 - 1.0;
    vec3 g111 = hash3(i + vec3(1.0, 1.0, 1.0)) * 2.0 - 1.0;
    float x00 = mix(dot(g000, f),                        dot(g100, f - vec3(1.0, 0.0, 0.0)), u.x);
    float x10 = mix(dot(g010, f - vec3(0.0, 1.0, 0.0)),  dot(g110, f - vec3(1.0, 1.0, 0.0)), u.x);
    float x01 = mix(dot(g001, f - vec3(0.0, 0.0, 1.0)),  dot(g101, f - vec3(1.0, 0.0, 1.0)), u.x);
    float x11 = mix(dot(g011, f - vec3(0.0, 1.0, 1.0)),  dot(g111, f - vec3(1.0, 1.0, 1.0)), u.x);
    return mix(mix(x00, x10, u.y), mix(x01, x11, u.y), u.z);
}

// Worley：F1/F2 双最近点距离（Voronoi 细胞）
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

    // 漂移的 Voronoi 细胞
    vec2 wp = uv * 12.0 + vec2(Time * 0.4, Time * 0.2);
    vec2 w = worley(wp);
    float cellEdge = w.y - w.x;
    float cellId = hash13(floor(wp));
    float edgeMask = 1.0 - smoothstep(0.0, 0.22, cellEdge);

    // 三维独立柏林噪声 → R/G/B 三通道（不同偏移与时间轴）
    vec3 n = vec3(
        perlin3(vec3(uv * 4.0, Time * 0.3)),
        perlin3(vec3(uv * 4.0 + 17.0, Time * 0.3 + 7.0)),
        perlin3(vec3(uv * 4.0 + 31.0, Time * 0.3 + 13.0))
    );
    // 每个维度乘 EGO 类型权重（Java 端按 activeEgo 上传）
    vec3 noiseColor = clamp(n * 0.5 + 0.5, 0.0, 1.0) * Weight;
    noiseColor = clamp(noiseColor, 0.0, 1.0);

    // 贴图基础 + 细胞扭曲
    vec2 warpUV = uv + (vec2(0.06, -0.04)) * (cellId - 0.5);
    vec3 flashImg = texture(SamplerFlash, uv).rgb;
    vec3 barImg = texture(SamplerBar, uv).rgb;
    vec3 warpedFlash = texture(SamplerFlash, warpUV).rgb;
    vec3 warpedBar = texture(SamplerBar, warpUV).rgb;
    vec3 base = flash ? flashImg : barImg;
    vec3 warped = flash ? warpedFlash : warpedBar;

    // 侵蚀从屏幕边缘向中心推进
    float distC = length(uv - vec2(0.5));
    float invade = 1.0 - smoothstep(0.0, 0.8, distC * (1.0 + clamp(Time, 0.0, 2.5) * 1.3));

    // Voronoi 边界上覆盖 RGB 噪声色（EGO 类型决定 RGB 权重）
    float flicker = 0.5 + 0.5 * sin(Time * (flash ? 22.0 : 3.0) + cellId * 6.28318);
    float edge = edgeMask * (0.5 + 0.5 * flicker) * invade * Intensity;

    // 一瞬间：中心脉冲环
    float pulse = 0.0;
    if (flash) {
        float p = clamp(Time / 1.4, 0.0, 1.0);
        pulse = smoothstep(0.05, 0.0, abs(distC - p * 0.7)) * (1.0 - p);
    }

    vec3 violet = vec3(0.55, 0.25, 0.75);
    vec3 color = mix(base, violet, 0.50 * invade);
    color = mix(color, warped, 0.35);
    color *= (0.60 + 0.40 * cellId) * (0.70 + 0.30 * sin(Time * 2.5 + cellId * 6.28318));
    // 覆盖：边界处用 RGB 噪声换色，再补一点亮度让边界发光
    color = mix(color, noiseColor, edge * 0.85);
    color += noiseColor * edge * 0.4;
    color += vec3(1.0, 0.8, 1.0) * pulse * Intensity;

    fragColor = vec4(color, 0.45 * Intensity);
}

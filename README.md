# Limbus Explore

Minecraft 1.20.1 Forge mod（Forge 47.3.0）。

## 0.3.0：可玩的 EGO 链路

本轮保留既有 HUD、侵蚀着色器和五个测试条目的存档 ID，重构服务端装备、释放事务与生命周期，并增加原创示例 EGO **余烬守望**。
这不是原作技能复刻，也不包含人格、拼点或回合制系统。

1. 用 **纸 + 煤炭 + 铁锭** 无序合成「余烬守望 · E.G.O 凭证」。获得纸后会解锁配方书条目。
2. 右键凭证，在服务端永久解锁。重复使用不会消耗凭证。
3. 按 **G**，选择 **ZAYIN** 槽，再选择余烬守望；等待服务端确认后显示装备。
4. 玩家击杀敌对生物获得 **1 暴怒 + 3 理智**。玩家和被动生物不提供奖励；服主可关闭该规则。
5. 按 **R**，短按普通释放：消耗 **2 暴怒、10 理智**，对前方 120°、4 格内可见敌对生物造成基础 **8 打击伤害**。
6. 长按切换侵蚀，再短按释放：消耗乘 1.5 并允许透支；改为周身 6 格、基础 **12 打击伤害**。两种形态均不穿墙、不伤害玩家或友方，仍经过原版护甲与伤害事件。
7. 进入 **30 秒 EGO 状态**，期间打击抗性 ×0.5，禁止再次释放和换装。状态结束清除临时覆盖及负罪孽余额。

死亡结束 EGO/混乱，但保留解锁、装备、正罪孽余额和理智；换维与非死亡克隆保留未过期状态，重建临时抗性，不重放瞬时技能。
离线时 EGO 的现实时间倒计时继续，重登后先处理过期清理。客户端断线清空所有本地状态镜像。

**安装**：客户端与服务端同时替换为 0.3.0，不要把多个版本一起放进 mods。网络协议为 `4`。
首次从纯客户端装备版本升级需按 G 重新装备；已有服务端装备存档可继续使用。

## 构建

要求 JDK 17（`org.gradle.java.home` 或用户级 `~/.gradle/gradle.properties` 里配置）。

```powershell
.\gradlew build        # 编译 + 打包，产物 build/libs/
.\gradlew runClient    # 开发客户端
.\gradlew runServer    # 开发服务器
.\gradlew runData      # 数据生成
```

## 罪孽资源

| 类 | 说明 |
|----|------|
| `sin/SinType` | 七种罪孽枚举。`id`、`color`、`displayKey()`、`texture()`、`byId(String)`。同步包数组顺序 = `values()` 顺序 |
| `sin/SinResources` | 服务端数据容器。`get/add/set/clear/copyFrom/toArray/fromArray/save/load`。不允许负值 |
| `sin/SinCapabilities` + `SinResourcesProvider` | capability 定义与挂载（键 `limbusexplore:sin_resources`），玩家死亡克隆跟随 |
| `sin/SinApi` | 业务入口。改完自动同步客户端 |
| `client/ClientSinResources` | 客户端镜像。`set(int[7])`、`get(SinType)`、`canPayClient(SinCost[])` |
| `net/PlayerDataSync` | 登录/换维/重生自动全量同步；`syncSin(Player)` |
| `net/SinSyncPacket` | S2C，int[7] |

`SinApi` 方法：

```java
int   get(Player, SinType);
void  add(Player, SinType, int);        // 加，不为负
void  set(Player, SinType, int);
void  clear(Player);
boolean consume(Player, SinType, int);  // 够扣才扣
boolean canPay(Player, SinCost[]);
boolean consumeAll(Player, SinCost[]);  // 全够才一起扣
```

## EGO

| 类 | 说明 |
|----|------|
| `ego/RiskLevel` | 等级枚举：ZAYIN/TETH/HE/WAW/ALEPH。`id`、`color`、`displayKey()` |
| `ego/SinCost` | `record SinCost(SinType sin, int amount)` |
| `ego/Ego` | 不可变定义目录，不再是枚举；同等级支持多个条目。`costs()` 返回防御性副本，`byId()` 按稳定 ID 查询 |
| `ego/EgoReleaseTransaction` | 内部纯数据事务：完整校验后统一扣罪孽、理智并写状态，失败不部分扣费 |
| `ego/EgoSkills` + `ego/skill/EgoSkill` | 定义与执行分离；成功提交后才执行觉醒/侵蚀的一次性技能 |
| `ego/EgoApi` | 释放判定（服务端）。`check`、`release` 返回 `ReleaseResult` |
| `ego/EgoResistance` | EGO 状态期间的三系抗性覆盖（`EgoStateListener`，见「EGO 状态」一节） |
| `ego/EgoLoadout` + `EgoLoadoutCapabilities` + `EgoLoadoutProvider` | 服务端 5 槽装备数据，跟随玩家存档与克隆 |
| `ego/EgoLoadoutApi` | 可信服务端解锁、装备/卸下、状态与槽位校验、装备查询和同步 |
| `client/ClientEgoLoadout` | 服务端装备的客户端镜像；装备/卸下只发送请求 |
| `net/EgoEquipPacket` + `EgoLoadoutSyncPacket` | C2S 单槽修改请求 / S2C 五槽与已解锁 ID 快照；没有 C2S 解锁入口 |
| `client/gui/EgoLoadoutScreen` | 装备界面 |
| `client/gui/EgoReleaseScreen` | 释放界面 |
| `net/EgoReleasePacket` | C2S，参数 egoId |
| `net/EgoReleaseResultPacket` | S2C，egoId + `ReleaseResult.ordinal()` |
| `net/EgoReleaseService` | 服务端处理：查 Ego → `EgoApi.release` → 回包 |
| `client/EgoReleaseClient` | 结果展示（actionbar + 粒子） |

槽位规则：5 槽，等级固定 `1=ZAYIN .. 5=ALEPH`（`levelOfSlot(slot)`），EGO 只能装进对应等级槽（`canEquip` 校验）。

```java
ClientEgoLoadout.get(slot);                    // Ego 或 null
ClientEgoLoadout.equippedList();               // 非空槽的已装备列表（按槽位顺序，内部缓存）
ClientEgoLoadout.levelOfSlot(slot);            // 槽位固定等级
ClientEgoLoadout.canEquip(slot, ego);
ClientEgoLoadout.equip(slot, ego);             // true 仅表示已发送请求，收到服务端快照才更新界面
ClientEgoLoadout.unequip(slot);                // 请求卸下，服务端确认后生效
```

服务端通过 `EgoLoadoutApi.set(serverPlayer, slot, ego)` 修改装备（`ego=null` 表示卸下），
校验槽位范围、等级、存活状态，以及是否处于混乱/EGO 状态。登录、换维、重生与每次装备请求后同步完整快照。
普通与侵蚀释放都在 `EgoApi` 中检查服务端装备，未装备不扣资源，返回 `NOT_EQUIPPED`。

**升级说明**：不再读取或写入客户端 `limbusexplore_loadout.json`，原文件不删除。
旧存档首次升级后装备槽为空，需要按 G 重新装备；之后装备按玩家、按存档保存，断线清空客户端镜像。
五个旧测试 EGO 默认可用；新条目余烬守望需要先使用凭证解锁，解锁数据和装备一起持久化。
网络协议已升至 `4`，客户端与服务端需同时更新。

`EgoApi`：

```java
EgoApi.ReleaseResult result = EgoApi.release(serverPlayer, ego);
// RELEASED        普通释放，已扣减，进入 30s EGO 状态
// CORRODED        侵蚀释放，消耗 ×1.5 向上取整 + 透支扣负，进入 30s EGO 状态
// SIN_LACK        罪孽资源不够（普通释放）
// SANITY_LACK     理智不够（普通释放，扣完低于 -45）
// IN_EGO_STATE    已在 EGO 状态中，禁止释放
// IN_CHAOS        混乱状态中，禁止释放
// NOT_EQUIPPED    服务端未装备此 EGO，普通/侵蚀都拒绝，不扣资源
// UNAVAILABLE     死亡、能力缺失或定义无效；不部分扣费
EgoApi.check(serverPlayer, ego);          // 只判断不扣（普通释放的预检）
EgoApi.releaseCorrosion(serverPlayer, ego); // 同一释放事务，按侵蚀费用扣减
```

界面操作：

- 装备界面（G 键）：上排 5 槽，左键选槽、右键卸下；下排 EGO 列表，左键装备（等级不匹配的条目灰显），ESC 关闭
- 释放界面（R 键）：只显示已装备的 EGO，按槽位顺序从左到右（空槽跳过）；窄屏自动分页，滚轮/左右方向键切页。**短按（<500ms）按当前形态释放，长按（>=500ms）切换侵蚀态**，**右键取消侵蚀态**；R 或 ESC 关闭
- 侵蚀态消耗：罪孽组合 ×1.5 向上取整、允许透支为负；理智 ×1.5 向上取整、不查下限直接扣（收缩到 -45）；两者都不够也不拦（透支），状态结束负值归 0

## EGO 状态（30s）

释放成功后进入 EGO 状态：30 秒、期间禁止再次释放、HUD 左侧显示倒计时与当前 EGO（金色=普通，红色=侵蚀）、结束时负值罪孽资源归 0。

| 类 | 说明 |
|----|------|
| `ego/EgoState` | 结束时间（绝对毫秒）、当前 EGO、形态；过期保留待清理标记，由服务端完成结束清理 |
| `ego/EgoStateCapabilities` + `EgoStateProvider` | capability 挂载（键 `limbusexplore:ego_state`） |
| `ego/EgoStateListener` | 三段钩子（默认空实现，战斗/表现效果挂这里） |
| `ego/EgoStateApi` | 服务端状态机（enter/end/tick/isInState/remainingSeconds/registerListener/sync） |
| `ego/EgoResistance` | 已挂上的监听：进状态把 `Ego.resistanceKind` 那一系抗性覆盖成 `resistanceRate` 倍，结束还原 |
| `client/ClientEgoState` | 客户端镜像（剩余秒数/当前 EGO/形态） |
| `net/EgoStateSyncPacket` + `PlayerDataSync` | 每秒同步剩余时间；登录/换维/重生先恢复/清理再全量同步 |

抗性覆盖走的是 `Resistance` 的**覆盖层**（`setOverride` / `clearOverride`），不动数据包 tag 给的基础值，
临时覆盖不写入存档，从有效 EGO 状态重建；旧档中的 `override_*` 不再加载。HUD 和 Jade 看到的是实际生效值。
`beforeEnter` 保留历史名称，但现在由释放事务成功提交后调用；它不应再次扣费或重放技能。

`EgoStateListener` 三个接口：

```java
EgoStateApi.registerListener(new EgoStateListener() {
    @Override public void beforeEnter(ServerPlayer player, Ego ego, boolean corroded) {}
    @Override public void whileInState(ServerPlayer player, Ego ego, boolean corroded, int remainingSeconds) {}
    @Override public void onEnd(ServerPlayer player, Ego ego, boolean corroded) {}
});
```

`EgoStateApi`：

```java
boolean EgoStateApi.isInState(player);
int    EgoStateApi.remainingSeconds(player);
void   EgoStateApi.enter(ServerPlayer, Ego, boolean corroded);   // 可信服务端直接入状态，不执行技能
void   EgoStateApi.end(ServerPlayer);                             // 状态结束（负值归 0 + onEnd）
void   EgoStateApi.registerListener(EgoStateListener);            // 挂三段钩子
void   EgoStateApi.sync(Player);                                  // 全量同步（PlayerDataSync 自动调）
void   EgoStateApi.reconcile(ServerPlayer);                       // 恢复有效覆盖，清理离线过期与旧档残留
```

## 侵蚀全屏效果（着色器）

`client/CorrosionBannerHud` + `assets/minecraft/shaders/core/corrosion_erosion.{json,vsh,fsh}`。
两段程序共用一个 shader，靠 `Mode` uniform 切换：`Mode=1` 是释放瞬间的龟裂爆闪（1.4s，淡入 0.7s / 淡出 0.7s），
`Mode=0` 是 30 秒状态期间的细胞流动 + 边界呼吸。

| uniform | 含义 |
|---------|------|
| `Time` | 秒。一瞬间是闪光内经过的时间，持续段是「已过秒数」（30 - 剩余） |
| `Intensity` | 闪光强度 0~1，持续段恒为 1 |
| `Mode` | 1 = 一瞬间，0 = 持续 |
| `Weight` | vec3，三维柏林噪声喂给 R/G/B 的权重，来自 `Ego.noiseR/G/B`（每个 EGO 一套色调） |
| `SamplerFlash` / `SamplerBar` | 两张底图（`textures/hud/corrosion_flash.png`、`corrosion.png`），单元 0 / 1 |

算法：Worley（Voronoi）F1/F2 边缘距离 + 三维改进柏林噪声，噪声按 `Weight` 分到三个通道。

几个踩过的坑，改动前先看一眼：

- **文件必须在 `assets/minecraft/` 下**：`ShaderInstance` 拼的是 `<命名空间>:shaders/core/<名字>.json`，注册时命名空间写的是 `minecraft`
- **注册走 `RegisterShadersEvent`**（在 `ClientModEvents`，MOD 总线），不要自己 `new` 了缓存：资源包重载后会换成新的实例
- **GLSL 里不能对 sampler 用三目运算**（`texture(a ? s1 : s2, uv)` 编译不过），要两张都采样再用 vec3 选
- shader 建不起来（json 写错之类）会 `LOGGER.error` 然后回退到全屏贴图 blit，不会黑屏

## 混乱值（Stagger）

满值起步的"抗性条"：受击扣混乱值，**扣到 0 进入混乱状态**，持续 **15 秒**，结束后回满重新累积。玩家和怪物都有。

| 类 | 说明 |
|----|------|
| `chaos/Chaos` | 容器：`MAX=100`、混乱计时、自然回复（40 tick +1）、定身锁定点、NBT |
| `chaos/ChaosCapabilities` + `ChaosProvider` | capability 挂载（键 `limbusexplore:chaos`，挂 **LivingEntity**，玩家与怪物共用） |
| `chaos/ChaosApi` | 入口与服务端状态机：`get/isInChaos/remainingSeconds/set/damage/breakNow/endChaos/reset/tick/sync` |
| `chaos/ChaosCombat` | 伤害挂钩（`LivingHurtEvent`）、tick 驱动、混乱中禁止攻击（`AttackEntityEvent`） |
| `chaos/ChaosListener` | 三段钩子：`beforeEnter` / `whileInChaos`（每秒）/ `onEnd` |
| `net/ChaosSyncPacket` | S2C 同步（`PlayerDataSync` 登录/换维/重生全量 + 变化时自动） |
| `net/EntityChaosPacket` | 怪物混乱状态广播（发给追踪该实体的玩家） |
| `client/ClientChaos` | 客户端镜像（玩家自己的混乱值） |
| `client/ClientEntityChaos` | 客户端记录哪些生物在混乱（按 tick 倒数，到点自动清） |
| `client/ChaosMarkRenderer` | 混乱生物头顶的"陷入混乱"图（billboard 始终面向玩家，最后 1 秒淡出） |
| `client/ChaosBarHud` | 混乱条（快捷栏上方，黄条；混乱中红色闪烁） |
| `client/gui/ChaosLockScreen` | 混乱锁定界面（吞掉所有输入，含 ESC，不可关闭） |
| `command/ChaosCommands` | `/chaos get|set|damage|break|reset`（权限 2） |

```java
Chaos.MAX = 100;                          // 混乱值上限
Chaos.CHAOS_TICKS = 20 * 15;              // 混乱持续 15 秒
Chaos.REGEN_INTERVAL = 40;                // 不受击时每 40 tick 回 1 点
ChaosApi.CHAOS_DAMAGE_MULTIPLIER = 1.5f;  // 混乱中受到的伤害倍率
```

- 混乱伤害取 `LivingHurtEvent` 中经三系抗性/等级修正后的伤害，四舍五入；正伤害最少 1，零伤害/免疫不扣混乱值。这里还未经过原版护甲与吸收结算。
- 混乱中的伤害继续吃三系抗性，再乘 1.5，不再积累混乱值。两个配置开关独立控制。
- **混乱状态**：玩家被定身（每 tick 拉回锁定点，客户端输入由 `ChaosLockScreen` 全部吞掉）＋ 禁止攻击 ＋ 禁止释放 EGO（`EgoApi` 返回 `IN_CHAOS`）；怪物停止 AI（清目标 + 停寻路 + 清速度）
- 进入混乱时发一圈 CRIT 粒子（服务端广播，怪物混乱也能看见）

## 理智值

| 类 | 说明 |
|----|------|
| `sanity/Sanity` | 容器，范围 `[-45, 45]`。`MIN`/`MAX` 常量 |
| `sanity/SanityCapabilities` + `SanityProvider` | capability 挂载（键 `limbusexplore:sanity`） |
| `sanity/SanityApi` | 业务入口，改完自动同步 |
| `client/ClientSanity` | 客户端镜像 |
| `net/PlayerDataSync` + `SanitySyncPacket` | 同步（登录/换维/重生自动） |

```java
int   SanityApi.get(Player);
void  SanityApi.add(Player, int);
void  SanityApi.set(Player, int);
void  SanityApi.reset(Player);        // 归 0
boolean SanityApi.canPay(Player, int);    // 扣完不低于 -45
boolean SanityApi.consume(Player, int);
boolean SanityApi.isInChaos(Player);      // get() <= Sanity.MIN
```

## 三系伤害与抗性

伤害分三系：**斩击 / 突刺 / 打击**；抗性倍率语义与图书馆、巴士一致：
`2.0 致命 / 1.5 弱点 / 1.0 普通 / 0.5 耐性 / 0.0 免疫`。

| 类 | 说明 |
|----|------|
| `combat/DamageKind` | 三系枚举，附带物品 tag / 攻击者 tag / 伤害类型 tag 的 key |
| `combat/DamageKindApi` | 判定入口（代码注册 → 数据包 tag → 兜底推断），另有 `heldKind(Player)` |
| `combat/Resistance` | 三系抗性容器 + 分级常量（FATAL/WEAK/NORMAL/ENDURED/IMMUNE）；`get()` 看覆盖层，`getBase()` 看基础值 |
| `combat/ResistanceCapabilities` + `ResistanceProvider` | capability 挂 LivingEntity（玩家+怪物），含实体 tag 的 `tierTag()` |
| `combat/ResistanceApi` | `get/set/apply/reset/multiplier/sync` + `setOverride/clearOverride`；tag 默认值只在首次查询时套一次 |
| `combat/CombatFormula` | 公式骨架：`原伤害 × 抗性 × 等级差修正`（等级差现为 1.0，钩子已留） |
| `combat/CombatHandler` | `LivingHurtEvent` 唯一入口：判系 → 乘抗性 → 喂混乱值；混乱中改走 ×1.5 |
| `net/ResistanceSyncPacket` | 玩家抗性同步（UI 用） |
| `client/ClientResistance` | 客户端镜像 |
| `command/ResistanceCommands` | `/resistance get|set|preset`（权限 2） |

**手持物判定表**（其它 mod 可用 tag 或 API 覆盖）：

| 手持 | 判定 |
|---|---|
| 空手、任意普通物品 | **打击** |
| 剑、斧 | **斩击** |
| 三叉戟、弓、弩 | **突刺** |
| 镐、锹、锄 | **打击** |

弹射物一律突刺、爆炸一律打击（与手持无关）；怪物优先看它的实体 tag / API 配置，没配才按手持物算。

**其他 mod 怎么接**（都不需要写代码，加数据包 json 即可）：

```
data/<你的包>/tags/items/weapons/slash.json          → 你的武器算斩击（pierce / blunt 同理）
data/<你的包>/tags/entity_types/attackers/pierce.json → 你的怪物用突刺打人
data/<你的包>/tags/damage_type/damage/slash.json     → 你的伤害类型算斩击
```

怪物抗性同理，往这些 tag 里塞实体 id：

```
#limbusexplore:resist/slash/{fatal,weak,endured,immune}
#limbusexplore:resist/pierce/{...}
#limbusexplore:resist/blunt/{...}
```

有依赖的 mod 也可以直接调 API：`DamageKindApi.registerItem/registerAttacker/registerDamageType`、
`ResistanceApi.set(entity, kind, value)`。

兼容性约定：只用 `LivingHurtEvent` **乘算**（不替换伤害、不取消事件、不改原版类、不注册自定义伤害类型），
未知来源一律兜底为打击；`damageKindEnabled` 配置项可整体关闭倍率。

## Jade（玉）兼容

装了 Jade 之后看向生物会多两行信息：

- 常驻：混乱值（低于 1/4 标红、低于 1/2 标黄、满值标绿）；混乱中显示「混乱中 · X 秒」
- 按住 Shift 详细模式：三系抗性，每系一行 `[图标] ×倍率`——图标是美工资源里那三张（`textures/hud/damage_{slash,pierce,blunt}.png`），倍率按档位配色（致命红 / 弱点金 / 普通白 / 耐性蓝 / 免疫灰）

图标走 Jade 的自定义元素（`Element` 基类，只实现 `render` + `getSize`），直接 `GuiGraphics.blit` 我们自己的贴图，不需要注册到方块图集。

| 文件 | 说明 |
|------|------|
| `compat/jade/LimbusJadePlugin` | 入口，`@WailaPlugin(modid)`，注册服务端数据与客户端显示 |
| `compat/jade/JadeEntityData` | 服务端：读混乱值与三系抗性写进 Jade 数据包（`IServerDataProvider`） |
| `compat/jade/JadeEntityComponent` | 客户端：读数据画 tooltip（`IEntityComponentProvider`） |

**可选依赖的处理**（关键）：依赖走 Modrinth 官方 maven，只编译期可见

```gradle
// build.gradle
repositories { maven { url = 'https://api.modrinth.com/maven'; content { includeGroup 'maven.modrinth' } } }
dependencies { compileOnly fg.deobf("maven.modrinth:jade:11.13.3+forge") }
```

- 没装 Jade 的客户端/服务器照常启动——`compat/jade/` 只有 Jade 自己扫 `@WailaPlugin` 时才会加载
- 升级 Jade：改上面那行的版本号即可，不用把第三方 jar 塞进仓库

## 键位与命令

| 键位 | 功能 |
|------|------|
| H | 左侧资源栏（理智 + 七罪孽图标）显隐 |
| G | 装备界面 |
| R | 释放界面 |

```text
/sins list | add <sin> <n> | set <sin> <n> | clear        （权限 2）
/sanity get | add <n> | set <n> | reset                     （权限 2）
/chaos get | set <n> | damage <n> | break | reset           （权限 2）
/resistance get | set <kind> <tier> | preset <name>         （权限 2）
```

## 配置与绕过入口

`config/limbusexplore-common.toml`（建议重启后验证；`/reload` 不是 Forge 配置重载命令）。这些开关就是官方绕过入口：整合包不想要哪块机制就关哪块，
关掉之后本 mod 对应部分不生效，也不影响别的 mod。

| 配置项 | 默认 | 关掉之后 |
|--------|------|----------|
| `logStartup` | true | 启动时不再打印 mod 加载日志 |
| `damageKindEnabled` | true | 禁用三系抗性/等级修正；混乱累计和挨打倍率仍由 chaosEnabled 控制 |
| `chaosEnabled` | true | 不累计、不增伤、不触发混乱；已有混乱在下一 tick 解除 |
| `killRewardsEnabled` | true | 关闭敌对生物击杀提供暴怒与理智的生存奖励 |
| `chaosMarkEnabled` | true | 客户端不画混乱头顶标记（纯显示开关，服务端行为不变） |
| `jadeCompatEnabled` | true | 不往 Jade 送数据，Jade 上看不到本 mod 的任何信息 |

外部 mod / 数据包接入时用 tag 而不是改代码，tag 见「三系伤害与抗性」一节；Jade 还有一个它自己的开关界面
（`limbusexplore:entity_stats`，Jade 的插件配置里能勾掉），效果和 `jadeCompatEnabled` 一样。

## 单元测试

```bash
gradlew test        # 只跑 src/test/java，不启动游戏，十几秒
```

测的是**不碰游戏启动的纯逻辑**：数据容器、倍率换算、状态机边界、同步包编解码。
这些正是出错了也不会崩、只会悄悄算错的地方，靠手测很难发现（写完第一版就抓到两个：
侵蚀透支的负值读档被清零、抗性存档把临时覆盖写死成基础值）。

| 测试 | 覆盖 |
|------|------|
| `sin/SinResourcesTest` | 加减夹 0 / `forceAdd` 允许透支 / 数组顺序 = 枚举顺序 / 存档往返保留负值 |
| `sanity/SanityTest` | ±45 夹取 / `consume` 不破线 / `consumeForce` 收到 -45 |
| `chaos/ChaosTest` | 扣到 0 才算破防 / 15 秒倒计时 / 每 40 tick 回 1 / 读档不停在 0 |
| `ego/EgoStateTest` | 30 秒有效性与剩余秒数向上取整 / 过期存档按无状态处理 |
| `ego/EgoTest` | 等级覆盖但不限制同级数量 / 抗性倍率 / 资源路径约定 |
| `ego/EgoReleaseTransactionTest` | 普通/侵蚀费用、失败原子性、重复请求、缺失能力与死亡保护 |
| `ego/ShockwaveSkillTest` | 扇形方向、范围、周身侵蚀范围 |
| `combat/DamageResolutionTest` | 两个开关独立性、免疫、零伤害、混乱与抗性叠乘 |
| `sin/SinPaymentTest` | 同罪孽重复费用合计、欠债偿还、溢出与负费用 |
| `ego/EgoLoadoutTest` | 槽位与等级边界 / 装备授权和卸下撤销 / NBT 往返 / 损坏或旧存档 / 克隆数据独立性 |
| `client/ClientEgoLoadoutTest` | 服务端快照更新 / 侵蚀选择重置 / 断线清理 / 首次同步前拒绝装备请求 |
| `combat/ResistanceTest` | 档位常量 / 覆盖层优先、清掉还原 / 存档不把覆盖写死 |
| `combat/DamageKindTest` | 三系 tag 路径契约（整合包按这个接）/ `byId` 大小写不敏感 |
| `combat/CombatFormulaTest` | 等级差恒为 1.0（接上等级系统时改这里） |
| `net/PacketRoundTripTest` | encode → decode → 再 encode 字节一致（漏改 decode、int/float 写错都会挂） |

要碰注册表的测试（比如 `ItemTags` 那条 tag 路径）先调 `TestBootstrap.ensure()`，它会 `Bootstrap.bootStrap()`
把注册表建起来——不建世界、不开客户端。网络包的 `handle` 不测（要建频道），只测编解码。

`gradlew runGameTestServer` 单独运行 `src/gameTest/java` 中的真实 Forge 服务端集成测试，测试类不进发行 JAR。
测试在 `build/gametest` 的专用世界中执行，不使用玩家存档；覆盖配方/凭证、释放/抗性/墙体遮挡、伪造释放、
死亡/非死亡克隆、离线过期、自然混乱结束、配置开关和击杀资源奖励。客户端渲染与真实双客户端网络体验仍需人工验收。

## 架构设计

### 分层

```
data   sin/sanity/ego/chaos/combat 容器+枚举   纯状态，只有 NBT 存取，不 import client/
api    SinApi/SanityApi/EgoApi/EgoStateApi/ChaosApi/ResistanceApi   业务唯一入口，改完自带同步
cap    *Capabilities + *Provider    capability 定义与挂载（FORGE 总线）
net    ModNetworking/包/PlayerDataSync/EgoReleaseService/PlayerDataLifecycle   同步、服务端处理、跨模块生命周期
client 缓存/界面对话/注册/运行期   只读镜像 + 预检，判定永远在服务端
compat 第三方 mod 兼容（Jade）   可选依赖，只编译期引用
command /sins /sanity /chaos /resistance   调试命令（权限 2）
```

依赖方向单向：`data ← api ← (command|net 服务端处理)`；`client` 依赖 `ego`/`net` 的包定义，但**服务端逻辑不依赖 client**。

模块之间也要单向，别成环。现在只有这些边：

```
ego → sin（消耗罪孽）        ego → sanity（消耗理智）      ego → chaos（混乱中禁止释放）
ego → combat（EGO 覆盖三系抗性）                              combat → chaos（受击喂混乱值）
```

`EgoResistance` 之所以写在 `ego` 包里而不是 `combat` 包里，就是因为放过去会变成 `combat ↔ ego` 双向依赖。
新加跨模块调用前先看一眼这几条边，方向不对就换一边放。

### 约定

1. **服务端权威**：判定与扣减全在服务端；客户端只有 `Client*` 镜像和预检（`canPay` 类），预检失败直接提示、不发包
2. **数据修改必须走 Api**：业务调用从 Api 进入；内部释放事务统一更新容器，生命周期协调器负责克隆与清理；完成后统一同步
3. **注册集中一处**：网络包全部在 `ModNetworking.register()`；客户端注册全部在 `ClientModEvents`（MOD 总线）；运行期逻辑全部在 `ClientGameEvents`（FORGE 总线）；玩家数据同步监听全部在 `net/PlayerDataSync`
   - 例外：每个模块的 `*Capabilities` 自己 `@Mod.EventBusSubscriber` 挂载能力（缺省 FORGE 总线），删模块时连它一起删就行
4. **总线**：MOD = 注册，FORGE = 运行期；一个监听类只挂一条总线（混了整类注册失败）
5. **命名**：id/资源文件名一律小写 snake_case；类名 = 模块 + 职责（`SinApi`、`EgoStateProvider`）
6. **同步包顺序**：`SinType.values()` 就是 `int[7]` 的顺序，别重排；协议破坏性变更 bump `ModNetworking.PROTOCOL`
7. **动了数据层就补测试**：容器边界、倍率公式、同步包编解码都进 `gradlew test`；「加了字段忘了改 decode」这种错只有往返测试抓得住

### 新模块模板（照 sin 抄）

```
容器(EnumMap/NBT) → Provider(ICapabilitySerializable) → Capabilities(attach)
→ Api(修改后 PlayerDataSync.syncXxx) → SyncPacket(注册进 ModNetworking) → 客户端镜像 Client*
```

跨模块克隆统一放在 `PlayerDataLifecycle`：只恢复旧实体能力一次，复制完成后统一失效，不能各模块重复监听 Clone。

### 已知待办（架构层面的）

- 服务端装备、解锁、一次性执行与生命周期已接通；新增 EGO 元数据放 `Ego`，行为放 `EgoSkills`，获取方式调用服务端 `EgoLoadoutApi.unlock`
- 侵蚀相关（理智 -45 崩溃、超频）在战斗系统落地时接 `EgoApi`/`SanityApi.isInChaos`

### 服务端装备改动的游戏内验收

单元测试不代替真实客户端/专用服务端验证。发布前需要逐项检查：

1. 单人 G 界面装备、卸下能在不关闭界面的情况下更新；错等级不能装备。
2. 重登、死亡重生、跨维度、从末地返回后装备仍保留；另一玩家/另一存档不继承装备。
3. 普通与侵蚀释放都只允许服务端已装备的 EGO；卸下后伪造释放请求不扣资源、不进入状态。
4. 混乱或 EGO 状态中伪造换装请求被拒绝；未知 ID、越界槽位不改变原装备。
5. 断线后连接其他服务器时不显示上一服务器的装备；协议 2/3 与当前 4 不兼容，拒绝连接。

## 项目结构

```
src/main/java/com/limbus/limbusexplore/
├── LimbusExplore.java              # 入口：@Mod、生命周期、注册表挂载
├── config/ModConfig.java           # common.toml 配置
├── sin/                            # 罪孽资源：SinType/SinResources/SinApi/Capability
├── sanity/                         # 理智值：Sanity/SanityApi/Capability
├── ego/                            # EGO 定义：RiskLevel/Ego/SinCost/EgoApi/EgoResistance
├── chaos/                          # 混乱值：Chaos/ChaosApi/Capability/监听
├── combat/                         # 三系伤害与抗性：DamageKind/Resistance/公式/结算入口
├── compat/jade/                    # Jade 兼容（可选依赖，只编译期引用）
├── net/                            # 频道、同步包、同步入口、释放服务
├── command/                        # 调试命令 /sins、/sanity、/chaos、/resistance
├── client/
│   ├── ClientSinResources.java     # 罪孽资源客户端缓存
│   ├── ClientSanity.java           # 理智值客户端缓存
│   ├── ClientChaos.java            # 混乱值客户端镜像
│   ├── ClientResistance.java       # 三系抗性客户端镜像
│   ├── ClientEntityChaos.java      # 生物混乱状态（头顶标记用）
│   ├── ClientEgoLoadout.java       # 5 槽装备状态
│   ├── ClientEgoState.java         # EGO 状态镜像
│   ├── SinResourcesHud.java        # 左侧资源栏
│   ├── SanityBarHud.java           # 理智值
│   ├── ChaosBarHud.java            # 混乱值条
│   ├── ChaosMarkRenderer.java      # 混乱头顶标记
│   ├── CorrosionBannerHud.java     # 侵蚀全屏着色器
│   ├── ClientModEvents.java        # MOD 总线客户端注册
│   ├── ClientGameEvents.java       # FORGE 总线客户端运行期
│   ├── EgoReleaseClient.java       # 释放结果展示
│   └── gui/                        # EgoLoadoutScreen / EgoReleaseScreen / ChaosLockScreen
└── registry/
    ├── ModItems.java               # 物品注册表
    └── ModCreativeTabs.java        # 创造模式标签

src/test/java/com/limbus/limbusexplore/   # 单元测试（gradlew test，不启动游戏）
├── TestBootstrap.java              # 需要注册表的测试先调它
├── sin/ sanity/ chaos/ ego/        # 容器与状态的边界行为
├── combat/                         # 抗性档位、覆盖层、tag 路径契约
└── net/PacketRoundTripTest.java    # 同步包编解码往返

src/main/resources/
├── META-INF/mods.toml
├── pack.mcmeta
└── assets/limbusexplore/
    ├── lang/en_us.json
    ├── lang/zh_cn.json
    ├── models/item/                # 物品模型
    ├── textures/hud/               # 罪孽图标 sin_<id>.png
    ├── textures/ego/               # EGO 图片（未放图时渲染回退罪孽图标）
    └── textures/item/              # 物品纹理
```

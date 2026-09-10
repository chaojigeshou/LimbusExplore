# Limbus Explore

Minecraft 1.20.1 Forge mod（Forge 47.3.0）。

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
| `sin/ModCapabilities` + `SinResourcesProvider` | capability 定义与挂载（键 `limbusexplore:sin_resources`），玩家死亡克隆跟随 |
| `sin/SinApi` | 业务入口。改完自动同步客户端 |
| `client/ClientSinResources` | 客户端镜像。`set(int[7])`、`get(SinType)`、`canPayClient(SinCost[])` |
| `net/PlayerSinSync` | 登录/换维/重生自动全量同步；`sync(Player)` |
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
| `ego/Ego` | 字段：`id`、`level`、`sin`、`costs`、`sanityCost`、`resistanceSin`、`resistanceRate`；方法：`displayKey()`、`descKey()`、`awakeningKey()`、`corrosionKey()`、`passiveKey()`、`texture()`、`byId(String)` |
| `ego/EgoApi` | 释放判定（服务端）。`check`、`release` 返回 `ReleaseResult` |
| `client/ego/ClientEgoLoadout` | 5 槽装备状态（客户端本地）。见下 |
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
ClientEgoLoadout.equip(slot, ego);             // 等级不匹配返回 false；已在别的槽会自动腾出
ClientEgoLoadout.unequip(slot);
```

`EgoApi`：

```java
EgoApi.ReleaseResult result = EgoApi.release(serverPlayer, ego);
// RELEASED        普通释放，已扣减，进入 30s EGO 状态
// CORRODED        侵蚀释放，消耗 ×1.5 向上取整 + 透支扣负，进入 30s EGO 状态
// SIN_LACK        罪孽资源不够（普通释放）
// SANITY_LACK     理智不够（普通释放，扣完低于 -45）
// IN_EGO_STATE    已在 EGO 状态中，禁止释放
EgoApi.check(serverPlayer, ego);          // 只判断不扣（普通释放的预检）
EgoApi.releaseCorrosion(serverPlayer, ego); // 侵蚀释放：SinApi.consumeAllForced + SanityApi.consumeForce
```

界面操作：

- 装备界面（G 键）：上排 5 槽，左键选槽、右键卸下；下排 EGO 列表，左键装备（等级不匹配的条目灰显），ESC 关闭
- 释放界面（R 键）：只显示已装备的 EGO，按槽位顺序从左到右（空槽跳过）；**短按（<500ms）按当前形态释放，长按（>=500ms）切换侵蚀态**（卡片变红、消耗按 1.5 倍展示、角标"侵蚀"），**右键取消侵蚀态**；R 或 ESC 关闭
- 侵蚀态消耗：罪孽组合 ×1.5 向上取整、允许透支为负；理智 ×1.5 向上取整、不查下限直接扣（收缩到 -45）；两者都不够也不拦（透支），状态结束负值归 0

## EGO 状态（30s）

释放成功后进入 EGO 状态：30 秒、期间禁止再次释放、HUD 左侧显示倒计时与当前 EGO（金色=普通，红色=侵蚀）、结束时负值罪孽资源归 0。

| 类 | 说明 |
|----|------|
| `ego/EgoState` | 状态数据：结束时间（绝对毫秒）、当前 EGO、形态；NBT 存取（过期自动作废） |
| `ego/EgoStateCapabilities` + `EgoStateProvider` | capability 挂载（键 `limbusexplore:ego_state`） |
| `ego/EgoStateListener` | 三段钩子（默认空实现，战斗/表现效果挂这里） |
| `ego/EgoStateApi` | 服务端状态机（enter/end/tick/isInState/remainingSeconds/registerListener/sync） |
| `client/ClientEgoState` | 客户端镜像（剩余秒数/当前 EGO/形态） |
| `net/EgoStateSyncPacket` + `PlayerEgoStateSync` | 每秒同步剩余时间；登录/换维/重生全量同步 |

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
void   EgoStateApi.enter(ServerPlayer, Ego, boolean corroded);   // 释放成功后由 EgoApi 调用
void   EgoStateApi.end(ServerPlayer);                             // 状态结束（负值归 0 + onEnd）
void   EgoStateApi.registerListener(EgoStateListener);            // 挂三段钩子
void   EgoStateApi.sync(Player);                                  // 全量同步（PlayerEgoStateSync 自动调）
```

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
| `client/ClientChaos` | 客户端镜像 |
| `client/ChaosBarHud` | 混乱条（快捷栏上方，黄条；混乱中红色闪烁） |
| `client/gui/ChaosLockScreen` | 混乱锁定界面（吞掉所有输入，含 ESC，不可关闭） |
| `command/ChaosCommands` | `/chaos get|set|damage|break|reset`（权限 2） |

```java
Chaos.MAX = 100;                          // 混乱值上限
Chaos.CHAOS_TICKS = 20 * 15;              // 混乱持续 15 秒
Chaos.REGEN_INTERVAL = 40;                // 不受击时每 40 tick 回 1 点
ChaosApi.CHAOS_DAMAGE_MULTIPLIER = 1.5f;  // 混乱中受到的伤害倍率
```

- 混乱伤害 = 原伤害 × 1.0 × 类型倍率（近战 1.0 / 弹射物 0.8 / 爆炸及其它 1.2）
- **混乱状态**：玩家被定身（每 tick 拉回锁定点，客户端输入由 `ChaosLockScreen` 全部吞掉）＋ 禁止攻击 ＋ 禁止释放 EGO（`EgoApi` 返回 `IN_CHAOS`）；怪物停止 AI（清目标 + 停寻路 + 清速度）
- 进入混乱时发一圈 CRIT 粒子（服务端广播，怪物混乱也能看见）

## 理智值

| 类 | 说明 |
|----|------|
| `sanity/Sanity` | 容器，范围 `[-45, 45]`。`MIN`/`MAX` 常量 |
| `sanity/SanityCapabilities` + `SanityProvider` | capability 挂载（键 `limbusexplore:sanity`） |
| `sanity/SanityApi` | 业务入口，改完自动同步 |
| `client/ClientSanity` | 客户端镜像 |
| `net/PlayerSanitySync` + `SanitySyncPacket` | 同步（登录/换维/重生自动） |

```java
int   SanityApi.get(Player);
void  SanityApi.add(Player, int);
void  SanityApi.set(Player, int);
void  SanityApi.reset(Player);        // 归 0
boolean SanityApi.canPay(Player, int);    // 扣完不低于 -45
boolean SanityApi.consume(Player, int);
boolean SanityApi.isInChaos(Player);      // get() <= Sanity.MIN
```

## 键位与命令

| 键位 | 功能 |
|------|------|
| H | 左侧资源栏（理智 + 七罪孽图标）显隐 |
| G | 装备界面 |
| R | 释放界面 |

```text
/sins list | add <sin> <n> | set <sin> <n> | clear       （权限 2）
/sanity get | add <n> | set <n> | reset                    （权限 2）
```

## 架构设计

### 分层

```
data   sin/Sanity/ego 容器+枚举     纯状态，只有 NBT 存取，不 import net/、client/
api    SinApi/SanityApi/EgoApi/EgoStateApi   业务唯一入口，改完自带同步
cap    *Capabilities + *Provider    capability 定义与玩家挂载（FORGE 总线）
net    ModNetworking/包/PlayerDataSync/EgoReleaseService   频道、同步、服务端处理
client 缓存/界面对话/注册/运行期   只读镜像 + 预检，判定永远在服务端
command /sins /sanity              调试命令（权限 2）
```

依赖方向单向：`data ← api ← (command|net 服务端处理)`；`client` 依赖 `ego`/`net` 的包定义，但**服务端逻辑不依赖 client**。

### 约定

1. **服务端权威**：判定与扣减全在服务端；客户端只有 `Client*` 镜像和预检（`canPay` 类），预检失败直接提示、不发包
2. **数据修改必须走 Api**：业务代码不得直接摸 capability、网络包、容器字段；Api 内部保证改完同步
3. **注册集中一处**：网络包全部在 `ModNetworking.register()`；客户端注册全部在 `ClientModEvents`（MOD 总线）；运行期逻辑全部在 `ClientGameEvents`（FORGE 总线）；玩家数据同步监听全部在 `net/PlayerDataSync`
4. **总线**：MOD = 注册，FORGE = 运行期；一个监听类只挂一条总线（混了整类注册失败）
5. **命名**：id/资源文件名一律小写 snake_case；类名 = 模块 + 职责（`SinApi`、`EgoStateProvider`）
6. **同步包顺序**：`SinType.values()` 就是 `int[7]` 的顺序，别重排；协议破坏性变更 bump `ModNetworking.PROTOCOL`

### 新模块模板（照 sin 抄）

```
容器(EnumMap/NBT) → Provider(ICapabilitySerializable) → Capabilities(attach+clone)
→ Api(修改后 PlayerDataSync.syncXxx) → SyncPacket(注册进 ModNetworking) → 客户端镜像 Client*
```

### 已知待办（架构层面的）

- 装备数据还在客户端（`ClientEgoLoadout`）：联机前搬到服务端 capability，`EgoReleaseService` 补「确实装在槽位上」的校验（否则客户端可伪造 egoId 释放）
- 侵蚀相关（理智 -45 崩溃、超频）在战斗系统落地时接 `EgoApi`/`SanityApi.isInChaos`

## 项目结构

```
src/main/java/com/limbus/limbusexplore/
├── LimbusExplore.java              # 入口：@Mod、生命周期、注册表挂载
├── config/ModConfig.java           # common.toml 配置
├── sin/                            # 罪孽资源：SinType/SinResources/SinApi/Capability
├── sanity/                         # 理智值：Sanity/SanityApi/Capability
├── ego/                            # EGO 定义：RiskLevel/Ego/SinCost/EgoApi
├── net/                            # 频道、同步包、同步入口、释放服务
├── command/                        # 调试命令 /sins、/sanity
├── client/
│   ├── ClientSinResources.java     # 罪孽资源客户端缓存
│   ├── ClientSanity.java           # 理智值客户端缓存
│   ├── SinResourcesHud.java        # 左侧资源栏
│   ├── ClientModEvents.java        # MOD 总线客户端注册
│   ├── ClientGameEvents.java       # FORGE 总线客户端运行期
│   ├── EgoReleaseClient.java       # 释放结果展示
│   ├── ego/                        # ClientEgoLoadout（装备状态）
│   └── gui/                        # EgoLoadoutScreen / EgoReleaseScreen
└── registry/
    ├── ModItems.java               # 物品注册表
    └── ModCreativeTabs.java        # 创造模式标签

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

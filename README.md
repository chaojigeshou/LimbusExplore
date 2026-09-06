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
ClientEgoLoadout.levelOfSlot(slot);            // 槽位固定等级
ClientEgoLoadout.canEquip(slot, ego);
ClientEgoLoadout.equip(slot, ego);             // 等级不匹配返回 false；已在别的槽会自动腾出
ClientEgoLoadout.unequip(slot);
```

`EgoApi`：

```java
EgoApi.ReleaseResult result = EgoApi.release(serverPlayer, ego);
// RELEASED        已扣减（SinApi.consumeAll(costs) + SanityApi.consume(sanityCost)）
// SIN_LACK        罪孽资源不够
// SANITY_LACK     理智不够（扣完低于 -45）
EgoApi.check(serverPlayer, ego);   // 只判断不扣
```

界面操作：

- 装备界面（G 键）：上排 5 槽，左键选槽、右键卸下；下排 EGO 列表，左键装备（等级不匹配的条目灰显），ESC 关闭
- 释放界面（R 键）：只显示已装备的 EGO，按槽位顺序从左到右（空槽跳过）；左键释放，R 或 ESC 关闭；卡片含名称、图片、等级、介绍、资源组合、理智消耗，不满足的项标红

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

## 生命周期

| 总线 | 事件 | 类 |
|------|------|----|
| MOD | `RegisterGuiOverlaysEvent`、`RegisterKeyMappingsEvent` | `client/ClientModEvents` |
| FORGE | tick、玩家事件、命令、capability | `ModCapabilities`、`SanityCapabilities`、`PlayerSinSync`、`PlayerSanitySync`、`ClientGameEvents`、`ModCommands`、`SanityCommands` |

两条总线的事件不要混在同一个监听类里，混了会导致整个类的自动订阅失败。

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

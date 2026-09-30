# EGO 释放界面：工业档案卡试作

## Git 基线

- 基线提交：`621a0bc`（修改界面前的完整 0.3.0 项目）。
- 基线标签：`baseline-0.3.0-pre-ego-ui`。
- 开发分支：`feat/ego-industrial-ui`。
- 构建缓存、开发世界、日志继续由 `.gitignore` 排除，不在基线中。
- 只建立本地版本记录，没有连接远端或推送。

查看本次修改：

```powershell
git diff baseline-0.3.0-pre-ego-ui..feat/ego-industrial-ui
```

## 本次实现

按参考图还原布局与视觉语言，而不是复制参考图中的战斗数据：

- 铜金觉醒 / 暗红侵蚀双状态，切角金属外框、铆钉、圆环和浅刮痕。
- 顶部理智消耗、风险等级铭牌、更多信息按钮。
- 真正圆形裁切的立绘区域。没有专属立绘时显示罪孽徽记与暗色档案背景。
- 每张卡片右侧固定七罪孽费用栏；零费用保留位置，不足的费用标红。
- 屏幕右侧显示玩家当前资源，底部显示选中 EGO 的三系抗性变化。
- 展示真实的持续时间、抗性和技能说明，不加入未实现的侵蚀概率、硬币威力或攻击容量。
- 点击“更多信息”打开觉醒、侵蚀和状态效果档案；弹窗点击不会穿透触发释放。
- 宽屏最多四张卡片；窄屏分页，绘制与点击共用缩放后的布局坐标。
- 保留短按释放、长按切侵蚀、右键取消、方向键/滚轮翻页、R/ESC 返回。
- 金属线条与圆环批量提交绘制，不为每个像素单独提交渲染。

本次未修改伤害、资源获取、服务端释放校验或网络协议。
同时修复了凭证物品引用 HUD 纹理时未加入物品图集导致的缺图警告。

## 美术替换入口

专属立绘放在：

```text
src/main/resources/assets/limbusexplore/textures/ego/<ego_id>.png
```

例如 `ember_watch.png`。使用方形图片，主要内容放在内切圆内，四角会被裁掉。
新增资源后按 F3+T 重载并重新打开 R 界面即可识别。
当前试作没有补画原作角色立绘，所以它是布局与卡框风格试作，不是像素级完整复刻。

## 验证

只跑改动涉及的布局与输入测试：

```powershell
.\gradlew test --tests "*EgoReleaseLayoutTest" --tests "*EgoReleaseGestureTest"
```

在独立客户端实际渲染并自动截图：

```powershell
.\gradlew -PegoUiPreview runClient
```

该模式使用 `build/ui-preview`，载入专用预览数据；不连接服务器，不进入或修改 `run/` 中的玩家存档。
自动捕获宽屏、详情、窄屏、第二页和空装备五种画面后关闭预览客户端。
截图默认位于 `build/ui-preview/screenshots`，可用 `-PegoUiPreviewOutput=<绝对路径>` 指定位置。
预览代码在 `src/uiPreview`，不会加入发行 JAR；正常 `runClient` 不加载它。

编译打包，不重复跑业务测试：

```powershell
.\gradlew build -x test
```

验收包与正常 0.3.0 使用相同版本元数据和协议；安装时替换旧 JAR，不能同时安装两个副本。

# RandomSpawn

> 新玩家首次进服时随机投放到野外的安全落点 — 一个为 Paper 1.21+ 设计的插件

## 功能

- **首次进服投放**：`player.hasPlayedBefore()` 为 false 的玩家进服后，自动传送到野外随机落点
- **随机范围**：x、z 的绝对值都取 `[min-distance, max-distance]`，正负随机
- **落点安全检查**：实心非液体地面、上方两格可通行；排除仙人掌、岩浆块、细雪、营火、甜浆果丛、火、蜘蛛网、脚手架、水、岩浆；排除树叶与原木；跳过海洋与河流生物群系
- **逐区块异步验证**：最多尝试 `max-chunk-attempts` 个区块，全部不合格就保留原出生点（不把人扔到虚空或海里）
- **可选设为重生点**：`set-respawn-point: true` 时把落点设为该玩家的床重生点（之后睡床会被床覆盖）

## 安装

1. 下载 [Releases](https://github.com/yunyun-3782/RandomSpawn/releases) 中的 `RandomSpawn-<版本>.jar`
2. 放入服务器 `plugins/` 目录
3. 重启服务器或 `/reload`
4. 编辑 `plugins/RandomSpawn/config.yml` 自定义配置

## 命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/randomspawn` | `randomspawn.admin` | 把自己投放到随机落点 |
| `/randomspawn <玩家>` | `randomspawn.admin` | 把指定玩家投放到随机落点（控制台可用） |

## 配置

```yaml
enabled: true            # 总开关
worlds: [world]          # 生效的世界（按玩家当前所在世界判断）
min-distance: 1000       # 落点距离范围下限
max-distance: 3000       # 落点距离范围上限
max-chunk-attempts: 10   # 最多验证多少个区块，全不合格就放弃
min-y: 63                # 最低落点高度，低于该值视为水下或地下
set-respawn-point: false # 是否把落点设成重生点
message: "&a你被随机投放到野外：&e%x%, %z%"
```

## 构建

```bash
git clone https://github.com/yunyun-3782/RandomSpawn.git
cd RandomSpawn
gradle build
# jar 在 build/libs/RandomSpawn-<版本>.jar
```

## 相关插件

| 插件 | 说明 | 链接 |
|------|------|------|
| 🫘 N00bCocoon | 新手茧 — 低价值新人保护 | [GitHub](https://github.com/yunyun-3782/cocoon) |
| 🔻 DeathDrop | 死亡按物品价值百分比掉落 | [GitHub](https://github.com/yunyun-3782/DeathDrop) |
| ⏰ RealtimeSync | 游戏昼夜与真实时间同步 | [GitHub](https://github.com/yunyun-3782/RealtimeSync) |

## 许可证

Copyright (c) 2025 Yunyun(云云)

Licensed under the [CaelLab BY-SA Code License, Version 3](https://www.caellab.com/license/cbs-v3).

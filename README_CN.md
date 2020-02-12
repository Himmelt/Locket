## Locket Plugin for Sponge
使用告示牌来保护你的方块吧！

![Locket](https://github.com/Himmelt/Locket/workflows/Locket/badge.svg)

## 命令
1. 主命令名  `/locket `
2. 命令别名  `/lock`

## 如何给一个方块上锁？
1. 手持告示牌右键目标方块！(需要权限`locket.lock`)
2. 右键选择一个贴在方块侧围的告示牌，执行指令`/lock`！(需要权限`locket.lock`)
## 如何添加一个用户？
1. 右键选择一个贴在方块侧围的告示牌，执行指令`/lock <行数> <玩家名>`！(需要权限`locket.lock`)
## 如何移除一个用户？
1. 右键选择一个贴在方块侧围的告示牌，执行指令`/lock remove <行数>`！(需要权限`locket.lock`)
## 如何从可保护列表添加/移除一个方块类型？
1. 执行指令`/lock type +/- <方块注册名>`！(需要权限`locket.admin`)
2. 手持方块时执行指令`/lock type +/- `！(需要权限`locket.admin`)
3. 在配置文件添加/删除方块类型，然后执行指令`/lock reload` (需要权限`locket.admin`)
## 如何从可保护列表添加/移除一个双联方块类型？
1. 执行指令`/lock type ++/-- <方块注册名>`！(需要权限`locket.admin`)
2. 手持方块时执行指令`/lock type --`！(需要权限`locket.admin`)
3. 在配置文件添加/删除方块类型，然后执行指令`/lock reload` (需要权限`locket.admin`)

## 已知缺陷
1. 放置于沙子等受重力影响的方块上的门，无法避免因下部沙子掉落而导致的破坏。

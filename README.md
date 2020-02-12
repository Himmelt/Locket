## Locket Plugin for Sponge
使用告示牌来保护你的方块吧!

Use the wall-sign to protect your blocks!

![Locket](https://github.com/Himmelt/Locket/workflows/Locket/badge.svg)

### [中文说明](README_CN.md)

## Main Command
1. main  `/locket `
2. alias `/lock`

## How to lock a block?
1. Holding a sign and right click the target block!(need permission `locket.lock`)
2. Right click to select a wall-sign, and execute the command `/lock`!(need permission `locket.lock`)
## How to add a user?
1. Right click to select a wall-sign, and execute the command `/lock <line(2|3)> <player>`!(need permission `locket.lock`)
## How to remove a user?
1. Right click to select a wall-sign, and execute the command `/lock remove <line>`!(need permission `locket.lock`)
## How to add/remove a block type to the lockable blocks list?
1. Execute the command `/lock type +/- <block_id>`!(need permission `locket.admin`)
2. Execute the command `/lock type +/- ` when held a block item.(need permission `locket.admin`)
3. Add/remove the type id in config file, and execute command `/lock reload`(need permission `locket.admin`)
## How to add/remove a double-block(chest e.g.) to the lockable blocks list?
1. Execute the command `/lock type ++/-- <block_id>`!(need permission `locket.admin`)
2. Execute the command `/lock type ++/-- ` when held a block item.(need permission `locket.admin`)
3. Add/remove the type id in config file, and execute command `/lock reload`(need permission `locket.admin`)

## Known Bugs
1. Doors on blocks with gravity influence can't be protected.

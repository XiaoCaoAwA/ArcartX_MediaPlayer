# XcMusicPlayer 网络通讯说明

## 概述

XcMusicPlayer 支持通过服务器数据包（CustomPacket）进行远程控制，实现服务端控制客户端音乐播放。

**包标识符**: `arcartx_xcmusic`

---

## 命令格式

所有命令通过 CustomPacket 发送，格式如下：

```
包ID: arcartx_xcmusic
参数: [命令, 参数1, 参数2, ...]
```

---

## 支持的命令

### 1. 播放控制

#### 播放音乐
```
命令: play
格式: arcartx_xcmusic play <url> <instanceName>
参数:
  - url: 音频URL或本地文件路径
  - instanceName: 实例名称
示例: arcartx_xcmusic play music/bgm.mp3 background
```

#### 淡入播放音乐
```
命令: fadeInPlay
格式: arcartx_xcmusic fadeInPlay <duration> <url> <instanceName>
参数:
  - duration: 淡入时长（毫秒）
  - url: 音频URL或本地文件路径
  - instanceName: 实例名称
示例: arcartx_xcmusic fadeInPlay 3000 music/bgm.mp3 background
```

---

### 2. 停止控制

#### 停止所有实例
```
命令: stop
格式: arcartx_xcmusic stop
示例: arcartx_xcmusic stop
```

#### 停止指定实例
```
命令: stop
格式: arcartx_xcmusic stop <instanceName>
参数:
  - instanceName: 实例名称
示例: arcartx_xcmusic stop background
```

#### 淡出停止（默认3秒）
```
命令: fadeOutStop
格式: arcartx_xcmusic fadeOutStop <instanceName>
参数:
  - instanceName: 实例名称
示例: arcartx_xcmusic fadeOutStop background
```

#### 淡出停止（指定时长）
```
命令: fadeOutStop
格式: arcartx_xcmusic fadeOutStop <duration> <instanceName>
参数:
  - duration: 淡出时长（毫秒）
  - instanceName: 实例名称
示例: arcartx_xcmusic fadeOutStop 5000 background
```

---

### 3. 音量控制

#### 设置主音量
```
命令: set_master_volume
格式: arcartx_xcmusic set_master_volume <volume>
参数:
  - volume: 音量值（0.0-1.0）
示例: arcartx_xcmusic set_master_volume 0.5
```

#### 设置实例音量
```
命令: set_volume
格式: arcartx_xcmusic set_volume <volume> <instanceName>
参数:
  - volume: 音量值（0.0-1.0）
  - instanceName: 实例名称
示例: arcartx_xcmusic set_volume 0.8 background
```

---

## 使用场景示例

### 场景1：区域背景音乐
当玩家进入特定区域时，服务器发送播放命令：
```
// 玩家进入城镇
arcartx_xcmusic fadeInPlay 2000 music/town.mp3 area_bgm

// 玩家离开城镇
arcartx_xcmusic fadeOutStop 2000 area_bgm
```

### 场景2：剧情音乐控制
在剧情对话或事件中控制音乐：
```
// 剧情开始，播放剧情音乐
arcartx_xcmusic play music/story.mp3 story_music
arcartx_xcmusic set_volume 0.6 story_music

// 剧情结束，停止音乐
arcartx_xcmusic fadeOutStop story_music
```

### 场景3：Boss战音乐
Boss战斗音乐切换：
```
// Boss战开始
arcartx_xcmusic fadeOutStop 1000 area_bgm
arcartx_xcmusic fadeInPlay 1000 music/boss.mp3 boss_music

// Boss战结束
arcartx_xcmusic fadeOutStop 2000 boss_music
arcartx_xcmusic fadeInPlay 2000 music/town.mp3 area_bgm
```

### 场景4：全局音量控制
管理员调整全局音效：
```
// 降低所有音乐音量（比如语音通话时）
arcartx_xcmusic set_master_volume 0.3

// 恢复正常音量
arcartx_xcmusic set_master_volume 1.0
```

---

## 技术实现

### 服务端发送示例（伪代码）

```java
// 发送播放命令
sendCustomPacket(player, "arcartx_xcmusic", "play", "music/bgm.mp3", "background");

// 发送淡入播放命令
sendCustomPacket(player, "arcartx_xcmusic", "fadeInPlay", "3000", "music/bgm.mp3", "background");

// 发送停止命令
sendCustomPacket(player, "arcartx_xcmusic", "stop", "background");

// 发送设置音量命令
sendCustomPacket(player, "arcartx_xcmusic", "set_volume", "0.5", "background");
```

### 客户端处理流程

1. `XcMusicNetworkListener` 监听 `CustomPacketEvent`
2. 检查包ID是否为 `arcartx_xcmusic`
3. 在独立线程中解析命令和参数
4. 调用 `XcMusicPlayerManager` 的相应方法
5. 执行音乐播放/停止/控制操作

---

## 命令参数说明

| 参数 | 类型 | 说明 | 示例 |
|------|------|------|------|
| url | String | 音频文件路径或URL<br>相对路径基于 `ArcartX/media` | `music/bgm.mp3`<br>`http://example.com/music.mp3` |
| instanceName | String | 实例唯一标识符 | `background`, `boss_music`, `sfx` |
| duration | Long | 时长（毫秒） | `3000` = 3秒 |
| volume | Float | 音量（0.0-1.0） | `0.5` = 50%, `1.0` = 100% |

---

## 与 Shimmer API 对比

| 功能 | 网络通讯 | Shimmer API |
|------|----------|-------------|
| 调用端 | 服务器 | 客户端脚本 |
| 应用场景 | 服务器主导的音乐控制 | 本地交互、UI反馈 |
| 实时性 | 取决于网络延迟 | 即时 |
| 权限控制 | 服务器控制 | 客户端自主 |

**推荐使用方式：**
- 区域音乐、剧情音乐 → 网络通讯
- UI音效、交互反馈 → Shimmer API

---

## 注意事项

⚠️ **参数验证**: 服务器应验证参数有效性，避免发送非法数据  
⚠️ **性能考虑**: 避免短时间内频繁发送命令  
⚠️ **实例管理**: 服务器应追踪每个玩家的音乐实例状态  
⚠️ **资源路径**: 确保音频文件在客户端存在或URL可访问  
⚠️ **音量范围**: 音量值会自动限制在 0.0-1.0 范围内  

---

## 线程安全

所有网络命令在独立的线程池中执行，保证：
- 不会阻塞主线程
- 并发请求安全处理
- 自动管理线程资源

---

## 错误处理

- 解析错误：自动忽略无效命令
- 实例不存在：操作静默失败（不会报错）
- 文件不存在：播放失败，自动清理资源
- 参数错误：捕获异常，防止崩溃


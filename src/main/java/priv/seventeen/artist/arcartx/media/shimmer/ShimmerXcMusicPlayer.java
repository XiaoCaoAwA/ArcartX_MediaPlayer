/*
    Copyright (C) 2025 17Artist

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package priv.seventeen.artist.arcartx.media.shimmer;

import priv.seventeen.artist.arcartx.media.player.XcMusicPlayerManager;
import priv.seventeen.artist.arcartx.shimmer.annotations.ShimmerInvokeHandler;
import priv.seventeen.artist.arcartx.shimmer.annotations.ShimmerNamespace;
import priv.seventeen.artist.arcartx.shimmer.callable.InvocationData;

/**
 * @program: ShimmerXcMusicPlayer
 * @description: 多实例音乐播放器 Shimmer接口
 * @author: XiaoCaoAwA
 * @create: 2025-12-01 00:00
 **/
@ShimmerNamespace("XcMusicPlayer")
public class ShimmerXcMusicPlayer {

    /**
     * 播放音乐
     * 用法：XcMusicPlayer.play('http://example.com/music.mp3','example')
     * 或：XcMusicPlayer.play('music.mp3','example')
     */
    @ShimmerInvokeHandler("play")
    public static void play(InvocationData data){
        if(data.size() >= 2){
            String url = data.get(0).stringValue();
            String instanceName = data.get(1).stringValue();
            XcMusicPlayerManager.play(url, instanceName);
        }
    }

    /**
     * 淡入播放音乐
     * 用法：XcMusicPlayer.fadeInPlay('3000','http://example.com/music.mp3','example')
     * 或：XcMusicPlayer.fadeInPlay('3000','music.mp3','example')
     */
    @ShimmerInvokeHandler("fadeInPlay")
    public static void fadeInPlay(InvocationData data){
        if(data.size() >= 3){
            long fadeDuration = data.get(0).longValue();
            String url = data.get(1).stringValue();
            String instanceName = data.get(2).stringValue();
            XcMusicPlayerManager.fadeInPlay(fadeDuration, url, instanceName);
        }
    }

    /**
     * 淡出淡入播放音乐
     * 如果实例存在，先淡出当前音乐，然后淡入播放新音乐
     * 用法：XcMusicPlayer.fadeOutInPlay('example','3000','music.mp3')
     */
    @ShimmerInvokeHandler("fadeOutInPlay")
    public static void fadeOutInPlay(InvocationData data){
        if(data.size() >= 3){
            String instanceName = data.get(0).stringValue();
            long totalDuration = data.get(1).longValue();
            String url = data.get(2).stringValue();
            XcMusicPlayerManager.fadeOutInPlay(instanceName, totalDuration, url);
        }
    }

    /**
     * 停止音乐
     * 用法：XcMusicPlayer.stop() - 停止所有实例
     * 或：XcMusicPlayer.stop('example') - 停止指定实例
     */
    @ShimmerInvokeHandler("stop")
    public static void stop(InvocationData data){
        if(data.size() >= 1){
            String instanceName = data.get(0).stringValue();
            XcMusicPlayerManager.stop(instanceName);
        } else {
            // 停止所有实例
            XcMusicPlayerManager.stop(null);
        }
    }

    /**
     * 淡出停止音乐
     * 用法：XcMusicPlayer.fadeOutStop('example') - 默认3秒淡出
     * 或：XcMusicPlayer.fadeOutStop('3000','example') - 指定时长淡出
     */
    @ShimmerInvokeHandler("fadeOutStop")
    public static void fadeOutStop(InvocationData data){
        if(data.size() >= 2){
            long fadeDuration = data.get(0).longValue();
            String instanceName = data.get(1).stringValue();
            XcMusicPlayerManager.fadeOutStop(fadeDuration, instanceName);
        } else if(data.size() >= 1){
            // 使用默认淡出时长（3秒）
            String instanceName = data.get(0).stringValue();
            XcMusicPlayerManager.fadeOutStop(instanceName);
        }
    }

    /**
     * 设置音量
     * 用法：XcMusicPlayer.setVolume(0.5, 'bgm') - 设置实例音量
     * 或：XcMusicPlayer.setVolume(0.5) - 设置主音量
     */
    @ShimmerInvokeHandler("setVolume")
    public static void setVolume(InvocationData data){
        if(data.size() >= 2){
            // 设置实例音量
            float volume = data.get(0).floatValue();
            String instanceName = data.get(1).stringValue();
            XcMusicPlayerManager.setInstanceVolume(volume, instanceName);
        } else if(data.size() >= 1){
            // 设置主音量
            float volume = data.get(0).floatValue();
            XcMusicPlayerManager.setMasterVolumeWithClamp(volume);
        }
    }

    /**
     * 获取音量
     * 用法：XcMusicPlayer.getVolume('bgm') - 获取实例音量
     * 或：XcMusicPlayer.getVolume() - 获取主音量
     */
    @ShimmerInvokeHandler("getVolume")
    public static float getVolume(InvocationData data){
        if(data.size() >= 1){
            // 获取实例音量
            String instanceName = data.get(0).stringValue();
            return XcMusicPlayerManager.getInstanceVolume(instanceName);
        } else {
            // 获取主音量
            return XcMusicPlayerManager.getMasterVolume();
        }
    }

}


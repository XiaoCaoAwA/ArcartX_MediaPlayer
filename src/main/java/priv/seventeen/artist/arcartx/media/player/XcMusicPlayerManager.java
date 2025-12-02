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
package priv.seventeen.artist.arcartx.media.player;

import lombok.Getter;
import lombok.Setter;
import priv.seventeen.artist.arcartx.media.player.type.XcMusicPlayer;
import priv.seventeen.artist.arcartx.media.utils.Utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @program: XcMusicPlayerManager
 * @description: 多实例音乐播放器管理器
 * @author: XiaoCaoAwA
 * @create: 2025-12-01 00:00
 **/
public class XcMusicPlayerManager {

    // 每个实例使用独立线程池，实现真正的并发播放
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    // 存储所有音乐播放器实例
    private static final Map<String, XcMusicPlayer> instances = new ConcurrentHashMap<>();

    // 主音量，影响所有实例
    @Getter @Setter
    private static volatile float masterVolume = 1.0f;

    /**
     * 播放音乐
     * @param url 音频URL（支持在线URL和本地相对路径）
     * @param instanceName 实例名称
     */
    public static void play(String url, String instanceName) {
        try {
            String target = Utils.parseUrl(url);
            if(target != null){
                // 停止同名实例并等待其完全停止
                stopInstanceAndWait(instanceName);
                
                // 创建新实例
                XcMusicPlayer player = new XcMusicPlayer(target, instanceName);
                instances.put(instanceName, player);
                
                // 在独立线程中播放
                executorService.submit(() -> player.play());
            }
        } catch (Exception e) {
            // 播放失败，清理实例
            stopInstance(instanceName);
        }
    }

    /**
     * 淡入播放音乐
     * @param fadeDuration 淡入时长（毫秒）
     * @param url 音频URL
     * @param instanceName 实例名称
     */
    public static void fadeInPlay(long fadeDuration, String url, String instanceName) {
        try {
            String target = Utils.parseUrl(url);
            if(target != null){
                // 停止同名实例并等待其完全停止
                stopInstanceAndWait(instanceName);
                
                // 创建新实例并设置淡入
                XcMusicPlayer player = new XcMusicPlayer(target, instanceName);
                player.startFadeIn(fadeDuration);
                instances.put(instanceName, player);
                
                // 在独立线程中播放
                executorService.submit(() -> player.play());
            }
        } catch (Exception e) {
            stopInstance(instanceName);
        }
    }

    /**
     * 停止指定实例
     * @param instanceName 实例名称，如果为null则停止所有实例
     */
    public static void stop(String instanceName) {
        if(instanceName == null || instanceName.isEmpty()){
            // 停止所有实例
            stopAll();
        } else {
            // 停止指定实例
            stopInstance(instanceName);
        }
    }

    /**
     * 淡出停止指定实例（默认3秒）
     * @param instanceName 实例名称
     */
    public static void fadeOutStop(String instanceName) {
        fadeOutStop(3000, instanceName);
    }

    /**
     * 淡出停止指定实例
     * @param fadeDuration 淡出时长（毫秒）
     * @param instanceName 实例名称
     */
    public static void fadeOutStop(long fadeDuration, String instanceName) {
        XcMusicPlayer player = instances.get(instanceName);
        if(player != null){
            player.startFadeOut(fadeDuration);
        }
    }

    /**
     * 停止所有实例
     */
    public static void stopAll() {
        for(XcMusicPlayer player : instances.values()){
            if(player != null){
                player.shutdown = true;
            }
        }
        instances.clear();
    }

    /**
     * 停止单个实例
     */
    private static void stopInstance(String instanceName) {
        XcMusicPlayer player = instances.remove(instanceName);
        if(player != null){
            player.shutdown = true;
        }
    }

    /**
     * 停止单个实例并等待其完全停止
     * 用于确保在启动新实例前，旧实例已完全清理
     */
    private static void stopInstanceAndWait(String instanceName) {
        XcMusicPlayer player = instances.remove(instanceName);
        if(player != null){
            player.shutdown = true;
            
            // 等待播放器线程完全停止（最多等待500ms）
            int waitCount = 0;
            while(!player.isClosed() && waitCount < 50){
                try {
                    Thread.sleep(10);
                    waitCount++;
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    /**
     * 从管理器中移除实例（由播放器自己调用）
     */
    public static void removeInstance(String instanceName) {
        instances.remove(instanceName);
    }

    /**
     * 设置指定实例的音量
     * @param volume 音量值（0-1）
     * @param instanceName 实例名称
     */
    public static void setInstanceVolume(float volume, String instanceName) {
        XcMusicPlayer player = instances.get(instanceName);
        if(player != null){
            player.setVolume(Math.max(0, Math.min(1, volume)));
        }
    }

    /**
     * 获取指定实例的音量
     * @param instanceName 实例名称
     * @return 音量值（0-1）
     */
    public static float getInstanceVolume(String instanceName) {
        XcMusicPlayer player = instances.get(instanceName);
        if(player != null){
            return player.getVolume();
        }
        return 0;
    }

    /**
     * 设置主音量（影响所有实例，带范围限制）
     * @param volume 音量值（0-1）
     */
    public static void setMasterVolumeWithClamp(float volume) {
        masterVolume = Math.max(0, Math.min(1, volume));
    }

}


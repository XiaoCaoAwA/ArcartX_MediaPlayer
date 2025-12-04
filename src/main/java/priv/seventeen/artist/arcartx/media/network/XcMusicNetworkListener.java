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
package priv.seventeen.artist.arcartx.media.network;

import priv.seventeen.artist.arcartx.common.api.events.EventHandler;
import priv.seventeen.artist.arcartx.common.api.events.Listener;
import priv.seventeen.artist.arcartx.common.api.events.arcartx.client.CustomPacketEvent;
import priv.seventeen.artist.arcartx.media.player.XcMusicPlayerManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @program: XcMusicNetworkListener
 * @description: XcMusicPlayer 网络通讯监听器
 * @author: XiaoCaoAwA
 * @create: 2025-12-02 00:00
 **/
public class XcMusicNetworkListener implements Listener {

    private static final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();

    @EventHandler
    public void onCustomPack(CustomPacketEvent event){
        if(event.getId().equalsIgnoreCase("arcartx_xcmusic")){
            networkExecutor.submit(() ->{
                handlePacket(event);
            });
        }
    }

    private void handlePacket(CustomPacketEvent event){
        // 停止所有实例：arcartx_xcmusic stop
        if(event.getArgSize() == 1 && event.getArg(0).equalsIgnoreCase("stop")){
            XcMusicPlayerManager.stop(null);
            return;
        }

        // 两个参数的命令
        if(event.getArgSize() == 2){
            String command = event.getArg(0);
            String arg1 = event.getArg(1);

            switch (command){
                case "stop" -> {
                    // 停止指定实例：arcartx_xcmusic stop <instanceName>
                    XcMusicPlayerManager.stop(arg1);
                }
                case "fadeOutStop" -> {
                    // 淡出停止（默认3秒）：arcartx_xcmusic fadeOutStop <instanceName>
                    XcMusicPlayerManager.fadeOutStop(arg1);
                }
                case "set_master_volume" -> {
                    // 设置主音量：arcartx_xcmusic set_master_volume <volume>
                    float volume = Float.parseFloat(arg1);
                    XcMusicPlayerManager.setMasterVolumeWithClamp(volume);
                }
            }
        }

        // 三个参数的命令
        if(event.getArgSize() == 3){
            String command = event.getArg(0);
            String arg1 = event.getArg(1);
            String arg2 = event.getArg(2);

            switch (command){
                case "play" -> {
                    // 播放：arcartx_xcmusic play <url> <instanceName>
                    XcMusicPlayerManager.play(arg1, arg2);
                }
                case "fadeOutStop" -> {
                    // 淡出停止（指定时长）：arcartx_xcmusic fadeOutStop <duration> <instanceName>
                    long duration = Long.parseLong(arg1);
                    XcMusicPlayerManager.fadeOutStop(duration, arg2);
                }
                case "set_volume" -> {
                    // 设置实例音量：arcartx_xcmusic set_volume <volume> <instanceName>
                    float volume = Float.parseFloat(arg1);
                    XcMusicPlayerManager.setInstanceVolume(volume, arg2);
                }
            }
        }

        // 四个参数的命令
        if(event.getArgSize() == 4){
            String command = event.getArg(0);
            String arg1 = event.getArg(1);
            String arg2 = event.getArg(2);
            String arg3 = event.getArg(3);

            switch (command){
                case "fadeInPlay" -> {
                    // 淡入播放：arcartx_xcmusic fadeInPlay <duration> <url> <instanceName>
                    long duration = Long.parseLong(arg1);
                    XcMusicPlayerManager.fadeInPlay(duration, arg2, arg3);
                }
                case "fadeOutInPlay" -> {
                    // 淡出淡入播放：arcartx_xcmusic fadeOutInPlay <instanceName> <totalDuration> <url>
                    long totalDuration = Long.parseLong(arg2);
                    XcMusicPlayerManager.fadeOutInPlay(arg1, totalDuration, arg3);
                }
            }
        }
    }
}


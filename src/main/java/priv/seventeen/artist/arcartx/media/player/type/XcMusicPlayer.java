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
package priv.seventeen.artist.arcartx.media.player.type;

import lombok.Getter;
import lombok.Setter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import priv.seventeen.artist.arcartx.common.game.Minecraft;
import priv.seventeen.artist.arcartx.media.player.XcMusicPlayerManager;

/**
 * @program: XcMusicPlayer
 * @description: 多实例音乐播放器
 * @author: XiaoCaoAwA
 * @create: 2025-12-01 00:00
 **/
public class XcMusicPlayer {

    private final FFmpegFrameGrabber frameGrabber;
    private final AudioPlayer audioPlayer;
    
    @Getter
    private final String instanceName;

    public volatile boolean shutdown = false;
    private volatile boolean closed = false;

    @Getter @Setter
    private volatile float volume = 1.0f;
    
    // 淡入淡出相关
    private volatile boolean fadeIn = false;
    private volatile boolean fadeOut = false;
    private volatile long fadeStartTime = 0;
    private volatile long fadeDuration = 0;
    private volatile float fadeStartVolume = 0;
    private volatile float fadeTargetVolume = 1.0f;

    private final static Minecraft minecraft = Minecraft.INSTANCE;

    public XcMusicPlayer(String url, String instanceName){
        this.instanceName = instanceName;
        this.frameGrabber = new FFmpegFrameGrabber(url);
        this.audioPlayer = new AudioPlayer(){
            @Override
            public float getVolume() {
                return XcMusicPlayer.this.getEffectiveVolume();
            }
        };
    }

    /**
     * 获取实际音量（考虑主音量、实例音量、淡入淡出）
     */
    protected float getEffectiveVolume(){
        float baseVolume = minecraft.getSoundSourceVolume("MASTER") 
                         * XcMusicPlayerManager.getMasterVolume() 
                         * this.volume;
        
        // 处理淡入淡出
        if(fadeIn || fadeOut){
            long elapsed = System.currentTimeMillis() - fadeStartTime;
            if(elapsed >= fadeDuration){
                // 淡入淡出结束
                fadeIn = false;
                fadeOut = false;
                if(fadeOut){
                    // 淡出完成，停止播放
                    shutdown = true;
                }
                return baseVolume * fadeTargetVolume;
            } else {
                // 计算当前淡入淡出音量
                float progress = (float)elapsed / fadeDuration;
                float currentFadeVolume = fadeStartVolume + (fadeTargetVolume - fadeStartVolume) * progress;
                return baseVolume * currentFadeVolume;
            }
        }
        
        return baseVolume;
    }

    /**
     * 开始淡入播放
     */
    public void startFadeIn(long durationMs){
        this.fadeIn = true;
        this.fadeOut = false;
        this.fadeStartTime = System.currentTimeMillis();
        this.fadeDuration = durationMs;
        this.fadeStartVolume = 0;
        this.fadeTargetVolume = 1.0f;
    }

    /**
     * 开始淡出停止
     */
    public void startFadeOut(long durationMs){
        this.fadeIn = false;
        this.fadeOut = true;
        this.fadeStartTime = System.currentTimeMillis();
        this.fadeDuration = durationMs;
        this.fadeStartVolume = 1.0f;
        this.fadeTargetVolume = 0;
    }

    private boolean checkError = false;

    public void startTick(){
        try {
            this.checkError = false;
            this.frameGrabber.start();
            this.audioPlayer.init(this.frameGrabber);
        } catch (Exception e){
            checkError = true;
            close();
        }
    }

    public void runTick(){
        while (true){
            if(this.checkError || this.closed || this.shutdown){
                close();
                break;
            }
            try {
                Frame frame = this.frameGrabber.grabSamples();

                if(frame == null){
                    // 音频播放结束
                    close();
                    break;
                }

                this.audioPlayer.processAudio(frame.samples);

            } catch (Exception e){
                checkError = true;
                close();
                break;
            }
        }
    }

    public void play(){
        startTick();
        runTick();
    }

    private void close(){
        if(closed){
            return;
        }
        closed = true;
        try {
            if(this.frameGrabber != null){
                this.frameGrabber.stop();
                this.frameGrabber.release();
                this.frameGrabber.close();
            }
            this.audioPlayer.close();
        } catch (Exception e){
            //
        }
        
        // 从管理器中移除
        XcMusicPlayerManager.removeInstance(instanceName);
    }

}


package com.gobang.audio;

import javafx.scene.media.AudioClip;

import java.io.File;

/**
 * 音效管理类
 */
public class SoundManager {
    private AudioClip placeSound;
    private AudioClip winSound;
    private AudioClip buttonClickSound;
    
    public SoundManager() {
        try {
            // 加载音效资源
            placeSound = loadSound("/sounds/place.wav");
            winSound = loadSound("/sounds/win.wav");
            buttonClickSound = loadSound("/sounds/button_click.wav");
        } catch (Exception e) {
            System.err.println("加载音效失败: " + e.getMessage());
            // 使用默认无声的音效
            placeSound = new AudioClip("data:audio/wav;base64,UklGRiQAAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQAAAAA=");
            winSound = new AudioClip("data:audio/wav;base64,UklGRiQAAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQAAAAA=");
            buttonClickSound = new AudioClip("data:audio/wav;base64,UklGRiQAAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQAAAAA=");
        }
    }
    
    private AudioClip loadSound(String resourcePath) {
        try {
            String url = getClass().getResource(resourcePath).toExternalForm();
            return new AudioClip(url);
        } catch (Exception e) {
            System.err.println("无法加载音效 " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }
    
    public void playPlaceSound() {
        if (placeSound != null) {
            placeSound.play();
        }
    }
    
    public void playWinSound() {
        if (winSound != null) {
            winSound.play();
        }
    }
    
    public void playButtonClickSound() {
        if (buttonClickSound != null) {
            buttonClickSound.play();
        }
    }
}    
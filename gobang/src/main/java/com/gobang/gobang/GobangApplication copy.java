package com.gobang;

import com.gobang.audio.SoundManager;
import com.gobang.gui.GobangGUI;
import javafx.application.Application;
import javafx.stage.Stage;

public class GobangApplication extends Application {

    private SoundManager soundManager;

    @Override
    public void start(Stage primaryStage) {
        // 初始化音效管理器
        soundManager = new SoundManager();
        
        // 创建游戏主界面
        GobangGUI gui = new GobangGUI(primaryStage, soundManager);
        gui.init();
    }

    public static void main(String[] args) {
        launch(args);
    }
}    
package com.gobang.gui;

import com.gobang.*;
import com.gobang.audio.SoundManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GobangGUI {
    private static final int BOARD_SIZE = 15;
    private static final int CELL_SIZE = 40;
    private static final int BOARD_WIDTH = CELL_SIZE * BOARD_SIZE;
    private static final int BOARD_HEIGHT = CELL_SIZE * BOARD_SIZE;
    
    private Stage primaryStage;
    private Board board;
    private Player humanPlayer;
    private Player aiPlayer;
    private Player currentPlayer;
    
    private StackPane[][] cellNodes;
    private Circle[][] stoneNodes;
    private Label statusLabel;
    private Label scoreLabel;
    private Button undoButton;
    private Button resetButton;
    private Button soundButton;
    
    private boolean gameOver = false;
    private boolean soundEnabled = true;
    private SoundManager soundManager;
    
    // 记录历史步骤用于悔棋
    private java.util.Stack<Move> moveHistory = new java.util.Stack<>();

    public GobangGUI(Stage primaryStage, SoundManager soundManager) {
        this.primaryStage = primaryStage;
        this.soundManager = soundManager;
    }
    
    public void init() {
        // 初始化游戏模型
        board = new Board();
        humanPlayer = new HumanPlayer("玩家", Stone.BLACK);
        aiPlayer = new AIPlayer("AI", Stone.WHITE);
        currentPlayer = humanPlayer;
        
        // 创建主界面
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setLeft(createSidePanel());
        root.setCenter(createBoardView());
        root.setBottom(createControlPanel());
        
        // 设置场景和舞台
        Scene scene = new Scene(root, BOARD_WIDTH + 300, BOARD_HEIGHT + 100);
        primaryStage.setTitle("五子棋");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private VBox createSidePanel() {
        VBox sidePanel = new VBox(20);
        sidePanel.setPadding(new Insets(10));
        sidePanel.setPrefWidth(200);
        
        // 游戏标题
        Label titleLabel = new Label("五子棋");
        titleLabel.setFont(new Font("SimHei", 24));
        titleLabel.setTextFill(Color.DARKRED);
        
        // 游戏信息面板
        GridPane infoPanel = new GridPane();
        infoPanel.setHgap(10);
        infoPanel.setVgap(10);
        infoPanel.setPadding(new Insets(10));
        infoPanel.setStyle("-fx-background-color: #f0f0f0; -fx-border-radius: 5px;");
        
        Label playerLabel = new Label("玩家:");
        Label playerStone = new Label("●");
        playerStone.setTextFill(Color.BLACK);
        playerStone.setFont(new Font(16));
        
        Label aiLabel = new Label("AI:");
        Label aiStone = new Label("○");
        aiStone.setTextFill(Color.WHITE);
        aiStone.setStyle("-fx-background-color: black; -fx-padding: 1px 5px; -fx-border-radius: 5px;");
        aiStone.setFont(new Font(16));
        
        scoreLabel = new Label("分数: 0");
        
        infoPanel.add(playerLabel, 0, 0);
        infoPanel.add(playerStone, 1, 0);
        infoPanel.add(aiLabel, 0, 1);
        infoPanel.add(aiStone, 1, 1);
        infoPanel.add(scoreLabel, 0, 2, 2, 1);
        
        // 难度选择
        Label difficultyLabel = new Label("AI难度:");
        Slider difficultySlider = new Slider(1, 3, 2);
        difficultySlider.setShowTickLabels(true);
        difficultySlider.setShowTickMarks(true);
        difficultySlider.setMajorTickUnit(1);
        difficultySlider.setBlockIncrement(1);
        difficultySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ((AIPlayer)aiPlayer).setDifficulty(newValue.intValue());
        });
        
        // 游戏历史面板
        Label historyLabel = new Label("游戏历史");
        historyLabel.setFont(new Font(16));
        
        VBox historyContainer = new VBox(5);
        ScrollPane historyScrollPane = new ScrollPane(historyContainer);
        historyScrollPane.setPrefHeight(200);
        historyScrollPane.setFitToWidth(true);
        
        sidePanel.getChildren().addAll(
            titleLabel, infoPanel, 
            difficultyLabel, difficultySlider,
            historyLabel, historyScrollPane
        );
        
        return sidePanel;
    }
    
    private StackPane createBoardView() {
        StackPane boardPane = new StackPane();
        
        // 创建棋盘背景
        Rectangle background = new Rectangle(BOARD_WIDTH, BOARD_HEIGHT);
        background.setFill(Color.rgb(238, 188, 103));
        background.setStroke(Color.BLACK);
        background.setStrokeWidth(2);
        background.setArcWidth(10);
        background.setArcHeight(10);
        
        // 添加木纹背景图
        try {
            Image woodTexture = new Image(getClass().getResourceAsStream("/wood_texture.jpg"));
            BackgroundImage bgImage = new BackgroundImage(
                woodTexture, BackgroundRepeat.REPEAT, 
                BackgroundRepeat.REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(1.0, 1.0, true, true, false, false)
            );
            boardPane.setBackground(new Background(bgImage));
        } catch (Exception e) {
            System.err.println("加载木纹背景失败: " + e.getMessage());
        }
        
        // 创建棋盘网格
        cellNodes = new StackPane[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                
                // 添加网格线
                Line hLine = new Line(0, 0, CELL_SIZE, 0);
                Line vLine = new Line(0, 0, 0, CELL_SIZE);
                
                // 边缘线加粗
                if (i == 0) {
                    hLine.setStartY(CELL_SIZE / 2.0);
                    hLine.setEndY(CELL_SIZE / 2.0);
                    hLine.setStrokeWidth(2);
                } else if (i == BOARD_SIZE - 1) {
                    hLine.setStartY(-CELL_SIZE / 2.0);
                    hLine.setEndY(-CELL_SIZE / 2.0);
                    hLine.setStrokeWidth(2);
                } else {
                    hLine.setStartY(-CELL_SIZE / 2.0);
                    hLine.setEndY(CELL_SIZE / 2.0);
                }
                
                if (j == 0) {
                    vLine.setStartX(CELL_SIZE / 2.0);
                    vLine.setEndX(CELL_SIZE / 2.0);
                    vLine.setStrokeWidth(2);
                } else if (j == BOARD_SIZE - 1) {
                    vLine.setStartX(-CELL_SIZE / 2.0);
                    vLine.setEndX(-CELL_SIZE / 2.0);
                    vLine.setStrokeWidth(2);
                } else {
                    vLine.setStartX(-CELL_SIZE / 2.0);
                    vLine.setEndX(CELL_SIZE / 2.0);
                }
                
                // 添加天元和星位标记
                if ((i == 3 || i == 7 || i == 11) && (j == 3 || j == 7 || j == 11)) {
                    Circle mark = new Circle(3);
                    mark.setFill(Color.BLACK);
                    cell.getChildren().add(mark);
                }
                
                // 添加悬停效果
                cell.setOnMouseEntered(e -> {
                    if (!gameOver && currentPlayer == humanPlayer && 
                        board.getGrid()[i][j] == Stone.EMPTY) {
                        cell.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2);");
                    }
                });
                
                cell.setOnMouseExited(e -> {
                    cell.setStyle("");
                });
                
                // 添加点击事件
                final int rowIndex = i;
                final int colIndex = j;
                cell.setOnMouseClicked(e -> handleCellClick(rowIndex, colIndex));
                
                cellNodes[i][j] = cell;
                boardPane.getChildren().add(cell);
                
                // 设置单元格位置
                StackPane.setAlignment(cell, javafx.geometry.Pos.TOP_LEFT);
                cell.setTranslateX(j * CELL_SIZE);
                cell.setTranslateY(i * CELL_SIZE);
            }
        }
        
        // 初始化棋子数组
        stoneNodes = new Circle[BOARD_SIZE][BOARD_SIZE];
        
        boardPane.getChildren().add(background);
        // 确保网格在背景之上
        boardPane.getChildren().removeAll(cellNodes);
        boardPane.getChildren().addAll(cellNodes);
        
        return boardPane;
    }
    
    private HBox createControlPanel() {
        HBox controlPanel = new HBox(20);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setAlignment(Pos.CENTER);
        
        statusLabel = new Label("当前玩家: " + currentPlayer.getName() + "(" + currentPlayer.getStone() + ")");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        undoButton = new Button("悔棋");
        undoButton.setDisable(true);
        undoButton.setOnAction(e -> handleUndo());
        
        resetButton = new Button("重置游戏");
        resetButton.setOnAction(e -> resetGame());
        
        soundButton = new Button("音效: 开");
        soundButton.setOnAction(e -> toggleSound());
        
        controlPanel.getChildren().addAll(statusLabel, undoButton, resetButton, soundButton);
        return controlPanel;
    }
    
    private void handleCellClick(int row, int col) {
        if (gameOver || currentPlayer != humanPlayer) {
            return;
        }
        
        if (board.placeStone(row, col, humanPlayer.getStone())) {
            // 记录移动历史
            moveHistory.push(new Move(row, col));
            undoButton.setDisable(false);
            
            // 更新UI
            placeStoneUI(row, col, humanPlayer.getStone());
            
            // 播放落子音效
            if (soundEnabled) {
                soundManager.playPlaceSound();
            }
            
            // 检查胜负
            if (board.checkWin(row, col, humanPlayer.getStone())) {
                statusLabel.setText("游戏结束: " + humanPlayer.getName() + "获胜!");
                gameOver = true;
                if (soundEnabled) {
                    soundManager.playWinSound();
                }
                highlightWinningLine(row, col, humanPlayer.getStone());
                return;
            }
            
            // 检查平局
            if (board.isFull()) {
                statusLabel.setText("游戏结束: 平局!");
                gameOver = true;
                return;
            }
            
            // 切换到AI回合
            switchPlayer();
            statusLabel.setText("当前玩家: " + currentPlayer.getName() + "(" + currentPlayer.getStone() + ")");
            
            // AI思考并落子(延迟执行，让玩家有时间看到自己的落子)
            Platform.runLater(() -> {
                try {
                    Thread.sleep(500); // 延迟500毫秒
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                
                Move aiMove = aiPlayer.getMove(board);
                board.placeStone(aiMove.getRow(), aiMove.getCol(), aiPlayer.getStone());
                // 记录AI移动
                moveHistory.push(aiMove);
                undoButton.setDisable(false);
                
                placeStoneUI(aiMove.getRow(), aiMove.getCol(), aiPlayer.getStone());
                
                // 播放落子音效
                if (soundEnabled) {
                    soundManager.playPlaceSound();
                }
                
                // 检查AI是否获胜
                if (board.checkWin(aiMove.getRow(), aiMove.getCol(), aiPlayer.getStone())) {
                    statusLabel.setText("游戏结束: " + aiPlayer.getName() + "获胜!");
                    gameOver = true;
                    if (soundEnabled) {
                        soundManager.playWinSound();
                    }
                    highlightWinningLine(aiMove.getRow(), aiMove.getCol(), aiPlayer.getStone());
                    return;
                }
                
                // 检查平局
                if (board.isFull()) {
                    statusLabel.setText("游戏结束: 平局!");
                    gameOver = true;
                    return;
                }
                
                // 切换回玩家回合
                switchPlayer();
                statusLabel.setText("当前玩家: " + currentPlayer.getName() + "(" + currentPlayer.getStone() + ")");
            });
        }
    }
    
    private void placeStoneUI(int row, int col, Stone stone) {
        Circle circle = new Circle(CELL_SIZE / 2.5);
        circle.setCenterX(CELL_SIZE / 2.0);
        circle.setCenterY(CELL_SIZE / 2.0);
        
        if (stone == Stone.BLACK) {
            circle.setFill(Color.BLACK);
        } else {
            circle.setFill(Color.WHITE);
            circle.setStroke(Color.BLACK);
            circle.setStrokeWidth(1);
        }
        
        // 添加落子动画
        circle.setScaleX(0);
        circle.setScaleY(0);
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(300), circle);
        st.setToX(1);
        st.setToY(1);
        st.play();
        
        // 添加阴影效果
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setRadius(5.0);
        shadow.setOffsetX(3.0);
        shadow.setOffsetY(3.0);
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        circle.setEffect(shadow);
        
        // 存储棋子节点引用
        stoneNodes[row][col] = circle;
        cellNodes[row][col].getChildren().add(circle);
    }
    
    private void highlightWinningLine(int row, int col, Stone stone) {
        // 获取获胜连线的位置
        java.util.List<int[]> winningPositions = board.getWinningLine(row, col, stone);
        
        if (winningPositions != null) {
            for (int[] pos : winningPositions) {
                Circle stoneCircle = stoneNodes[pos[0]][pos[1]];
                if (stoneCircle != null) {
                    // 添加发光效果
                    javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.8);
                    stoneCircle.setEffect(glow);
                    
                    // 添加脉冲动画
                    javafx.animation.ScaleTransition pulse = new javafx.animation.ScaleTransition(
                        javafx.util.Duration.millis(1000), stoneCircle);
                    pulse.setFromX(1.0);
                    pulse.setFromY(1.0);
                    pulse.setToX(1.1);
                    pulse.setToY(1.1);
                    pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
                    pulse.setAutoReverse(true);
                    pulse.play();
                }
            }
        }
    }
    
    private void handleUndo() {
        if (moveHistory.isEmpty()) {
            return;
        }
        
        // 播放按钮点击音效
        if (soundEnabled) {
            soundManager.playButtonClickSound();
        }
        
        // 撤销AI的最后一步
        if (!moveHistory.isEmpty()) {
            Move aiMove = moveHistory.pop();
            board.getGrid()[aiMove.getRow()][aiMove.getCol()] = Stone.EMPTY;
            cellNodes[aiMove.getRow()][aiMove.getCol()].getChildren().remove(
                stoneNodes[aiMove.getRow()][aiMove.getCol()]);
            stoneNodes[aiMove.getRow()][aiMove.getCol()] = null;
        }
        
        // 撤销玩家的最后一步
        if (!moveHistory.isEmpty()) {
            Move playerMove = moveHistory.pop();
            board.getGrid()[playerMove.getRow()][playerMove.getCol()] = Stone.EMPTY;
            cellNodes[playerMove.getRow()][playerMove.getCol()].getChildren().remove(
                stoneNodes[playerMove.getRow()][playerMove.getCol()]);
            stoneNodes[playerMove.getRow()][playerMove.getCol()] = null;
            
            // 如果游戏已经结束，撤销后重新开始
            if (gameOver) {
                gameOver = false;
                statusLabel.setText("当前玩家: " + currentPlayer.getName() + "(" + currentPlayer.getStone() + ")");
            }
        }
        
        // 如果历史记录为空，禁用悔棋按钮
        if (moveHistory.isEmpty()) {
            undoButton.setDisable(true);
        }
    }
    
    private void toggleSound() {
        soundEnabled = !soundEnabled;
        soundButton.setText("音效: " + (soundEnabled ? "开" : "关"));
        
        // 播放按钮点击音效
        if (soundEnabled) {
            soundManager.playButtonClickSound();
        }
    }
    
    private void switchPlayer() {
        currentPlayer = (currentPlayer == humanPlayer) ? aiPlayer : humanPlayer;
    }
    
    private void resetGame() {
        // 播放按钮点击音效
        if (soundEnabled) {
            soundManager.playButtonClickSound();
        }
        
        board.clear();
        gameOver = false;
        currentPlayer = humanPlayer;
        statusLabel.setText("当前玩家: " + currentPlayer.getName() + "(" + currentPlayer.getStone() + ")");
        undoButton.setDisable(true);
        
        // 清除所有棋子
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                cellNodes[i][j].getChildren().removeIf(node -> node instanceof Circle);
                stoneNodes[i][j] = null;
            }
        }
        
        // 清空历史记录
        moveHistory.clear();
    }
}    
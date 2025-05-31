package com.gobang.gobang.gui;

import com.gobang.*;
import com.gobang.gobang.audio.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Stack;
import java.util.List;

public class GobangSwingGUI extends JFrame {
    private static final int BOARD_SIZE = 15;
    private static final int CELL_SIZE = 40;
    private static final int BOARD_WIDTH = CELL_SIZE * BOARD_SIZE;
    private static final int BOARD_HEIGHT = CELL_SIZE * BOARD_SIZE;
    
    private Board board;
    private Player humanPlayer;
    private Player aiPlayer;
    private Player currentPlayer;
    
    private CellButton[][] cellButtons;
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private JButton undoButton;
    private JButton resetButton;
    private JButton soundButton;
    
    private boolean gameOver = false;
    private boolean soundEnabled = true;
    private SoundManager soundManager;
    
    // 记录历史步骤用于悔棋
    private Stack<Move> moveHistory = new Stack<>();

    public GobangSwingGUI(SoundManager soundManager) {
        this.soundManager = soundManager;
        init();
    }
    
    public void init() {
        // 初始化游戏模型
        board = new Board();
        humanPlayer = new HumanPlayer("玩家", Stone.BLACK);
        aiPlayer = new AIPlayer("AI", Stone.WHITE);
        currentPlayer = humanPlayer;
        
        // 设置窗口
        setTitle("五子棋");
        setSize(BOARD_WIDTH + 300, BOARD_HEIGHT + 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.add(createSidePanel(), BorderLayout.WEST);
        mainPanel.add(createBoardView(), BorderLayout.CENTER);
        mainPanel.add(createControlPanel(), BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        setVisible(true);
    }
    
    private JPanel createSidePanel() {
        JPanel sidePanel = new JPanel(new BorderLayout(0, 15));
        sidePanel.setPreferredSize(new Dimension(220, getHeight()));
        sidePanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        // 游戏标题
        JLabel titleLabel = new JLabel("五子棋");
        titleLabel.setFont(new Font("SimHei", Font.BOLD, 28));
        titleLabel.setForeground(Color.DARKRED);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // 游戏信息面板
        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("游戏信息"));
        infoPanel.add(new JLabel("玩家:"));
        JLabel playerStone = new JLabel("●");
        playerStone.setFont(new Font("Arial", Font.BOLD, 24));
        infoPanel.add(playerStone);
        infoPanel.add(new JLabel("AI:"));
        JLabel aiStone = new JLabel("○");
        aiStone.setFont(new Font("Arial", Font.BOLD, 24));
        infoPanel.add(aiStone);
        scoreLabel = new JLabel("分数: 0");
        infoPanel.add(new JLabel());
        infoPanel.add(scoreLabel);
        
        // 难度选择
        JPanel difficultyPanel = new JPanel(new BorderLayout(0, 5));
        difficultyPanel.setBorder(BorderFactory.createTitledBorder("AI难度"));
        JLabel difficultyLabel = new JLabel("选择难度:");
        JSlider difficultySlider = new JSlider(1, 3, 2);
        difficultySlider.setPaintLabels(true);
        difficultySlider.setPaintTicks(true);
        difficultySlider.setMajorTickSpacing(1);
        difficultySlider.setPreferredSize(new Dimension(180, 50));
        difficultySlider.addChangeListener(e -> {
            JSlider slider = (JSlider) e.getSource();
            ((AIPlayer) aiPlayer).setDifficulty(slider.getValue());
        });
        difficultyPanel.add(difficultyLabel, BorderLayout.NORTH);
        difficultyPanel.add(difficultySlider, BorderLayout.CENTER);
        
        // 游戏历史面板
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("游戏历史"));
        JTextArea historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setLineWrap(true);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane historyScrollPane = new JScrollPane(historyArea);
        historyScrollPane.setPreferredSize(new Dimension(180, 180));
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);
        
        sidePanel.add(titleLabel, BorderLayout.NORTH);
        sidePanel.add(infoPanel, BorderLayout.CENTER);
        sidePanel.add(difficultyPanel, BorderLayout.SOUTH);
        
        return sidePanel;
    }
    
    private JPanel createBoardView() {
        JPanel boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制棋盘背景（木纹效果模拟）
                g2d.setColor(new Color(238, 188, 103));
                g2d.fillRoundRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT, 10, 10);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT, 10, 10);
                
                // 绘制网格线
                for (int i = 0; i < BOARD_SIZE; i++) {
                    for (int j = 0; j < BOARD_SIZE; j++) {
                        // 边缘线加粗
                        if (i == 0 || i == BOARD_SIZE - 1) {
                            g2d.setStroke(new BasicStroke(2));
                        } else {
                            g2d.setStroke(new BasicStroke(1));
                        }
                        
                        if (j == 0 || j == BOARD_SIZE - 1) {
                            g2d.setStroke(new BasicStroke(2));
                        } else {
                            g2d.setStroke(new BasicStroke(1));
                        }
                        
                        // 绘制横线
                        g2d.drawLine(j * CELL_SIZE, i * CELL_SIZE, j * CELL_SIZE, (i + 1) * CELL_SIZE);
                        // 绘制竖线
                        g2d.drawLine(j * CELL_SIZE, i * CELL_SIZE, (j + 1) * CELL_SIZE, i * CELL_SIZE);
                        
                        // 绘制天元和星位（棋盘上的小圆点）
                        if ((i == 3 || i == 7 || i == 11) && (j == 3 || j == 7 || j == 11)) {
                            g2d.fillOval(j * CELL_SIZE - 4, i * CELL_SIZE - 4, 8, 8);
                        }
                    }
                }
            }
        };
        
        boardPanel.setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        boardPanel.setLayout(null);
        boardPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // 创建单元格按钮
        cellButtons = new CellButton[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                CellButton cell = new CellButton(i, j);
                cell.setBounds(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                cell.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (!gameOver && currentPlayer == humanPlayer && 
                            board.getGrid()[i][j] == Stone.EMPTY) {
                            cell.setOpaque(true);
                            cell.setBackground(new Color(255, 255, 255, 100)); // 半透明白色悬停效果
                        }
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        cell.setOpaque(false);
                        cell.setBackground(null);
                    }
                });
                
                cell.addActionListener(e -> handleCellClick(cell.getRow(), cell.getCol()));
                boardPanel.add(cell);
                cellButtons[i][j] = cell;
            }
        }
        
        return boardPanel;
    }
    
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        statusLabel = new JLabel("当前玩家: " + currentPlayer.getName() + "(" + currentPlayer.getStone() + ")");
        statusLabel.setFont(new Font("SimHei", Font.BOLD, 18));
        
        undoButton = new JButton("悔棋");
        undoButton.setEnabled(false);
        undoButton.setFont(new Font("SimHei", Font.PLAIN, 14));
        undoButton.setPreferredSize(new Dimension(80, 35));
        undoButton.addActionListener(e -> handleUndo());
        
        resetButton = new JButton("重置游戏");
        resetButton.setFont(new Font("SimHei", Font.PLAIN, 14));
        resetButton.setPreferredSize(new Dimension(100, 35));
        resetButton.addActionListener(e -> resetGame());
        
        soundButton = new JButton("音效: 开");
        soundButton.setFont(new Font("SimHei", Font.PLAIN, 14));
        soundButton.setPreferredSize(new Dimension(80, 35));
        soundButton.addActionListener(e -> toggleSound());
        
        controlPanel.add(statusLabel);
        controlPanel.add(undoButton);
        controlPanel.add(resetButton);
        controlPanel.add(soundButton);
        
        return controlPanel;
    }
    
    private void handleCellClick(int row, int col) {
        if (gameOver || currentPlayer != humanPlayer) {
            return;
        }
        
        if (board.placeStone(row, col, humanPlayer.getStone())) {
            // 记录移动历史
            moveHistory.push(new Move(row, col));
            undoButton.setEnabled(true);
            
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
            
            // AI思考并落子（开启新线程避免界面卡顿）
            new Thread(() -> {
                try {
                    Thread.sleep(500); // 延迟500毫秒，模拟AI思考时间
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                
                Move aiMove = aiPlayer.getMove(board);
                board.placeStone(aiMove.getRow(), aiMove.getCol(), aiPlayer.getStone());
                // 记录AI移动
                moveHistory.push(aiMove);
                
                // 在EDT线程中更新UI
                SwingUtilities.invokeLater(() -> {
                    undoButton.setEnabled(true);
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
            }).start();
        }
    }
    
    private void placeStoneUI(int row, int col, Stone stone) {
        CellButton cell = cellButtons[row][col];
        cell.setStone(stone);
        cell.repaint(); // 重绘单元格以显示棋子
    }
    
    private void highlightWinningLine(int row, int col, Stone stone) {
        // 获取获胜连线的位置
        List<int[]> winningPositions = board.getWinningLine(row, col, stone);
        
        if (winningPositions != null) {
            for (int[] pos : winningPositions) {
                CellButton cell = cellButtons[pos[0]][pos[1]];
                if (cell != null) {
                    cell.setHighlighted(true);
                    cell.repaint(); // 重绘单元格以显示高亮效果
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
            cellButtons[aiMove.getRow()][aiMove.getCol()].setStone(Stone.EMPTY);
            cellButtons[aiMove.getRow()][aiMove.getCol()].setHighlighted(false);
            cellButtons[aiMove.getRow()][aiMove.getCol()].repaint();
        }
        
        // 撤销玩家的最后一步
        if (!moveHistory.isEmpty()) {
            Move playerMove = moveHistory.pop();
            board.getGrid()[playerMove.getRow()][playerMove.getCol()] = Stone.EMPTY;
            cellButtons[playerMove.getRow()][playerMove.getCol()].setStone(Stone.EMPTY);
            cellButtons[playerMove.getRow()][playerMove.getCol()].setHighlighted(false);
            cellButtons[playerMove.getRow()][playerMove.getCol()].repaint();
            
            // 如果游戏已经结束，撤销后重新开始
            if (gameOver) {
                gameOver = false;
                statusLabel.setText("当前玩家: " + currentPlayer.getName() + "(" + currentPlayer.getStone() + ")");
            }
        }
        
        // 如果历史记录为空，禁用悔棋按钮
        if (moveHistory.isEmpty()) {
            undoButton.setEnabled(false);
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
        undoButton.setEnabled(false);
        
        // 清除所有棋子
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                cellButtons[i][j].setStone(Stone.EMPTY);
                cellButtons[i][j].setHighlighted(false);
                cellButtons[i][j].repaint();
            }
        }
    }
}
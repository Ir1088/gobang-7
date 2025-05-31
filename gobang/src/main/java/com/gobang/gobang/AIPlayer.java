package com.gobang.gobang;

import java.util.*;

/**
 * AI玩家类 - 实现基于评分系统的AI决策
 */
public class AIPlayer implements Player {
    private String name;
    private Stone stone;
    private int difficulty; // 1: 初级, 2: 中级, 3: 高级
    
    // 棋型评分表
    private static final int EMPTY = 0;
    private static final int BLACK = 1;
    private static final int WHITE = 2;
    
    // 评分表 - 用于评估不同棋型的价值
    private static final int[][] SCORE_TABLE = {
        {0, 10, 100, 1000, 10000, 100000},  // 自己的棋型分数
        {0, 5, 50, 500, 5000, 50000}        // 对手的棋型分数
    };
    
    public AIPlayer(String name, Stone stone) {
        this.name = name;
        this.stone = stone;
        this.difficulty = 2; // 默认中级难度
    }
    
    public void setDifficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(3, difficulty));
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Stone getStone() {
        return stone;
    }
    
    @Override
    public Move getMove(Board board) {
        
        Stone[][] grid = board.getGrid();
        int size = board.getSize();
        Stone opponentStone = (stone == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
        
        // 获取所有可能的落子位置
        List<Move> possibleMoves = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == Stone.EMPTY) {
                    // 只考虑周围有棋子的位置，提高效率
                    if (hasAdjacentStone(grid, i, j, 2)) {
                        possibleMoves.add(new Move(i, j));
                    }
                }
            }
        }
        
        // 根据难度决定搜索深度
        int depth = (difficulty == 1) ? 2 : (difficulty == 2) ? 3 : 4;
        
        // 使用Alpha-Beta剪枝搜索最佳位置
        int bestScore = Integer.MIN_VALUE;
        Move bestMove = null;
        long startTime = System.currentTimeMillis();
        
        // 打乱顺序，增加随机性
        Collections.shuffle(possibleMoves);
        
        for (Move move : possibleMoves) {
            // 模拟落子
            grid[move.getRow()][move.getCol()] = stone;
            int score = minimax(grid, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, false, opponentStone);
            // 撤销落子
            grid[move.getRow()][move.getCol()] = Stone.EMPTY;
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        long endTime = System.currentTimeMillis();
        System.out.printf("AI思考时间: %.2f秒, 选择位置: [%d, %d], 评分为: %d\n", 
            (endTime - startTime) / 1000.0, bestMove.getRow(), bestMove.getCol(), bestScore);
        
        return bestMove;
    }
    
    private int minimax(Stone[][] grid, int depth, int alpha, int beta, boolean isMaximizing, Stone currentPlayer) {
        // 到达搜索深度或游戏结束
        if (depth == 0) {
            return evaluateBoard(grid, stone);
        }
        
        Stone opponentStone = (currentPlayer == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
        int bestScore = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        
        // 获取所有可能的落子位置
        List<Move> possibleMoves = new ArrayList<>();
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid.length; j++) {
                if (grid[i][j] == Stone.EMPTY) {
                    if (hasAdjacentStone(grid, i, j, 2)) {
                        possibleMoves.add(new Move(i, j));
                    }
                }
            }
        }
        
        // 如果没有可能的移动，返回当前评估值
        if (possibleMoves.isEmpty()) {
            return evaluateBoard(grid, stone);
        }
        
        // 按评分排序，提高剪枝效率
        possibleMoves.sort((a, b) -> {
            grid[a.getRow()][a.getCol()] = currentPlayer;
            int scoreA = evaluateBoard(grid, currentPlayer);
            grid[a.getRow()][a.getCol()] = Stone.EMPTY;
            
            grid[b.getRow()][b.getCol()] = currentPlayer;
            int scoreB = evaluateBoard(grid, currentPlayer);
            grid[b.getRow()][b.getCol()] = Stone.EMPTY;
            
            return scoreB - scoreA;
        });
        
        for (Move move : possibleMoves) {
            // 模拟落子
            grid[move.getRow()][move.getCol()] = currentPlayer;
            
            // 检查是否获胜
            if (checkWin(grid, move.getRow(), move.getCol(), currentPlayer)) {
                int score = isMaximizing ? 
                    (100000 - depth * 100) : 
                    (-100000 + depth * 100);
                grid[move.getRow()][move.getCol()] = Stone.EMPTY;
                return score;
            }
            
            // 递归搜索
            int score = minimax(grid, depth - 1, alpha, beta, !isMaximizing, opponentStone);
            
            // 撤销落子
            grid[move.getRow()][move.getCol()] = Stone.EMPTY;
            
            // 更新最佳分数
            if (isMaximizing) {
                bestScore = Math.max(bestScore, score);
                alpha = Math.max(alpha, score);
            } else {
                bestScore = Math.min(bestScore, score);
                beta = Math.min(beta, score);
            }
            
            // Alpha-Beta剪枝
            if (beta <= alpha) {
                break;
            }
        }
        
        return bestScore;
    }
    
    private int evaluateBoard(Stone[][] grid, Stone aiStone) {
        Stone opponentStone = (aiStone == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
        int score = 0;
        int size = grid.length;
        
        // 检查所有行
        for (int i = 0; i < size; i++) {
            score += evaluateLine(grid, i, 0, 0, 1, size, aiStone, opponentStone);
        }
        
        // 检查所有列
        for (int j = 0; j < size; j++) {
            score += evaluateLine(grid, 0, j, 1, 0, size, aiStone, opponentStone);
        }
        
        // 检查正对角线
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == 0 || j == 0) {
                    int length = Math.min(size - i, size - j);
                    score += evaluateLine(grid, i, j, 1, 1, length, aiStone, opponentStone);
                }
            }
        }
        
        // 检查反对角线
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == 0 || j == size - 1) {
                    int length = Math.min(size - i, j + 1);
                    score += evaluateLine(grid, i, j, 1, -1, length, aiStone, opponentStone);
                }
            }
        }
        
        return score;
    }
    
    private int evaluateLine(Stone[][] grid, int startRow, int startCol, int dr, int dc, int length, Stone aiStone, Stone opponentStone) {
        int aiCount = 0;
        int opponentCount = 0;
        int emptyCount = 0;
        
        for (int i = 0; i < length; i++) {
            int row = startRow + i * dr;
            int col = startCol + i * dc;
            
            if (grid[row][col] == aiStone) {
                aiCount++;
            } else if (grid[row][col] == opponentStone) {
                opponentCount++;
            } else {
                emptyCount++;
            }
        }
        
        // 不能同时有AI和对手的棋子
        if (aiCount > 0 && opponentCount > 0) {
            return 0;
        }
        
        // 计算棋型评分
        if (aiCount > 0) {
            return SCORE_TABLE[0][aiCount];
        } else if (opponentCount > 0) {
            return -SCORE_TABLE[1][opponentCount];
        } else {
            return 0;
        }
    }
    
    private boolean hasAdjacentStone(Stone[][] grid, int row, int col, int distance) {
        int size = grid.length;
        for (int i = Math.max(0, row - distance); i <= Math.min(size - 1, row + distance); i++) {
            for (int j = Math.max(0, col - distance); j <= Math.min(size - 1, col + distance); j++) {
                if (grid[i][j] != Stone.EMPTY) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean checkWin(Stone[][] grid, int row, int col, Stone stone) {
        int size = grid.length;
        
        // 方向数组：右，下，右下，右上
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        
        for (int[] dir : directions) {
            int count = 1;
            int dr = dir[0];
            int dc = dir[1];
            
            // 正方向
            for (int i = 1; i < 5; i++) {
                int r = row + i * dr;
                int c = col + i * dc;
                if (r < 0 || r >= size || c < 0 || c >= size || grid[r][c] != stone) {
                    break;
                }
                count++;
            }
            
            // 反方向
            for (int i = 1; i < 5; i++) {
                int r = row - i * dr;
                int c = col - i * dc;
                if (r < 0 || r >= size || c < 0 || c >= size || grid[r][c] != stone) {
                    break;
                }
                count++;
            }
            
            if (count >= 5) {
                return true;
            }
        }
        
        return false;
    }
}    
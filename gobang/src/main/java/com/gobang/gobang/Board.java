package com.gobang;

import java.util.ArrayList;
import java.util.List;

/**
 * 五子棋棋盘类
 */
public class Board {
    private Stone[][] grid;
    private int size;
    
    public Board() {
        this(15); // 默认15x15棋盘
    }
    
    public Board(int size) {
        this.size = size;
        grid = new Stone[size][size];
        clear();
    }
    
    public void clear() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = Stone.EMPTY;
            }
        }
    }
    
    public boolean placeStone(int row, int col, Stone stone) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            return false;
        }
        
        if (grid[row][col] != Stone.EMPTY) {
            return false;
        }
        
        grid[row][col] = stone;
        return true;
    }
    
    public boolean checkWin(int row, int col, Stone stone) {
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
    
    /**
     * 获取获胜的五个棋子位置
     */
    public List<int[]> getWinningLine(int row, int col, Stone stone) {
        // 方向数组：右，下，右下，右上
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        
        for (int[] dir : directions) {
            List<int[]> positions = new ArrayList<>();
            positions.add(new int[]{row, col});
            
            int dr = dir[0];
            int dc = dir[1];
            
            // 正方向
            for (int i = 1; i < 5; i++) {
                int r = row + i * dr;
                int c = col + i * dc;
                if (r < 0 || r >= size || c < 0 || c >= size || grid[r][c] != stone) {
                    break;
                }
                positions.add(new int[]{r, c});
            }
            
            // 反方向
            for (int i = 1; i < 5; i++) {
                int r = row - i * dr;
                int c = col - i * dc;
                if (r < 0 || r >= size || c < 0 || c >= size || grid[r][c] != stone) {
                    break;
                }
                positions.add(new int[]{r, c});
            }
            
            if (positions.size() >= 5) {
                return positions;
            }
        }
        
        return null;
    }
    
    public boolean isFull() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == Stone.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public Stone[][] getGrid() {
        return grid;
    }
    
    public int getSize() {
        return size;
    }
}    
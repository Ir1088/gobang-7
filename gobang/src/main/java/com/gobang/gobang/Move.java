package com.gobang.gobang;

/**
 * 移动类 - 表示棋盘上的一个位置
 */
public class Move {
    private int row;
    private int col;
    
    public Move(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
    
    @Override
    public String toString() {
        return "[" + row + ", " + col + "]";
    }
}    
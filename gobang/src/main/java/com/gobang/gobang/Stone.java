package com.gobang.gobang;

/**
 * 棋子枚举
 */
public enum Stone {
    EMPTY, BLACK, WHITE;
    
    @Override
    public String toString() {
        switch(this) {
            case BLACK: return "黑棋";
            case WHITE: return "白棋";
            default: return "空";
        }
    }
}    
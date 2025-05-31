package com.gobang.gobang;

/**
 * 人类玩家类
 */
public class HumanPlayer implements Player {
    private String name;
    private Stone stone;
    
    public HumanPlayer(String name, Stone stone) {
        this.name = name;
        this.stone = stone;
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
        // 人类玩家的移动由UI事件触发，这里不需要实现
        return null;
    }
}    
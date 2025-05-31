package com.gobang.gobang;

/**
 * 玩家接口
 */
public interface Player {
    String getName();
    Stone getStone();
    Move getMove(Board board);
}    
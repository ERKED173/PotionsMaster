package ru.erked.pcook.ai;

public class GameAction {

    private Gem gem;
    private Type type;
    private int hand_position;
    private int board_position_1;
    private int board_position_2;

    public GameAction(Type type, Gem gem, int board_position_1, int board_position_2, int hand_position) {
        this.type = type;
        this.gem = gem;
        this.hand_position = hand_position;
        this.board_position_1 = board_position_1;
        this.board_position_2 = board_position_2;
    }

    GameAction (GameAction another) {
        this(
                another.type,
                another.gem,
                another.board_position_1,
                another.board_position_2,
                another.hand_position
        );
    }

    public Gem getGem() { return gem; }
    public Type getType() { return type; }
    public int getBoardPosition1() { return board_position_1; }
    public int getBoardPosition2() { return board_position_2; }
    public int getHandPosition() { return hand_position; }

    public boolean equals (GameAction action) {
        return action != null &&
                type == action.type &&
                (gem == null ? action.gem == null : gem.equals(action.gem)) &&
                hand_position == action.hand_position &&
                board_position_1 == action.board_position_1 &&
                board_position_2 == action.board_position_2;
    }

    public enum Type {
        PLACE_AI,
        PLACE_PLAYER,
        PLACE_UP,
        PLACE_DOWN,
        PLACE_LEFT,
        PLACE_RIGHT,
        PLACE_RANDOM_PLAYER,
        PLACE_RANDOM_AI,
        ADD_TO_PLAYER_HAND,
        ADD_TO_AI_HAND,
        DISCARD,
        MOVE_UP,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE_RIGHT,
        ATTACK_UP,
        ATTACK_DOWN,
        ATTACK_LEFT,
        ATTACK_RIGHT
    }

}

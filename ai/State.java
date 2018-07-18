package ru.erked.pcook.ai;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

public class State {

    private int turn;
    private int board_size;

    private int score_ai;
    private int score_player;

    private int visits;
    private int availability;
    private int total_reward;

    private State parent;
    private GameAction applied_action;
    private ArrayList<State> children;

    private ArrayList<Gem> deck_ai;
    private ArrayList<Gem> deck_player;
    private ArrayList<Gem> ai_hand;
    private ArrayList<Gem> player_hand;
    private ArrayList<Gem> board_gems;

    private ArrayList<Gem> init_deck_ai;
    private ArrayList<Gem> init_deck_player;

    State (
            int board_size,
            ArrayList<Gem> ai_hand,
            ArrayList<Gem> board_gems,
            int turn,
            int score_ai,
            int score_player
    ) {
        this.turn = turn;
        this.board_size = board_size;
        this.score_ai = score_ai;
        this.score_player = score_player;

        applied_action = null;

        this.ai_hand = new ArrayList<>();
        for (Gem g : ai_hand)
            if (g != null)
                this.ai_hand.add(new Gem(g));
            else
                this.ai_hand.add(null);

        this.player_hand = new ArrayList<>();

        this.board_gems = new ArrayList<>();
        for (Gem g : board_gems)
            if (g != null)
                this.board_gems.add(new Gem(g));
            else
                this.board_gems.add(null);

        deck_ai = new ArrayList<>();
        deck_player = new ArrayList<>();

        parent = null;
        children = new ArrayList<>();

        visits = 0;
        availability = 1;
        total_reward = 0;

        init_deck_ai = new ArrayList<>();
        init_deck_player = new ArrayList<>();
        for (int i = 0; i < board_size; ++i) {
            for (int j = 0; j < board_size; ++j) {
                init_deck_ai.add(
                        new Gem(true, Gem.GemColor.values()[i], Gem.GemForm.values()[j]));
                init_deck_player.add(
                        new Gem(false, Gem.GemColor.values()[i], Gem.GemForm.values()[j]));
            }
        }
        for (int i = 0; i < init_deck_ai.size(); ++i) {
            for (Gem g : ai_hand) {
                if (g != null && g.equals(init_deck_ai.get(i))) {
                    init_deck_ai.set(i, null);
                    init_deck_player.set(i, null);
                }
            }
        }

        for (int i = 0; i < board_gems.size(); ++i) {
            for (Gem g : board_gems) {
                if (g != null &&
                        (g.equals(init_deck_ai.get(i)) || g.equals(init_deck_player.get(i)))) {
                    init_deck_ai.set(i, null);
                    init_deck_player.set(i, null);
                }
            }
        }
    }

    State (State another) {
        turn = another.turn;
        board_size = another.board_size;
        score_ai = another.score_ai;
        score_player = another.score_player;

        ai_hand = new ArrayList<>();
        for (Gem g : another.ai_hand)
            if (g != null)
                ai_hand.add(new Gem(g));
            else
                ai_hand.add(null);

        board_gems = new ArrayList<>();
        for (Gem g : another.board_gems)
            if (g != null)
                board_gems.add(new Gem(g));
            else
                board_gems.add(null);

        children = new ArrayList<>();

        visits = 0;
        availability = 1;
        total_reward = 0;

        deck_ai = new ArrayList<>();
        deck_player = new ArrayList<>();
        player_hand = new ArrayList<>();

        for (Gem g : another.player_hand)
            if (g != null)
                player_hand.add(new Gem(g));
            else
                player_hand.add(null);

        for (Gem g : another.deck_ai)
            if (g != null)
                deck_ai.add(new Gem(g));
            else
                deck_ai.add(null);
        for (Gem g : another.deck_player)
            if (g != null)
                deck_player.add(new Gem(g));
            else
                deck_player.add(null);

        setParent(another.getParent());
        if (another.getAppliedAction() != null)
            setAppliedAction(another.getAppliedAction());
    }

    public ArrayList<Gem> getAiHand() {
        return ai_hand;
    }
    public ArrayList<Gem> getPlayerHand() {
        return player_hand;
    }
    public ArrayList<Gem> getBoardGems() {
        return board_gems;
    }

    public int getVisits() {
        return visits;
    }
    public int getAvailability() {
        return availability;
    }
    public int getTotalReward() {
        return total_reward;
    }

    public State getParent() {
        return parent;
    }
    public void setParent(State parent) { this.parent = parent; }
    public ArrayList<State> getChildren() { return children; }

    public void setAppliedAction(GameAction applied_action) {
        this.applied_action = new GameAction(applied_action);
    }
    public GameAction getAppliedAction() {
        return applied_action;
    }

    public int getTurn () { return turn; }
    public int getBoardSize() {
        return board_size;
    }

    public void fillDeterminization (int hand_size) {
        ArrayList<Integer> deck_indices = new ArrayList<>();
        for (int i = 0; i < init_deck_player.size(); ++i) {
            if (init_deck_player.get(i) != null)
                deck_indices.add(i);
        }

        player_hand.clear();
        for (int i = 0; i < hand_size; ++i) {
            int random_index = MathUtils.random(deck_indices.size() - 1);
            player_hand.add(new Gem(init_deck_player.get(deck_indices.get(random_index))));
            deck_indices.remove(random_index);
        }

        this.deck_ai.clear();
        this.deck_player.clear();
        for (int i = 0; i < deck_indices.size(); ++i) {
            this.deck_ai.add(init_deck_ai.get(deck_indices.get(i)));
            this.deck_player.add(init_deck_player.get(deck_indices.get(i)));
        }
    }

    boolean isApplicable (GameAction gameAction) {
        switch (gameAction.getType()) {
            case DISCARD: {
                return gameAction.getHandPosition() != -1;
            }
            case ADD_TO_AI_HAND: {
                return getAIDeckSize() > 0 && getAIHandSize() < board_size;
            }
            case ADD_TO_PLAYER_HAND: {
                return getPlayerDeckSize() > 0 && getPlayerHandSize() < board_size;
            }
            case PLACE_RANDOM_AI: case PLACE_RANDOM_PLAYER: {
                return true;
            }
            case PLACE_UP: {
                if (turn % 2 == 0) {
                    return gameAction.getBoardPosition2() < board_size * board_size &&
                            board_gems.get(gameAction.getBoardPosition2()) == null &&
                            (board_gems.get(gameAction.getBoardPosition1()).
                                    getColor().equals(gameAction.getGem().getColor()) ||
                                    board_gems.get(gameAction.getBoardPosition1()).getForm().
                                            equals(gameAction.getGem().getForm()));
                } else {
                    return gameAction.getBoardPosition2() < board_size * board_size &&
                            board_gems.get(gameAction.getBoardPosition2()) == null &&
                            (board_gems.get(gameAction.getBoardPosition1()).
                                    getColor().equals(gameAction.getGem().getColor()) ||
                                    board_gems.get(gameAction.getBoardPosition1()).getForm().
                                            equals(gameAction.getGem().getForm()));
                }
            }
            case PLACE_DOWN: {
                if (turn % 2 == 0) {
                    return gameAction.getBoardPosition2() > -1 &&
                            board_gems.get(gameAction.getBoardPosition2()) == null &&
                            (board_gems.get(gameAction.getBoardPosition1()).
                                    getColor().equals(gameAction.getGem().getColor()) ||
                                    board_gems.get(gameAction.getBoardPosition1()).getForm().
                                            equals(gameAction.getGem().getForm()));
                } else {
                    return gameAction.getBoardPosition2() > -1 &&
                            board_gems.get(gameAction.getBoardPosition2()) == null &&
                            (board_gems.get(gameAction.getBoardPosition1()).
                                    getColor().equals(gameAction.getGem().getColor()) ||
                                    board_gems.get(gameAction.getBoardPosition1()).getForm().
                                            equals(gameAction.getGem().getForm()));
                }
            }
            case PLACE_LEFT: {
                if (turn % 2 == 0) {
                    return gameAction.getBoardPosition2() > -1 &&
                            gameAction.getBoardPosition2() / board_size == (gameAction.getBoardPosition2() + 1) / board_size &&
                            board_gems.get(gameAction.getBoardPosition2()) == null &&
                            (board_gems.get(gameAction.getBoardPosition1()).
                                    getColor().equals(gameAction.getGem().getColor()) ||
                                    board_gems.get(gameAction.getBoardPosition1()).getForm().
                                            equals(gameAction.getGem().getForm()));
                } else {
                    return gameAction.getBoardPosition2() > -1 &&
                            gameAction.getBoardPosition2() / board_size == (gameAction.getBoardPosition2() + 1) / board_size &&
                            board_gems.get(gameAction.getBoardPosition2()) == null &&
                            (board_gems.get(gameAction.getBoardPosition1()).
                                    getColor().equals(gameAction.getGem().getColor()) ||
                                    board_gems.get(gameAction.getBoardPosition1()).getForm().
                                            equals(gameAction.getGem().getForm()));
                }
            }
            case PLACE_RIGHT: {
                if (turn % 2 == 0) {
                    return gameAction.getBoardPosition2() < board_size * board_size &&
                            gameAction.getBoardPosition2() / board_size == (gameAction.getBoardPosition2() - 1) / board_size &&
                            board_gems.get(gameAction.getBoardPosition2()) == null &&
                            (board_gems.get(gameAction.getBoardPosition1()).
                                    getColor().equals(gameAction.getGem().getColor()) ||
                                    board_gems.get(gameAction.getBoardPosition1()).getForm().
                                            equals(gameAction.getGem().getForm()));
                } else {
                    return gameAction.getBoardPosition2() < board_size * board_size &&
                            gameAction.getBoardPosition2() / board_size == (gameAction.getBoardPosition2() - 1) / board_size &&
                            board_gems.get(gameAction.getBoardPosition2()) == null &&
                            (board_gems.get(gameAction.getBoardPosition1()).
                                    getColor().equals(gameAction.getGem().getColor()) ||
                                    board_gems.get(gameAction.getBoardPosition1()).getForm().
                                            equals(gameAction.getGem().getForm()));
                }
            }
            case MOVE_UP: {
                return gameAction.getBoardPosition1() < board_size * board_size &&
                        board_gems.get(gameAction.getBoardPosition1()) == null &&
                        board_gems.get(gameAction.getBoardPosition1() - board_size) != null &&
                        (board_gems.get(gameAction.getBoardPosition1() - board_size).
                                getColor().equals(gameAction.getGem().getColor()) ||
                                board_gems.get(gameAction.getBoardPosition1() - board_size).getForm().
                                        equals(gameAction.getGem().getForm()));
            }
            case MOVE_DOWN: {
                return gameAction.getBoardPosition1() > -1 &&
                        board_gems.get(gameAction.getBoardPosition1()) == null &&
                        board_gems.get(gameAction.getBoardPosition1() + board_size) != null &&
                        (board_gems.get(gameAction.getBoardPosition1() + board_size).
                                getColor().equals(gameAction.getGem().getColor()) ||
                                board_gems.get(gameAction.getBoardPosition1() + board_size).getForm().
                                        equals(gameAction.getGem().getForm()));
            }
            case MOVE_LEFT: {
                return gameAction.getBoardPosition1() > -1 &&
                        gameAction.getBoardPosition1() / board_size == (gameAction.getBoardPosition1() + 2) / board_size &&
                        board_gems.get(gameAction.getBoardPosition1()) == null &&
                        board_gems.get(gameAction.getBoardPosition1() + 1) != null &&
                        (board_gems.get(gameAction.getBoardPosition1() + 1).
                                getColor().equals(gameAction.getGem().getColor()) ||
                                board_gems.get(gameAction.getBoardPosition1() + 1).getForm().
                                        equals(gameAction.getGem().getForm()));
            }
            case MOVE_RIGHT: {
                return gameAction.getBoardPosition1() < board_size * board_size &&
                        gameAction.getBoardPosition1() / board_size == (gameAction.getBoardPosition1() - 2) / board_size &&
                        board_gems.get(gameAction.getBoardPosition1()) == null &&
                        board_gems.get(gameAction.getBoardPosition1() - 1) != null &&
                        (board_gems.get(gameAction.getBoardPosition1() - 1).
                                getColor().equals(gameAction.getGem().getColor()) ||
                                board_gems.get(gameAction.getBoardPosition1() - 1).getForm().
                                        equals(gameAction.getGem().getForm()));
            }
            case ATTACK_UP: {
                return gameAction.getBoardPosition1() < board_size * board_size &&
                        board_gems.get(gameAction.getBoardPosition1()) != null &&
                        board_gems.get(gameAction.getBoardPosition1() - board_size) != null &&
                        ((!board_gems.get(gameAction.getBoardPosition1()).isIsAI() && turn % 2 == 0) ||
                                (board_gems.get(gameAction.getBoardPosition1()).isIsAI() && turn % 2 != 0)) &&
                        (board_gems.get(gameAction.getBoardPosition1() - board_size).
                                getColor().equals(gameAction.getGem().getColor()) ||
                                board_gems.get(gameAction.getBoardPosition1() - board_size).getForm().
                                        equals(gameAction.getGem().getForm()));
            }
            case ATTACK_DOWN: {
                return gameAction.getBoardPosition1() > -1 &&
                        board_gems.get(gameAction.getBoardPosition1()) != null &&
                        board_gems.get(gameAction.getBoardPosition1() + board_size) != null &&
                        ((!board_gems.get(gameAction.getBoardPosition1()).isIsAI() && turn % 2 == 0) ||
                                (board_gems.get(gameAction.getBoardPosition1()).isIsAI() && turn % 2 != 0)) &&
                        (board_gems.get(gameAction.getBoardPosition1() + board_size).
                                getColor().equals(gameAction.getGem().getColor()) ||
                                board_gems.get(gameAction.getBoardPosition1() + board_size).getForm().
                                        equals(gameAction.getGem().getForm()));
            }
            case ATTACK_LEFT: {
                return gameAction.getBoardPosition1() > -1 &&
                        gameAction.getBoardPosition1() / board_size == (gameAction.getBoardPosition1() + 2) / board_size &&
                        board_gems.get(gameAction.getBoardPosition1()) != null &&
                        board_gems.get(gameAction.getBoardPosition1() + 1) != null &&
                        ((!board_gems.get(gameAction.getBoardPosition1()).isIsAI() && turn % 2 == 0) ||
                                (board_gems.get(gameAction.getBoardPosition1()).isIsAI() && turn % 2 != 0)) &&
                        (board_gems.get(gameAction.getBoardPosition1() + 1).
                                getColor().equals(gameAction.getGem().getColor()) ||
                                board_gems.get(gameAction.getBoardPosition1() + 1).getForm().
                                        equals(gameAction.getGem().getForm()));
            }
            case ATTACK_RIGHT: {
                return gameAction.getBoardPosition1() < board_size * board_size &&
                        gameAction.getBoardPosition1() / board_size == (gameAction.getBoardPosition1() - 2) / board_size &&
                        board_gems.get(gameAction.getBoardPosition1()) != null &&
                        board_gems.get(gameAction.getBoardPosition1() - 1) != null &&
                        ((!board_gems.get(gameAction.getBoardPosition1()).isIsAI() && turn % 2 == 0) ||
                                (board_gems.get(gameAction.getBoardPosition1()).isIsAI() && turn % 2 != 0)) &&
                        (board_gems.get(gameAction.getBoardPosition1() - 1).
                                getColor().equals(gameAction.getGem().getColor()) ||
                                board_gems.get(gameAction.getBoardPosition1() - 1).getForm().
                                        equals(gameAction.getGem().getForm()));
            }
        }
        return false;
    }

    void applyAction (GameAction gameAction) {
        switch (gameAction.getType()) {
            case DISCARD: {

                if (turn % 2 == 0) {
                    ai_hand.set(gameAction.getHandPosition(), null);

                    GameAction action = new GameAction(GameAction.Type.ADD_TO_AI_HAND, null, -1,-1, -1);
                    if (isApplicable(action))
                        applyAction(action);

                    score_ai -= 10;
                } else {
                    player_hand.set(gameAction.getHandPosition(), null);

                    GameAction action = new GameAction(GameAction.Type.ADD_TO_PLAYER_HAND, null, -1,-1, -1);
                    if (isApplicable(action))
                        applyAction(action);

                    score_player -= 10;
                }

                turn++;
                break;
            }
            case ADD_TO_AI_HAND: {

                int empty_index = 0;
                for (int i = 0; i < board_size; ++i) {
                    if (ai_hand.get(i) == null) {
                        empty_index = i;
                        break;
                    }
                }

                ArrayList<Integer> deck_indexes = new ArrayList<>();
                for (int i = 0; i < deck_ai.size(); ++i)
                    if (deck_ai.get(i) != null)
                        deck_indexes.add(i);

                int random_index = deck_indexes.get(MathUtils.random(deck_indexes.size() - 1));
                ai_hand.set(empty_index, deck_ai.get(random_index));

                deck_ai.set(random_index, null);
                deck_player.set(random_index, null);

                break;
            }
            case ADD_TO_PLAYER_HAND: {

                int empty_index = 0;
                for (int i = 0; i < board_size; ++i) {
                    if (player_hand.get(i) == null) {
                        empty_index = i;
                        break;
                    }
                }

                ArrayList<Integer> deck_indexes = new ArrayList<>();
                for (int i = 0; i < deck_player.size(); ++i)
                    if (deck_player.get(i) != null)
                        deck_indexes.add(i);

                int random_index = deck_indexes.get(MathUtils.random(deck_indexes.size() - 1));
                player_hand.set(empty_index, deck_player.get(random_index));

                deck_ai.set(random_index, null);
                deck_player.set(random_index, null);

                break;
            }
            case PLACE_RANDOM_PLAYER: {

                ArrayList<Integer> deck_indexes = new ArrayList<>();
                for (int i = 0; i < deck_player.size(); ++i)
                    if (deck_player.get(i) != null)
                        deck_indexes.add(i);

                int random_deck_index = deck_indexes.get(MathUtils.random(deck_indexes.size() - 1));

                boolean was_not_placed = true;
                int random_board_index = 0;
                while (was_not_placed) {
                    int random = MathUtils.random(3);
                    switch (random) {
                        case 0:
                            random_board_index = board_size + 1;
                            break;
                        case 1:
                            random_board_index = board_size + (board_size - 2);
                            break;
                        case 2:
                            random_board_index = (board_size * board_size - 1) - board_size - (board_size - 2);
                            break;
                        case 3:
                            random_board_index = (board_size * board_size - 1) - (board_size + 1);
                            break;
                    }
                    if (board_gems.get(random_board_index) == null)
                        was_not_placed = false;
                }

                board_gems.set(random_board_index, deck_player.get(random_deck_index));

                deck_ai.set(random_deck_index, null);
                deck_player.set(random_deck_index, null);

                break;
            }
            case PLACE_RANDOM_AI: {

                ArrayList<Integer> deck_indexes = new ArrayList<>();
                for (int i = 0; i < deck_ai.size(); ++i)
                    if (deck_ai.get(i) != null)
                        deck_indexes.add(i);

                int random_deck_index = deck_indexes.get(MathUtils.random(deck_indexes.size() - 1));

                boolean was_not_placed = true;
                int random_board_index = 0;
                while (was_not_placed) {
                    int random = MathUtils.random(3);
                    switch (random) {
                        case 0:
                            random_board_index = board_size + 1;
                            break;
                        case 1:
                            random_board_index = board_size + (board_size - 2);
                            break;
                        case 2:
                            random_board_index = (board_size * board_size - 1) - board_size - (board_size - 2);
                            break;
                        case 3:
                            random_board_index = (board_size * board_size - 1) - (board_size + 1);
                            break;
                    }
                    if (board_gems.get(random_board_index) == null)
                        was_not_placed = false;
                }

                board_gems.set(random_board_index, deck_ai.get(random_deck_index));

                deck_ai.set(random_deck_index, null);
                deck_player.set(random_deck_index, null);

                break;
            }
            case PLACE_UP: case PLACE_DOWN: case PLACE_LEFT: case PLACE_RIGHT: {

                if (turn % 2 == 0) {
                    board_gems.set(gameAction.getBoardPosition2(), ai_hand.get(gameAction.getHandPosition()));
                    ai_hand.set(gameAction.getHandPosition(), null);

                    GameAction add_new = new GameAction(GameAction.Type.ADD_TO_AI_HAND, null, -1, -1, -1);
                    if (isApplicable(add_new))
                        applyAction(add_new);

                    turn++;
                    score_ai += 10;

                } else {
                    board_gems.set(gameAction.getBoardPosition2(), player_hand.get(gameAction.getHandPosition()));
                    player_hand.set(gameAction.getHandPosition(), null);

                    GameAction add_new = new GameAction(GameAction.Type.ADD_TO_PLAYER_HAND, null, -1, -1, -1);
                    if (isApplicable(add_new))
                        applyAction(add_new);

                    turn++;
                    score_player += 10;
                }
                break;
            }
            case MOVE_UP: {

                board_gems.set(
                        gameAction.getBoardPosition1(),
                        board_gems.get(gameAction.getBoardPosition2()));

                board_gems.set(gameAction.getBoardPosition2(), null);

                turn++;

                break;
            }
            case MOVE_DOWN: {

                board_gems.set(
                        gameAction.getBoardPosition1(),
                        board_gems.get(gameAction.getBoardPosition2()));

                board_gems.set(gameAction.getBoardPosition2(), null);

                turn++;

                break;
            }
            case MOVE_LEFT: {

                board_gems.set(
                        gameAction.getBoardPosition1(),
                        board_gems.get(gameAction.getBoardPosition2()));

                board_gems.set(gameAction.getBoardPosition2(), null);

                turn++;

                break;
            }
            case MOVE_RIGHT: {

                board_gems.set(
                        gameAction.getBoardPosition1(),
                        board_gems.get(gameAction.getBoardPosition2()));

                board_gems.set(gameAction.getBoardPosition2(), null);

                turn++;

                break;
            }
            case ATTACK_UP: {

                board_gems.set(
                        gameAction.getBoardPosition1(),
                        board_gems.get(gameAction.getBoardPosition2()));

                board_gems.set(gameAction.getBoardPosition2(), null);

                if (turn % 2 == 0) {
                    score_ai += 20;
                } else {
                    score_player += 20;
                }

                turn++;

                break;
            }
            case ATTACK_DOWN: {

                board_gems.set(
                        gameAction.getBoardPosition1(),
                        board_gems.get(gameAction.getBoardPosition2()));

                board_gems.set(gameAction.getBoardPosition2(), null);

                if (turn % 2 == 0) {
                    score_ai += 20;
                } else {
                    score_player += 20;
                }

                turn++;

                break;
            }
            case ATTACK_LEFT: {

                board_gems.set(
                        gameAction.getBoardPosition1(),
                        board_gems.get(gameAction.getBoardPosition2()));

                board_gems.set(gameAction.getBoardPosition2(), null);

                if (turn % 2 == 0) {
                    score_ai += 20;
                } else {
                    score_player += 20;
                }

                turn++;

                break;
            }
            case ATTACK_RIGHT: {

                board_gems.set(
                        gameAction.getBoardPosition1(),
                        board_gems.get(gameAction.getBoardPosition2()));

                board_gems.set(gameAction.getBoardPosition2(), null);

                if (turn % 2 == 0) {
                    score_ai += 20;
                } else {
                    score_player += 20;
                }

                turn++;

                break;
            }
        }
    }

    boolean isNotTerminal () {
        boolean has_ai = false;
        boolean has_player = false;
        for (Gem g : player_hand) {
            if (g != null) {
                has_player = true;
                break;
            }
        }
        for (Gem g : ai_hand) {
            if (g != null) {
                has_ai = true;
                break;
            }
        }
        if (!(has_ai && has_player)) return false;

        has_ai = false;
        has_player = false;
        for (Gem g : board_gems) {
            if (g != null && g.isIsAI()) {
                has_ai = true;
                break;
            }
        }
        for (Gem g : board_gems) {
            if (g != null && !g.isIsAI()) {
                has_player = true;
                break;
            }
        }
        if (!(has_ai && has_player)) return false;

        if (turn % 2 == 0) {
            for (int i = 0; i < board_gems.size(); ++i) {
                if (board_gems.get(i) != null && board_gems.get(i).isIsAI()) {
                    for (int j = 0; j < ai_hand.size(); ++j) {
                        if (ai_hand.get(j) != null) {
                            GameAction place_up = new GameAction(
                                    GameAction.Type.PLACE_UP,
                                    ai_hand.get(j),
                                    i,
                                    i + board_size,
                                    j
                            );
                            GameAction place_down = new GameAction(
                                    GameAction.Type.PLACE_DOWN,
                                    ai_hand.get(j),
                                    i,
                                    i - board_size,
                                    j
                            );
                            GameAction place_left = new GameAction(
                                    GameAction.Type.PLACE_LEFT,
                                    ai_hand.get(j),
                                    i,
                                    i - 1,
                                    j
                            );
                            GameAction place_right = new GameAction(
                                    GameAction.Type.PLACE_RIGHT,
                                    ai_hand.get(j),
                                    i,
                                    i + 1,
                                    j
                            );
                            if (isApplicable(place_up)) {
                                return true;
                            }
                            if (isApplicable(place_down)) {
                                return true;
                            }
                            if (isApplicable(place_left)) {
                                return true;
                            }
                            if (isApplicable(place_right)) {
                                return true;
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < board_gems.size(); ++i) {
                if (board_gems.get(i) != null && board_gems.get(i).isIsAI()) {
                    if (isApplicable(new GameAction(
                            GameAction.Type.MOVE_UP,
                            board_gems.get(i),
                            i + 2*board_size,
                            -1,
                            -1)) ||
                            isApplicable(new GameAction(
                                    GameAction.Type.ATTACK_UP,
                                    board_gems.get(i),
                                    i + 2*board_size,
                                    -1,
                                    -1))) {
                        return true;
                    }
                    if (isApplicable(new GameAction(
                            GameAction.Type.MOVE_DOWN,
                            board_gems.get(i),
                            i - 2*board_size,
                            -1,
                            -1)) ||
                            isApplicable(new GameAction(
                                    GameAction.Type.ATTACK_DOWN,
                                    board_gems.get(i),
                                    i - 2*board_size,
                                    -1,
                                    -1))) {
                        return true;
                    }
                    if (isApplicable(new GameAction(
                            GameAction.Type.MOVE_LEFT,
                            board_gems.get(i),
                            i - 2,
                            -1,
                            -1)) ||
                            isApplicable(new GameAction(
                                    GameAction.Type.ATTACK_LEFT,
                                    board_gems.get(i),
                                    i - 2,
                                    -1,
                                    -1))) {
                        return true;
                    }
                    if (isApplicable(new GameAction(
                            GameAction.Type.MOVE_RIGHT,
                            board_gems.get(i),
                            i + 2,
                            -1,
                            -1)) ||
                            isApplicable(new GameAction(
                                    GameAction.Type.ATTACK_RIGHT,
                                    board_gems.get(i),
                                    i + 2,
                                    -1,
                                    -1))) {
                        return true;
                    }
                }
            }
            return getAIDeckSize() != 0;
        } else {
            for (int i = 0; i < board_gems.size(); ++i) {
                if (board_gems.get(i) != null && !board_gems.get(i).isIsAI()) {
                    for (int j = 0; j < player_hand.size(); ++j) {
                        if (player_hand.get(j) != null) {
                            GameAction place_up = new GameAction(
                                    GameAction.Type.PLACE_UP,
                                    player_hand.get(j),
                                    i,
                                    i + board_size,
                                    j
                            );
                            GameAction place_down = new GameAction(
                                    GameAction.Type.PLACE_DOWN,
                                    player_hand.get(j),
                                    i,
                                    i - board_size,
                                    j
                            );
                            GameAction place_left = new GameAction(
                                    GameAction.Type.PLACE_LEFT,
                                    player_hand.get(j),
                                    i,
                                    i - 1,
                                    j
                            );
                            GameAction place_right = new GameAction(
                                    GameAction.Type.PLACE_RIGHT,
                                    player_hand.get(j),
                                    i,
                                    i + 1,
                                    j
                            );
                            if (isApplicable(place_up)) {
                                return true;
                            }
                            if (isApplicable(place_down)) {
                                return true;
                            }
                            if (isApplicable(place_left)) {
                                return true;
                            }
                            if (isApplicable(place_right)) {
                                return true;
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < board_gems.size(); ++i) {
                if (board_gems.get(i) != null && !board_gems.get(i).isIsAI()) {
                    if (isApplicable(new GameAction(
                            GameAction.Type.MOVE_UP,
                            board_gems.get(i),
                            i + 2*board_size,
                            -1,
                            -1)) ||
                            isApplicable(new GameAction(
                                    GameAction.Type.ATTACK_UP,
                                    board_gems.get(i),
                                    i + 2*board_size,
                                    -1,
                                    -1))) {
                        return true;
                    }
                    if (isApplicable(new GameAction(
                            GameAction.Type.MOVE_DOWN,
                            board_gems.get(i),
                            i - 2*board_size,
                            -1,
                            -1)) ||
                            isApplicable(new GameAction(
                                    GameAction.Type.ATTACK_DOWN,
                                    board_gems.get(i),
                                    i - 2*board_size,
                                    -1,
                                    -1))) {
                        return true;
                    }
                    if (isApplicable(new GameAction(
                            GameAction.Type.MOVE_LEFT,
                            board_gems.get(i),
                            i - 2,
                            -1,
                            -1)) ||
                            isApplicable(new GameAction(
                                    GameAction.Type.ATTACK_LEFT,
                                    board_gems.get(i),
                                    i - 2,
                                    -1,
                                    -1))) {
                        return true;
                    }
                    if (isApplicable(new GameAction(
                            GameAction.Type.MOVE_RIGHT,
                            board_gems.get(i),
                            i + 2,
                            -1,
                            -1)) ||
                            isApplicable(new GameAction(
                                    GameAction.Type.ATTACK_RIGHT,
                                    board_gems.get(i),
                                    i + 2,
                                    -1,
                                    -1))) {
                        return true;
                    }
                }
            }
            return getPlayerDeckSize() != 0;
        }
    }

    void increaseVisits () { visits++; }
    void increaseAvailability () { availability++; }
    void addTotalReward (int reward) { total_reward += reward; }

    boolean isVictory () {
        return score_ai > score_player;
    }

    private int getAIHandSize () {
        int size = 0;
        for (Gem g : ai_hand) if (g != null) size++;
        return size;
    }
    private int getPlayerHandSize () {
        int size = 0;
        for (Gem g : player_hand) if (g != null) size++;
        return size;
    }
    private int getAIDeckSize () {
        int size = 0;
        for (Gem g : deck_ai) if (g != null) size++;
        return size;
    }
    private int getPlayerDeckSize () {
        int size = 0;
        for (Gem g : deck_player) if (g != null) size++;
        return size;
    }

}

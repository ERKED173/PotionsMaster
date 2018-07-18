package ru.erked.pcook.ai;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

import ru.erked.pcook.screens.FightAI;

public class ISMCTS {

    public ISMCTS () {}

    public GameAction compute (
            int board_size,
            ArrayList<Gem> ai_hand,
            ArrayList<Gem> board_gems,
            int turn,
            int score_ai,
            int score_player,
            int hand_size,
            float time
    ) {
        // Initial state
        State s = new State(board_size, ai_hand, board_gems, turn, score_ai, score_player);

        long start = System.currentTimeMillis();

        float elapsed = (System.currentTimeMillis() - start) / (1000f + time);

        int iterations = 0;

        while (elapsed < time) {

            iterations++;

            FightAI.percents = elapsed / time;

            s.fillDeterminization(hand_size);

            s = selection(s);

            ArrayList<GameAction> u_children = u(s);
            if (u_children.size() != 0)  {
                s = expansion(s, u_children);
            }

            int reward = simulation(s);

            s = backpropagation(s, reward);

            elapsed = (System.currentTimeMillis() - start) / (1000f + time);
        }

        System.out.println(iterations + " iterations");

        int max_visits = Integer.MIN_VALUE;
        int index = 0;
        for (int i = 0; i < s.getChildren().size(); ++i) {
            if (s.getChildren().get(i).getVisits() > max_visits) {
                max_visits = s.getChildren().get(i).getVisits();
                index = i;
            }
        }

        return s.getChildren().get(index).getAppliedAction();

    }

    private State selection (State current) {
        while (u(current).size() == 0 && current.isNotTerminal()) {
            double bestValue = Double.MIN_VALUE;
            int index = 0;
            for (int i = 0; i < current.getChildren().size(); ++i) {
                double value = ucb1(current.getChildren().get(i));
                if (bestValue < value) {
                    bestValue = value;
                    index = i;
                }
            }
            current = current.getChildren().get(index);
        }
        return current;
    }

    private static double ucb1 (State state) {
        double epsilon = 1e-6;
        return state.getVisits() == 0 ? Double.MAX_VALUE :
                (state.getTotalReward() / (state.getVisits() + epsilon)) +
                        (Math.sqrt( (Math.log(state.getAvailability())) / (state.getVisits() + epsilon) ));
    }

    private State expansion (State selected, ArrayList<GameAction> u_children) {
        GameAction action = u_children.get(MathUtils.random(u_children.size() - 1));
        State new_state = new State(selected);
        new_state.applyAction(action);
        new_state.setParent(selected);
        new_state.setAppliedAction(action);
        selected.getChildren().add(new_state);
        return new_state;
    }
    private int simulation (State selected) {
        State copy = new State(selected);
        while (true) {
            if (!copy.isNotTerminal()) break;

            ArrayList<GameAction> children = new ArrayList<>();
            int random_action = MathUtils.random(5);
            switch (random_action) {
                case 0: {
                    children.addAll(checkDiscard(selected));
                    if (children.size() == 0) {
                        children.addAll(checkPlace(selected));
                        if (children.size() == 0) {
                            children.addAll(checkMoveAttack(selected));
                        }
                    }
                    break;
                }
                case 1: {
                    children.addAll(checkDiscard(selected));
                    if (children.size() == 0) {
                        children.addAll(checkMoveAttack(selected));
                        if (children.size() == 0) {
                            children.addAll(checkPlace(selected));
                        }
                    }
                    break;
                }
                case 2: {
                    children.addAll(checkMoveAttack(selected));
                    if (children.size() == 0) {
                        children.addAll(checkPlace(selected));
                        if (children.size() == 0) {
                            children.addAll(checkDiscard(selected));
                        }
                    }
                    break;
                }
                case 3: {
                    children.addAll(checkMoveAttack(selected));
                    if (children.size() == 0) {
                        children.addAll(checkDiscard(selected));
                        if (children.size() == 0) {
                            children.addAll(checkPlace(selected));
                        }
                    }
                    break;
                }
                case 4: {
                    children.addAll(checkPlace(selected));
                    if (children.size() == 0) {
                        children.addAll(checkDiscard(selected));
                        if (children.size() == 0) {
                            children.addAll(checkMoveAttack(selected));
                        }
                    }
                    break;
                }
                case 5: {
                    children.addAll(checkPlace(selected));
                    if (children.size() == 0) {
                        children.addAll(checkMoveAttack(selected));
                        if (children.size() == 0) {
                            children.addAll(checkDiscard(selected));
                        }
                    }
                    break;
                }
            }

            if (children.size() == 0) {
                return copy.isVictory() ? 1 : 0;
            } else {
                GameAction chosen = children.get(MathUtils.random(children.size() - 1));
                copy.applyAction(chosen);
            }

        }
        return copy.isVictory() ? 1 : 0;
    }
    private State backpropagation (State selected, int reward) {
        while (selected.getParent() != null) {
            selected.increaseVisits();

            for (State s : selected.getParent().getChildren()) {
                // TODO: normal availability
                s.increaseAvailability();
            }

            selected.addTotalReward(reward);
            selected = selected.getParent();
        }
        return selected;
    }

    private ArrayList<GameAction> u (State state) {
        ArrayList<GameAction> list = new ArrayList<>();

        ArrayList<GameAction> c_actions = c(state);

        for (GameAction c_act : c_actions) {
            boolean contains = false;
            for (State child : state.getChildren()) {
                if (c_act.equals(child.getAppliedAction())) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                list.add(c_act);
            }
        }

        return list;
    }

    private ArrayList<GameAction> c (State state) {
        ArrayList<GameAction> list = new ArrayList<>();

        list.addAll(checkDiscard(state));
        list.addAll(checkPlace(state));
        list.addAll(checkMoveAttack(state));

        return list;
    }

    private ArrayList<GameAction> checkDiscard (State state) {
        ArrayList<GameAction> list = new ArrayList<>();

        if (state.getTurn() % 2 == 0) {
            for (int i = 0; i < state.getAiHand().size(); ++i) {
                if (state.getAiHand().get(i) != null) {
                    GameAction action = new GameAction(
                            GameAction.Type.DISCARD,
                            null,
                            -1,
                            -1,
                            i
                    );
                    if (state.isApplicable(action)) {
                        list.add(action);
                    }
                }
            }
        } else {
            for (int i = 0; i < state.getPlayerHand().size(); ++i) {
                if (state.getPlayerHand().get(i) != null) {
                    GameAction action = new GameAction(
                            GameAction.Type.DISCARD,
                            null,
                            -1,
                            -1,
                            i
                    );
                    if (state.isApplicable(action)) {
                        list.add(action);
                    }
                }
            }
        }

        return list;
    }
    private ArrayList<GameAction> checkPlace (State state) {
        ArrayList<GameAction> list = new ArrayList<>();

        if (state.getTurn() % 2 == 0) {
            for (int i = 0; i < state.getBoardGems().size(); ++i) {
                if (state.getBoardGems().get(i) != null && state.getBoardGems().get(i).isIsAI()) {
                    for (int j = 0; j < state.getAiHand().size(); ++j) {
                        if (state.getAiHand().get(j) != null) {

                            GameAction place_up = new GameAction(
                                    GameAction.Type.PLACE_UP,
                                    state.getAiHand().get(j),
                                    i,
                                    i + state.getBoardSize(),
                                    j
                            );
                            GameAction place_down = new GameAction(
                                    GameAction.Type.PLACE_DOWN,
                                    state.getAiHand().get(j),
                                    i,
                                    i - state.getBoardSize(),
                                    j
                            );
                            GameAction place_left = new GameAction(
                                    GameAction.Type.PLACE_LEFT,
                                    state.getAiHand().get(j),
                                    i,
                                    i - 1,
                                    j
                            );
                            GameAction place_right = new GameAction(
                                    GameAction.Type.PLACE_RIGHT,
                                    state.getAiHand().get(j),
                                    i,
                                    i + 1,
                                    j
                            );

                            if (state.isApplicable(place_up)) {
                                list.add(place_up);
                            }
                            if (state.isApplicable(place_down)) {
                                list.add(place_down);
                            }
                            if (state.isApplicable(place_left)) {
                                list.add(place_left);
                            }
                            if (state.isApplicable(place_right)) {
                                list.add(place_right);
                            }
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < state.getBoardGems().size(); ++i) {
                if (state.getBoardGems().get(i) != null && !state.getBoardGems().get(i).isIsAI()) {
                    for (int j = 0; j < state.getPlayerHand().size(); ++j) {
                        if (state.getPlayerHand().get(j) != null) {
                            GameAction place_up = new GameAction(
                                    GameAction.Type.PLACE_UP,
                                    state.getPlayerHand().get(j),
                                    i,
                                    i + state.getBoardSize(),
                                    j
                            );
                            GameAction place_down = new GameAction(
                                    GameAction.Type.PLACE_DOWN,
                                    state.getPlayerHand().get(j),
                                    i,
                                    i - state.getBoardSize(),
                                    j
                            );
                            GameAction place_left = new GameAction(
                                    GameAction.Type.PLACE_LEFT,
                                    state.getPlayerHand().get(j),
                                    i,
                                    i - 1,
                                    j
                            );
                            GameAction place_right = new GameAction(
                                    GameAction.Type.PLACE_RIGHT,
                                    state.getPlayerHand().get(j),
                                    i,
                                    i + 1,
                                    j
                            );

                            if (state.isApplicable(place_up)) {
                                list.add(place_up);
                            }
                            if (state.isApplicable(place_down)) {
                                list.add(place_down);
                            }
                            if (state.isApplicable(place_left)) {
                                list.add(place_left);
                            }
                            if (state.isApplicable(place_right)) {
                                list.add(place_right);
                            }
                        }
                    }
                }
            }
        }

        return list;
    }
    private ArrayList<GameAction> checkMoveAttack (State state) {
        ArrayList<GameAction> list = new ArrayList<>();

        if (state.getTurn() % 2 == 0) {
            for (int i = 0; i < state.getBoardGems().size(); ++i) {
                if (state.getBoardGems().get(i) != null &&
                        state.getBoardGems().get(i).isIsAI()) {

                    GameAction move_up = new GameAction(
                            GameAction.Type.MOVE_UP,
                            state.getBoardGems().get(i),
                            i + 2 * state.getBoardSize(),
                            i,
                            -1
                    );
                    GameAction attack_up = new GameAction(
                            GameAction.Type.ATTACK_UP,
                            state.getBoardGems().get(i),
                            i + 2 * state.getBoardSize(),
                            i,
                            -1
                    );
                    GameAction move_down = new GameAction(
                            GameAction.Type.MOVE_DOWN,
                            state.getBoardGems().get(i),
                            i - 2 * state.getBoardSize(),
                            i,
                            -1
                    );
                    GameAction attack_down = new GameAction(
                            GameAction.Type.ATTACK_DOWN,
                            state.getBoardGems().get(i),
                            i - 2 * state.getBoardSize(),
                            i,
                            -1
                    );
                    GameAction move_left = new GameAction(
                            GameAction.Type.MOVE_LEFT,
                            state.getBoardGems().get(i),
                            i - 2,
                            i,
                            -1
                    );
                    GameAction attack_left = new GameAction(
                            GameAction.Type.ATTACK_LEFT,
                            state.getBoardGems().get(i),
                            i - 2,
                            i,
                            -1
                    );
                    GameAction move_right = new GameAction(
                            GameAction.Type.MOVE_RIGHT,
                            state.getBoardGems().get(i),
                            i + 2,
                            i,
                            -1
                    );
                    GameAction attack_right = new GameAction(
                            GameAction.Type.ATTACK_RIGHT,
                            state.getBoardGems().get(i),
                            i + 2,
                            i,
                            -1
                    );

                    if (state.isApplicable(move_up)) {
                        list.add(move_up);
                    }
                    if (state.isApplicable(attack_up)) {
                        list.add(attack_up);
                    }
                    if (state.isApplicable(move_down)) {
                        list.add(move_down);
                    }
                    if (state.isApplicable(attack_down)) {
                        list.add(attack_down);
                    }
                    if (state.isApplicable(move_left)) {
                        list.add(move_left);
                    }
                    if (state.isApplicable(attack_left)) {
                        list.add(attack_left);
                    }
                    if (state.isApplicable(move_right)) {
                        list.add(move_right);
                    }
                    if (state.isApplicable(attack_right)) {
                        list.add(attack_right);
                    }
                }
            }
        } else {
            for (int i = 0; i < state.getBoardGems().size(); ++i) {
                if (state.getBoardGems().get(i) != null &&
                        !state.getBoardGems().get(i).isIsAI()) {
                    GameAction move_up = new GameAction(
                            GameAction.Type.MOVE_UP,
                            state.getBoardGems().get(i),
                            i + 2 * state.getBoardSize(),
                            i,
                            -1
                    );
                    GameAction attack_up = new GameAction(
                            GameAction.Type.ATTACK_UP,
                            state.getBoardGems().get(i),
                            i + 2 * state.getBoardSize(),
                            i,
                            -1
                    );
                    GameAction move_down = new GameAction(
                            GameAction.Type.MOVE_DOWN,
                            state.getBoardGems().get(i),
                            i - 2 * state.getBoardSize(),
                            i,
                            -1
                    );
                    GameAction attack_down = new GameAction(
                            GameAction.Type.ATTACK_DOWN,
                            state.getBoardGems().get(i),
                            i - 2 * state.getBoardSize(),
                            i,
                            -1
                    );
                    GameAction move_left = new GameAction(
                            GameAction.Type.MOVE_LEFT,
                            state.getBoardGems().get(i),
                            i - 2,
                            i,
                            -1
                    );
                    GameAction attack_left = new GameAction(
                            GameAction.Type.ATTACK_LEFT,
                            state.getBoardGems().get(i),
                            i - 2,
                            i,
                            -1
                    );
                    GameAction move_right = new GameAction(
                            GameAction.Type.MOVE_RIGHT,
                            state.getBoardGems().get(i),
                            i + 2,
                            i,
                            -1
                    );
                    GameAction attack_right = new GameAction(
                            GameAction.Type.ATTACK_RIGHT,
                            state.getBoardGems().get(i),
                            i + 2,
                            i,
                            -1
                    );

                    if (state.isApplicable(move_up)) {
                        list.add(move_up);
                    }
                    if (state.isApplicable(attack_up)) {
                        list.add(attack_up);
                    }
                    if (state.isApplicable(move_down)) {
                        list.add(move_down);
                    }
                    if (state.isApplicable(attack_down)) {
                        list.add(attack_down);
                    }
                    if (state.isApplicable(move_left)) {
                        list.add(move_left);
                    }
                    if (state.isApplicable(attack_left)) {
                        list.add(attack_left);
                    }
                    if (state.isApplicable(move_right)) {
                        list.add(move_right);
                    }
                    if (state.isApplicable(attack_right)) {
                        list.add(attack_right);
                    }
                }
            }
        }

        return list;
    }
}

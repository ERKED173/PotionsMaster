package ru.erked.pcook.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.ArrayList;

import ru.erked.pcook.GameStarter;
import ru.erked.pcook.ai.GameAction;
import ru.erked.pcook.ai.Gem;
import ru.erked.pcook.systems.AdvParticle;
import ru.erked.pcook.systems.AdvScreen;
import ru.erked.pcook.systems.AdvSprite;
import ru.erked.pcook.systems.Button;
import ru.erked.pcook.systems.Font;
import ru.erked.pcook.systems.TextLine;
import ru.erked.pcook.systems.TextNotification;

public class FightHuman extends AdvScreen {

    /* AI + Interface */
    private int turn;
    private ArrayList<Gem> deck_ai;
    private ArrayList<Gem> deck_player;
    private ArrayList<Gem> hand_ai;
    private ArrayList<Gem> hand_player;
    private ArrayList<Gem> board_gems;
    private ArrayList<AdvSprite> s_deck_ai;
    private ArrayList<AdvSprite> s_deck_player;
    private ArrayList<AdvSprite> s_hand_ai;
    private ArrayList<AdvSprite> s_hand_player;
    private ArrayList<AdvSprite> s_board_gems;
    private float gem_size;
    private int board_size;
    private float board_tile;
    private int selected_hand_ai = -1;
    private int selected_hand_player = -1;
    private int selected_board_ai = -1;
    private int selected_board_player = -1;
    private ArrayList<AdvSprite> place_regions;
    private ArrayList<AdvSprite> move_regions;
    private ArrayList<AdvSprite> attack_regions;
    private ArrayList<AdvSprite> black_gems_ai;
    private ArrayList<AdvSprite> black_gems_player;

    /* Buttons */
    private ArrayList<Button> buttons;

    /* Click listeners */
    private ArrayList<ClickListener> hand_listeners_ai;
    private ArrayList<ClickListener> hand_listeners_player;
    private ArrayList<ClickListener> board_listeners_ai;
    private ArrayList<ClickListener> board_listeners_player;

    /* Particles */
    private AdvParticle explosion;

    /* Random */
    private float state_time = 0f; // For all animations
    private final int RHOMBUSES = 10; // Background
    private boolean handsFilled = false;
    private int score_ai = 0;
    private int score_player = 0;
    private boolean was_touched = false;

    /* Sprites */
    private ArrayList<AdvSprite> background;
    private Animation<TextureRegion> background_anim;
    private ArrayList<AdvSprite> cont_player;
    private ArrayList<AdvSprite> cont_ai;
    private AdvSprite board;

    /* Text Lines */
    private ArrayList<TextLine> v_lines;
    private ArrayList<TextLine> h_lines;
    private ArrayList<TextNotification> notifs;

    FightHuman(GameStarter game) {
        //
        super(game);
        //
    }

    @Override
    public void show () {
        //
        super.show();

        board_size = (g.fight_board_size + 1) / 2 + 3;
        board_tile = .85f*g.w / (float)(board_size);
        gem_size = (16f / 17f)*(board_tile);

        if (g.is_music) {
            g.sounds.music_2.setLooping(true);
            g.sounds.music_2.setVolume(g.music_volume);
            g.sounds.music_2.play();
        }

        button_initialization();
        particle_initialization();
        texture_initialization();
        game_initialization();
        text_initialization();
        stage_addition();
        listeners_initialization();

        spawnNotification(g.textSystem.get("game_is_starting"), 3f);

        /* For smooth transition btw screens */
        for (Actor act : stage.getActors())
            act.getColor().set(act.getColor().r, act.getColor().g, act.getColor().b, 0f);
        //
    }

    @Override
    public void render (float delta) {
        //
        super.render(delta);
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        state_time += delta;

        /* Transition btw screens */
        if (background.get(0).getColor().a == 0f) {
            if (change_screen) {
                this.dispose();
                g.setScreen(next_screen);
            } else {
                for (Actor act : stage.getActors())
                        act.addAction(Actions.alpha(1f, .5f));
            }
        }

        /* Teleportation of rhombuses */
        for (AdvSprite s : background) {
            if (s.getX() < -g.w / 4f) {
                s.setX(s.getX() + (g.w / 4f) * RHOMBUSES);
                s.setY(s.getY() - (g.w / 4f));
            }
            if (s.getY() > g.h + (g.w / 4f)) {
                s.setY(s.getY() - (g.w / 4f) * RHOMBUSES);
            }
        }

        /* Animation */
        for (AdvSprite s : background) {s.getSprite().setRegion(background_anim.getKeyFrame(state_time, true));}

        /* End of the game */
        if (isTerminal() && !change_screen && turn != 0 && notifs.size() == 0) {

            for (Actor act : stage.getActors()) act.addAction(Actions.alpha(0f, 4f));

            spawnNotification(g.textSystem.get("fight_over"), 4f);

            //if (g.is_sound) g.sounds.click.play(g.sound_volume); // TODO: game over sound
            if (g.sounds.music_2.isPlaying()) g.sounds.music_2.stop();

            change_screen = true;
            next_screen = new FightResults(g, score_ai, score_player, turn);
        }

        /* Hide or show black gems */
        if (!change_screen && turn != 0 && notifs.size() == 0) {
            if (!was_touched) {
                was_touched = Gdx.input.justTouched();
            } else {
                for (AdvSprite s : turn % 2 == 0 ? black_gems_ai : black_gems_player) {
                    if (!s.hasActions())
                        s.addAction(Actions.sequence(
                                Actions.alpha(0f, .125f),
                                Actions.visible(false)
                        ));
                }
                was_touched = false;
            }
        }

        stage.act(delta);
        stage.draw();

        /* Game initialization */
        if (turn == 0 && notifs.size() == 0 && !handsFilled) {

            for (int i = 0; i < board_size; ++i) {
                applyAction(new GameAction(GameAction.Type.ADD_TO_AI_HAND, null, -1, -1, -1));
                applyAction(new GameAction(GameAction.Type.ADD_TO_PLAYER_HAND, null, -1, -1, -1));
            }

            applyAction(new GameAction(GameAction.Type.PLACE_RANDOM_AI, null, -1, -1, -1));
            applyAction(new GameAction(GameAction.Type.PLACE_RANDOM_PLAYER, null, -1, -1, -1));

            turn++;
            handsFilled = true;

            spawnNotification(g.textSystem.get("turn") + " " + turn, 2f);
        }

        /* Notifications' deletion*/
        for (int i = notifs.size() - 1; i >= 0; --i)
            if (notifs.get(i).isOver()) stage.getActors().removeValue(notifs.remove(i), true);

        /* ESC listener */
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) { this.dispose(); Gdx.app.exit(); }
        //
    }

    private void button_initialization () {
        //

        buttons = new ArrayList<>();

        Button back = new Button(
                g,
                .775f * g.w,
                .0125f * g.w,
                .2125f * g.w,
                g.fonts.f_10.getFont(),
                g.textSystem.get("back_btn"),
                1,
                "back_btn"
        );
        back.get().addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (g.is_sound) g.sounds.click.play(g.sound_volume); // Click sound
                if (g.sounds.music_2.isPlaying()) g.sounds.music_2.stop();
                change_screen = true;
                next_screen = new Menu(g);
                back.get().setChecked(false);
                for (Actor act : stage.getActors()) act.addAction(Actions.alpha(0f, 0.5f));
            }
        });

        Button discard = new Button(
                g,
                .0125f * g.w,
                .0125f * g.w,
                .2125f * g.w,
                g.lang == 0 ? g.fonts.f_15.getFont() : g.fonts.f_20.getFont(),
                g.textSystem.get("discard"),
                1,
                "discard"
        );
        discard.get().addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (!change_screen && notifs.size() == 0 && handsFilled) {
                    if (g.is_sound) g.sounds.click.play(g.sound_volume); // Click sound
                    GameAction action = new GameAction(GameAction.Type.DISCARD, null, -1, -1, -1);
                    if (isApplicable(action)) {
                        applyAction(action);

                        for (AdvSprite s : place_regions)
                            s.addAction(
                                    Actions.sequence(
                                            Actions.alpha(0f, .125f),
                                            Actions.visible(false)
                                    )
                            );

                    } else {
                        spawnNotification(g.textSystem.get("choose_a_gem"), 1.5f);
                    }
                    discard.get().setChecked(false);
                } else {
                    discard.get().setChecked(false);
                }
            }
        });

        buttons.add(back);
        buttons.add(discard);
        //
    }
    private void game_initialization () {
        //
        turn = 0;

        float gem_spawn_x = g.w + .5f * gem_size;
        float gem_spawn_y = .5f * g.h - .5f * gem_size;

        deck_ai = new ArrayList<>();
        s_deck_ai = new ArrayList<>();
        deck_player = new ArrayList<>();
        s_deck_player = new ArrayList<>();

        hand_ai = new ArrayList<>();
        s_hand_ai = new ArrayList<>();
        hand_player = new ArrayList<>();
        s_hand_player = new ArrayList<>();

        board_gems = new ArrayList<>();
        s_board_gems = new ArrayList<>();

        place_regions = new ArrayList<>();
        for (int i = 0; i < board_size * board_size; ++i) {
            place_regions.add(new AdvSprite(
                    g.atlas.createSprite("region_place"),
                    board.getX() + (1f / 34f) * board_tile + (i % board_size) * board_tile,
                    board.getY() + (1f / 34f) * board_tile + (i / board_size) * board_tile,
                    gem_size,
                    gem_size
            ));
            place_regions.get(i).setVisible(false);

            int finalI = i;
            place_regions.get(i).addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!place_regions.get(finalI).hasActions()) {
                        if (turn % 2 == 0) {
                            applyAction(new GameAction(
                                    GameAction.Type.PLACE_AI,
                                    hand_ai.get(selected_hand_ai),
                                    finalI,
                                    -1,
                                    -1));
                        } else {
                            applyAction(new GameAction(
                                    GameAction.Type.PLACE_PLAYER,
                                    hand_player.get(selected_hand_player),
                                    finalI,
                                    -1,
                                    -1));
                        }

                        if (g.is_sound) g.sounds.gem_place.play(g.sound_volume);

                        for (AdvSprite s : place_regions)
                            s.addAction(
                                    Actions.sequence(
                                            Actions.alpha(0f, .125f),
                                            Actions.visible(false)
                                    )
                            );
                    }
                }
            });
        }

        move_regions = new ArrayList<>();
        for (int i = 0; i < board_size * board_size; ++i) {
            move_regions.add(new AdvSprite(
                    g.atlas.createSprite("region_move"),
                    board.getX() + (1f / 34f) * board_tile + (i % board_size) * board_tile,
                    board.getY() + (1f / 34f) * board_tile + (i / board_size) * board_tile,
                    gem_size,
                    gem_size
            ));
            move_regions.get(i).setVisible(false);

            int finalI = i;
            move_regions.get(i).addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!move_regions.get(finalI).hasActions()) {
                        if (finalI - (turn % 2 == 0 ? selected_board_ai : selected_board_player) == 2 * board_size) {
                            applyAction(new GameAction(
                                    GameAction.Type.MOVE_UP,
                                    board_gems.get((turn % 2 == 0 ? selected_board_ai : selected_board_player)),
                                    finalI,
                                    -1,
                                    -1));
                        } else if (finalI - (turn % 2 == 0 ? selected_board_ai : selected_board_player) == -2 * board_size) {
                            applyAction(new GameAction(
                                    GameAction.Type.MOVE_DOWN,
                                    board_gems.get((turn % 2 == 0 ? selected_board_ai : selected_board_player)),
                                    finalI,
                                    -1,
                                    -1));
                        } else if (finalI - (turn % 2 == 0 ? selected_board_ai : selected_board_player) == 2) {
                            applyAction(new GameAction(
                                    GameAction.Type.MOVE_RIGHT,
                                    board_gems.get((turn % 2 == 0 ? selected_board_ai : selected_board_player)),
                                    finalI,
                                    -1,
                                    -1));
                        } else if (finalI - (turn % 2 == 0 ? selected_board_ai : selected_board_player) == -2) {
                            applyAction(new GameAction(
                                    GameAction.Type.MOVE_LEFT,
                                    board_gems.get((turn % 2 == 0 ? selected_board_ai : selected_board_player)),
                                    finalI,
                                    -1,
                                    -1));
                        }

                        if (g.is_sound) g.sounds.gem_place.play(g.sound_volume);

                        for (AdvSprite s : move_regions)
                            s.addAction(
                                    Actions.sequence(
                                            Actions.alpha(0f, .125f),
                                            Actions.visible(false)
                                    )
                            );
                        for (AdvSprite s : attack_regions)
                            s.addAction(
                                    Actions.sequence(
                                            Actions.alpha(0f, .125f),
                                            Actions.visible(false)
                                    )
                            );
                    }
                }
            });
        }

        attack_regions = new ArrayList<>();
        for (int i = 0; i < board_size * board_size; ++i) {
            attack_regions.add(new AdvSprite(
                    g.atlas.createSprite("region_attack"),
                    board.getX() + (1f / 34f) * board_tile + (i % board_size) * board_tile,
                    board.getY() + (1f / 34f) * board_tile + (i / board_size) * board_tile,
                    gem_size,
                    gem_size
            ));
            attack_regions.get(i).setVisible(false);

            int finalI = i;
            attack_regions.get(i).addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!attack_regions.get(finalI).hasActions()) {
                        if (finalI - (turn % 2 == 0 ? selected_board_ai : selected_board_player) == 2 * board_size) {
                            applyAction(new GameAction(
                                    GameAction.Type.ATTACK_UP,
                                    board_gems.get((turn % 2 == 0 ? selected_board_ai : selected_board_player)),
                                    finalI,
                                    -1,
                                    -1));
                        } else if (finalI - (turn % 2 == 0 ? selected_board_ai : selected_board_player) == -2 * board_size) {
                            applyAction(new GameAction(
                                    GameAction.Type.ATTACK_DOWN,
                                    board_gems.get((turn % 2 == 0 ? selected_board_ai : selected_board_player)),
                                    finalI,
                                    -1,
                                    -1));
                        } else if (finalI - (turn % 2 == 0 ? selected_board_ai : selected_board_player) == 2) {
                            applyAction(new GameAction(
                                    GameAction.Type.ATTACK_RIGHT,
                                    board_gems.get((turn % 2 == 0 ? selected_board_ai : selected_board_player)),
                                    finalI,
                                    -1,
                                    -1));
                        } else if (finalI - (turn % 2 == 0 ? selected_board_ai : selected_board_player) == -2) {
                            applyAction(new GameAction(
                                    GameAction.Type.ATTACK_LEFT,
                                    board_gems.get((turn % 2 == 0 ? selected_board_ai : selected_board_player)),
                                    finalI,
                                    -1,
                                    -1));
                        }

                        if (g.is_sound) g.sounds.explosion.play(g.sound_volume);

                        for (AdvSprite s : move_regions)
                            s.addAction(
                                    Actions.sequence(
                                            Actions.alpha(0f, .125f),
                                            Actions.visible(false)
                                    )
                            );
                        for (AdvSprite s : attack_regions)
                            s.addAction(
                                    Actions.sequence(
                                            Actions.alpha(0f, .125f),
                                            Actions.visible(false)
                                    )
                            );
                    }
                }
            });
        }

        // Allocate a space for the hands
        for (int i = 0; i < board_size; ++i) {
            hand_ai.add(null);
            s_hand_ai.add(null);
            hand_player.add(null);
            s_hand_player.add(null);
        }

        // Allocate a space for arrays
        for (int i = 0; i < board_size * board_size; ++i) {
            deck_ai.add(null);
            s_deck_ai.add(null);
            deck_player.add(null);
            s_deck_player.add(null);
            board_gems.add(null);
            s_board_gems.add(null);
        }

        // Get all possible gems according to the size of the board and add it to the decks
        for (int i = 0; i < board_size; ++i) {
            for (int j = 0; j < board_size; ++j) {

                int index = i * board_size + j;

                deck_ai.set(index, new Gem(
                        true,
                        Gem.GemColor.values()[i],
                        Gem.GemForm.values()[j]
                ));
                s_deck_ai.set(index, new AdvSprite(
                        g.atlas.createSprite(deck_ai.get(index).getPath()),
                        gem_spawn_x,
                        gem_spawn_y,
                        gem_size,
                        gem_size
                ));

                deck_player.set(index, new Gem(
                        false,
                        Gem.GemColor.values()[i],
                        Gem.GemForm.values()[j]
                ));
                s_deck_player.set(index, new AdvSprite(
                        g.atlas.createSprite(deck_player.get(index).getPath()),
                        gem_spawn_x,
                        gem_spawn_y,
                        gem_size,
                        gem_size
                ));
            }
        }

        //
    }
    private void listeners_initialization () {
        //
        hand_listeners_ai = new ArrayList<>();
        for (int i = 0; i < board_size; ++i) {
            int finalI = i;
            hand_listeners_ai.add(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (turn % 2 == 0) {

                        explosion.stop();

                        for (AdvSprite s : move_regions)
                            s.addAction(
                                    Actions.sequence(
                                            Actions.alpha(0f, .125f),
                                            Actions.visible(false)
                                    )
                            );
                        for (AdvSprite s : attack_regions)
                            s.addAction(
                                    Actions.sequence(
                                            Actions.alpha(0f, .125f),
                                            Actions.visible(false)
                                    )
                            );
                        selected_board_ai = -1;

                        if (selected_hand_ai == -1) {

                            if (g.is_sound) g.sounds.gem_click.play(g.sound_volume);

                            cont_ai.get(finalI).setColor(Color.CORAL);
                            selected_hand_ai = finalI;

                            for (int i = 0; i < board_gems.size(); ++i) {
                                if (isApplicable(new GameAction(
                                        GameAction.Type.PLACE_AI,
                                        hand_ai.get(selected_hand_ai),
                                        i,
                                        -1,
                                        -1))) {
                                    place_regions.get(i).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                            }

                        } else if (selected_hand_ai != finalI) {

                            if (g.is_sound) g.sounds.gem_click.play(g.sound_volume);

                            cont_ai.get(selected_hand_ai).setColor(Color.WHITE);
                            cont_ai.get(finalI).setColor(Color.CORAL);
                            selected_hand_ai = finalI;

                            for (int i = 0; i < board_gems.size(); ++i) {
                                if (isApplicable(new GameAction(
                                        GameAction.Type.PLACE_AI,
                                        hand_ai.get(selected_hand_ai),
                                        i,
                                        -1,
                                        -1))) {
                                    place_regions.get(i).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                } else if (place_regions.get(i).isVisible()) {
                                    place_regions.get(i).addAction(
                                            Actions.sequence(
                                                    Actions.alpha(0f, .125f),
                                                    Actions.visible(false)
                                            )
                                    );
                                }
                            }

                        } else {

                            if (g.is_sound) g.sounds.gem_place.play(g.sound_volume);

                            cont_ai.get(selected_hand_ai).setColor(Color.WHITE);
                            selected_hand_ai = -1;

                            for (AdvSprite s : place_regions)
                                s.addAction(
                                    Actions.sequence(
                                            Actions.alpha(0f, .125f),
                                            Actions.visible(false)
                                    )
                            );

                        }
                    }
                }
            });
        }

        hand_listeners_player = new ArrayList<>();
        for (int i = 0; i < board_size; ++i) {
            int finalI = i;
            hand_listeners_player.add(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (turn % 2 != 0) {

                        explosion.stop();

                        for (AdvSprite s : move_regions)
                            s.addAction(
                                    Actions.sequence(
                                            Actions.alpha(0f, .125f),
                                            Actions.visible(false)
                                    )
                            );
                        for (AdvSprite s : attack_regions)
                            s.addAction(
                                    Actions.sequence(
                                            Actions.alpha(0f, .125f),
                                            Actions.visible(false)
                                    )
                            );
                        selected_board_player = -1;

                        if (selected_hand_player == -1) {

                            if (g.is_sound) g.sounds.gem_click.play(g.sound_volume);

                            cont_player.get(finalI).setColor(Color.CORAL);
                            selected_hand_player = finalI;

                            for (int i = 0; i < board_gems.size(); ++i) {
                                if (isApplicable(new GameAction(
                                        GameAction.Type.PLACE_PLAYER,
                                        hand_player.get(selected_hand_player),
                                        i,
                                        -1,
                                        -1))) {
                                    place_regions.get(i).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                            }

                        } else if (selected_hand_player != finalI) {

                            if (g.is_sound) g.sounds.gem_click.play(g.sound_volume);

                            cont_player.get(selected_hand_player).setColor(Color.WHITE);
                            cont_player.get(finalI).setColor(Color.CORAL);
                            selected_hand_player = finalI;

                            for (int i = 0; i < board_gems.size(); ++i) {
                                if (isApplicable(new GameAction(
                                        GameAction.Type.PLACE_PLAYER,
                                        hand_player.get(selected_hand_player),
                                        i,
                                        -1,
                                        -1))) {
                                    place_regions.get(i).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                } else if (place_regions.get(i).isVisible()) {
                                    place_regions.get(i).addAction(
                                            Actions.sequence(
                                                    Actions.alpha(0f, .125f),
                                                    Actions.visible(false)
                                            )
                                    );
                                }
                            }

                        } else {

                            if (g.is_sound) g.sounds.gem_place.play(g.sound_volume);

                            cont_player.get(selected_hand_player).setColor(Color.WHITE);
                            selected_hand_player = -1;

                            for (AdvSprite s : place_regions)
                                s.addAction(
                                        Actions.sequence(
                                                Actions.alpha(0f, .125f),
                                                Actions.visible(false)
                                        )
                                );

                        }
                    }
                }
            });
        }

        board_listeners_ai = new ArrayList<>();
        for (int i = 0; i < board_gems.size(); ++i) {
            int finalI = i;
            board_listeners_ai.add(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (turn % 2 == 0) {

                        explosion.stop();

                        for (AdvSprite s : place_regions)
                            s.addAction(
                                    Actions.sequence(
                                            Actions.alpha(0f, .125f),
                                            Actions.visible(false)
                                    )
                            );
                        if (selected_hand_ai  != -1)
                            cont_ai.get(selected_hand_ai).setColor(Color.WHITE);
                        selected_hand_ai = -1;

                        if (selected_board_ai == -1) {

                            if (g.is_sound) g.sounds.gem_click.play(g.sound_volume);

                            selected_board_ai = finalI;

                            for (int i = 0; i < board_gems.size(); ++i){
                                if (isApplicable(new GameAction(
                                        GameAction.Type.MOVE_UP,
                                        board_gems.get(selected_board_ai),
                                        selected_board_ai + 2*board_size,
                                        -1,
                                        -1))) {
                                    move_regions.get(selected_board_ai + 2*board_size).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.MOVE_DOWN,
                                        board_gems.get(selected_board_ai),
                                        selected_board_ai - 2*board_size,
                                        -1,
                                        -1))) {
                                    move_regions.get(selected_board_ai - 2*board_size).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.MOVE_LEFT,
                                        board_gems.get(selected_board_ai),
                                        selected_board_ai - 2,
                                        -1,
                                        -1))) {
                                    move_regions.get(selected_board_ai - 2).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.MOVE_RIGHT,
                                        board_gems.get(selected_board_ai),
                                        selected_board_ai + 2,
                                        -1,
                                        -1))) {
                                    move_regions.get(selected_board_ai + 2).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }

                                // ------------------------------

                                if (isApplicable(new GameAction(
                                        GameAction.Type.ATTACK_UP,
                                        board_gems.get(selected_board_ai),
                                        selected_board_ai + 2*board_size,
                                        -1,
                                        -1))) {
                                    attack_regions.get(selected_board_ai + 2*board_size).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.ATTACK_DOWN,
                                        board_gems.get(selected_board_ai),
                                        selected_board_ai - 2*board_size,
                                        -1,
                                        -1))) {
                                    attack_regions.get(selected_board_ai - 2*board_size).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.ATTACK_LEFT,
                                        board_gems.get(selected_board_ai),
                                        selected_board_ai - 2,
                                        -1,
                                        -1))) {
                                    attack_regions.get(selected_board_ai - 2).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.ATTACK_RIGHT,
                                        board_gems.get(selected_board_ai),
                                        selected_board_ai + 2,
                                        -1,
                                        -1))) {
                                    attack_regions.get(selected_board_ai + 2).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                            }

                        } else if (selected_board_ai != finalI) {

                            if (g.is_sound) g.sounds.gem_click.play(g.sound_volume);

                            selected_board_ai = finalI;

                            for (int i = 0; i < board_gems.size(); ++i){

                                move_regions.get(i).addAction(
                                        Actions.sequence(
                                                Actions.alpha(0f, .125f),
                                                Actions.visible(false)
                                        )
                                );
                                attack_regions.get(i).addAction(
                                        Actions.sequence(
                                                Actions.alpha(0f, .125f),
                                                Actions.visible(false)
                                        )
                                );

                                if (isApplicable(new GameAction(
                                        GameAction.Type.MOVE_UP,
                                        board_gems.get(selected_board_ai),
                                        selected_board_ai + 2*board_size,
                                        -1,
                                        -1))) {
                                    move_regions.get(selected_board_ai + 2*board_size).clearActions();
                                    move_regions.get(selected_board_ai + 2*board_size).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.MOVE_DOWN,
                                        board_gems.get(selected_board_ai),
                                        selected_board_ai - 2*board_size,
                                        -1,
                                        -1))) {
                                    move_regions.get(selected_board_ai - 2*board_size).clearActions();
                                    move_regions.get(selected_board_ai - 2*board_size).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.MOVE_LEFT,
                                        board_gems.get(selected_board_ai),
                                        selected_board_ai - 2,
                                        -1,
                                        -1))) {
                                    move_regions.get(selected_board_ai - 2).clearActions();
                                    move_regions.get(selected_board_ai - 2).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.MOVE_RIGHT,
                                        board_gems.get(selected_board_ai),
                                        selected_board_ai + 2,
                                        -1,
                                        -1))) {
                                    move_regions.get(selected_board_ai + 2).clearActions();
                                    move_regions.get(selected_board_ai + 2).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }

                                // --------------------------------

                                if (isApplicable(new GameAction(
                                        GameAction.Type.ATTACK_UP,
                                        board_gems.get(selected_board_ai),
                                        selected_board_ai + 2*board_size,
                                        -1,
                                        -1))) {
                                    attack_regions.get(selected_board_ai + 2*board_size).clearActions();
                                    attack_regions.get(selected_board_ai + 2*board_size).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.ATTACK_DOWN,
                                        board_gems.get(selected_board_ai),
                                        selected_board_ai - 2*board_size,
                                        -1,
                                        -1))) {
                                    attack_regions.get(selected_board_ai - 2*board_size).clearActions();
                                    attack_regions.get(selected_board_ai - 2*board_size).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.ATTACK_LEFT,
                                        board_gems.get(selected_board_ai),
                                        selected_board_ai - 2,
                                        -1,
                                        -1))) {
                                    attack_regions.get(selected_board_ai - 2).clearActions();
                                    attack_regions.get(selected_board_ai - 2).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.ATTACK_RIGHT,
                                        board_gems.get(selected_board_ai),
                                        selected_board_ai + 2,
                                        -1,
                                        -1))) {
                                    attack_regions.get(selected_board_ai + 2).clearActions();
                                    attack_regions.get(selected_board_ai + 2).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                            }

                        } else {

                            if (g.is_sound) g.sounds.gem_place.play(g.sound_volume);

                            selected_board_ai = -1;

                            for (AdvSprite s : move_regions)
                                s.addAction(
                                        Actions.sequence(
                                                Actions.alpha(0f, .125f),
                                                Actions.visible(false)
                                        )
                                );

                            for (AdvSprite s : attack_regions)
                                s.addAction(
                                        Actions.sequence(
                                                Actions.alpha(0f, .125f),
                                                Actions.visible(false)
                                        )
                                );

                        }
                    }
                }
            });
        }

        board_listeners_player = new ArrayList<>();
        for (int i = 0; i < board_size * board_size; ++i) {
            int finalI = i;
            board_listeners_player.add(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (turn % 2 != 0) {

                        explosion.stop();

                        for (AdvSprite s : place_regions)
                            s.addAction(
                                    Actions.sequence(
                                            Actions.alpha(0f, .125f),
                                            Actions.visible(false)
                                    )
                            );
                        if (selected_hand_player != -1)
                            cont_player.get(selected_hand_player).setColor(Color.WHITE);
                        selected_hand_player = -1;

                        if (selected_board_player == -1) {

                            if (g.is_sound) g.sounds.gem_click.play(g.sound_volume);

                            selected_board_player = finalI;

                            for (int i = 0; i < board_gems.size(); ++i){
                                if (isApplicable(new GameAction(
                                        GameAction.Type.MOVE_UP,
                                        board_gems.get(selected_board_player),
                                        selected_board_player + 2*board_size,
                                        -1,
                                        -1))) {
                                    move_regions.get(selected_board_player + 2*board_size).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.MOVE_DOWN,
                                        board_gems.get(selected_board_player),
                                        selected_board_player - 2*board_size,
                                        -1,
                                        -1))) {
                                    move_regions.get(selected_board_player - 2*board_size).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.MOVE_LEFT,
                                        board_gems.get(selected_board_player),
                                        selected_board_player - 2,
                                        -1,
                                        -1))) {
                                    move_regions.get(selected_board_player - 2).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.MOVE_RIGHT,
                                        board_gems.get(selected_board_player),
                                        selected_board_player + 2,
                                        -1,
                                        -1))) {
                                    move_regions.get(selected_board_player + 2).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }

                                // ------------------------------

                                if (isApplicable(new GameAction(
                                        GameAction.Type.ATTACK_UP,
                                        board_gems.get(selected_board_player),
                                        selected_board_player + 2*board_size,
                                        -1,
                                        -1))) {
                                    attack_regions.get(selected_board_player + 2*board_size).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.ATTACK_DOWN,
                                        board_gems.get(selected_board_player),
                                        selected_board_player - 2*board_size,
                                        -1,
                                        -1))) {
                                    attack_regions.get(selected_board_player - 2*board_size).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.ATTACK_LEFT,
                                        board_gems.get(selected_board_player),
                                        selected_board_player - 2,
                                        -1,
                                        -1))) {
                                    attack_regions.get(selected_board_player - 2).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.ATTACK_RIGHT,
                                        board_gems.get(selected_board_player),
                                        selected_board_player + 2,
                                        -1,
                                        -1))) {
                                    attack_regions.get(selected_board_player + 2).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                            }

                        } else if (selected_board_player != finalI) {

                            if (g.is_sound) g.sounds.gem_click.play(g.sound_volume);

                            selected_board_player = finalI;

                            for (int i = 0; i < board_gems.size(); ++i){

                                move_regions.get(i).addAction(
                                        Actions.sequence(
                                                Actions.alpha(0f, .125f),
                                                Actions.visible(false)
                                        )
                                );
                                attack_regions.get(i).addAction(
                                        Actions.sequence(
                                                Actions.alpha(0f, .125f),
                                                Actions.visible(false)
                                        )
                                );

                                if (isApplicable(new GameAction(
                                        GameAction.Type.MOVE_UP,
                                        board_gems.get(selected_board_player),
                                        selected_board_player + 2*board_size,
                                        -1,
                                        -1))) {
                                    move_regions.get(selected_board_player + 2*board_size).clearActions();
                                    move_regions.get(selected_board_player + 2*board_size).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }

                                if (isApplicable(new GameAction(
                                        GameAction.Type.MOVE_DOWN,
                                        board_gems.get(selected_board_player),
                                        selected_board_player - 2*board_size,
                                        -1,
                                        -1))) {
                                    move_regions.get(selected_board_player - 2*board_size).clearActions();
                                    move_regions.get(selected_board_player - 2*board_size).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.MOVE_LEFT,
                                        board_gems.get(selected_board_player),
                                        selected_board_player - 2,
                                        -1,
                                        -1))) {
                                    move_regions.get(selected_board_player - 2).clearActions();
                                    move_regions.get(selected_board_player - 2).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.MOVE_RIGHT,
                                        board_gems.get(selected_board_player),
                                        selected_board_player + 2,
                                        -1,
                                        -1))) {
                                    move_regions.get(selected_board_player + 2).clearActions();
                                    move_regions.get(selected_board_player + 2).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }

                                // --------------------------------

                                if (isApplicable(new GameAction(
                                        GameAction.Type.ATTACK_UP,
                                        board_gems.get(selected_board_player),
                                        selected_board_player + 2*board_size,
                                        -1,
                                        -1))) {
                                    attack_regions.get(selected_board_player + 2*board_size).clearActions();
                                    attack_regions.get(selected_board_player + 2*board_size).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.ATTACK_DOWN,
                                        board_gems.get(selected_board_player),
                                        selected_board_player - 2*board_size,
                                        -1,
                                        -1))) {
                                    attack_regions.get(selected_board_player - 2*board_size).clearActions();
                                    attack_regions.get(selected_board_player - 2*board_size).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.ATTACK_LEFT,
                                        board_gems.get(selected_board_player),
                                        selected_board_player - 2,
                                        -1,
                                        -1))) {
                                    attack_regions.get(selected_board_player - 2).clearActions();
                                    attack_regions.get(selected_board_player - 2).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                                if (isApplicable(new GameAction(
                                        GameAction.Type.ATTACK_RIGHT,
                                        board_gems.get(selected_board_player),
                                        selected_board_player + 2,
                                        -1,
                                        -1))) {
                                    attack_regions.get(selected_board_player + 2).clearActions();
                                    attack_regions.get(selected_board_player + 2).addAction(
                                            Actions.sequence(
                                                    Actions.visible(true),
                                                    Actions.alpha(1f, .125f)
                                            )
                                    );
                                }
                            }

                        } else {

                            if (g.is_sound) g.sounds.gem_place.play(g.sound_volume);

                            selected_board_player = -1;

                            for (AdvSprite s : move_regions)
                                s.addAction(
                                        Actions.sequence(
                                                Actions.alpha(0f, .125f),
                                                Actions.visible(false)
                                        )
                                );

                            for (AdvSprite s : attack_regions)
                                s.addAction(
                                        Actions.sequence(
                                                Actions.alpha(0f, .125f),
                                                Actions.visible(false)
                                        )
                                );

                        }
                    }
                }
            });
        }
        //
    }
    private void particle_initialization () {
        //
        explosion = new AdvParticle(
                g,
                "explosion",
                0f,
                0f,
                .75f * gem_size,
                AdvParticle.Type.POINT
        );
        explosion.stop();
        //
    }
    private void text_initialization () {
        //
        v_lines = new ArrayList<>();
        h_lines = new ArrayList<>();

        Font font = board_size >= 6 ? g.fonts.f_15 : g.fonts.f_10 ;
        for (int i = 0; i < board_size; ++i) {
            String text = i + 1 + "";
            v_lines.add(new TextLine(
                    font,
                    text,
                    board.getRight() + .0375f*g.w - .5f*font.getWidth(text),
                    board.getY() +
                            .5f * board_tile +
                            i * board_tile +
                            .5f * font.getHeight("A")
            ));
            v_lines.add(new TextLine(
                    font,
                    text,
                    .0375f*g.w - .5f*font.getWidth(text),
                    board.getY() +
                            .5f * board_tile +
                            i * board_tile +
                            .5f * font.getHeight("A")
            ));
            h_lines.add(new TextLine(
                    font,
                    text,
                    board.getX() +
                            .5f * board_tile +
                            i * board_tile + -
                            .5f * font.getWidth(text),
                    board.getTop() + .0375f*g.w + .5f * font.getHeight("A")
            ));
            h_lines.add(new TextLine(
                    font,
                    text,
                    board.getX() +
                            .5f * board_tile +
                            i * board_tile + -
                            .5f * font.getWidth(text),
                    board.getY() - .0375f*g.w + .5f * font.getHeight("A")
            ));
        }

        notifs = new ArrayList<>();
        //
    }
    private void texture_initialization () {
        //
        background = new ArrayList<>();
        for (int i = 0; i < RHOMBUSES * RHOMBUSES; i++) {
            background.add(new AdvSprite(
                    g.atlas.createSprite("background", 0),
                    (i % RHOMBUSES) * g.w / 4f,
                    (i / RHOMBUSES) * g.w / 4f - (g.w / 4f),
                    g.w / 4f,
                    g.w / 4f
            ));
            background.get(i).addAction(
                    Actions.forever(
                            Actions.moveBy(-(g.w / 4f), (g.w / 4f), 5f)));
        }

        background_anim = new Animation<>(
                .09375f,
                g.atlas.findRegions("background"),
                Animation.PlayMode.LOOP_PINGPONG);

        board = new AdvSprite(
                g.atlas.createSprite("board_" + board_size + "x"  + board_size),
                .075f*g.w,
                .5375f*g.h - .425f*g.w,
                .85f*g.w,
                .85f*g.w
        );

        cont_player = new ArrayList<>();
        for (int i = 0; i < board_size; ++i) {
            cont_player.add(new AdvSprite(
                    g.atlas.createSprite("container"),
                    board.getX() + i * board_tile,
                     .125f * g.w + .5f *(board.getY() - .125f * g.w) - .65f * board_tile,
                    board_tile,
                    board_tile
            ));
        }

        cont_ai = new ArrayList<>();
        for (int i = 0; i < board_size; ++i) {
            cont_ai.add(new AdvSprite(
                    g.atlas.createSprite("container"),
                    board.getX() + i * board_tile,
                    (board.getY() + board.getHeight()) +
                            .5f * (g.h - (board.getY() + board.getHeight())) -
                            .35f * board_tile,
                    board_tile,
                    board_tile
            ));
        }

        black_gems_player = new ArrayList<>();
        for (int i = 0; i < board_size; ++i) {
            black_gems_player.add(new AdvSprite(
                    g.atlas.createSprite("gem_black"),
                    cont_player.get(i).getX(),
                    cont_player.get(i).getY(),
                    board_tile,
                    board_tile
            ));
        }

        black_gems_ai = new ArrayList<>();
        for (int i = 0; i < board_size; ++i) {
            black_gems_ai.add(new AdvSprite(
                    g.atlas.createSprite("gem_black"),
                    cont_ai.get(i).getX(),
                    cont_ai.get(i).getY(),
                    board_tile,
                    board_tile
            ));
        }
        //
    }

    private boolean isApplicable (GameAction gameAction) {
        switch (gameAction.getType()) {
            case DISCARD: {
                return turn % 2 == 0 ? selected_hand_ai != -1 : selected_hand_player != -1;
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
            case PLACE_AI: {
                boolean is_gem_up;
                boolean is_gem_down;
                boolean is_gem_left ;
                boolean is_gem_right;

                is_gem_up = gameAction.getBoardPosition1() / board_size < board_size - 1 &&
                        board_gems.get(gameAction.getBoardPosition1()) == null &&
                        board_gems.get(gameAction.getBoardPosition1() + board_size) != null &&
                        board_gems.get(gameAction.getBoardPosition1() + board_size).isIsAI() &&
                        (board_gems.get(gameAction.getBoardPosition1() + board_size).
                                getColor().equals(gameAction.getGem().getColor()) ||
                                board_gems.get(gameAction.getBoardPosition1() + board_size).getForm().
                                equals(gameAction.getGem().getForm()));

                is_gem_down = gameAction.getBoardPosition1() / board_size > 0 &&
                        board_gems.get(gameAction.getBoardPosition1()) == null &&
                        board_gems.get(gameAction.getBoardPosition1() - board_size) != null &&
                        board_gems.get(gameAction.getBoardPosition1() - board_size).isIsAI() &&
                        (board_gems.get(gameAction.getBoardPosition1() - board_size).
                                getColor().equals(gameAction.getGem().getColor()) ||
                                board_gems.get(gameAction.getBoardPosition1() - board_size).getForm().
                                        equals(gameAction.getGem().getForm()));

                is_gem_left = gameAction.getBoardPosition1() % board_size > 0 &&
                        board_gems.get(gameAction.getBoardPosition1()) == null &&
                        board_gems.get(gameAction.getBoardPosition1() - 1) != null &&
                        board_gems.get(gameAction.getBoardPosition1() - 1).isIsAI() &&
                        (board_gems.get(gameAction.getBoardPosition1() - 1).
                                getColor().equals(gameAction.getGem().getColor()) ||
                                board_gems.get(gameAction.getBoardPosition1() - 1).getForm().
                                        equals(gameAction.getGem().getForm()));

                is_gem_right = gameAction.getBoardPosition1() % board_size < board_size - 1 &&
                        board_gems.get(gameAction.getBoardPosition1()) == null &&
                        board_gems.get(gameAction.getBoardPosition1() + 1) != null &&
                        board_gems.get(gameAction.getBoardPosition1() + 1).isIsAI() &&
                        (board_gems.get(gameAction.getBoardPosition1() + 1).
                                getColor().equals(gameAction.getGem().getColor()) ||
                                board_gems.get(gameAction.getBoardPosition1() + 1).getForm().
                                        equals(gameAction.getGem().getForm()));

                return is_gem_up || is_gem_down || is_gem_left || is_gem_right;
            }
            case PLACE_PLAYER: {
                boolean is_gem_up;
                boolean is_gem_down;
                boolean is_gem_left ;
                boolean is_gem_right;

                is_gem_up = gameAction.getBoardPosition1() / board_size < board_size - 1 &&
                        board_gems.get(gameAction.getBoardPosition1()) == null &&
                        board_gems.get(gameAction.getBoardPosition1() + board_size) != null &&
                        !board_gems.get(gameAction.getBoardPosition1() + board_size).isIsAI() &&
                        (board_gems.get(gameAction.getBoardPosition1() + board_size).
                                getColor().equals(gameAction.getGem().getColor()) ||
                                board_gems.get(gameAction.getBoardPosition1() + board_size).getForm().
                                        equals(gameAction.getGem().getForm()));

                is_gem_down = gameAction.getBoardPosition1() / board_size > 0 &&
                        board_gems.get(gameAction.getBoardPosition1()) == null &&
                        board_gems.get(gameAction.getBoardPosition1() - board_size) != null &&
                        !board_gems.get(gameAction.getBoardPosition1() - board_size).isIsAI() &&
                        (board_gems.get(gameAction.getBoardPosition1() - board_size).
                                getColor().equals(gameAction.getGem().getColor()) ||
                                board_gems.get(gameAction.getBoardPosition1() - board_size).getForm().
                                        equals(gameAction.getGem().getForm()));

                is_gem_left = gameAction.getBoardPosition1() % board_size > 0 &&
                        board_gems.get(gameAction.getBoardPosition1()) == null &&
                        board_gems.get(gameAction.getBoardPosition1() - 1) != null &&
                        !board_gems.get(gameAction.getBoardPosition1() - 1).isIsAI() &&
                        (board_gems.get(gameAction.getBoardPosition1() - 1).
                                getColor().equals(gameAction.getGem().getColor()) ||
                                board_gems.get(gameAction.getBoardPosition1() - 1).getForm().
                                        equals(gameAction.getGem().getForm()));

                is_gem_right = gameAction.getBoardPosition1() % board_size < board_size - 1 &&
                        board_gems.get(gameAction.getBoardPosition1()) == null &&
                        board_gems.get(gameAction.getBoardPosition1() + 1) != null &&
                        !board_gems.get(gameAction.getBoardPosition1() + 1).isIsAI() &&
                        (board_gems.get(gameAction.getBoardPosition1() + 1).
                                getColor().equals(gameAction.getGem().getColor()) ||
                                board_gems.get(gameAction.getBoardPosition1() + 1).getForm().
                                        equals(gameAction.getGem().getForm()));

                return is_gem_up || is_gem_down || is_gem_left || is_gem_right;
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

    private void applyAction (GameAction gameAction) {
        switch (gameAction.getType()) {
            case DISCARD: {
                if (selected_hand_ai != -1) {
                    cont_ai.get(selected_hand_ai).setColor(Color.WHITE);

                    s_hand_ai.get(selected_hand_ai).addAction(
                            Actions.moveTo(0 - 1.5f * gem_size, .5f * g.h - .5f * gem_size, .5f)
                    );

                    s_hand_ai.set(selected_hand_ai, null);
                    hand_ai.set(selected_hand_ai, null);

                    GameAction action = new GameAction(GameAction.Type.ADD_TO_AI_HAND, null, -1,-1, -1);
                    if (isApplicable(action))
                        applyAction(action);

                    score_ai -= 10;
                }
                if (selected_hand_player != -1) {
                    cont_player.get(selected_hand_player).setColor(Color.WHITE);

                    s_hand_player.get(selected_hand_player).addAction(
                            Actions.moveTo(0 - 1.5f * gem_size, .5f * g.h - .5f * gem_size, .5f)
                    );

                    s_hand_player.set(selected_hand_player, null);
                    hand_player.set(selected_hand_player, null);

                    GameAction action = new GameAction(GameAction.Type.ADD_TO_PLAYER_HAND, null, -1, -1, -1);
                    if (isApplicable(action))
                        applyAction(action);

                    score_player -= 10;
                }

                for (AdvSprite s : black_gems_ai) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                            ));
                }
                for (AdvSprite s : black_gems_player) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                was_touched = false;

                selected_hand_ai = -1;
                selected_hand_player = -1;
                turn++;
                spawnNotification(g.textSystem.get("turn") + " " + turn, 2f);
                break;
            }
            case ADD_TO_AI_HAND: {

                int empty_index = 0;
                for (int i = 0; i < board_size; ++i) {
                    if (hand_ai.get(i) == null) {
                        empty_index = i;
                        break;
                    }
                }

                ArrayList<Integer> deck_indexes = new ArrayList<>();
                for (int i = 0; i < deck_ai.size(); ++i)
                    if (deck_ai.get(i) != null)
                        deck_indexes.add(i);

                int random_index = deck_indexes.get(MathUtils.random(deck_indexes.size() - 1));
                hand_ai.set(empty_index, deck_ai.get(random_index));

                deck_ai.set(random_index, null);
                deck_player.set(random_index, null);

                s_hand_ai.set(empty_index, s_deck_ai.get(random_index));

                s_deck_ai.set(random_index, null);
                s_deck_player.set(random_index, null);

                s_hand_ai.get(empty_index).addAction(
                        Actions.moveTo(
                                cont_ai.get(empty_index).getX() + (1f / 34f) * board_tile,
                                cont_ai.get(empty_index).getY() + (1f / 34f) * board_tile,
                                .5f
                        )
                );
                s_hand_ai.get(empty_index).addListener(hand_listeners_ai.get(empty_index));

                break;
            }
            case ADD_TO_PLAYER_HAND: {

                int empty_index = 0;
                for (int i = 0; i < board_size; ++i) {
                    if (hand_player.get(i) == null) {
                        empty_index = i;
                        break;
                    }
                }

                ArrayList<Integer> deck_indexes = new ArrayList<>();
                for (int i = 0; i < deck_player.size(); ++i)
                    if (deck_player.get(i) != null)
                        deck_indexes.add(i);

                int random_index = deck_indexes.get(MathUtils.random(deck_indexes.size() - 1));
                hand_player.set(empty_index, deck_player.get(random_index));

                deck_ai.set(random_index, null);
                deck_player.set(random_index, null);

                s_hand_player.set(empty_index, s_deck_player.get(random_index));

                s_deck_ai.set(random_index, null);
                s_deck_player.set(random_index, null);

                s_hand_player.get(empty_index).addAction(
                        Actions.moveTo(
                                cont_player.get(empty_index).getX() + (1f / 34f) * board_tile,
                                cont_player.get(empty_index).getY() + (1f / 34f) * board_tile,
                                .5f
                        )
                );
                s_hand_player.get(empty_index).addListener(hand_listeners_player.get(empty_index));

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
                s_board_gems.set(random_board_index, s_deck_player.get(random_deck_index));

                deck_ai.set(random_deck_index, null);
                s_deck_ai.set(random_deck_index, null);
                deck_player.set(random_deck_index, null);
                s_deck_player.set(random_deck_index, null);

                s_board_gems.get(random_board_index).addAction(
                        Actions.moveTo(
                                board.getX() + (1f / 34f) * board_tile +
                                        (random_board_index % board_size) * board_tile,
                                board.getY() + (1f / 34f) * board_tile +
                                        (random_board_index / board_size) * board_tile,
                                .5f
                        )
                );
                s_board_gems.get(random_board_index).addListener(board_listeners_player.get(random_board_index));

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
                s_board_gems.set(random_board_index, s_deck_ai.get(random_deck_index));

                deck_ai.set(random_deck_index, null);
                s_deck_ai.set(random_deck_index, null);
                deck_player.set(random_deck_index, null);
                s_deck_player.set(random_deck_index, null);

                s_board_gems.get(random_board_index).addAction(
                        Actions.moveTo(
                                board.getX() + (1f / 34f) * board_tile +
                                        (random_board_index % board_size) * board_tile,
                                board.getY() + (1f / 34f) * board_tile +
                                        (random_board_index / board_size) * board_tile,
                                .5f
                        )
                );
                s_board_gems.get(random_board_index).addListener(board_listeners_ai.get(random_board_index));

                break;
            }
            case PLACE_PLAYER: {

                board_gems.set(gameAction.getBoardPosition1(), hand_player.get(selected_hand_player));
                hand_player.set(selected_hand_player, null);

                s_board_gems.set(gameAction.getBoardPosition1(), s_hand_player.get(selected_hand_player));
                s_hand_player.set(selected_hand_player, null);

                s_board_gems.get(gameAction.getBoardPosition1()).addAction(
                        Actions.moveTo(
                                board.getX() + (1f / 34f) * board_tile +
                                        (gameAction.getBoardPosition1() % board_size) * board_tile,
                                board.getY() + (1f / 34f) * board_tile +
                                        (gameAction.getBoardPosition1() / board_size) * board_tile,
                                .5f
                        )
                );
                s_board_gems.get(gameAction.getBoardPosition1()).clearListeners();
                s_board_gems.get(gameAction.getBoardPosition1())
                        .addListener(board_listeners_player.get(gameAction.getBoardPosition1()));

                cont_player.get(selected_hand_player).setColor(Color.WHITE);

                GameAction add_new = new GameAction(GameAction.Type.ADD_TO_PLAYER_HAND, null, -1, -1, -1);
                if (isApplicable(add_new))
                    applyAction(add_new);

                for (AdvSprite s : black_gems_ai) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                for (AdvSprite s : black_gems_player) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                was_touched = false;

                selected_hand_player = -1;
                turn++;

                spawnNotification(g.textSystem.get("turn") + " " + turn, 2f);

                score_player += 10;

                break;
            }
            case PLACE_AI: {

                board_gems.set(gameAction.getBoardPosition1(), hand_ai.get(selected_hand_ai));
                hand_ai.set(selected_hand_ai, null);

                s_board_gems.set(gameAction.getBoardPosition1(), s_hand_ai.get(selected_hand_ai));
                s_hand_ai.set(selected_hand_ai, null);

                s_board_gems.get(gameAction.getBoardPosition1()).addAction(
                        Actions.moveTo(
                                board.getX() + (1f / 34f) * board_tile +
                                        (gameAction.getBoardPosition1() % board_size) * board_tile,
                                board.getY() + (1f / 34f) * board_tile +
                                        (gameAction.getBoardPosition1() / board_size) * board_tile,
                                .5f
                        )
                );
                s_board_gems.get(gameAction.getBoardPosition1()).clearListeners();
                s_board_gems.get(gameAction.getBoardPosition1())
                        .addListener(board_listeners_ai.get(gameAction.getBoardPosition1()));

                cont_ai.get(selected_hand_ai).setColor(Color.WHITE);

                GameAction add_new = new GameAction(GameAction.Type.ADD_TO_AI_HAND, null, -1, -1, -1);
                if (isApplicable(add_new))
                    applyAction(add_new);

                for (AdvSprite s : black_gems_ai) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                for (AdvSprite s : black_gems_player) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                was_touched = false;

                selected_hand_ai = -1;
                turn++;

                spawnNotification(g.textSystem.get("turn") + " " + turn, 2f);

                score_ai += 10;

                break;
            }
            case MOVE_UP: {

                board_gems.set(
                        gameAction.getBoardPosition1(),
                        board_gems.get(turn % 2 == 0 ? selected_board_ai : selected_board_player));

                board_gems.set(turn % 2 == 0 ? selected_board_ai : selected_board_player, null);

                s_board_gems.set(gameAction.getBoardPosition1(), s_board_gems.get(turn % 2 == 0 ? selected_board_ai : selected_board_player));
                s_board_gems.set(turn % 2 == 0 ? selected_board_ai : selected_board_player, null);

                s_board_gems.get(gameAction.getBoardPosition1()).addAction(
                        Actions.moveBy(0f, 2f * board_tile, .5f)
                );
                s_board_gems.get(gameAction.getBoardPosition1()).clearListeners();
                s_board_gems.get(gameAction.getBoardPosition1()).addListener(
                        turn % 2 == 0 ? board_listeners_ai.get(gameAction.getBoardPosition1()) : board_listeners_player.get(gameAction.getBoardPosition1())
                );

                for (AdvSprite s : black_gems_ai) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                for (AdvSprite s : black_gems_player) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                was_touched = false;

                selected_board_ai = -1;
                selected_board_player = -1;
                turn++;

                spawnNotification(g.textSystem.get("turn") + " " + turn, 2f);

                break;
            }
            case MOVE_DOWN: {

                board_gems.set(
                        gameAction.getBoardPosition1(),
                        board_gems.get(turn % 2 == 0 ? selected_board_ai : selected_board_player));

                board_gems.set(turn % 2 == 0 ? selected_board_ai : selected_board_player, null);

                s_board_gems.set(gameAction.getBoardPosition1(), s_board_gems.get(turn % 2 == 0 ? selected_board_ai : selected_board_player));
                s_board_gems.set(turn % 2 == 0 ? selected_board_ai : selected_board_player, null);

                s_board_gems.get(gameAction.getBoardPosition1()).addAction(
                        Actions.moveBy(0f, -2f * board_tile, .5f)
                );
                s_board_gems.get(gameAction.getBoardPosition1()).clearListeners();
                s_board_gems.get(gameAction.getBoardPosition1()).addListener(
                        turn % 2 == 0 ? board_listeners_ai.get(gameAction.getBoardPosition1()) : board_listeners_player.get(gameAction.getBoardPosition1())
                );

                for (AdvSprite s : black_gems_ai) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                for (AdvSprite s : black_gems_player) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                was_touched = false;

                selected_board_ai = -1;
                selected_board_player = -1;
                turn++;

                spawnNotification(g.textSystem.get("turn") + " " + turn, 2f);

                break;
            }
            case MOVE_LEFT: {

                board_gems.set(
                        gameAction.getBoardPosition1(),
                        board_gems.get(turn % 2 == 0 ? selected_board_ai : selected_board_player));

                board_gems.set(turn % 2 == 0 ? selected_board_ai : selected_board_player, null);

                s_board_gems.set(gameAction.getBoardPosition1(), s_board_gems.get(turn % 2 == 0 ? selected_board_ai : selected_board_player));
                s_board_gems.set(turn % 2 == 0 ? selected_board_ai : selected_board_player, null);

                s_board_gems.get(gameAction.getBoardPosition1()).addAction(
                        Actions.moveBy(-2f * board_tile, 0f,.5f)
                );
                s_board_gems.get(gameAction.getBoardPosition1()).clearListeners();
                s_board_gems.get(gameAction.getBoardPosition1()).addListener(
                        turn % 2 == 0 ? board_listeners_ai.get(gameAction.getBoardPosition1()) : board_listeners_player.get(gameAction.getBoardPosition1())
                );

                for (AdvSprite s : black_gems_ai) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                for (AdvSprite s : black_gems_player) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                was_touched = false;

                selected_board_ai = -1;
                selected_board_player = -1;
                turn++;

                spawnNotification(g.textSystem.get("turn") + " " + turn, 2f);

                break;
            }
            case MOVE_RIGHT: {

                board_gems.set(
                        gameAction.getBoardPosition1(),
                        board_gems.get(turn % 2 == 0 ? selected_board_ai : selected_board_player));

                board_gems.set(turn % 2 == 0 ? selected_board_ai : selected_board_player, null);

                s_board_gems.set(gameAction.getBoardPosition1(), s_board_gems.get(turn % 2 == 0 ? selected_board_ai : selected_board_player));
                s_board_gems.set(turn % 2 == 0 ? selected_board_ai : selected_board_player, null);

                s_board_gems.get(gameAction.getBoardPosition1()).addAction(
                        Actions.moveBy(2f * board_tile, 0f,.5f)
                );
                s_board_gems.get(gameAction.getBoardPosition1()).clearListeners();
                s_board_gems.get(gameAction.getBoardPosition1()).addListener(
                        turn % 2 == 0 ? board_listeners_ai.get(gameAction.getBoardPosition1()) : board_listeners_player.get(gameAction.getBoardPosition1())
                );

                for (AdvSprite s : black_gems_ai) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                for (AdvSprite s : black_gems_player) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                was_touched = false;

                selected_board_ai = -1;
                selected_board_player = -1;
                turn++;

                spawnNotification(g.textSystem.get("turn") + " " + turn, 2f);

                break;
            }
            case ATTACK_UP: {

                board_gems.set(
                        gameAction.getBoardPosition1(),
                        board_gems.get(turn % 2 == 0 ? selected_board_ai : selected_board_player));

                board_gems.set(turn % 2 == 0 ? selected_board_ai : selected_board_player, null);

                s_board_gems.get(gameAction.getBoardPosition1()).setVisible(false);

                s_board_gems.set(gameAction.getBoardPosition1(), s_board_gems.get(turn % 2 == 0 ? selected_board_ai : selected_board_player));
                s_board_gems.set(turn % 2 == 0 ? selected_board_ai : selected_board_player, null);

                s_board_gems.get(gameAction.getBoardPosition1()).addAction(
                        Actions.moveBy(0f, 2f * board_tile, .5f)
                );
                s_board_gems.get(gameAction.getBoardPosition1()).clearListeners();
                s_board_gems.get(gameAction.getBoardPosition1()).addListener(
                        turn % 2 == 0 ? board_listeners_ai.get(gameAction.getBoardPosition1()) : board_listeners_player.get(gameAction.getBoardPosition1())
                );

                explosion.setPosition(
                        board.getX() + (gameAction.getBoardPosition1() % board_size) * board_tile + .5f * board_tile,
                        board.getY() + (gameAction.getBoardPosition1() / board_size) * board_tile + .5f * board_tile
                );
                explosion.start();

                if (turn % 2 == 0) {
                    score_ai += 20;
                } else {
                    score_player += 20;
                }

                for (AdvSprite s : black_gems_ai) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                for (AdvSprite s : black_gems_player) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                was_touched = false;

                selected_board_ai = -1;
                selected_board_player = -1;
                turn++;

                spawnNotification(g.textSystem.get("turn") + " " + turn, 2f);

                break;
            }
            case ATTACK_DOWN: {

                board_gems.set(
                        gameAction.getBoardPosition1(),
                        board_gems.get(turn % 2 == 0 ? selected_board_ai : selected_board_player));

                board_gems.set(turn % 2 == 0 ? selected_board_ai : selected_board_player, null);

                s_board_gems.get(gameAction.getBoardPosition1()).setVisible(false);

                s_board_gems.set(gameAction.getBoardPosition1(), s_board_gems.get(turn % 2 == 0 ? selected_board_ai : selected_board_player));
                s_board_gems.set(turn % 2 == 0 ? selected_board_ai : selected_board_player, null);

                s_board_gems.get(gameAction.getBoardPosition1()).addAction(
                        Actions.moveBy(0f, -2f * board_tile, .5f)
                );
                s_board_gems.get(gameAction.getBoardPosition1()).clearListeners();
                s_board_gems.get(gameAction.getBoardPosition1()).addListener(
                        turn % 2 == 0 ? board_listeners_ai.get(gameAction.getBoardPosition1()) : board_listeners_player.get(gameAction.getBoardPosition1())
                );

                explosion.setPosition(
                        board.getX() + (gameAction.getBoardPosition1() % board_size) * board_tile + .5f * board_tile,
                        board.getY() + (gameAction.getBoardPosition1() / board_size) * board_tile + .5f * board_tile
                );
                explosion.start();

                if (turn % 2 == 0) {
                    score_ai += 20;
                } else {
                    score_player += 20;
                }

                for (AdvSprite s : black_gems_ai) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                for (AdvSprite s : black_gems_player) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                was_touched = false;

                selected_board_ai = -1;
                selected_board_player = -1;
                turn++;

                spawnNotification(g.textSystem.get("turn") + " " + turn, 2f);

                break;
            }
            case ATTACK_LEFT: {

                board_gems.set(
                        gameAction.getBoardPosition1(),
                        board_gems.get(turn % 2 == 0 ? selected_board_ai : selected_board_player));

                board_gems.set(turn % 2 == 0 ? selected_board_ai : selected_board_player, null);

                s_board_gems.get(gameAction.getBoardPosition1()).setVisible(false);

                s_board_gems.set(gameAction.getBoardPosition1(), s_board_gems.get(turn % 2 == 0 ? selected_board_ai : selected_board_player));
                s_board_gems.set(turn % 2 == 0 ? selected_board_ai : selected_board_player, null);

                s_board_gems.get(gameAction.getBoardPosition1()).addAction(
                        Actions.moveBy(-2f * board_tile, 0f,.5f)
                );
                s_board_gems.get(gameAction.getBoardPosition1()).clearListeners();
                s_board_gems.get(gameAction.getBoardPosition1()).addListener(
                        turn % 2 == 0 ? board_listeners_ai.get(gameAction.getBoardPosition1()) : board_listeners_player.get(gameAction.getBoardPosition1())
                );

                explosion.setPosition(
                        board.getX() + (gameAction.getBoardPosition1() % board_size) * board_tile + .5f * board_tile,
                        board.getY() + (gameAction.getBoardPosition1() / board_size) * board_tile + .5f * board_tile
                );
                explosion.start();

                if (turn % 2 == 0) {
                    score_ai += 20;
                } else {
                    score_player += 20;
                }

                for (AdvSprite s : black_gems_ai) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                for (AdvSprite s : black_gems_player) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                was_touched = false;

                selected_board_ai = -1;
                selected_board_player = -1;
                turn++;

                spawnNotification(g.textSystem.get("turn") + " " + turn, 2f);

                break;
            }
            case ATTACK_RIGHT: {

                board_gems.set(
                        gameAction.getBoardPosition1(),
                        board_gems.get(turn % 2 == 0 ? selected_board_ai : selected_board_player));

                board_gems.set(turn % 2 == 0 ? selected_board_ai : selected_board_player, null);

                s_board_gems.get(gameAction.getBoardPosition1()).setVisible(false);

                s_board_gems.set(gameAction.getBoardPosition1(), s_board_gems.get(turn % 2 == 0 ? selected_board_ai : selected_board_player));
                s_board_gems.set(turn % 2 == 0 ? selected_board_ai : selected_board_player, null);

                s_board_gems.get(gameAction.getBoardPosition1()).addAction(
                        Actions.moveBy(2f * board_tile, 0f,.5f)
                );
                s_board_gems.get(gameAction.getBoardPosition1()).clearListeners();
                s_board_gems.get(gameAction.getBoardPosition1()).addListener(
                        turn % 2 == 0 ? board_listeners_ai.get(gameAction.getBoardPosition1()) : board_listeners_player.get(gameAction.getBoardPosition1())
                );

                explosion.setPosition(
                        board.getX() + (gameAction.getBoardPosition1() % board_size) * board_tile + .5f * board_tile,
                        board.getY() + (gameAction.getBoardPosition1() / board_size) * board_tile + .5f * board_tile
                );
                explosion.start();

                if (turn % 2 == 0) {
                    score_ai += 20;
                } else {
                    score_player += 20;
                }

                for (AdvSprite s : black_gems_ai) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                for (AdvSprite s : black_gems_player) {
                    s.clearActions();
                    s.addAction(Actions.sequence(
                            Actions.visible(true),
                            Actions.alpha(1f, .125f)
                    ));
                }
                was_touched = false;

                selected_board_ai = -1;
                selected_board_player = -1;
                turn++;

                spawnNotification(g.textSystem.get("turn") + " " + turn, 2f);

                break;
            }
        }
    }

    private boolean isTerminal () {
        if (getAIHandSize() == 0 || getPlayerHandSize() == 0) return true;

        if (turn % 2 == 0) {
            for (int i = 0; i < hand_ai.size(); ++i) {
                for (int j = 0; j < board_gems.size(); ++j) {
                    if (hand_ai.get(i) != null && isApplicable(
                            new GameAction(
                                    GameAction.Type.PLACE_AI,
                                    hand_ai.get(i),
                                    j,
                                    -1,
                                    -1))
                            ) {
                        return false;
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
                        return false;
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
                        return false;
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
                        return false;
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
                        return false;
                    }
                }
            }
            return getAIDeckSize() == 0;
        } else {
            for (int i = 0; i < hand_player.size(); ++i) {
                for (int j = 0; j < board_gems.size(); ++j) {
                    if (hand_player.get(i) != null && isApplicable(
                            new GameAction(
                                    GameAction.Type.PLACE_PLAYER,
                                    hand_player.get(i),
                                    j,
                                    -1,
                                    -1))) {
                        return false;
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
                        return false;
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
                        return false;
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
                        return false;
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
                        return false;
                    }
                }
            }
            return getPlayerDeckSize() == 0;
        }
    }

    private int getAIHandSize () {
        int size = 0;
        for (Gem g : hand_ai) if (g != null) size++;
        return size;
    }
    private int getPlayerHandSize () {
        int size = 0;
        for (Gem g : hand_player) if (g != null) size++;
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

    private void spawnNotification(String text, float lifetime) {
        //
        notifs.add(new TextNotification(
                g.fonts.f_0S,
                text,
                .5f * (g.w - g.fonts.f_0S.getWidth(text)),
                .6f * g.h,
                lifetime
        ));
        stage.addActor(notifs.get(notifs.size() - 1));
        //
    }
    private void stage_addition () {
        //
        for (AdvSprite s : background) stage.addActor(s);

        stage.addActor(board);

        for (AdvSprite s : cont_ai) stage.addActor(s);
        for (AdvSprite s : cont_player) stage.addActor(s);

        for (AdvSprite s : s_deck_ai) stage.addActor(s);
        for (AdvSprite s : s_deck_player) stage.addActor(s);

        for (AdvSprite s : place_regions) stage.addActor(s);
        for (AdvSprite s : move_regions) stage.addActor(s);
        for (AdvSprite s : attack_regions) stage.addActor(s);

        for (AdvSprite s : black_gems_ai) stage.addActor(s);
        for (AdvSprite s : black_gems_player) stage.addActor(s);

        stage.addActor(explosion);

        for (TextLine t : v_lines) stage.addActor(t);
        for (TextLine t : h_lines) stage.addActor(t);

        for (Button b : buttons) stage.addActor(b.get());
        //
    }

    @Override
    public void resume() {
        if (g.is_music) {
            g.sounds.music_2.setLooping(true);
            g.sounds.music_2.setVolume(g.music_volume);
            g.sounds.music_2.play();
        }
    }

}

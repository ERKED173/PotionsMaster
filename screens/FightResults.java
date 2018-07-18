package ru.erked.pcook.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.ArrayList;

import ru.erked.pcook.GameStarter;
import ru.erked.pcook.systems.AdvScreen;
import ru.erked.pcook.systems.AdvSprite;
import ru.erked.pcook.systems.Button;
import ru.erked.pcook.systems.TextLine;

public class FightResults extends AdvScreen {

    /* Sprites */
    private Animation<TextureRegion> background_anim;
    private ArrayList<AdvSprite> background;

    /* Buttons */
    private Button ok;

    /* Text */
    private TextLine turns;
    private TextLine winner;
    private TextLine ai_score;
    private TextLine player_score;
    private TextLine game_results;

    /* Random */
    private float state_time = 0f; // For all animations
    private final int RHOMBUSES = 10; // Background
    private int turns_num;
    private int score_ai;
    private int score_player;

    FightResults (GameStarter game, int score_ai, int score_player, int turns) {
        //
        super(game);

        this.score_ai = score_ai;
        this.score_player = score_player;
        turns_num = turns;

        //
    }

    @Override
    public void show () {
        //
        super.show();

        if (g.is_music) {
            g.sounds.music_1.setLooping(true);
            g.sounds.music_1.setVolume(g.music_volume);
            g.sounds.music_1.play();
        }

        text_initialization();
        texture_initialization();
        button_initialization();
        stage_addition();

        /* For smooth transition btw screens */
        for (Actor act : stage.getActors()) act.getColor().set(act.getColor().r, act.getColor().g, act.getColor().b, 0f);
        //
    }

    @Override
    public void render (float delta) {
        //
        super.render(delta);
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        g.sounds.music_1.setVolume(g.music_volume);

        state_time += delta;

        /* Transition btw screens */
        if (background.get(0).getColor().a == 0f) {
            if (change_screen) {
                this.dispose();
                g.setScreen(next_screen);
            } else {
                for (Actor act : stage.getActors()) act.addAction(Actions.alpha(1f, 0.5f));
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

        stage.act(delta);
        stage.draw();

        /* ESC listener. */
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) { this.dispose();Gdx.app.exit(); }
        //
    }

    private void text_initialization() {
        //
        game_results = new TextLine(
                g.fonts.f_0S,
                g.textSystem.get("game_results"),
                0f,
                0.9f * g.h
        );
        game_results.setX(0.5f * (g.w - game_results.getWidth()));

        String win_text = (score_ai == score_player) ?
                (g.textSystem.get("draw")) :
                (g.textSystem.get("winner") + " " +
                        (score_player > score_ai ?
                                (g.textSystem.get("blue_player")) :
                                (g.textSystem.get("red_player"))));
        winner = new TextLine(
                g.fonts.f_5S,
                win_text,
                0f,
                0.75f * g.h
        );
        winner.setX(0.5f * (g.w - winner.getWidth()));

        ai_score = new TextLine(
                g.fonts.f_5S,
                g.textSystem.get("red_player_score") + ": " + score_ai,
                0f,
                0.6f * g.h
        );
        ai_score.setX(0.5f * (g.w - ai_score.getWidth()));

        player_score = new TextLine(
                g.fonts.f_5S,
                g.textSystem.get("blue_player_score") + ": " + score_player,
                0f,
                0.5f * g.h
        );
        player_score.setX(0.5f * (g.w - player_score.getWidth()));

        turns = new TextLine(
                g.fonts.f_5S,
                g.textSystem.get("turns") + ": " + turns_num,
                0f,
                0.4f * g.h
        );
        turns.setX(0.5f * (g.w - turns.getWidth()));
        //
    }
    private void texture_initialization() {
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
            background.get(i).addAction(Actions.forever(Actions.moveBy(-(g.w / 4f), (g.w / 4f), 5f)));
        }

        background_anim = new Animation<>(
                0.09375f,
                g.atlas.findRegions("background"),
                Animation.PlayMode.LOOP_PINGPONG);

        //
    }
    private void button_initialization() {
        //
        /* --BACK-- */
        ok = new Button(
                g,
                0.675f * g.w,
                0.025f * g.w,
                0.3f * g.w,
                g.fonts.f_5.getFont(),
                g.textSystem.get("ok_btn"),
                1,
                "ok_btn"
        );
        ok.get().addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (g.is_sound) g.sounds.click.play(g.sound_volume); // Click sound
                change_screen = true;
                next_screen = new Menu(g);
                ok.get().setChecked(false);
                for (Actor act : stage.getActors()) act.addAction(Actions.alpha(0f, 0.5f));
            }
        });
        //
    }

    private void stage_addition () {
        //
        for (AdvSprite s : background) stage.addActor(s);
        stage.addActor(ok.get());
        stage.addActor(game_results);
        stage.addActor(winner);
        stage.addActor(ai_score);
        stage.addActor(player_score);
        stage.addActor(turns);
        //
    }

    @Override
    public void resume() {
        if (g.is_music) {
            g.sounds.music_1.setLooping(true);
            g.sounds.music_1.setVolume(g.music_volume);
            g.sounds.music_1.play();
        }
    }

}

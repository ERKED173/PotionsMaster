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

public class FightParams extends AdvScreen {

    /* Sprites */
    private Animation<TextureRegion> background_anim;
    private ArrayList<AdvSprite> background;

    /* Buttons */
    private Button back;
    private Button fight;
    private ArrayList<Button> sizes;

    /* Text */
    private TextLine text_options;
    private TextLine board_size;

    /* Random */
    private float state_time = 0f; // For all animations
    private final int RHOMBUSES = 10; // Background

    FightParams (GameStarter game) {
        //
        super(game);
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
        text_options = new TextLine(
                g.fonts.f_0S,
                g.textSystem.get("fight_parameters"),
                0f,
                0.9f * g.h
        );
        text_options.setX(0.5f * (g.w - text_options.getWidth()));

        board_size = new TextLine(
                g.fonts.f_5S,
                g.textSystem.get("board_size") + ":",
                0f,
                0.6f * g.h
        );
        board_size.setX(0.5f * (g.w - board_size.getWidth()));
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
        back = new Button(
                g,
                0.025f * g.w,
                0.025f * g.w,
                0.3f * g.w,
                g.fonts.f_5.getFont(),
                g.textSystem.get("back_btn"),
                1,
                "back_btn"
        );
        back.get().addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (g.is_sound) g.sounds.click.play(g.sound_volume); // Click sound
                change_screen = true;
                next_screen = new Menu(g);
                back.get().setChecked(false);
                for (Actor act : stage.getActors()) act.addAction(Actions.alpha(0f, 0.5f));
            }
        });

        /* --FIGHT-- */
        fight = new Button(
                g,
                0.675f * g.w,
                0.025f * g.w,
                0.3f * g.w,
                g.fonts.f_5.getFont(),
                g.textSystem.get("fight"),
                1,
                "fight"
        );
        fight.get().addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (g.is_sound) g.sounds.click.play(g.sound_volume); // Click sound
                change_screen = true;
                if (g.sounds.music_1.isPlaying()) g.sounds.music_1.stop();
                next_screen = new FightHuman(g);
                fight.get().setChecked(false);
                for (Actor act : stage.getActors()) act.addAction(Actions.alpha(0f, 0.5f));
            }
        });

        sizes = new ArrayList<>();
        for (int i = 0; i < 5; ++i) {
            sizes.add(new Button(
                    g,
                    .1375f * g.w + 0.15f * g.w * i,
                    .5f * g.h - .0625f * g.w,
                    .125f * g.w,
                    g.fonts.f_0.getFont(),
                    (i + 4) + "",
                    2,
                    i + ""
            ));
            if (i == (g.fight_board_size - 1) / 2) sizes.get(i).get().setChecked(true);
            int finalI = i;
            sizes.get(i).get().addListener(new ClickListener(){
                @Override
                public void clicked (InputEvent event, float x, float y) {
                    if (g.is_sound) g.sounds.click.play(g.sound_volume); // Click sound
                    g.fight_board_size = finalI * 2 + 1;
                    for (int i = 0; i < 5; ++i) {
                        if (i != finalI) {
                            sizes.get(i).get().setChecked(false);
                        } else {
                            sizes.get(i).get().setChecked(true);
                        }
                    }
                }
            });
        }

        //
    }

    private void stage_addition () {
        //
        for (AdvSprite s : background) stage.addActor(s);
        stage.addActor(back.get());
        stage.addActor(fight.get());
        for (Button b : sizes) stage.addActor(b.get());
        stage.addActor(text_options);
        stage.addActor(board_size);
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

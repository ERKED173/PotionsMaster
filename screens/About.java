package ru.erked.pcook.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import ru.erked.pcook.systems.AdvScreen;
import ru.erked.pcook.systems.AdvSprite;
import ru.erked.pcook.systems.Button;
import ru.erked.pcook.systems.InGameText;

public class About extends AdvScreen {

    /* Sprites */
    private Animation<TextureRegion> background_anim;
    private ArrayList<AdvSprite> background;
    private AdvSprite rhombus;

    /* Buttons */
    private Button back;

    /* Text */
    private InGameText captions;

    /* Random */
    private float state_time = 0f; // For all animations
    private final int RHOMBUSES = 10; // Background

    About (GameStarter game) {
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

        /* Move rhombus & text_h using cursor */
        if (Gdx.input.isTouched()) {
            rhombus.addAction(Actions.moveBy(0f, -Gdx.input.getDeltaY()));
            captions.addAction(Actions.moveBy(0f, -Gdx.input.getDeltaY()));
            captions.updatePos(0f);
        }

        /* Teleportation of rhombus & text_h */
        if (captions.getY() > 1.4f * g.h) {
            captions.addAction(Actions.moveBy(0f, -2.05f * g.h));
            rhombus.setY(rhombus.getY() - 2.05f * g.h);
        }
        if (captions.getY() + captions.getHeight() < -0.05f * g.h) {
            captions.addAction(Actions.moveBy(0f,2.05f * g.h));
            rhombus.setY(rhombus.getY() + 2.05f * g.h);
        }

        /* Teleportation of rhombs */
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
        for (AdvSprite s : background) { s.getSprite().setRegion(background_anim.getKeyFrame(state_time, true)); }

        stage.act(delta);
        stage.draw();

        /* ESC listener */
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) { this.dispose(); Gdx.app.exit(); }
        //
    }

    private void text_initialization() {
        //
        captions = new InGameText(g, g.fonts.f_5S, new String[]{
                "captions_1","captions_2","captions_3","captions_4","captions_5","captions_6",
                "captions_7","captions_8","captions_9","captions_10","captions_11","captions_12",
                "captions_13","captions_14"
        }, new int[]{0,2,3,5,6,8,9,10,12,13,14,15,16,18}, true);
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

        rhombus = new AdvSprite(
                g.atlas.createSprite("rhombus"),
                0.375f * g.w,
                -0.275f * g.w,
                0.25f * g.w,
                0.25f * g.w
        );
        rhombus.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (g.is_sound) g.sounds.s_list.get(MathUtils.random(g.sounds.s_list.size() - 1)).play(g.sound_volume);
            }
        });

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
                0.675f * g.w,
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
        //
    }

    private void stage_addition () {
        //
        for (AdvSprite s : background) stage.addActor(s);
        stage.addActor(rhombus);
        stage.addActor(captions);
        stage.addActor(back.get());
        //
    }

}

package ru.erked.pcook.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
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

public class Menu extends AdvScreen {

    /* Sprites */
    private Animation<TextureRegion> background_anim;
    private ArrayList<AdvSprite> background;
    private AdvSprite logo;

    /* Buttons */
    private ArrayList<Button> buttons;

    /* Text Lines */
    private TextLine version;

    /* Random */
    private float state_time = 0f; // For all animations
    private final int RHOMBUSES = 10; // Background

    Menu (GameStarter game) {
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
        stage_addition(); // Addition of actors to the stage

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
                for (Actor act : stage.getActors()) act.addAction(Actions.alpha(1f, .5f));
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

        /* ESC listener */
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) { this.dispose(); Gdx.app.exit(); }
        if (Gdx.input.isKeyPressed(Input.Buttons.BACK)) { this.dispose(); Gdx.app.exit(); }
        //
    }

    private void text_initialization () {
        //
        version = new TextLine(
                g.fonts.f_15,
                g.textSystem.get("version"),
                .5f*(g.w - g.fonts.f_15.getWidth(g.textSystem.get("version"))),
                1.5f* g.fonts.f_15.getHeight("A")
        );
        version.setColor(Color.GRAY);
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
            background.get(i).addAction(Actions.forever(Actions.moveBy(-(g.w / 4f), (g.w / 4f), 5f)));
        }

        logo = new AdvSprite(
                g.atlas.createSprite("logo"),
                .125f * g.w,
                g.h - .4f * g.w,
                .75f * g.w,
                .375f * g.w
        );
        logo.addAction(Actions.forever( Actions.sequence(
                Actions.parallel(
                        Actions.sizeBy(.125f * g.w, .0625f * g.w, 4f),
                        Actions.moveBy(-.0625f * g.w, -.03125f * g.w, 4f)
                ),
                Actions.parallel(
                        Actions.sizeBy(-.125f * g.w, -.0625f * g.w, 4f),
                        Actions.moveBy(.0625f * g.w, .03125f * g.w, 4f)
                )
        )));

        background_anim = new Animation<>(
                .09375f,
                g.atlas.findRegions("background"),
                Animation.PlayMode.LOOP_PINGPONG);
        //
    }
    private void button_initialization () {
        //

        buttons = new ArrayList<>();

        /* --EXIT-- */
        Button exit = new Button(
                g,
                .875f * g.w,
                .025f * g.w,
                .35f * g.w,
                g.fonts.f_5.getFont(),
                g.textSystem.get("exit_btn"),
                1,
                "exit_btn"
        );
        exit.get().addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (!change_screen) {
                    if (g.is_sound) g.sounds.click.play(g.sound_volume); // Click sound
                    dispose();
                    Gdx.app.exit();
                }
            }
        });

        /* --ABOUT-- */
        Button about = new Button(
                g,
                .875f * g.w,
                .085f * g.w,
                .35f * g.w,
                g.fonts.f_5.getFont(),
                g.textSystem.get("about_btn"),
                1,
                "about_btn"
        );
        about.get().addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (!change_screen) {
                    if (g.is_sound) g.sounds.click.play(g.sound_volume); // Click sound
                    change_screen = true;
                    next_screen = new About(g);
                    about.get().setChecked(false);
                    for (Actor act : stage.getActors()) act.addAction(Actions.alpha(0f, .5f));
                }
            }
        });

        /* --OPTIONS-- */
        Button options = new Button(
                g,
                .875f * g.w,
                .145f * g.w,
                .35f * g.w,
                g.fonts.f_5.getFont(),
                g.textSystem.get("options_btn"),
                1,
                "options_btn"
        );
        options.get().addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (!change_screen) {
                    if (g.is_sound) g.sounds.click.play(g.sound_volume); // Click sound
                    change_screen = true;
                    next_screen = new Options(g);
                    options.get().setChecked(false);
                    for (Actor act : stage.getActors()) act.addAction(Actions.alpha(0f, .5f));
                }
            }
        });

        /* --START-- */
        Button start = new Button(
                g,
                .875f * g.w,
                .205f * g.w,
                .35f * g.w,
                g.fonts.f_5.getFont(),
                g.textSystem.get("start_btn"),
                1,
                "start_btn"
        );
        start.get().addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (!change_screen) {
                    if (g.is_sound) g.sounds.click.play(g.sound_volume); // Click sound
                    if (g.sounds.music_1.isPlaying()) g.sounds.music_1.stop();
                    change_screen = true;
                    next_screen = new FightAI(g);
                    start.get().setChecked(false);
                    for (Actor act : stage.getActors()) act.addAction(Actions.alpha(0f, .5f));
                }
            }
        });

        /* --1 VS 1-- */
        Button one_vs_one = new Button(
                g,
                .875f * g.w,
                .205f * g.w,
                .35f * g.w,
                g.fonts.f_5.getFont(),
                g.textSystem.get("one_vs_one"),
                1,
                "one_vs_one"
        );
        one_vs_one.get().addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (!change_screen) {
                    if (g.is_sound) g.sounds.click.play(g.sound_volume);
                    change_screen = true;
                    next_screen = new FightParams(g);
                    one_vs_one.get().setChecked(false);
                    for (Actor act : stage.getActors()) act.addAction(Actions.alpha(0f, .5f));
                }
            }
        });

        buttons.add(start);
        buttons.add(one_vs_one);
        buttons.add(options);
        buttons.add(about);
        buttons.add(exit);

        /*-- Button position calculation --*/
        float center_height = .45f * g.h;
        if (buttons.size() % 2 == 0) {
            float margin = ((int)(buttons.size() / 2f)) * .55f*buttons.get(0).get().getHeight();
            float x_pos = .5f * g.w - .5f*buttons.get(0).get().getWidth();
            for (int i = 0; i < buttons.size(); ++i) {
                buttons.get(i).get().setPosition(x_pos, center_height + margin);
                margin -= 1.1f*buttons.get(0).get().getHeight();
            }
        } else {
            float margin = ((int)(buttons.size() / 2f)) * .55f*buttons.get(0).get().getHeight();
            float x_pos = .5f * g.w - .5f*buttons.get(0).get().getWidth();
            for (int i = 0; i < buttons.size(); ++i) {
                buttons.get(i).get().setPosition(x_pos, center_height + margin);
                margin -= 1.1f*buttons.get(0).get().getHeight();
            }
        }
        //
    }

    private void stage_addition () {
        //
        for (AdvSprite s : background) stage.addActor(s);
        stage.addActor(logo);
        stage.addActor(version);
        for (Button b : buttons) stage.addActor(b.get());
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

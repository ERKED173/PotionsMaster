package ru.erked.pcook.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.ArrayList;

import ru.erked.pcook.GameStarter;
import ru.erked.pcook.entities.Entity;
import ru.erked.pcook.systems.AdvAnimation;
import ru.erked.pcook.systems.AdvMap;
import ru.erked.pcook.systems.AdvScreen;
import ru.erked.pcook.systems.AdvSprite;
import ru.erked.pcook.systems.Button;
import ru.erked.pcook.systems.TextLine;

public class Game extends AdvScreen implements GestureDetector.GestureListener {

    /* Animations */
    private AdvAnimation a_wizard;

    /* Buttons */
    private Button menu;

    /* Dialogs */

    /* Entities */
    private Entity wizard;

    /* Particles */

    /* Random */
    private float state_time = 0f; // For all animations
    private Vector2 old_wizard_pos = new Vector2();
    private Vector2 t_d = new Vector2();

    /* Sprites */

    /* TextLines */
    private TextLine debug_line;

    /* Tiled Map */
    private AdvMap map;

    Game(GameStarter game) {
        //
        super(game);
        //
    }

    @Override
    public void show () {
        //
        super.show();

        if (g.is_music) {
            g.sounds.music_2.setLooping(true);
            g.sounds.music_2.setVolume(g.music_volume);
            g.sounds.music_2.play();
        }

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new GestureDetector(this));
        Gdx.input.setInputProcessor(multiplexer);

        button_initialization();
        texture_initialization();
        map_initialization();
        entity_initialization();
        animation_initialization();
        dialog_initialization();
        text_initialization();
        particle_initialization();

        stage_addition();

        /* Initial map position */
        map.setPosition(
                wizard.getWidth() * wizard.getPosition().x,
                wizard.getHeight() * wizard.getPosition().y
        );
        debug_line.setPosition(
                map.getX() - .45f*g.w,
                map.getY() - .45f*g.h
        );
        menu.get().setPosition(
                map.getX() + .475f*g.w - menu.get().getWidth(),
                map.getY() - .5f*g.h + .025f*g.w
        );

        /* For smooth transition btw screens */
        for (Actor act : stage.getActors()) act.getColor().set(act.getColor().r, act.getColor().g, act.getColor().b, 0f);
        //
    }

    @Override
    public void render (float delta) {
        //
        super.render(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        state_time += delta;

        /* Transition btw screens */
        if (map.getColor().a == 0f) {
            if (change_screen) {
                this.dispose();
                g.setScreen(next_screen);
            } else {
                for (Actor act : stage.getActors()) act.addAction(Actions.alpha(1f, .5f));
            }
        }

        /* Animation */
        wizard.getSprite().setRegion(a_wizard.getKeyFrame(delta, true));

        /* Actions to perform when screen is touched */
        if (Gdx.input.isTouched()) {
            wizard_movement();
        } else {
            wizard_sprite(); // Calculates appropriate sprite
        }

        /* Camera position */
        if (wizard.getPosition().x != old_wizard_pos.x || wizard.getPosition().y != old_wizard_pos.y) {

            Vector2 translation = new Vector2(
                    wizard.getWidth() * wizard.getPosition().x - map.getX() + .5f*wizard.getWidth(),
                    wizard.getHeight() * wizard.getPosition().y - map.getY() + .5f*wizard.getHeight()
            );

            map.addAction(Actions.moveTo(
                    wizard.getWidth() * wizard.getPosition().x + .5f*wizard.getWidth(),
                    wizard.getHeight() * wizard.getPosition().y + .5f*wizard.getHeight(),
                    wizard.getSpeed()
            ));

            ui_calculation(translation);
        }
        old_wizard_pos.x = wizard.getPosition().x;
        old_wizard_pos.y = wizard.getPosition().y;

        debug_line.setText("FPS: " + Gdx.graphics.getFramesPerSecond());

        stage.act(delta);
        stage.draw();

        /* ESC listener */
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) { this.dispose(); Gdx.app.exit(); }
        //
    }

    private void animation_initialization () {
        //
        String[] w_keys = new String[]{
                "walk_up_left",
                "walk_up",
                "walk_up_right",
                "walk_right",
                "walk_down_right",
                "walk_down",
                "walk_down_left",
                "walk_left",
                "breath_up_left",
                "breath_up",
                "breath_up_right",
                "breath_right",
                "breath_down_right",
                "breath_down",
                "breath_down_left",
                "breath_left"
        }; // 16 animations
        ArrayList<Animation<TextureRegion>> a_w = new ArrayList<>();
        a_w.add(new Animation<>(
                wizard.getSpeed() / 2f,
                g.atlas.findRegions("wizard_left_up"),
                Animation.PlayMode.LOOP
        ));
        a_w.add(new Animation<>(
                wizard.getSpeed() / 2f,
                g.atlas.findRegions("wizard_up"),
                Animation.PlayMode.LOOP
        ));
        a_w.add(new Animation<>(
                wizard.getSpeed() / 2f,
                g.atlas.findRegions("wizard_right_up"),
                Animation.PlayMode.LOOP
        ));
        a_w.add(new Animation<>(
                wizard.getSpeed() / 2f,
                g.atlas.findRegions("wizard_right"),
                Animation.PlayMode.LOOP
        ));
        a_w.add(new Animation<>(
                wizard.getSpeed() / 2f,
                g.atlas.findRegions("wizard_right_down"),
                Animation.PlayMode.LOOP
        ));
        a_w.add(new Animation<>(
                wizard.getSpeed() / 2f,
                g.atlas.findRegions("wizard_down"),
                Animation.PlayMode.LOOP
        ));
        a_w.add(new Animation<>(
                wizard.getSpeed() / 2f,
                g.atlas.findRegions("wizard_left_down"),
                Animation.PlayMode.LOOP
        ));
        a_w.add(new Animation<>(
                wizard.getSpeed() / 2f,
                g.atlas.findRegions("wizard_left"),
                Animation.PlayMode.LOOP
        ));
        a_w.add(new Animation<>(
                .5f,
                g.atlas.findRegions("wizard_left_up_b"),
                Animation.PlayMode.LOOP
        ));
        a_w.add(new Animation<>(
                .5f,
                g.atlas.findRegions("wizard_up_b"),
                Animation.PlayMode.LOOP
        ));
        a_w.add(new Animation<>(
                .5f,
                g.atlas.findRegions("wizard_right_up_b"),
                Animation.PlayMode.LOOP
        ));
        a_w.add(new Animation<>(
                .5f,
                g.atlas.findRegions("wizard_right_b"),
                Animation.PlayMode.LOOP
        ));
        a_w.add(new Animation<>(
                .5f,
                g.atlas.findRegions("wizard_right_down_b"),
                Animation.PlayMode.LOOP
        ));
        a_w.add(new Animation<>(
                .5f,
                g.atlas.findRegions("wizard_down_b"),
                Animation.PlayMode.LOOP
        ));
        a_w.add(new Animation<>(
                .5f,
                g.atlas.findRegions("wizard_left_down_b"),
                Animation.PlayMode.LOOP
        ));
        a_w.add(new Animation<>(
                .5f,
                g.atlas.findRegions("wizard_left_b"),
                Animation.PlayMode.LOOP
        ));
        a_wizard = new AdvAnimation(a_w ,w_keys);
        a_wizard.addNextStates(new String[]{"breath_down_left"});
        //
    }
    private void button_initialization () {
        //
        menu = new Button(
                g,
                0f,
                0f,
                .25f*g.w,
                g.fonts.f_15.getFont(),
                g.textSystem.get("menu_btn"),
                1,
                "menu_btn"
        );
        menu.get().addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!change_screen && dialogsAreNotDisplayed()) {
                    if (g.is_sound) g.sounds.click.play(g.sound_volume);
                    if (g.is_music) if (g.sounds.music_2.isPlaying()) g.sounds.music_2.stop();
                    change_screen = true;
                    next_screen = new Menu(g);
                    menu.get().setChecked(false);
                    for (Actor a : stage.getActors()) a.addAction(Actions.alpha(0f, .5f));
                } else {
                    menu.get().setChecked(false);
                }
            }
        });
        //
    }
    private void dialog_initialization () {
        //
        //
    }
    private void entity_initialization () {
        //
        wizard = new Entity(
                g.atlas.createSprite("wizard_left"),
                0f,
                0f,
                g.tile_size,
                g.tile_size,
                .33333f
        );
        wizard.setIntPos(new Vector2(
                15,
                15
        ));
        wizard.setPosition(
                wizard.getPosition().x * g.tile_size,
                wizard.getPosition().y * g.tile_size
        );
        old_wizard_pos.x = 10;
        old_wizard_pos.y = 55;
        //
    }
    private void map_initialization () {
        //
        map = new AdvMap(stage, g.pixel_size); // Generate new map
        //map = new AdvMap("castle", stage, g.pixel_size); // Opens a map
        //
    }
    private void particle_initialization ()  {
        //
        //
    }
    private void texture_initialization () {
        //
        //
    }
    private void text_initialization () {
        //
        debug_line = new TextLine(
                g.fonts.f_0S,
                wizard.getX() + "  ---  " + wizard.getY(),
                map.getX() - .95f*map.getWidth(),
                map.getY() - .95f*map.getHeight()
        );
        //
    }

    private void stage_addition() {
        //
        stage.addActor(map);
        stage.addActor(wizard);
        stage.addActor(debug_line);
        stage.addActor(menu.get());
        //
    }
    private void ui_calculation (Vector2 t) {
        //
        debug_line.addAction(Actions.moveBy(
                t.x,
                t.y,
                wizard.getSpeed()
        ));
        menu.get().addAction(Actions.moveBy(
                t.x,
                t.y,
                wizard.getSpeed()
        ));
        //
    }
    private void wizard_movement () {
        float d_x = Gdx.input.getX(0) - t_d.x, d_y = (g.h - Gdx.input.getY(0)) - t_d.y;
        float d = (float)(Math.sqrt((double)(d_x * d_x + d_y * d_y)));

        if (d > 0.075f*g.w) {
            if (Math.cos(Math.toRadians(180 - 22.5)) >= d_x / d) {
                wizard.move(Entity.Direction.LEFT, map);
                a_wizard.changeState("walk_left");
            } else if (Math.cos(Math.toRadians(22.5)) <= d_x / d) {
                wizard.move(Entity.Direction.RIGHT, map);
                a_wizard.changeState("walk_right");
            } else if (Math.sin(Math.toRadians(90 - 22.5)) <= d_y / d) {
                wizard.move(Entity.Direction.UP, map);
                a_wizard.changeState("walk_up");
            } else if (Math.sin(Math.toRadians(-90 - 22.5)) >= d_y / d) {
                wizard.move(Entity.Direction.DOWN, map);
                a_wizard.changeState("walk_down");
            } else {
                if (d_x / d < 0f && d_y / d > 0f) {
                    wizard.move(Entity.Direction.LEFT_UP, map);
                    a_wizard.changeState("walk_up_left");
                }
                if (d_x / d < 0f && d_y / d < 0f) {
                    wizard.move(Entity.Direction.LEFT_DOWN, map);
                    a_wizard.changeState("walk_down_left");
                }
                if (d_x / d > 0f && d_y / d > 0f) {
                    wizard.move(Entity.Direction.RIGHT_UP, map);
                    a_wizard.changeState("walk_up_right");
                }
                if (d_x / d > 0f && d_y / d < 0f) {
                    wizard.move(Entity.Direction.RIGHT_DOWN, map);
                    a_wizard.changeState("walk_down_right");
                }
            }
        }

    }
    private void wizard_sprite () {
        //
        switch (a_wizard.getLastState()) {
            case "walk_up_left": { a_wizard.addNextStates(new String[]{"breath_up_left"}); break; }
            case "walk_up": { a_wizard.addNextStates(new String[]{"breath_up"}); break; }
            case "walk_up_right": { a_wizard.addNextStates(new String[]{"breath_up_right"}); break; }
            case "walk_right": { a_wizard.addNextStates(new String[]{"breath_right"}); break; }
            case "walk_down_right": { a_wizard.addNextStates(new String[]{"breath_down_right"}); break; }
            case "walk_down": { a_wizard.addNextStates(new String[]{"breath_down"}); break; }
            case "walk_down_left": { a_wizard.addNextStates(new String[]{"breath_down_left"}); break; }
            case "walk_left": { a_wizard.addNextStates(new String[]{"breath_left"}); break; }
        }
        //
    }

    @Override
    public void dispose() {
        super.dispose();
        map.dispose();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (!Gdx.input.isTouched(1)) {
            t_d.x = x;
            t_d.y = g.h - y;
        }

        int obj_x = (int) ((t_d.x  - .5f*g.w) / g.tile_size) + (int)(wizard.getPosition().x);
        int obj_y = (int) (((g.h - y) - .5f*g.h) / g.tile_size) + (int)(wizard.getPosition().y);

        /*if (map.getLayer("chests").getCell(obj_x, obj_y) != null &&
                map.getLayer("chests").getCell(obj_x, obj_y).getTile().getProperties().containsKey("box") &&
                wizard.canInteract(new Vector2(obj_x, obj_y))) {
            g.sounds.wizard_touch[MathUtils.random(2)].play(g.sound_volume);
        }*/

        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }
}

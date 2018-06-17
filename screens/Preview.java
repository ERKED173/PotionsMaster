package ru.erked.pcook.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import ru.erked.pcook.GameStarter;
import ru.erked.pcook.systems.AdvScreen;
import ru.erked.pcook.systems.Ingredient;
import ru.erked.pcook.systems.Potion;
import ru.erked.pcook.systems.TextSystem;
import ru.erked.pcook.utils.Fonts;
import ru.erked.pcook.utils.Ingredients;
import ru.erked.pcook.utils.Potions;
import ru.erked.pcook.utils.Sounds;

public class Preview extends AdvScreen {

    private float timer = 0f; // Timer for transition btw ru.erked.pcook.screens

    public Preview (GameStarter game) {
        //
        super(game);
        //
    }

    @Override
    public void show () {
        //
        /*  Initialisation of main game ru.erked.pcook.systems. */
        g.prefs = Gdx.app.getPreferences("potions_cook_preferences");
        g.lang = g.prefs.getInteger("language", 0);
        g.atlas = new TextureAtlas("textures/textures.atlas");
        g.textSystem = new TextSystem(g.lang);
        g.sounds = new Sounds();
        g.fonts = new Fonts(g.textSystem.get("FONT_CHARS"));
        g.ingredients = new Ingredients(g);
        g.potions = new Potions();

        g.w = Gdx.graphics.getWidth();
        g.h = Gdx.graphics.getHeight();

        g.pixel_size = .00625f * g.w;
        g.tile_size = g.pixel_size * 16f;

        String[] inv = g.prefs.getString("inventory", "").split(" ");
        for (String s : inv) {
            for (Ingredient i : g.ingredients.list) {
                if (i.getId().equals(s))
                    g.ingredients.inventory.add(i);
            }
        }

        String[] add = g.prefs.getString("added", "").split(" ");
        for (String s : add) {
            for (Ingredient i : g.ingredients.list) {
                if (i.getId().equals(s))
                    g.ingredients.added.add(i);
            }
        }

        g.is_music = g.prefs.getBoolean("is_music_on", true);
        g.is_sound = g.prefs.getBoolean("is_sound_on", true);

        g.music_volume = g.prefs.getFloat("music_volume", 1f);
        g.sound_volume = g.prefs.getFloat("sound_volume", 1f);

        g.day = g.prefs.getInteger("day", 1);
        g.money = g.prefs.getInteger("money", 0);
        g.potion_cooked = g.prefs.getInteger("potion_cooked", 0);

        for (int i = 0; i < g.potion_cooked; ++i) {
            String[] p_h = g.prefs.getString("potion_history_" + i, "").split(" ");
            g.potions.list.add(new Potion(
                    new String[] {p_h[4], p_h[5], p_h[6], p_h[7]},
                    new int[] {Integer.parseInt(p_h[0]), Integer.parseInt(p_h[1]), Integer.parseInt(p_h[2]), Integer.parseInt(p_h[3])},
                    Integer.parseInt(p_h[8]),
                    Integer.parseInt(p_h[9])
            ));
        }

        if (g.is_music) {
            g.sounds.music_1.setLooping(true);
            g.sounds.music_1.setVolume(g.music_volume);
            g.sounds.music_1.play();
        }

    }

    @Override
    public void render (float delta) {
        //
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        /* To the next screen. */
        timer += delta;
        if (timer >= 5.5f) g.setScreen(new Menu(g));
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) timer += 5.5f;
        if (Gdx.input.justTouched()) timer += 5.5f;
        //
    }


}

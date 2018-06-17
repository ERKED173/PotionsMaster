package ru.erked.pcook.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.Stage;

import java.util.ArrayList;

import ru.erked.pcook.GameStarter;

public class AdvScreen implements Screen{

    protected GameStarter g;
    protected Stage stage;

    protected boolean change_screen = false;
    protected Screen next_screen;

    private ArrayList<Dialog> dialogs;

    public AdvScreen (GameStarter g) {
        this.g = g;
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        dialogs = new ArrayList<>();
    }

    // Values that will be saved by default
    private void save_data() {
        StringBuilder inv = new StringBuilder();
        StringBuilder add = new StringBuilder();
        StringBuilder p_h = new StringBuilder();
        for (Ingredient i : g.ingredients.inventory) inv.append(i.getId()).append(" ");
        for (Ingredient i : g.ingredients.added) add.append(i.getId()).append(" ");
        int i = 0;
        for (Potion p : g.potions.list) {
            p_h.append(p.getIntName()[0]).append(" ")
                    .append(p.getIntName()[1]).append(" ")
                    .append(p.getIntName()[2]).append(" ")
                    .append(p.getIntName()[3]).append(" ")
                    .append(p.getIngredients()[0]).append(" ")
                    .append(p.getIngredients()[1]).append(" ")
                    .append(p.getIngredients()[2]).append(" ")
                    .append(p.getIngredients()[3]).append(" ")
                    .append(p.getDate()).append(" ")
                    .append(p.getBottle()).append(" ");
            g.prefs.putString("potion_history_" + i, p_h.toString());
            p_h = new StringBuilder();
            i++;
        }
        g.prefs.putString("inventory", inv.toString());
        g.prefs.putString("added", add.toString());
        g.prefs.putFloat("music_volume", g.music_volume);
        g.prefs.putFloat("sound_volume", g.sound_volume);
        g.prefs.putBoolean("is_music_on", g.is_music);
        g.prefs.putBoolean("is_sound_on", g.is_sound);
        g.prefs.putInteger("potion_cooked", g.potion_cooked);
        g.prefs.putInteger("language", g.lang);
        g.prefs.putInteger("day", g.day);
        g.prefs.flush();
    }

    protected void addDialog (Dialog dialog) {
        stage.addActor(dialog);
        stage.addActor(dialog.getText());
        dialogs.add(dialog);
        for (Button b : dialog.getButtons()) stage.addActor(b.get());
    }

    protected boolean dialogsAreNotDisplayed() {
        boolean some_showing = false;
        for (Dialog d : dialogs) if (d.isDisplayed()) { some_showing = true; break; }
        return !some_showing;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {
        save_data();
        for (Music m : g.sounds.m_list) if (m.isPlaying()) m.pause();
    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        save_data();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}

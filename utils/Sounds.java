package ru.erked.pcook.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.ArrayList;
import java.util.Arrays;

public class Sounds {

    /* Random */
    public Sound click;
    public Sound harp;
    public Sound cooking;
    public Sound explosion;
    public Sound gem_click;
    public Sound gem_place;
    public Sound cooking_power;

    /* Wizard */
    public Sound[] wizard_touch;

    /* Music */
    public Music music_1;
    public Music music_2;

    public Music fire;
    public Music forest;
    public Music bubbles;
    public Music crickets;

    /* All sounds & music */
    public ArrayList<Sound> s_list;
    public ArrayList<Music> m_list;

    public Sounds () {
        s_list = new ArrayList<>();
        m_list = new ArrayList<>();

        click = Gdx.audio.newSound(Gdx.files.internal("sounds/random/click.wav"));
        harp = Gdx.audio.newSound(Gdx.files.internal("sounds/random/harp.wav"));
        cooking = Gdx.audio.newSound(Gdx.files.internal("sounds/random/cooking.wav"));
        cooking_power = Gdx.audio.newSound(Gdx.files.internal("sounds/random/cooking_power.wav"));
        gem_click = Gdx.audio.newSound(Gdx.files.internal("sounds/random/gem_click.wav"));
        gem_place = Gdx.audio.newSound(Gdx.files.internal("sounds/random/gem_place.wav"));
        explosion = Gdx.audio.newSound(Gdx.files.internal("sounds/random/explosion.wav"));

        wizard_touch = new Sound[3];
        for (int i = 0; i < wizard_touch.length; ++i) wizard_touch[i] = Gdx.audio.newSound(Gdx.files.internal("sounds/wizard/touch_" + (i + 1) + ".wav"));

        music_1 = Gdx.audio.newMusic(Gdx.files.internal("sounds/music/music_1.mp3"));
        music_2 = Gdx.audio.newMusic(Gdx.files.internal("sounds/music/music_2.mp3"));

        fire = Gdx.audio.newMusic(Gdx.files.internal("sounds/lab/fire.wav"));
        forest = Gdx.audio.newMusic(Gdx.files.internal("sounds/lab/forest.wav"));
        bubbles = Gdx.audio.newMusic(Gdx.files.internal("sounds/random/bubbles.wav"));
        crickets = Gdx.audio.newMusic(Gdx.files.internal("sounds/lab/crickets.wav"));

        s_list.add(click);
        s_list.add(harp);
        s_list.add(cooking);
        s_list.add(cooking_power);
        s_list.add(explosion);
        s_list.add(gem_click);
        s_list.addAll(Arrays.asList(wizard_touch));

        m_list.add(music_1);
        m_list.add(music_2);
        m_list.add(fire);
        m_list.add(forest);
        m_list.add(bubbles);
        m_list.add(crickets);
    }

}

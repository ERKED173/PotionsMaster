package ru.erked.pcook;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import ru.erked.pcook.screens.Preview;
import ru.erked.pcook.systems.TextSystem;
import ru.erked.pcook.utils.Fonts;
import ru.erked.pcook.utils.Ingredients;
import ru.erked.pcook.utils.Potions;
import ru.erked.pcook.utils.Sounds;


public class GameStarter extends Game {

	public int lang;
	public TextureAtlas atlas;
	public TextSystem textSystem;
	public Sounds sounds;
	public Fonts fonts;
	public Ingredients ingredients;
	public Potions potions;
	public Preferences prefs;
	public float w;
	public float h;
	public float pixel_size;
	public float tile_size;

	public boolean is_potion_cooked;
	public Color potion_color;

	/* Values to memorise */
	public boolean is_sound = true;
	public boolean is_music = true;

	public float music_volume = 1f; // From 0f to 1f
	public float sound_volume = 1f; // From 0f to 1f

	public int week = 1;
	public int money = 0;

	public int fight_board_size = 10;

	public int potions_cooked = 0;

	public GameStarter(int lang) { this.lang = lang; }

	@Override
	public void create () {
		setScreen(new Preview(this));
	}

	@Override
	public void dispose () {}
}

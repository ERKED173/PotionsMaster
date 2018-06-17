package ru.erked.pcook.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;

import ru.erked.pcook.GameStarter;

public class DesktopLauncher {
	public static void main (String[] arg) {
		/**/
		TexturePacker.Settings settings = new TexturePacker.Settings();
		settings.maxWidth = 2048;
		settings.maxHeight = 2048;
		TexturePacker.process(
				settings,
				"C:/Projects/Java/Potions_Cook/resource",
				"C:/Projects/Java/Potions_Cook/source/android/assets/textures",
				"textures");
		/**/
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.height = 700;
		config.width = (int)(config.height / 1.777778f);
		config.width = (int)(config.height / 1.8f);
		config.width = (int)(config.height / 1.6f);
		new LwjglApplication(new GameStarter(1), config);
	}
}

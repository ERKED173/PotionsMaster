package ru.erked.pcook.systems;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class AdvCamera extends Actor {

    private OrthographicCamera camera;

    public AdvCamera(float worldWidth, float worldHeight) {
        setSize(worldWidth, worldHeight);
        camera = new OrthographicCamera(getWidth(), getHeight());
    }

    public void update () {
        camera.position.x = getX();
        camera.position.y = getY();
        camera.viewportWidth = getWidth();
        camera.viewportHeight = getHeight();
        camera.update();
    }

    public void set (Camera camera) { this.camera = (OrthographicCamera)(camera); }
    public OrthographicCamera get () {
        return camera;
    }

}

package ru.erked.pcook.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import ru.erked.pcook.systems.AdvMap;
import ru.erked.pcook.systems.AdvSprite;

public class Entity extends AdvSprite {

    private float speed;
    private float timer = 0f;
    private Vector2 position;
    public enum Direction { LEFT, RIGHT, UP, DOWN, LEFT_UP, RIGHT_UP, LEFT_DOWN, RIGHT_DOWN }

    public Entity (Sprite s, float x, float y, float w, float h, float speed) {
        super(s, x, y, w, h);
        this.speed = speed;
        position = new Vector2((int)(x / w), (int)(y / h));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    public void move (Direction direction, AdvMap map) {
        timer += Gdx.graphics.getDeltaTime();
        if (timer > speed) {
            timer = 0f;
            Vector2 m_p = new Vector2(); // move position
            switch (direction) {
                case LEFT:  { m_p.x = position.x - 1; m_p.y = position.y; break; }
                case RIGHT: { m_p.x = position.x + 1; m_p.y = position.y; break; }
                case UP:    { m_p.x = position.x; m_p.y = position.y + 1; break; }
                case DOWN:  { m_p.x = position.x; m_p.y = position.y - 1; break; }
                case LEFT_UP:  { m_p.x = position.x - 1; m_p.y = position.y + 1; break; }
                case RIGHT_UP:  { m_p.x = position.x + 1; m_p.y = position.y + 1; break; }
                case LEFT_DOWN:  { m_p.x = position.x - 1; m_p.y = position.y - 1; break; }
                case RIGHT_DOWN:  { m_p.x = position.x + 1; m_p.y = position.y - 1; break; }
            }
            if (isPossibleToMove(m_p, map)) {
                position = m_p;
                switch (direction) {
                    case LEFT:  { addAction(Actions.moveTo(getX() - getWidth(), getY(), speed)); break; }
                    case RIGHT: { addAction(Actions.moveTo(getX() + getWidth(), getY(), speed)); break; }
                    case UP:    { addAction(Actions.moveTo(getX(), getY() + getHeight(), speed)); break; }
                    case DOWN:  { addAction(Actions.moveTo(getX(), getY() - getHeight(), speed)); break; }
                    case LEFT_UP:  { addAction(Actions.moveTo(getX() - getWidth(), getY() + getHeight(), speed)); break; }
                    case RIGHT_UP:  { addAction(Actions.moveTo(getX() + getWidth(), getY() + getHeight(), speed)); break; }
                    case LEFT_DOWN:  { addAction(Actions.moveTo(getX() - getWidth(), getY() - getHeight(), speed)); break; }
                    case RIGHT_DOWN:  { addAction(Actions.moveTo(getX() + getWidth(), getY() - getHeight(), speed)); break; }
                }
            }
        }
    }

    public boolean canInteract(Vector2 obj_p) {
        return position.x == obj_p.x && position.y == obj_p.y - 1;
    }

    public boolean canInteract(Vector2 obj_p, int length) {
        return position.x >= obj_p.x && position.x <= obj_p.x + length && position.y == obj_p.y - 1;
    }

    private boolean isPossibleToMove(Vector2 p, AdvMap map) {
        boolean is_possible = true;
        for (MapLayer layer : map.getMapLayers()) {
            TiledMapTileLayer.Cell cell = ((TiledMapTileLayer)(layer)).getCell((int) p.x, (int) p.y);
            if (cell != null) {
                if (cell.getTile().getProperties().containsKey("blocked")) {
                    is_possible = false;
                    break;
                }
            }
        }
        return is_possible;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setIntPos(Vector2 position) {
        this.position = position;
    }

    public float getSpeed() {
        return speed;
    }

}

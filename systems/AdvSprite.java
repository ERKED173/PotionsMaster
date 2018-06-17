package ru.erked.pcook.systems;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class AdvSprite extends Actor {

    Sprite sprite;
    private boolean drawPartially;
    private float p_x;
    private float p_y;
    private float p_width;
    private float p_height;
    private boolean p_flipY;

    public AdvSprite (Sprite sprite, float x, float y, float width, float height) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setOrigin(width / 2f, height / 2f);
        this.sprite = sprite;
        this.sprite.setBounds(x, y, width, height);
        this.sprite.setOriginCenter();
        drawPartially = false;
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        update();
        if (drawPartially)
            drawPart(batch, p_x, p_y, p_width, p_height);
        else
            sprite.draw(batch, parentAlpha);
    }

    private void drawPart (Batch batch, float x, float y, float width, float height) {
        batch.setColor(sprite.getColor());
        if (p_flipY) {
            batch.draw(
                    sprite.getTexture(),
                    getX(),
                    getY() + getHeight() * (1f - height),
                    getOriginX(),
                    getOriginY(),
                    getWidth() * width,
                    getHeight() * height,
                    getScaleX(),
                    getScaleY(),
                    getRotation(),
                    (int) (sprite.getRegionX() + sprite.getRegionWidth() * x),
                    (int) (sprite.getRegionY() + sprite.getRegionHeight() * y),
                    (int) (sprite.getRegionWidth() * width),
                    (int) (sprite.getRegionHeight() * height),
                    sprite.isFlipX(),
                    sprite.isFlipY()
            );
        } else {
            batch.draw(
                    sprite.getTexture(),
                    getX(),
                    getY(),
                    getOriginX(),
                    getOriginY(),
                    getWidth() * width,
                    getHeight() * height,
                    getScaleX(),
                    getScaleY(),
                    getRotation(),
                    (int) (sprite.getRegionX() + sprite.getRegionWidth() * x),
                    (int) (sprite.getRegionY() + sprite.getRegionHeight() * y + sprite.getRegionHeight() * (1f - height)),
                    (int) (sprite.getRegionWidth() * width),
                    (int) (sprite.getRegionHeight() * height),
                    sprite.isFlipX(),
                    sprite.isFlipY()
            );
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void setDrawPartially (float width, float height, boolean flipY) {
        p_x = (1f - width)  / 2f;
        p_y = (1f - height) / 2f;
        p_width = width;
        p_height = height;
        p_flipY = flipY;
        drawPartially = true;
    }

    public void setDrawPartially (float x, float y, float width, float height, boolean flipY) {
        p_x = x;
        p_y = y;
        p_width = width;
        p_height = height;
        p_flipY = flipY;
        drawPartially = true;
    }

    public void setPartialSize (float p_width, float p_height) {
        this.p_width = p_width;
        this.p_height = p_height;
    }

    public Vector2 getPartialSize () {
        return new Vector2(p_width, p_height);
    }

    public void setPartialCoordinates (float p_x, float p_y) {
        this.p_x = p_x;
        this.p_y = p_y;
    }

    public Vector2 getPartialCoordinates () {
        return new Vector2(p_x, p_y);
    }

    public void setPartialFlipY (boolean p_flipY) {
        this.p_flipY = p_flipY;
    }

    public void resetDrawPartially () { drawPartially = false; }

    private void update() {
        sprite.setBounds(getX(), getY(), getWidth(), getHeight());
        sprite.setOrigin(getOriginX(), getOriginY());
        sprite.setRotation(getRotation());
        sprite.setColor(getColor());
    }

    public Sprite getSprite () {
        return sprite;
    }
}

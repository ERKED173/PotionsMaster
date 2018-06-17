package ru.erked.pcook.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import ru.erked.pcook.GameStarter;

public class Dialog extends AdvSprite {

    private AdvSprite picture;
    private Animation<TextureRegion> picture_anim;
    private boolean is_displayed = true;
    private InGameText text;
    private Button[] buttons;
    private float state_time = 0f;

    public Dialog (GameStarter game, float x, float y, float width, String key, boolean is_key, String[] text_btn, ClickListener[] listeners) {
        super(game.atlas.createSprite("dialog"), x, y, width, 0.5f * width);
        text = new InGameText(game, game.fonts.f_15S, key, 0.9f*width, x + 0.05f*width, y + 0.2f*width, is_key);
        addButtons(game, text_btn, listeners);
    }

    public Dialog (GameStarter game, float width, String key, boolean is_key, String[] text_btn, ClickListener[] listeners) {
        this(game, 0.5f*(game.w - width), 0.5f*(game.h - 0.5f*width), width, key, is_key, text_btn, listeners);
    }

    public Dialog (GameStarter game, float x, float y, float width, String key, boolean is_key, String picture, String[] text_btn, ClickListener[] listeners) {
        super(game.atlas.createSprite("picture_dialog"), x, y, width, width);
        float picture_height = 0.5f * width;
        float picture_width = picture_height * (game.atlas.createSprite(picture).getWidth() / game.atlas.createSprite(picture).getHeight());
        this.picture = new AdvSprite(
                game.atlas.createSprite(picture),
                x + 0.5f * (width - picture_width),
                y + 0.4f * width,
                picture_width,
                picture_height
        );
        text = new InGameText(game, game.fonts.f_15S, key, 0.9f*width, x + 0.05f*width, y + 0.2f*width, is_key);
        addButtons(game, text_btn, listeners);
    }

    public Dialog (GameStarter game, float width, String key, boolean is_key, String picture, String[] text_btn, ClickListener[] listeners) {
        this(game, 0.5f*(game.w - width), 0.5f*(game.h - 0.5f*width), width, key, is_key, picture, text_btn, listeners);
    }

    public Dialog (GameStarter game, float x, float y, float width, String key, boolean is_key, String picture, float duration, Animation.PlayMode playmode, String[] text_btn, ClickListener[] listeners) {
        this(game, x, y, width, key, is_key, picture, text_btn, listeners);
        picture_anim = new Animation<>(
                duration,
                game.atlas.findRegions(picture),
                playmode
        );
    }

    public Dialog (GameStarter game, float width, String key, boolean is_key, String picture, float duration, Animation.PlayMode playmode, String[] text_btn, ClickListener[] listeners) {
        this(game, 0.5f*(game.w - width), 0.5f*(game.h - width), width, key, is_key, picture, duration, playmode, text_btn, listeners);
    }

    private void addButtons (GameStarter game, String[] text_btn, ClickListener[] listeners) {
        buttons = new Button[text_btn.length];
        for (int i = 0; i < text_btn.length; i++) {
            buttons[i] = new Button(
                    game,
                    getX()+0.5f*getWidth()-(text_btn.length%2==0?(text_btn.length/2-i)*0.21f*getWidth():(text_btn.length/2-i)*0.21f*getWidth()+0.1f*getWidth()),
                    getY() + 0.05f * getWidth(),
                    0.2f * getWidth(),
                    game.fonts.f_15.getFont(),
                    game.textSystem.get(text_btn[i]),
                    1,
                    "dialog_btn_" + (i + 1)
            );
            buttons[i].get().addListener(listeners[i]);
        }
    }

    public Button[] getButtons () {
        return buttons;
    }

    public InGameText getText() {
        return text;
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        update();
        sprite.draw(batch, parentAlpha);
        text.draw(batch, parentAlpha);
        for (Button b : buttons) {
            b.get().draw(batch, parentAlpha);
            b.get().setChecked(false);
        }
        if (picture_anim != null) {
            state_time += Gdx.graphics.getDeltaTime();
            picture.getSprite().setRegion(picture_anim.getKeyFrame(state_time, true));
        }
        if (picture != null) {
            picture.setColor(getColor());
            picture.draw(batch, parentAlpha);
        }
    }

    public void show () {
        is_displayed = true;
        setVisible(true);
        text.setVisible(true);
        if (picture != null) picture.setVisible(true);
        for (Button b : buttons) b.get().setVisible(true);
    }
    public void hide () {
        is_displayed = false;
        setVisible(false);
        text.setVisible(false);
        if (picture != null) picture.setVisible(false);
        for (Button b : buttons) b.get().setVisible(false);
    }

    public boolean isDisplayed() {
        return is_displayed;
    }

    private void update () {
        sprite.setBounds(getX(), getY(), getWidth(), getHeight());
        sprite.setRotation(getRotation());
        sprite.setColor(getColor());
    }
}

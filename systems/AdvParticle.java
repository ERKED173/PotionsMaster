package ru.erked.pcook.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.scenes.scene2d.Actor;

import ru.erked.pcook.GameStarter;

public class AdvParticle extends Actor {

    public enum Type {LINE, SINGLE, CIRCLE}

    private ParticleEffectPool.PooledEffect pooled_effect;
    private ParticleEffectPool particle_pool;

    private float scale;
    private boolean is_stopped = false;

    public AdvParticle (GameStarter game, String particle, float x, float y, float width, Type type) {
        ParticleEffect effect = new ParticleEffect();
        effect.load(Gdx.files.internal("particles/" + particle + ".p"), game.atlas);
        particle_pool = new ParticleEffectPool(effect, 1, 2);
        pooled_effect = particle_pool.obtain();
        switch (type) {
            case LINE: case CIRCLE: {
                scale = width / effect.getEmitters().first().getSpawnWidth().getHighMax();
                pooled_effect.scaleEffect(scale);
                break;
            }
            case SINGLE: {
                scale = width / effect.getEmitters().first().getXScale().getHighMax();
                pooled_effect.scaleEffect(scale);
                break;
            }
        }
        setWidth(width);
        setHeight(width);
        setX(x);
        setY(y);
    }

    public void start () {
        if (is_stopped) {
            is_stopped = false;
            pooled_effect = particle_pool.obtain();
            pooled_effect.scaleEffect(scale);
            pooled_effect.reset(false);
        }
    }

    public void stop () {
        if (!is_stopped) {
            is_stopped = true;
            pooled_effect.allowCompletion();
        }
    }

    public boolean isStopped () { return is_stopped; }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float temp[] = pooled_effect.getEmitters().first().getTint().getColors();
        temp[0] = getColor().r;
        temp[1] = getColor().g;
        temp[2] = getColor().b;
        if (!pooled_effect.isComplete()) pooled_effect.draw(batch, Gdx.graphics.getDeltaTime());
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        pooled_effect.setPosition(getX(), getY());
    }

}

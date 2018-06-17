package ru.erked.pcook.systems;

import com.badlogic.gdx.math.MathUtils;

public class Ingredient {

    private String id;
    private String name;
    private String description;
    private int cost;
    private float dark_light;
    private float taste;
    private float vigorousness;
    private float effect;
    private boolean isAvailable;

    public Ingredient (String id, String name, String description, int cost, float dark_light, float taste, float vigorousness, float effect) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.dark_light = dark_light;
        this.taste = taste;
        this.vigorousness = vigorousness;
        this.effect = effect;
        this.isAvailable = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public float getDark_light() {
        return dark_light;
    }

    public void setDark_light(float dark_light) {
        this.dark_light = dark_light;
    }

    public float getTaste() {
        return taste;
    }

    public void setTaste(float taste) {
        this.taste = taste;
    }

    public float getVigorousness() {
        return vigorousness;
    }

    public void setVigorousness(float vigorousness) {
        this.vigorousness = vigorousness;
    }

    public float getEffect() {
        return effect;
    }

    public void setEffect(float effect) {
        this.effect = effect;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public int getPartIndex (int n) {
        switch (n) {
            case 0: {
                if (dark_light >= 25)
                    return MathUtils.random(11) + 12;
                else if (dark_light < 25 && dark_light > -25)
                    return MathUtils.random(23);
                else
                    return MathUtils.random(11);
            }
            case 1: {
                if (taste >= 66)
                    return MathUtils.random(11) + 12;
                else if (taste < 66 && taste > 33)
                    return MathUtils.random(23);
                else
                    return MathUtils.random(11);
            }
            case 2: {
                if (vigorousness >= 66)
                    return MathUtils.random(11) + 12;
                else if (vigorousness < 66 && vigorousness > 33)
                    return MathUtils.random(23);
                else
                    return MathUtils.random(11);
            }
            case 3: {
                if (effect >= 66)
                    return MathUtils.random(11) + 12;
                else if (effect < 66 && effect > 33)
                    return MathUtils.random(23);
                else
                    return MathUtils.random(11);
            }
            default: {
                return 0;
            }
        }
    }
}

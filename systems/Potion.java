package ru.erked.pcook.systems;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

import ru.erked.pcook.GameStarter;

public class Potion {

    private int[] int_name;
    private String[] ingredients;
    private int date;
    private int bottle;

    /** Constructor for the cases when we get information from the memory */
    public Potion (String[] ingredients, int[] int_name, int date, int bottle) {
        this.int_name = int_name;
        this.ingredients = ingredients;
        this.date = date;
        this.bottle = bottle;
    }

    /** Constructor for cases when we create new potion */
    public Potion (GameStarter g, String[] ingredients, int date, int bottle) {
        this.ingredients = ingredients;
        this.date = date;
        this.bottle = bottle;
        int_name = new int[4];

        ArrayList<Ingredient> ing = new ArrayList<>();
        for (String s : ingredients) {
            for (Ingredient i : g.ingredients.list) {
                if (s.equals(i.getId())) ing.add(i);
            }
        }

        int rand = MathUtils.random(3);
        int_name[0] = ing.get(rand).getPartIndex(0);
        ing.remove(rand);
        rand = MathUtils.random(2);
        int_name[1] = ing.get(rand).getPartIndex(1);
        ing.remove(rand);
        rand = MathUtils.random(1);
        int_name[2] = ing.get(rand).getPartIndex(2);
        ing.remove(rand);
        rand = 0;
        int_name[3] = ing.get(rand).getPartIndex(3);
        ing.remove(rand);
    }

    public String[] getCenteredName(GameStarter g) {
        String[] centered_name = new String[3];

        StringBuilder sb = new StringBuilder();
        sb.append(g.textSystem.get("potion_dl_" + int_name[0])).append(" ")
            .append(g.textSystem.get("potion_t_" + int_name[1]));
        centered_name[0] = sb.toString();
        centered_name[1] = g.textSystem.get("potion_of");
        sb = new StringBuilder();
        sb.append(g.textSystem.get("potion_v_" + int_name[2])).append(" ")
            .append(g.textSystem.get("potion_e_" + int_name[3]));
        centered_name[2] = sb.toString();

        return centered_name;
    }

    public int[] getIntName() {
        return int_name;
    }

    public String[] getIngredients() {
        return ingredients;
    }

    public int getDate() {
        return date;
    }

    public int getBottle() {
        return bottle;
    }

    public String getPlainName(GameStarter g) {
        return g.textSystem.get("potion_dl_" + int_name[0]) + " " +
                g.textSystem.get("potion_t_" + int_name[1]) + " " +
                g.textSystem.get("potion_of") + " " +
                g.textSystem.get("potion_v_" + int_name[2]) + " " +
                g.textSystem.get("potion_e_" + int_name[3]);
    }
}

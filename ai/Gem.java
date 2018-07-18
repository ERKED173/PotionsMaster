package ru.erked.pcook.ai;

public class Gem {

    private boolean is_ai;
    private GemColor color;
    private GemForm form;

    public Gem (boolean is_ai, GemColor color, GemForm form) {
        this.is_ai = is_ai;
        this.color = color;
        this.form = form;
    }

    public Gem (Gem another) {
        this(another.isIsAI(), another.getColor(), another.getForm());
    }

    public boolean isIsAI() { return is_ai; }

    public GemForm getForm() { return form; }
    public GemColor getColor() { return color; }

    public boolean equals (Gem g) {
        return g != null &&
                is_ai == g.is_ai &&
                color.equals(g.color) &&
                form.equals(g.form);
    }

    public String getPath () {
        return "gem_" + color.toString().toLowerCase() + "_" +
                form.toString().toLowerCase() + "_" + (is_ai ? "r" : "b");
    }

    public enum GemColor {
        RED,
        BLUE,
        GREEN,
        YELLOW,
        SKY,
        ORANGE,
        PINK,
        PURPLE
    }
    public enum GemForm {
        CIRCLE,
        SQUARE,
        TRIANGLE,
        CROSS,
        RING,
        DOTS,
        GRID,
        RHOMBUS
    }

}

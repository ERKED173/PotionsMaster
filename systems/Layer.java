package ru.erked.pcook.systems;

public class Layer {

    private String name;
    private long[][] tiles;

    Layer(String name, long[][] tiles) {
        this.name = name;
        this.tiles = tiles;
    }

    public String getName () { return name; }

    public String getData() {
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < 64; ++i) {
            for (int j = 0; j < 64; ++j) {
                data.append(tiles[i][j]).append(',');
            }
            data.append("\n");
        }
        data.deleteCharAt(data.toString().length() - 2);
        return data.toString();
    }

    public long[][] getTiles() {
        return tiles;
    }
}

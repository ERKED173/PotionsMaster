package ru.erked.pcook.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

import java.util.ArrayList;

public class AdvMap extends Actor {

    private ArrayList<Layer> layers;
    private AdvCamera camera;
    private TiledMap map;
    private OrthogonalTiledMapRenderer map_renderer;

    /** Constructor for loading maps from the memory */
    public AdvMap(String name, Stage stage, float unitScale) {
        map = new TmxMapLoader().load("maps/" + name + ".tmx");
        map_renderer = new OrthogonalTiledMapRenderer(map, unitScale, stage.getBatch());
        camera = new AdvCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.set(stage.getCamera());
    }

    /** Constructor for map generation and then loading it from the memory */
    public AdvMap(Stage stage, float unitScale) {
        layers = new ArrayList<>();
        String name = generate();

        /* Create local files for the map */
        FileHandle temp = Gdx.files.internal("maps/test_tileset.tsx");
        FileHandle handle = Gdx.files.local("maps/test_tileset.tsx");
        handle.writeString(temp.readString(), false);

        temp = Gdx.files.internal("maps/tiles.png");
        handle = Gdx.files.local("maps/tiles.png");
        handle.writeBytes(temp.readBytes(), false);

        handle = Gdx.files.local("maps/" + name + ".tmx"); // Map file
        write_map(handle);

        /* Creation of the map as usual */
        map = new TmxMapLoader().load("maps/" + name + ".tmx");
        map_renderer = new OrthogonalTiledMapRenderer(map, unitScale, stage.getBatch());
        camera = new AdvCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.set(stage.getCamera());
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        camera.setPosition(getX(), getY());
        camera.update();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        map_renderer.setView(camera.get());
        Color old_color = map_renderer.getBatch().getColor();
        map_renderer.getBatch().end();
        map_renderer.getBatch().setColor(getColor());
        map_renderer.render();
        map_renderer.getBatch().setColor(old_color);
        map_renderer.getBatch().begin();
    }

    public TiledMapTileLayer getLayer (String layer) {
        return (TiledMapTileLayer) map.getLayers().get(layer);
    }

    public MapLayers getMapLayers() {
        return map.getLayers();
    }

    private String generate () {
        layers.add(new Layer("ground", ground_generation()));
        layers.add(new Layer("trees", trees_generation(50)));
        layers.add(new Layer("big_trees", big_trees_generation(25)));
        return "test_name"; // TODO: give a map (file as well) a name
    }
    private long[][] ground_generation() {
        /* IMPORTANT */
        int points = 10; // Number of points for the algorithm
        int[] ground_types = new int[] {1, 2, 3, 4, 5}; // indexes of the tiles
        /* IMPORTANT */

        // Initialization
        long[][] values = new long[64][64];
        for (int i = 0; i < 64; ++i) {
            for (int j = 0; j < 64; ++j) {
                values[i][j] = 1;
            }
        }

        // Tiles that will be the ground
        // And random points on the map
        Vector3[] v = new Vector3[points * ground_types.length];
        for (int i = 0; i < ground_types.length; ++i) {
            for (int j = 0; j < points; ++j) {
                v[i * points + j] = new Vector3(MathUtils.random(63), MathUtils.random(63), ground_types[i]);
            }
        }

        // Algorithm (nearest)
        for (int i = 0; i < 64; ++i) {
            for (int j = 0; j < 64; ++j) {
                int best_vector = 0;
                double min_distance = Double.MAX_VALUE;
                for (int k = 0; k < ground_types.length * points; ++k) {
                    double d = (Math.sqrt( (j - v[k].x)*(j - v[k].x) + (i - v[k].y)*(i - v[k].y) ));
                    if ( d < min_distance ) {
                        best_vector = k;
                        min_distance = d;
                    }
                }
                values[i][j] = (int)(v[best_vector].z);
            }
        }

        return values;
    }
    private long[][] trees_generation (int trees_n) {
        //

        /* New vector - new type of tree */
        Vector2[] trees_tiles = new Vector2[]{
                new Vector2(117, 53),
                new Vector2(118, 54),
                new Vector2(119, 55),
                new Vector2(120, 56),
        };
        long[][] values = new long[64][64];

        for (int i = 0; i < 64; ++i) {
            for (int j = 0; j < 64; ++j) {
                values[i][j] = 0;
            }
        }

        int add_trees = MathUtils.random(trees_n / 2);
        for (int i = 0; i < trees_n + add_trees; ++i) {
            Vector2 p = new Vector2(MathUtils.random(60) + 1, MathUtils.random(60) + 2);
            int rand_tree = MathUtils.random(trees_tiles.length - 1);

            if (values[(int)(p.x)][(int)(p.y)] == 0 && values[(int)(p.x + 1)][(int)(p.y)] == 0) {
                values[(int)(p.x + 1)][(int)(p.y)] = (int)(trees_tiles[rand_tree].x);
                values[(int)(p.x)][(int)(p.y)] = (int)(trees_tiles[rand_tree].y);

                // Mirrored or not
                if (MathUtils.randomBoolean()) {
                    values[(int)(p.x + 1)][(int)(p.y)] += Math.pow(2, 31);
                    values[(int)(p.x)][(int)(p.y)] += Math.pow(2, 31);
                }
            }

        }

        return values;
    }
    private long[][] big_trees_generation (int trees_n) {
        //
        Vector2[] trees_tiles = new Vector2[]{
                new Vector2(121, 57)
        };
        long[][] values = new long[64][64];
        for (int i = 0; i < 64; ++i) {
            for (int j = 0; j < 64; ++j) {
                values[i][j] = 0;
            }
        }

        int add_trees = MathUtils.random(trees_n / 2);
        for (int i = 0; i < trees_n + add_trees; ++i) {
            Vector2 p = new Vector2(MathUtils.random(60) + 1, MathUtils.random(52) + 4);
            int rand_tree = MathUtils.random(trees_tiles.length - 1);

            if (values[(int)(p.x)][(int)(p.y)] == 0 &&
                    values[(int)(p.x + 1)][(int)(p.y)] == 0 &&
                    values[(int)(p.x)][(int)(p.y + 1)] == 0 &&
                    values[(int)(p.x + 1)][(int)(p.y + 1)] == 0 &&
                    layers.get(1).getTiles()[(int)(p.x)][(int)(p.y)] == 0 &&
                    layers.get(1).getTiles()[(int)(p.x + 1)][(int)(p.y)] == 0 &&
                    layers.get(1).getTiles()[(int)(p.x)][(int)(p.y + 1)] == 0 &&
                    layers.get(1).getTiles()[(int)(p.x + 1)][(int)(p.y + 1)] == 0) {

                values[(int)(p.x)][(int)(p.y)] = (int)(trees_tiles[rand_tree].y);
                values[(int)(p.x + 1)][(int)(p.y)] = (int)(trees_tiles[rand_tree].x);
                values[(int)(p.x)][(int)(p.y + 1)] = (int)(trees_tiles[rand_tree].y + 1);
                values[(int)(p.x + 1)][(int)(p.y + 1)] = (int)(trees_tiles[rand_tree].x + 1);

                // Mirrored or not
                if (MathUtils.randomBoolean()) {
                    values[(int)(p.x)][(int)(p.y)] = (int)(trees_tiles[rand_tree].y + 1);
                    values[(int)(p.x)][(int)(p.y)] += Math.pow(2, 31);
                    values[(int)(p.x + 1)][(int)(p.y)] = (int)(trees_tiles[rand_tree].x + 1);
                    values[(int)(p.x + 1)][(int)(p.y)] += Math.pow(2, 31);
                    values[(int)(p.x)][(int)(p.y + 1)] = (int)(trees_tiles[rand_tree].y);
                    values[(int)(p.x)][(int)(p.y + 1)] += Math.pow(2, 31);
                    values[(int)(p.x + 1)][(int)(p.y + 1)] = (int)(trees_tiles[rand_tree].x);
                    values[(int)(p.x + 1)][(int)(p.y + 1)] += Math.pow(2, 31);

                }
            }
        }

        return values;
        //
    }

    public void addLayer (Layer layer) { layers.add(layer); }

    public Layer getLayer (int i) { return layers.get(i); }

    public int getLayerNumber () { return layers.size(); }

    public ArrayList<Layer> getLayers() {
        return layers;
    }

    private void write_map (FileHandle handle) {
        //
        handle.writeString("", false);
        handle.writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", true);
        handle.writeString("<map version=\"1.0\" tiledversion=\"1.1.5\" orientation=\"orthogonal\" " +
                "renderorder=\"right-down\" width=\"64\" height=\"64\" tilewidth=\"16\" tileheight=\"16\" " +
                "infinite=\"0\" nextobjectid=\"1\">\n", true);
        handle.writeString("    <tileset firstgid=\"1\" source=\"test_tileset.tsx\"/>\n", true);
        for (Layer l : layers) {
            handle.writeString("    <layer name=\"", true);
            handle.writeString(l.getName(), true);
            handle.writeString("\" width=\"64\" height=\"64\">\n", true);
            handle.writeString("        <data encoding=\"csv\">\n", true);
            handle.writeString(l.getData(), true);
            handle.writeString("        </data>\n", true);
            handle.writeString("    </layer>\n", true);
        }
        handle.writeString("</map>\n", true);
        //
    }

    public void dispose () {
        map.dispose();
        map_renderer.dispose();
    }
}

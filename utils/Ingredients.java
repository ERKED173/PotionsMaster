package ru.erked.pcook.utils;

import java.util.ArrayList;

import ru.erked.pcook.GameStarter;
import ru.erked.pcook.systems.Ingredient;

public class Ingredients {

    public Ingredient ashLeaf;
    public Ingredient swampMushroom;
    public Ingredient blueMoss;
    public Ingredient hornedGrasshopper;
    public Ingredient birchBark;
    public Ingredient obliqueGrass;
    public Ingredient beetleHearth;
    public Ingredient silverApple;
    public Ingredient lightButterfly;
    public Ingredient friendlyCherry;
    public Ingredient treeflowerSeeds;
    public Ingredient forestFairyPollen;
    public Ingredient spiderSting;
    public Ingredient demonTear;
    public Ingredient blackDrone;
    public Ingredient firepineLeaf;
    public Ingredient madCaterpillarHorn;
    public Ingredient northCannibalFur;
    public Ingredient poisonousStrawberry;
    public Ingredient arrowheadOfBandurOrc;
    public Ingredient undergroundRatSkull;
    public Ingredient afterlifeBloodDrop;
    public Ingredient waterGiantFoot;
    public Ingredient woodGoblinCross;

    public ArrayList<Ingredient> list;
    public ArrayList<Ingredient> inventory;
    public ArrayList<Ingredient> added;

    public Ingredients (GameStarter game) {
        list = new ArrayList<>();
        inventory = new ArrayList<>();
        added = new ArrayList<>();

        ashLeaf = new Ingredient("ash_leaf", game.textSystem.get("ash_leaf"), "ash_leaf_d",
                0, 15f, 5f, 0f, 7.5f);
        swampMushroom = new Ingredient("swamp_mushroom", game.textSystem.get("swamp_mushroom"), "swamp_mushroom_d",
                0, 10f, 35f, 27.5f, 12.5f);
        blueMoss = new Ingredient("blue_moss", game.textSystem.get("blue_moss"), "blue_moss_d",
                0, 10f, 0f, 35f, 17.5f);
        hornedGrasshopper = new Ingredient("horned_grasshopper", game.textSystem.get("horned_grasshopper"), "horned_grasshopper_d",
                0, 15f, 22.5f, 25f, 12.5f);
        birchBark = new Ingredient("birch_bark", game.textSystem.get("birch_bark"), "birch_bark_d",
                0, 17.5f, 2.5f, 7.5f, 10f);
        obliqueGrass = new Ingredient("oblique_grass", game.textSystem.get("oblique_grass"), "oblique_grass_d",
                0, 7.5f, 2.5f, 15f, 32.5f);
        beetleHearth = new Ingredient("beetle_hearth", game.textSystem.get("beetle_hearth"), "beetle_hearth_d",
                0, 15f, 42.5f, 22.5f, 15f);
        silverApple = new Ingredient("silver_apple", game.textSystem.get("silver_apple"), "silver_apple_d",
                0, 45f, 50f, 32.5f, 27.5f);
        lightButterfly = new Ingredient("light_butterfly", game.textSystem.get("light_butterfly"), "light_butterfly_d",
                0, 35f, 5f, 7.5f, 27.5f);
        friendlyCherry = new Ingredient("friendly_cherry", game.textSystem.get("friendly_cherry"), "friendly_cherry_d",
                0, 27.5f, 65f, 37.5f, 40f);
        treeflowerSeeds = new Ingredient("treeflower_seeds", game.textSystem.get("treeflower_seeds"), "treeflower_seeds_d",
                0, 25f, 12.5f, 30f, 22.5f);
        forestFairyPollen = new Ingredient("forest_fairy_pollen", game.textSystem.get("forest_fairy_pollen"), "forest_fairy_pollen_d",
                0, 52.5f, 77.5f, 2.5f, 37.5f);
        spiderSting = new Ingredient("spider_sting", game.textSystem.get("spider_sting"), "spider_sting_d",
                0, -10f, 15f, 45f, 32.5f);
        demonTear = new Ingredient("demon_tear", game.textSystem.get("demon_tear"), "demon_tear_d",
                0, -77.5f, 17.5f, 55f, 87.5f);
        blackDrone = new Ingredient("black_drone", game.textSystem.get("black_drone"), "black_drone_d",
                0, -12.5f, 10f, 7.5f, 5f);
        firepineLeaf = new Ingredient("firepine_leaf", game.textSystem.get("firepine_leaf"), "firepine_leaf_d",
                0, -25f, 7.5f, 55f, 32.5f);
        madCaterpillarHorn = new Ingredient("mad_caterpillar_horn", game.textSystem.get("mad_caterpillar_horn"), "mad_caterpillar_horn_d",
                0, -15f, 2.5f, 10f, 17.5f);
        northCannibalFur = new Ingredient("north_cannibal_fur", game.textSystem.get("north_cannibal_fur"), "north_cannibal_fur_d",
                0, -45f, 0f, 65f, 42.5f);
        poisonousStrawberry = new Ingredient("poisonous_strawberry", game.textSystem.get("poisonous_strawberry"), "poisonous_strawberry_d",
                0, -27.5f, 82.5f, 7.5f, 40f);
        arrowheadOfBandurOrc = new Ingredient("arrowhead_of_bandur_orc", game.textSystem.get("arrowhead_of_bandur_orc"), "arrowhead_of_bandur_orc_d",
                0, -65f, 0f, 10f, 77.5f);
        undergroundRatSkull = new Ingredient("underground_rat_skull", game.textSystem.get("underground_rat_skull"), "underground_rat_skull_d",
                0, -15f, 10f, 5f, 27.5f);
        afterlifeBloodDrop = new Ingredient("afterlife_blood_drop", game.textSystem.get("afterlife_blood_drop"), "afterlife_blood_drop_d",
                0, -62.5f, 35f, 47.5f, 32.5f);
        waterGiantFoot = new Ingredient("water_giant_foot", game.textSystem.get("water_giant_foot"), "water_giant_foot_d",
                0, -57.5f, 50f, 27.5f, 30f);
        woodGoblinCross = new Ingredient("wood_goblin_cross", game.textSystem.get("wood_goblin_cross"), "wood_goblin_cross_d",
                0, -65f, 5f, 42.5f, 22.5f);

        arrowheadOfBandurOrc.setAvailable(true);
        blackDrone.setAvailable(true);
        waterGiantFoot.setAvailable(true);
        afterlifeBloodDrop.setAvailable(true);
        firepineLeaf.setAvailable(true);
        spiderSting.setAvailable(true);
        beetleHearth.setAvailable(true);
        blueMoss.setAvailable(true);

        list.add(ashLeaf);
        list.add(blueMoss);
        list.add(demonTear);
        list.add(birchBark);
        list.add(blackDrone);
        list.add(spiderSting);
        list.add(silverApple);
        list.add(firepineLeaf);
        list.add(obliqueGrass);
        list.add(beetleHearth);
        list.add(swampMushroom);
        list.add(lightButterfly);
        list.add(friendlyCherry);
        list.add(waterGiantFoot);
        list.add(treeflowerSeeds);
        list.add(woodGoblinCross);
        list.add(northCannibalFur);
        list.add(forestFairyPollen);
        list.add(hornedGrasshopper);
        list.add(afterlifeBloodDrop);
        list.add(madCaterpillarHorn);
        list.add(poisonousStrawberry);
        list.add(undergroundRatSkull);
        list.add(arrowheadOfBandurOrc);
    }

}

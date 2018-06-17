package ru.erked.pcook.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

public class TextSystem {

    private Map<String, String> hash_map;

    public TextSystem(int lang){
        String text;
        switch (lang){
            case 0: {
                FileHandle textFile = Gdx.files.internal("lang/EN.txt");
                text = textFile.readString();
                break;
            }
            case 1: {
                FileHandle textFile = Gdx.files.internal("lang/RU.txt");
                text = textFile.readString();
                break;
            }
            default:{
                text = "null";
            }
        }
        hash_map = new HashMap<>();
        int i = 0;
        boolean is_key = true;
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        while (i < text.length()) {
            if (is_key) {
                if (text.charAt(i) == '=') {
                    is_key = false;
                } else {
                    if (text.charAt(i) != '\n' && text.charAt(i) != '\r') key.append(text.charAt(i));
                }
            } else {
                if (text.charAt(i) == '☺') {
                    is_key = true;
                    hash_map.put(key.toString(), value.toString());
                    //System.out.println(key.toString() + "_" + value.toString());
                    key = new StringBuilder(); value = new StringBuilder();
                } else {
                    if (text.charAt(i) != '\n' && text.charAt(i) != '\r') value.append(text.charAt(i));
                }
            }
            i++;
        }
    }

    public String get(String key){
        return hash_map.get(key) == null ? "null" : hash_map.get(key);
    }

}

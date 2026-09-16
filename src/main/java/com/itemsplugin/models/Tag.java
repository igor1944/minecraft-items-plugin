package com.itemsplugin.models;

import org.bukkit.Color;

public class Tag {

    private String id;
    private String name;
    private Color color;

    public Tag(String id, String name, Color color) {
        this.id = id;
        this.name = name == null ? "" : name;
        this.color = color == null ? Color.WHITE : color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color == null ? Color.WHITE : color;
    }

    public String getColorHex() {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}

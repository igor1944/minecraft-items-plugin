package com.itemsplugin.models;

import org.bukkit.Color;

public class TagDraft {

    private String originalId;
    private String id = "new_tag";
    private String name = "Новый тег";
    private Color color = Color.WHITE;

    public static TagDraft fromTag(Tag tag) {
        TagDraft draft = new TagDraft();
        draft.originalId = tag.getId();
        draft.id = tag.getId();
        draft.name = tag.getName();
        draft.color = tag.getColor();
        return draft;
    }

    public String getOriginalId() {
        return originalId;
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
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}

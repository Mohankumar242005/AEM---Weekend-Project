package com.weekend.core.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

@Model(adaptables = Resource.class)
public class CharacterPanelModel {

    @SlingObject
    private Resource currentResource;

    private List<Resource> characters;

    @PostConstruct
    protected void init() {
        characters = new ArrayList<>();
        Resource charactersNode = currentResource.getChild("characters");
        if (charactersNode != null) {
            for (Resource child : charactersNode.getChildren()) {
                characters.add(child);
            }
        }
    }

    public List<Resource> getCharacters() {
        return Collections.unmodifiableList(characters);
    }
}

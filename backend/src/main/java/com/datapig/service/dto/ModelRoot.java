package com.datapig.service.dto; 

import com.datapig.service.dto.ModelEntity;

import java.util.List;

public class ModelRoot {

    private String name;
    private String description;
    private String version;
    private List<ModelEntity> entities;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public List<ModelEntity> getEntities() {
        return entities;
    }
    public void setEntities(List<ModelEntity> entities) {
        this.entities = entities;
    }

   
}
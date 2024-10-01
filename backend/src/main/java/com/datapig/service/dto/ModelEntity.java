package com.datapig.service.dto; 

import com.google.gson.annotations.SerializedName;
import java.util.List;
import com.datapig.service.dto.ModelAnnotation;
import com.datapig.service.dto.ModelAttribute;
public class ModelEntity {

    @SerializedName("$type")
    private String type;

    private String name;
    private String description;
    private List<ModelAnnotation> annotations;
    private List<ModelAttribute> attributes;
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
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
    public List<ModelAnnotation> getAnnotations() {
        return annotations;
    }
    public void setAnnotations(List<ModelAnnotation> annotations) {
        this.annotations = annotations;
    }
    public List<ModelAttribute> getAttributes() {
        return attributes;
    }
    public void setAttributes(List<ModelAttribute> attributes) {
        this.attributes = attributes;
    }

    
}

package com.datapig.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;


class Annotation {
    private String name;
    private String value;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}

class TraitArgument {
    private String name;
    private String value;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}

class Trait {
    @SerializedName("traitReference")
    private String traitReference;
    private List<TraitArgument> arguments;

    public String getTraitReference() { return traitReference; }
    public void setTraitReference(String traitReference) { this.traitReference = traitReference; }

    public List<TraitArgument> getArguments() { return arguments; }
    public void setArguments(List<TraitArgument> arguments) { this.arguments = arguments; }
}

class Attribute {
    private String name;
    private String dataType;
    private int maxLength;
    @SerializedName("cdm:traits")
    private List<Trait> traits;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }

    public int getMaxLength() { return maxLength; }
    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }

    public List<Trait> getTraits() { return traits; }
    public void setTraits(List<Trait> traits) { this.traits = traits; }
}

class Entity {
    @SerializedName("$type")
    private String type;
    private String name;
    private String description;
    private List<Annotation> annotations;
    private List<Attribute> attributes;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Annotation> getAnnotations() { return annotations; }
    public void setAnnotations(List<Annotation> annotations) { this.annotations = annotations; }

    public List<Attribute> getAttributes() { return attributes; }
    public void setAttributes(List<Attribute> attributes) { this.attributes = attributes; }
}

class Root {
    private String name;
    private String description;
    private String version;
    private List<Entity> entities;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public List<Entity> getEntities() { return entities; }
    public void setEntities(List<Entity> entities) { this.entities = entities; }
}
package com.datapig.service.dto; 

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ModelTrait {

    @SerializedName("traitReference")
    private String traitReference;

    private List<ModelTraitArgument> arguments;

    public String getTraitReference() {
        return traitReference;
    }

    public void setTraitReference(String traitReference) {
        this.traitReference = traitReference;
    }

    public List<ModelTraitArgument> getArguments() {
        return arguments;
    }

    public void setArguments(List<ModelTraitArgument> arguments) {
        this.arguments = arguments;
    }

   
}




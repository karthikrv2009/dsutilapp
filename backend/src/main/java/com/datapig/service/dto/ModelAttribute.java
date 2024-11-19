
package com.datapig.service.dto; 

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ModelAttribute {

    private String name;
    private String dataType;
    private int maxLength;

    @SerializedName("cdm:traits")
    private List<ModelTrait> traits;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public List<ModelTrait> getTraits() {
        return traits;
    }

    public void setTraits(List<ModelTrait> traits) {
        this.traits = traits;
    }

  
}
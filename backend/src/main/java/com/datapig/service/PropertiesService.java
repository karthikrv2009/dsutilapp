package com.datapig.service;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datapig.service.dto.ModelRoot;
import com.datapig.service.dto.ModelEntity;
import com.datapig.service.dto.ModelAttribute;
import com.datapig.service.dto.ModelTrait;
import com.datapig.service.dto.ModelTraitArgument;
import com.datapig.service.dto.ModelTable;
import com.datapig.service.dto.ModelTableAttributes;
import org.springframework.stereotype.Service;

import com.datapig.repository.PropertiesRepository;

import org.springframework.beans.factory.annotation.Autowired;
import com.datapig.entity.ApplicationProperty;

@Service
public class PropertiesService {


    @Autowired
    private PropertiesRepository propertiesRepository;

    public void saveProperties(Map<String, String> properties) {
        properties.forEach((key, value) -> {
            ApplicationProperty property = propertiesRepository.findByPropertyName(key)
                    .orElse(new ApplicationProperty(key, value));
            property.setPropertyValue(value);
            propertiesRepository.save(property);
        });
    }
    
    public Map<String, String> getAllProperties() {
        List<ApplicationProperty> propertyList = propertiesRepository.findAll();
        Map<String, String> propertiesMap = new HashMap<>();
        for (ApplicationProperty property : propertyList) {
            propertiesMap.put(property.getPropertyName(), property.getPropertyValue());
        }
        return propertiesMap;
    }

    public String getPropertyValue(String propertyName) {
        return propertiesRepository.findByPropertyName(propertyName)
                .map(ApplicationProperty::getPropertyValue)
                .orElseThrow(() -> new RuntimeException("Property not found: " + propertyName));
    }


}

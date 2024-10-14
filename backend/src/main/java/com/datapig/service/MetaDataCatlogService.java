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

import com.datapig.repository.MetaDataCatlogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import com.datapig.entity.MetaDataCatlog;

@Service
public class MetaDataCatlogService {

    @Autowired
    private MetaDataCatlogRepository metaDataCatlogRepository;

    public List<MetaDataCatlog> findAll() {
        return metaDataCatlogRepository.findAll();
    }

    public MetaDataCatlog save(MetaDataCatlog metaDataCatlog) {
        return metaDataCatlogRepository.save(metaDataCatlog);
    }
}

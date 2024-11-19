package com.datapig.service;

import org.springframework.stereotype.Service;

import com.datapig.repository.MetaDataPointerRepository;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.TreeSet;
import java.util.Comparator;

import com.datapig.entity.MetaDataPointer;

@Service
public class MetaDataPointerService {

    @Autowired
    private MetaDataPointerRepository metaDataPointerRepository;

    public List<MetaDataPointer> findAll() {
        return metaDataPointerRepository.findAll();
    }

    public MetaDataPointer save(MetaDataPointer metaDataPointer) {
        return metaDataPointerRepository.save(metaDataPointer);
    }


    public TreeSet<MetaDataPointer> getMetaDataPointerBystageStatus(Short stageStatus) {
        List<MetaDataPointer> entityOptional = metaDataPointerRepository.findBystageStatus(stageStatus);
        TreeSet<MetaDataPointer> metaDataPointer=new TreeSet<>(Comparator.comparing(MetaDataPointer::getAdlscreationtimestamp).thenComparing(MetaDataPointer::getFolderName));
        metaDataPointer.addAll(entityOptional);
        return metaDataPointer;
    }

    public MetaDataPointer getMetaDataPointer(String folder){
       return metaDataPointerRepository.findByfolderName(folder);
    }

}


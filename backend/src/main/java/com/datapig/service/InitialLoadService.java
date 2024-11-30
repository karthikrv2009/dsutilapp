package com.datapig.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datapig.entity.IntialLoad;
import com.datapig.repository.IntitalLoadRepository;


@Service
public class InitialLoadService {
    @Autowired
    IntitalLoadRepository intitalLoadRepository;
    
    public IntialLoad getIntialLoad(String name){
        return intitalLoadRepository.findByName(name);
    }

    public IntialLoad save(IntialLoad initialLoad){
        return intitalLoadRepository.save(initialLoad);
    }
}

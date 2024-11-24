package com.datapig.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datapig.repository.IntitalLoadRepository;


@Service
public class InitialLoadService {
    @Autowired
    IntitalLoadRepository intitalLoadRepository;
    
}

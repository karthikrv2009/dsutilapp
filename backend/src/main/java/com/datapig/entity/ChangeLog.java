package com.datapig.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "ChangeLog")
public class ChangeLog {

    @Id
    @Column(name = "foldername", length = 50, nullable = false)
    private String folderName;

    @Column(name = "stagestatus", nullable = false)
    private Short stageStatus;

    @Column(name = "stagetime")
    private LocalDateTime stageTime;

    // Getters and Setters
}


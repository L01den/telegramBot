package com.example.SpringBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@Entity(name = "salary")
public class Salary {
        @Id
        private LocalDate date;
//        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int money;
//        private String userName;
}

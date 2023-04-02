package com.example.SpringBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity(name = "Joke")
public class Joke {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;
        private String jokeText;

}

package com.example.SpringBot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Entity(name = "joke")
public class Joke {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;
        @Column(name = "text")
        private String jokeText;

        public Joke(String jokeText) {
                this.jokeText = jokeText;
        }
}

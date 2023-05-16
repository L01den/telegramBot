package com.example.SpringBot.service;

import com.example.SpringBot.model.Joke;
import com.example.SpringBot.repository.JokeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Service
@Transactional(readOnly = true)
public class JokeServices {
    @Autowired
    private final JokeRepository jokeRepository;

    public JokeServices(JokeRepository jokeRepository) {
        this.jokeRepository = jokeRepository;
    }

    public String getJoke(){
        int countJoke = 15;
        Random random = new Random();
        Optional<Joke> joke = jokeRepository.findById(random.nextInt(countJoke)+1);
        String text = "";
        if(joke.isPresent()){
            text = joke.get().getJokeText();
        }
        return text;
    }
}

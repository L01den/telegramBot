package com.example.SpringBot.service;

import com.example.SpringBot.dataBase.Dao;
import com.example.SpringBot.model.Joke;

import java.util.Random;

public class Command {
    public String getJoke(){
        Dao dao = new Dao();
        Joke jokes = dao.findJoke(3);
        String answer = jokes.getJokeText();
        return answer;
    }
}

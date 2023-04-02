package com.example.SpringBot.dataBase;



import com.example.SpringBot.model.Joke;
import com.example.SpringBot.model.Salary;
import com.example.SpringBot.model.User;


import java.util.List;


public class Dao {

    public Salary findById(int id) {
        return HibernateSessionFactoryUtil.getSessionFactory().openSession().get(Salary.class, id);

    }
    public User findBy(int id) {
        return HibernateSessionFactoryUtil.getSessionFactory().openSession().get(User.class, id);

    }

    public Joke findJoke(int id){
//        List<String> jokes = HibernateSessionFactoryUtil.getSessionFactory().openSession().createQuery("Select jokeText From Joke").list();
        Joke joke = HibernateSessionFactoryUtil.getSessionFactory().openSession().get(Joke.class, id);
        return joke;
    }

    public List<Salary> findAll() {
        List<Salary> sal = (List<Salary>)  HibernateSessionFactoryUtil.getSessionFactory().openSession().createQuery("Select money From salary").list();
        return sal;
    }

}

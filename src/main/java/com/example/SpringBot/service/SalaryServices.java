package com.example.SpringBot.service;

import com.example.SpringBot.model.Salary;
import com.example.SpringBot.repository.SalaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SalaryServices{

    @Autowired
    public final SalaryRepository salaryRepository;

    public SalaryServices(SalaryRepository salaryRepository) {
        this.salaryRepository = salaryRepository;
    }

    @Transactional
    public int calculateSalary(long chatId, String revenue, Message msg){
        int money = Integer.parseInt(revenue);
        String name = msg.getChat().getUserName();
//        String name = "Ray";
        int salary = (int) (money * 0.02);
        if(salary < 1300){
            salary = 1300;
        }
        if (name.equals("l01d3n")) {
            salary = (salary - 300) / 10;
        } else {
            salary = (salary + 200) / 10;
        }
        salary = salary * 10;
        String textSend = Integer.toString(salary);
        save(salary, name, "зп");
        return salary;
    }

    @Transactional
    public void save(int money, String name, String comment) {
        Salary salary = new Salary();
        salary.setMoney(money);
        salary.setDate(LocalDate.now());
//        sal.setDate(LocalDate.of(2023, 04, 19));
        salary.setUserName(name);
        salary.setComment(comment);
        salaryRepository.save(salary);
    }

    @Transactional
    public void addMoney(long chatId, String data, String comment, Message msg) {
        int money = Integer.parseInt(data);
        String name = msg.getChat().getUserName();
        save(money, name, comment);
    }

    private List<Salary> findByUserName(String name) {
        List<Salary> userSalary = salaryRepository.findByUserName(name);
        return userSalary;
    }

    public String getAllSalary(String name){
        List<Salary> salary = findByUserName(name).stream().sorted(Comparator.comparingInt(Salary::getId)).collect(Collectors.toList());
        return salaryToString(salary);
    }

    public String getSalaryYesterday(String name){
        LocalDate date = LocalDate.now();
        date = date.minusDays(1);
        List<Salary> salary = salaryRepository.findByDateAndUserName(date, name);
        return salaryToString(salary);
    }

    private String salaryToString(List<Salary> salary){
        StringBuilder sb = new StringBuilder();
        for (Salary s: salary){
            sb.append(s.toString() + "\n");
        }
        String stringSalary = sb.toString();
        return stringSalary;
    }

}
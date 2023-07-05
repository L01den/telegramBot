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
public class SalaryServices {

    @Autowired
    public final SalaryRepository salaryRepository;

    public SalaryServices(SalaryRepository salaryRepository) {
        this.salaryRepository = salaryRepository;
    }

    @Transactional
    public int calculateSalary(String revenue, String name) {
        int money = Integer.parseInt(revenue);

//        String name = "Ray";
        int salary = (int) (money * 0.02);
        if (salary < 1300) {
            salary = 1300;
        }
        if (name.equals("l01d3n")) {
            salary = (salary - 300) / 10;
        } else if (name.equals("Ray")) {
            salary = (salary + 200) / 10;
        } else {
            salary = salary / 10;
        }
        salary = salary * 10;
        save(salary, name, "зп");
        return salary;

    }

    @Transactional
    public void save(int money, String name, String comment) {
        LocalDate date = LocalDate.now();
        Salary salary;
        salary = findByDateAndUserName(date, name);
        if(salary == null){
            salary = new Salary(date, money, name, comment);

        } else{
            salary.setMoney(money);
        }
//        salary.setMoney(money);
//        salary.setDate(date);
//        salary.setDate(LocalDate.of(2023, 07, 03));
//        salary.setUserName(name);
//        salary.setComment(comment);
        salaryRepository.save(salary);

    }

    @Transactional
    public void addMoney(String data, String comment, Message msg) {
        int money = Integer.parseInt(data);
        String name = msg.getChat().getUserName();
        save(money, name, comment);
    }

    private List<Salary> findByUserName(String name) {
        List<Salary> userSalary = salaryRepository.findByUserName(name);
        return userSalary;
    }

    public String getAllSalary(String name) {
        List<Salary> salary = findByUserName(name).stream().sorted(Comparator.comparingInt(Salary::getId)).collect(Collectors.toList());
        return salaryToString(salary);
    }

    public String getLastSalary(String name) {
        Salary salary = salaryRepository.findFirstByUserNameOrderByIdDesc(name);
//        Salary salary = salaryRepository.findFirstByUserNameOrderByIdDesc("Ray");
        return String.valueOf(salary);
    }

    private String salaryToString(List<Salary> salary) {
        StringBuilder sb = new StringBuilder();
        for (Salary s : salary) {
            sb.append(s.toString() + "\n");
        }
        String stringSalary = sb.toString();
        return stringSalary;
    }

    public String getSalaryInAMonth(String userName, LocalDate startDate, LocalDate endDate) {
        List<Salary> salaryInAMonth = salaryRepository.findByUserNameAndDateBetween(userName, startDate, endDate);
        return salaryToString(salaryInAMonth);
    }

    public int getSumSalaryInAMonth(String userName, LocalDate startDate, LocalDate endDate) {
        int salaryInAMonth = salaryRepository.findSumInAMonth(userName, startDate, endDate);
        return salaryInAMonth;

    }

    private Salary findByDateAndUserName(LocalDate date, String name) {
        Salary salary = salaryRepository.findByDateAndUserName(date, name);
        return salary;
    }
}
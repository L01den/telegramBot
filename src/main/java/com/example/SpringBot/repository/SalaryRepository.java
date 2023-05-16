package com.example.SpringBot.repository;

import com.example.SpringBot.model.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface SalaryRepository extends JpaRepository<Salary, Integer> {
    List<Salary> findByUserName(String name);

    List<Salary> findByDateAndUserName(LocalDate date, String name);

    List<Salary> findByDateBetween(LocalDate startDate, LocalDate endDate);

}

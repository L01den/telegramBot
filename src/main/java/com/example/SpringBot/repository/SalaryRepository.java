package com.example.SpringBot.repository;

import com.example.SpringBot.model.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface SalaryRepository extends JpaRepository<Salary, Integer> {
    List<Salary> findByUserName(String name);

    Salary findByDateAndUserName(LocalDate date, String name);

    List<Salary> findByUserNameAndDateBetween(String name, LocalDate startDate, LocalDate endDate);

    Salary findFirstByUserNameOrderByIdDesc(String userName);

    @Query("SELECT SUM(money) FROM salary WHERE userName = ?1 and date BETWEEN ?2 AND ?3")
    int findSumInAMonth(String name, LocalDate startDate, LocalDate endDate);

}

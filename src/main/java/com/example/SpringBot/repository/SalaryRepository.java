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

    Salary findByDateAndUserNameAndComment(LocalDate date, String name, String comment);

    List<Salary> findByUserNameAndDateBetween(String name, LocalDate startDate, LocalDate endDate);


//    List<Salary> findByAllOrderByIdAsc();

    Salary findFirstByUserNameOrderByIdDesc(String userName);

    List<Salary> findAll();

//    List<Salary> findAllOrderByDateAsc();

    @Query("SELECT SUM(money) FROM salary WHERE userName = ?1 and date BETWEEN ?2 AND ?3")
    int findSumInAMonth(String name, LocalDate startDate, LocalDate endDate);

}

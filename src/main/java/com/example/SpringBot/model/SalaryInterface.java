package com.example.SpringBot.model;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;

public interface SalaryInterface extends CrudRepository<Salary, LocalDate> {
}

package com.example.SpringBot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity(name = "salary")
public class Salary {
        @Id
        @Column(name = "id")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;
        @Column(name = "date")
        private LocalDate date;
        @Column(name = "money")
        private int money;
        @Column(name = "user_name")
        private String userName;
        @Column(name = "comment")
        private String comment;

        public Salary(LocalDate date, int money, String userName, String comment) {
                this.date = date;
                this.money = money;
                this.userName = userName;
                this.comment = comment;
        }

        @Override
        public String toString() {
                return date + " - " + money + "(" + comment + ")";
        }
}

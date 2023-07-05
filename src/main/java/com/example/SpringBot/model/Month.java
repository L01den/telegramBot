package com.example.SpringBot.model;

public enum Month {
    JANUARY ("Январь"),
    FEBRUARY ("Феварль"),
    MARCH ("Март"),
    APRIL ("Апрель"),
    MAY ("Май"),
    JUNE ("Июнь"),
    JULY ("Июль"),
    AUGUST ("Август"),
    SEPTEMBER ("Сентябрь"),
    OCTOBER ("Октябрь"),
    NOVEMBER ("Ноябрь"),
    DECEMBER ("Декабрь");

    private String title;

    Month(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}

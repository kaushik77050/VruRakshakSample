package com.example.sample;

public class TimeAndDate {
    String time;
    String date;

    public TimeAndDate(){}

    public TimeAndDate(String date, String time) {
        this.time = time;
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

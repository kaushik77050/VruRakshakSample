package com.example.sample.Model;

public class Users {
    private String email,name,pass,phone;

    public Users(){}

    public Users(String email, String name, String pass, String phone) {
        this.email = email;
        this.name = name;
        this.pass = pass;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

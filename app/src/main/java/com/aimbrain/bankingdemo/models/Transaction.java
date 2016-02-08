package com.aimbrain.bankingdemo.models;

import java.util.ArrayList;


public class Transaction {

    private String name;
    private double value;

    public Transaction(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public static ArrayList<Transaction> generateTransactions(){
        ArrayList<Transaction> transactionsArrayList = new ArrayList<>();

        transactionsArrayList.add(new Transaction("Room rental", -2.000));
        transactionsArrayList.add(new Transaction("Dinner", -230));
        transactionsArrayList.add(new Transaction("Swimming pool", -50));
        transactionsArrayList.add(new Transaction("Salary", 4.600));
        transactionsArrayList.add(new Transaction("Pizza", -16));
        transactionsArrayList.add(new Transaction("Extra money", 500));
        transactionsArrayList.add(new Transaction("Room rental", -2.060));
        transactionsArrayList.add(new Transaction("Lotery win", 10.000));
        transactionsArrayList.add(new Transaction("New car", -7.800));
        transactionsArrayList.add(new Transaction("Ice creams", -10));
        transactionsArrayList.add(new Transaction("Room rental", -2.000));
        transactionsArrayList.add(new Transaction("Dinner", -230));
        transactionsArrayList.add(new Transaction("Swimming pool", -50));
        transactionsArrayList.add(new Transaction("Salary", 4.600));
        transactionsArrayList.add(new Transaction("Pizza", -16));
        transactionsArrayList.add(new Transaction("Extra money", 500));
        transactionsArrayList.add(new Transaction("Room rental", -2.060));
        transactionsArrayList.add(new Transaction("Lotery win", 10.000));
        transactionsArrayList.add(new Transaction("New car", -7.800));
        transactionsArrayList.add(new Transaction("Ice creams", -10));
        transactionsArrayList.add(new Transaction("Room rental", -2.000));
        transactionsArrayList.add(new Transaction("Dinner", -230));
        transactionsArrayList.add(new Transaction("Swimming pool", -50));
        transactionsArrayList.add(new Transaction("Salary", 4.600));
        transactionsArrayList.add(new Transaction("Pizza", -16));
        transactionsArrayList.add(new Transaction("Extra money", 500));
        transactionsArrayList.add(new Transaction("Room rental", -2.060));
        transactionsArrayList.add(new Transaction("Lotery win", 10.000));
        transactionsArrayList.add(new Transaction("New car", -7.800));
        transactionsArrayList.add(new Transaction("Ice creams", -10));
        transactionsArrayList.add(new Transaction("Room rental", -2.000));
        transactionsArrayList.add(new Transaction("Dinner", -230));
        transactionsArrayList.add(new Transaction("Swimming pool", -50));
        transactionsArrayList.add(new Transaction("Salary", 4.600));
        transactionsArrayList.add(new Transaction("Pizza", -16));
        transactionsArrayList.add(new Transaction("Extra money", 500));
        transactionsArrayList.add(new Transaction("Room rental", -2.060));
        transactionsArrayList.add(new Transaction("Lotery win", 10.000));
        transactionsArrayList.add(new Transaction("New car", -7.800));
        transactionsArrayList.add(new Transaction("Ice creams", -10));

        return transactionsArrayList;
    }
}

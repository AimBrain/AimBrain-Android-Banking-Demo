package com.aimbrain.bankingdemo.models;


public class BankRow {

    private String firstValue;
    private String secondValue;


    public BankRow(String firstValue, String secondValue) {
        this.firstValue = firstValue;
        this.secondValue = secondValue;
    }

    public String getFirstValue() {
        return firstValue;
    }

    public String getSecondValue() {
        return secondValue;
    }
}

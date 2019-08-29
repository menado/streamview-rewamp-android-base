package com.streamhash.streamview.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by codegama on 21/9/17.
 */

public class Card implements Serializable {

    private int id;
    private String number;
    private String month;
    private String year;
    private String cvv;
    private String last4;
    private String cardToken;
    private boolean isDefault;

    public Card() {

    }

    public String getCardToken() {
        return cardToken;
    }

    public void setCardToken(String cardToken) {
        this.cardToken = cardToken;
    }

    @Override
    public String toString() {
        return "Card{" +
                "id='" + id + '\'' +
                ", number='" + number + '\'' +
                ", month='" + month + '\'' +
                ", year='" + year + '\'' +
                ", cvv='" + cvv + '\'' +
                ", last4='" + last4 + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getLast4() {
        return last4;
    }

    public void setLast4(String last4) {
        this.last4 = last4;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}

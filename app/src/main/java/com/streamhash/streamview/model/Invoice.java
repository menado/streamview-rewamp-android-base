package com.streamhash.streamview.model;

import java.io.Serializable;

public class Invoice implements Serializable {

    private boolean isPayingForPlan;
    private String paymentId;
    private double paidAmount;
    private double totalAmount;
    private String title;
    private String currency;
    private String currencySymbol;
    private boolean isCouponApplied;
    private String couponCode;
    private String status;
    private double couponAmount;
    private int months;

    private SubscriptionPlan plan;
    private Video video;

    @Override
    public String toString() {
        return "Invoice{" +
                "isPayingForPlan=" + isPayingForPlan +
                ", paymentId='" + paymentId + '\'' +
                ", paidAmount=" + paidAmount +
                ", totalAmount=" + totalAmount +
                ", title='" + title + '\'' +
                ", currency='" + currency + '\'' +
                ", currencySymbol='" + currencySymbol + '\'' +
                ", isCouponApplied=" + isCouponApplied +
                ", couponCode='" + couponCode + '\'' +
                ", status='" + status + '\'' +
                ", couponAmount=" + couponAmount +
                ", months=" + months +
                '}';
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMonths() {
        return months;
    }

    public void setMonths(int months) {
        this.months = months;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isPayingForPlan() {
        return isPayingForPlan;
    }

    public void setPayingForPlan(boolean payingForPlan) {
        isPayingForPlan = payingForPlan;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCouponApplied() {
        return isCouponApplied;
    }

    public void setCouponApplied(boolean couponApplied) {
        isCouponApplied = couponApplied;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public double getCouponAmount() {
        return couponAmount;
    }

    public void setCouponAmount(double couponAmount) {
        this.couponAmount = couponAmount;
    }

}

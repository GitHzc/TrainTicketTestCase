package org.services.test.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class VoucherResponseDto implements Serializable {

    private static final long serialVersionUID = -6964700951137353564L;

    @JsonProperty("voucher_id")
    private long voucherId;
    @JsonProperty("order_id")
    private String orderId;
    @JsonProperty("travelDate")
    private long travelDate;
    @JsonProperty("contactName")
    private String contactName;
    @JsonProperty("train_number")
    private String trainNumber;
    @JsonProperty("seat_number")
    private String seatNumber;
    @JsonProperty("start_station")
    private String startStation;
    @JsonProperty("dest_station")
    private String destStation;
    @JsonProperty("price")
    private int price;

    public long getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(long voucherId) {
        this.voucherId = voucherId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public long getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(long travelDate) {
        this.travelDate = travelDate;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getStartStation() {
        return startStation;
    }

    public void setStartStation(String startStation) {
        this.startStation = startStation;
    }

    public String getDestStation() {
        return destStation;
    }

    public void setDestStation(String destStation) {
        this.destStation = destStation;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}

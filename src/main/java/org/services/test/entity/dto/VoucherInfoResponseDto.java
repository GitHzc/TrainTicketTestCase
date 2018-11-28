package org.services.test.entity.dto;

import java.io.Serializable;

public class VoucherInfoResponseDto implements Serializable {

    private static final long serialVersionUID = 7811605821861683118L;

    private String contactName;

    private String dest_station;

    private String order_id;

    private double price;

    private String seat_number;

    private String start_station;

    private String train_number;

    private String travelDate;

    private int voucher_id;

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getDest_station() {
        return dest_station;
    }

    public void setDest_station(String dest_station) {
        this.dest_station = dest_station;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getSeat_number() {
        return seat_number;
    }

    public void setSeat_number(String seat_number) {
        this.seat_number = seat_number;
    }

    public String getStart_station() {
        return start_station;
    }

    public void setStart_station(String start_station) {
        this.start_station = start_station;
    }

    public String getTrain_number() {
        return train_number;
    }

    public void setTrain_number(String train_number) {
        this.train_number = train_number;
    }

    public String getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(String travelDate) {
        this.travelDate = travelDate;
    }

    public int getVoucher_id() {
        return voucher_id;
    }

    public void setVoucher_id(int voucher_id) {
        this.voucher_id = voucher_id;
    }
}
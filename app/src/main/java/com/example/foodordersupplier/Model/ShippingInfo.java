package com.example.foodordersupplier.Model;

public class ShippingInfo {

    private String supplierEmail,orderId;
    private double lat, lng;

    public ShippingInfo() {
    }


    public ShippingInfo(String supplierEmail, String orderId, double lat, double lng) {
        this.supplierEmail = supplierEmail;
        this.orderId = orderId;
        this.lat = lat;
        this.lng = lng;
    }

    public String getSupplierEmail() {
        return supplierEmail;
    }

    public void setSupplierEmail(String supplierEmail) {
        this.supplierEmail = supplierEmail;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}

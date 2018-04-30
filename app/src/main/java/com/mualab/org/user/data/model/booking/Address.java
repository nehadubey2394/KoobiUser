package com.mualab.org.user.data.model.booking;

import java.io.Serializable;

/**
 */

public class Address implements Serializable{
    int id;
    public String city;
    public String state;
    public String country;
    public String stAddress1;
    public String stAddress2;
    public String placeName;
    public String fullAddress;
    public String postalCode;
    public String latitude;
    public String longitude;

    @Override
    public String toString() {
        return fullAddress;
    }
}

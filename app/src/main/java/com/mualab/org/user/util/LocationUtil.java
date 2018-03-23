package com.mualab.org.user.util;


import com.google.android.gms.location.places.Place;
import com.mualab.org.user.model.booking.Address;

/**
 * Created by dharmraj on 23/3/18.
 */

public class LocationUtil {

    public Address getAddressDetails(Place place) {
        Address address = new Address();
        address.placeName = String.valueOf(place.getName());
        if (place.getAddress() != null) {
            String[] addressSlice = place.getAddress().toString().split(", ");
            address.country = addressSlice[addressSlice.length - 1];
            if (addressSlice.length > 1) {
                String[] stateAndPostalCode = addressSlice[addressSlice.length - 2].split(" ");
                if (stateAndPostalCode.length > 1) {
                    address.postalCode = stateAndPostalCode[stateAndPostalCode.length - 1];
                    address.state = "";
                    for (int i = 0; i < stateAndPostalCode.length - 1; i++) {
                        address. state += (i == 0 ? "" : " ") + stateAndPostalCode[i];
                    }
                } else {
                    address.state = stateAndPostalCode[stateAndPostalCode.length - 1];
                }
            }
            if (addressSlice.length > 2)
                address.city = addressSlice[addressSlice.length - 3];
            if (addressSlice.length == 4)
                address.stAddress1 = addressSlice[0];
            else if (addressSlice.length > 3) {
                address.stAddress2 = addressSlice[addressSlice.length - 4];
                address.stAddress1 = "";
                for (int i = 0; i < addressSlice.length - 4; i++) {
                    address.stAddress1 += (i == 0 ? "" : ", ") + addressSlice[i];
                }
            }
        }

        if(place.getLatLng()!=null) {
            address.latitude = "" + place.getLatLng().latitude;
            address.longitude = "" + place.getLatLng().longitude;
        }

        return address;
    }
}

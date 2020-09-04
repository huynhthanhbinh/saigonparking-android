package com.vtb.parkingmap.models;


import com.bht.saigonparking.api.grpc.booking.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by BACHMAP
 */
@Getter
@Setter
@AllArgsConstructor
public class History {
    private String parkinglotName;
    private String licensePlate;
    private String createAt;
    private String idBooking;
    private BookingStatus status;
}

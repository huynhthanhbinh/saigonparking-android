package com.vtb.parkingmap.support;


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
}

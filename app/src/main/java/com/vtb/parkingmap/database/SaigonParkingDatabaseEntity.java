package com.vtb.parkingmap.database;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public final class SaigonParkingDatabaseEntity {

    private double latitude;
    private double longitude;
    private double mylat;
    private double mylong;
    private int tmpType;
    private long id;
    private double position3lat;
    private double position3long;
    private String bookingId;
    public static final SaigonParkingDatabaseEntity DEFAULT_INSTANCE = SaigonParkingDatabaseEntity.builder().build();
}
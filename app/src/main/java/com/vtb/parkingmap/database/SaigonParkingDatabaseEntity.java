package com.vtb.parkingmap.database;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public final class SaigonParkingDatabaseEntity {

    private double latitude;
    private double longitude;
    private double mylat;
    private double mylong;
    private int tmptype;
    private long id;
    private double position3lat;
    private double position3long;

    public static final SaigonParkingDatabaseEntity DEFAULT_INSTANCE = SaigonParkingDatabaseEntity.builder().build();
}
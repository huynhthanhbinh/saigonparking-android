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

    private Double latitude;
    private Double longitude;
    private Double mylat;
    private Double mylong;
    private Integer tmpType;
    private Long id;
    private Double position3lat;
    private Double position3long;
    private String bookingId;
    public static final SaigonParkingDatabaseEntity DEFAULT_INSTANCE = SaigonParkingDatabaseEntity.builder().build();
}
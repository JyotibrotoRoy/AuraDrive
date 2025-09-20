package com.Jyotibroto.auradrive.dto;

import lombok.Data;

@Data
public class NearbyDriversResponseDto {
    private String id;
    private String userName;
    private LocationDto currentLocation;

}

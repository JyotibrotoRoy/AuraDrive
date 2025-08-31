package com.Jyotibroto.auradrive.dto;

import lombok.Data;

@Data
public class TripRequestDto {
    private LocationDto startLocation;
    private LocationDto endLocation;
}

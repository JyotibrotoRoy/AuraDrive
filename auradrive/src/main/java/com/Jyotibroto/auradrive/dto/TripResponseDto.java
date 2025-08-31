package com.Jyotibroto.auradrive.dto;

import com.Jyotibroto.auradrive.enums.TripStatus;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@Data
public class TripResponseDto {
    private String id;
    private String riderId;
    private String diverId;
    private LocationDto startLocation;
    private LocationDto endLocation;
    private TripStatus status;
    private LocalDateTime createdAt;
    private String otpForRider;
}

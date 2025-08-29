package com.Jyotibroto.auradrive.entity;

import com.Jyotibroto.auradrive.enums.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import java.rmi.server.ObjID;
import java.time.LocalDateTime;

@Document(collection = "trips")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Component
public class Trip {
    @Id
    private String id;
    private ObjectId riderId;
    private ObjectId driverId;
    private GeoJsonPoint startLocation;
    private GeoJsonPoint endLocation;
    private TripStatus tripStatus;
    private LocalDateTime createdAt;
}

package com.Jyotibroto.auradrive.service;

import com.Jyotibroto.auradrive.dto.LocationDto;
import com.Jyotibroto.auradrive.dto.LocationUpdateDto;
import com.Jyotibroto.auradrive.dto.NearbyDriversResponseDto;
import com.Jyotibroto.auradrive.entity.Trip;
import com.Jyotibroto.auradrive.entity.User;
import com.Jyotibroto.auradrive.enums.ROLES;
import com.Jyotibroto.auradrive.repository.TripRepository;
import com.Jyotibroto.auradrive.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DriverService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final String DRIVER_LOCATION_KEYS = "DRIVER_LOCATIONS";

    @Autowired
    public DriverService(UserRepository userRepository, RedisTemplate<String, String> redisTemplate, TripRepository tripRepository, SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    public void updateDriverAvailability(String driverEmail, boolean isAvailable) {
        User driver = userRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Driver not found with email: "+ driverEmail));

        driver.setAvailable(isAvailable);
        userRepository.save(driver);
    }

    public List<NearbyDriversResponseDto> findNearByDrivers(LocationDto riderLocation) {
        Point searchPoint = new Point(riderLocation.getLongitude(), riderLocation.getLatitude());

        Distance radius = new Distance(5, Metrics.KILOMETERS);

        Circle searchCircle = new Circle(searchPoint, radius);

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates().sortAscending();

        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = redisTemplate.opsForGeo().radius(DRIVER_LOCATION_KEYS, searchCircle, args);
        if(geoResults == null) {
            return Collections.emptyList();
        }

        List<String> driverIDs = geoResults.getContent().stream()
                .map(geoResult -> geoResult.getContent().getName())
                .toList();
        if(driverIDs.isEmpty()) {
            return Collections.emptyList();
        }

        List<ObjectId> driverObjectIds = driverIDs.stream().map(ObjectId::new).toList();

        Map<String, User> driverMap = userRepository.findAllById(driverObjectIds).stream()
                .collect(Collectors.toMap(user -> user.getId().toString(), user -> user));

        return geoResults.getContent().stream()
                .map(geoResult -> {
                    NearbyDriversResponseDto dto = new NearbyDriversResponseDto();
                    String driverId = geoResult.getContent().getName();
                    User driver = driverMap.get(driverId);

                    dto.setId(driverId);
                    if(driver != null) {
                        dto.setUserName(driver.getUserName());
                    }

                    Point point = geoResult.getContent().getPoint();
                    LocationDto locationDto = new LocationDto();
                    locationDto.setLongitude(point.getX());
                    locationDto.setLatitude(point.getY());
                    dto.setCurrentLocation(locationDto);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    private NearbyDriversResponseDto mapToNearByDriverDto(GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult) {
        NearbyDriversResponseDto response = new NearbyDriversResponseDto();

        RedisGeoCommands.GeoLocation<String> location = geoResult.getContent();

        response.setId(location.getName());

        Point point = location.getPoint();

        LocationDto locationDto = new LocationDto();
        locationDto.setLongitude(point.getX());
        locationDto.setLatitude(point.getY());
        response.setCurrentLocation(locationDto);

        return response;
    }

    public void updateDriverLocation(String driverEmail, LocationDto locationDto) {
        try{

            log.info("updateLocation in service called for email: {} ", driverEmail);
            User driver = userRepository.findByEmail(driverEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("Driver not found"));

            Point point = new Point(locationDto.getLongitude(), locationDto.getLatitude());
            String driverId = driver.getId().toString();

            long result = redisTemplate.opsForGeo().add(DRIVER_LOCATION_KEYS, point, driverId);
            log.info("Redis GEOADD result for driver {}: {}", driverId, result);
        }
        catch (Exception e) {
            log.error("!!! Exception while updating location in Redis !!!", e);
        }
    }

    public void updateAndBroadcastLocation(String tripId, LocationUpdateDto locationUpdate) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if(trip.getDriverId() != null && trip.getDriverId().toString().equals(locationUpdate.getDriverId())){
            Point point = new Point(locationUpdate.getLongitude(),locationUpdate.getLatitude());
            redisTemplate.opsForGeo().add(DRIVER_LOCATION_KEYS, point, locationUpdate.getDriverId());

            String destination = "/topic/trip/" + tripId;
            messagingTemplate.convertAndSend(destination, locationUpdate);
        }
    }
}

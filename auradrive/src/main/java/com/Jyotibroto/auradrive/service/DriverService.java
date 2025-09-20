package com.Jyotibroto.auradrive.service;

import com.Jyotibroto.auradrive.dto.LocationDto;
import com.Jyotibroto.auradrive.dto.NearbyDriversResponseDto;
import com.Jyotibroto.auradrive.entity.User;
import com.Jyotibroto.auradrive.enums.ROLES;
import com.Jyotibroto.auradrive.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DriverService {
    @Autowired
    private UserRepository userRepository;

    public void updateDriverAvailability(String driverEmail, boolean isAvailable) {
        User driver = userRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Driver not found with email: "+ driverEmail));

        driver.setAvailable(isAvailable);
        userRepository.save(driver);
    }

    public List<NearbyDriversResponseDto> findNearByDrivers(LocationDto riderLocation) {
        Point searchPoint = new Point(riderLocation.getLongitude(), riderLocation.getLatitude());

        Distance radius = new Distance(5, Metrics.KILOMETERS);

        List<User> nearByDrivers = userRepository.findByRoleAndAvailableTrueAndCurrentLocationNear(
                ROLES.DRIVER,
                searchPoint,
                radius
        );
        return nearByDrivers.stream().map(this::mapToNearByDriverDto).collect(Collectors.toList());
    }

    private NearbyDriversResponseDto mapToNearByDriverDto(User driver) {
        NearbyDriversResponseDto response = new NearbyDriversResponseDto();
        response.setId(driver.getId().toString());
        response.setUserName(driver.getUserName());

        LocationDto location = new LocationDto();
        if(driver.getCurrentLocation() != null) {
            location.setLongitude(driver.getCurrentLocation().getX());
            location.setLatitude(driver.getCurrentLocation().getY());
        }
        response.setCurrentLocation(location);

        return response;
    }

    public void updateDriverLocation(String driverEmail, LocationDto locationDto) {
        User driver = userRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Driver not found"));

        GeoJsonPoint newLocation = new GeoJsonPoint(locationDto.getLongitude(), locationDto.getLatitude());

        driver.setCurrentLocation(newLocation);

        userRepository.save(driver);
    }
}

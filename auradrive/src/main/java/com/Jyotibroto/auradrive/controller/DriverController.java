package com.Jyotibroto.auradrive.controller;

import com.Jyotibroto.auradrive.dto.DriverStatusRequestDto;
import com.Jyotibroto.auradrive.dto.FindDriverRequestDto;
import com.Jyotibroto.auradrive.dto.LocationDto;
import com.Jyotibroto.auradrive.dto.NearbyDriversResponseDto;
import com.Jyotibroto.auradrive.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {
    @Autowired
    private DriverService driverService;

    @PatchMapping("/me/status")
    public ResponseEntity<?> updateAvailability(@RequestBody DriverStatusRequestDto request,
                                                @AuthenticationPrincipal UserDetails driverDetails) {
        driverService.updateDriverAvailability(driverDetails.getUsername(), request.isAvailable());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("me/location")
    public ResponseEntity<?> updateLocation(@RequestBody LocationDto locationDto,
                                            @AuthenticationPrincipal UserDetails driverDetails) {
        driverService.updateDriverLocation(driverDetails.getUsername(), locationDto);
        return ResponseEntity.ok().build();
    }
}

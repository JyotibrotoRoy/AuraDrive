package com.Jyotibroto.auradrive.repository;

import com.Jyotibroto.auradrive.entity.Trip;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TripRepository extends MongoRepository<Trip, String> {
}

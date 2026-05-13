package com.example.coreserver.repository;

import com.example.coreserver.entity.Geofence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface GeofenceRepository extends JpaRepository<Geofence, Long> {
}
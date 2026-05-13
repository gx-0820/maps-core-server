package com.example.coreserver.mapper;

import com.example.coreserver.entity.Geofence;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GeofenceMapper {
    @Select("SELECT * FROM geofence WHERE id = #{id}")
    Geofence selectById(Long id);
}
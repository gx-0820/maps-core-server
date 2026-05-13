package com.example.coreserver.service.business;
import com.example.coreserver.dto.*;
import com.example.coreserver.entity.Geofence;
import com.example.coreserver.exception.*;
import com.example.coreserver.repository.GeofenceRepository;
import com.example.coreserver.utils.KmlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class GeofenceService {

    @Autowired
    private GeofenceRepository repository;

    public GeofenceService(GeofenceRepository repository) {
        this.repository = repository;
    }
    public Geofence create(CreateGeofenceRequest req) {
        validateRadii(req.getCoreRadius(), req.getBufferRadius(), req.getAlertRadius());

        Geofence entity = new Geofence();
        entity.setName(req.getName());
        entity.setCoreLongitude(req.getCoreLongitude());
        entity.setCoreLatitude(req.getCoreLatitude());
        entity.setCoreRadius(req.getCoreRadius());
        entity.setBufferRadius(req.getBufferRadius());
        entity.setAlertRadius(req.getAlertRadius());
        return repository.save(entity);
    }

    public Geofence update(Long id, UpdateGeofenceRequest req) {
        Geofence existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("禁飞区不存在"));

        if (req.getName() != null) existing.setName(req.getName());
        if (req.getCoreLongitude() != null) existing.setCoreLongitude(req.getCoreLongitude());
        if (req.getCoreLatitude() != null) existing.setCoreLatitude(req.getCoreLatitude());

        // 合并新旧半径值并校验
        Double newCore = req.getCoreRadius() != null ? req.getCoreRadius() : existing.getCoreRadius();
        Double newBuffer = req.getBufferRadius() != null ? req.getBufferRadius() : existing.getBufferRadius();
        Double newAlert = req.getAlertRadius() != null ? req.getAlertRadius() : existing.getAlertRadius();
        validateRadii(newCore, newBuffer, newAlert);

        existing.setCoreRadius(newCore);
        existing.setBufferRadius(newBuffer);
        existing.setAlertRadius(newAlert);

        return repository.save(existing);
    }

    private void validateRadii(Double core, Double buffer, Double alert) {
        if (core <= 0 || buffer <= 0 || alert <= 0) {
            throw new InvalidParameterException("所有半径必须大于0");
        }
        if (buffer <= core) {
            throw new InvalidParameterException("缓冲半径必须大于核心半径");
        }
        if (alert <= buffer) {
            throw new InvalidParameterException("告警半径必须大于缓冲半径");
        }
    }

    public List<Geofence> findAll() {
        try {
            return repository.findAll();
        } catch (Exception e) {
            log.error("Geofence query failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public byte[] exportKml() throws IOException {
        return KmlUtils.exportToKml(repository.findAll()).getBytes();
    }

    public String importKml(MultipartFile file) throws IOException {
        List<Geofence> geofences = KmlUtils.parseFromKml(file.getInputStream());
        repository.saveAll(geofences);
        return "成功导入 " + geofences.size() + " 条记录";
    }
}
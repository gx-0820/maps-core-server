package com.example.coreserver.service.countermeasure;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CountermeasureJammingAdapterRegistry {

    private final List<CountermeasureJammingCommandAdapter> adapters;

    public CountermeasureJammingAdapterRegistry(List<CountermeasureJammingCommandAdapter> adapters) {
        this.adapters = adapters;
    }

    public Optional<CountermeasureJammingCommandAdapter> find(String adapterId) {
        if (adapterId == null || adapterId.isBlank()) {
            return Optional.empty();
        }
        return adapters.stream()
                .filter(adapter -> adapter.adapterId().equals(adapterId))
                .findFirst();
    }
}

package com.example.coreserver.service.device;

import com.example.coreserver.entity.countermeasure.CountermeasureType;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class DeviceLockManager {
    private final ConcurrentMap<CountermeasureType, ReentrantLock> locks = new ConcurrentHashMap<>();

    public boolean acquireLock(CountermeasureType type) {
        return locks.computeIfAbsent(type, t -> new ReentrantLock())
                .tryLock();
    }

    public void releaseLock(CountermeasureType type) {
        ReentrantLock lock = locks.get(type);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}

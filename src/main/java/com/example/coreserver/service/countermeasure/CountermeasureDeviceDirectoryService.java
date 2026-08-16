package com.example.coreserver.service.countermeasure;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coreserver.dto.countermeasure.CountermeasureModeOptionDevice;
import com.example.coreserver.dto.countermeasure.CountermeasureModeOptionsResponse;
import com.example.coreserver.entity.Device;
import com.example.coreserver.entity.countermeasure.CountermeasureDeviceDirection;
import com.example.coreserver.entity.countermeasure.CountermeasureMode;
import com.example.coreserver.entity.countermeasure.CountermeasureOmcFlag;
import com.example.coreserver.service.DeviceManagerService;
import com.example.coreserver.service.device.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;

/**
 * 自动处置反制设备目录服务。
 * 干扰设备按本地 device 表解析；诱骗设备仍复用 device-server 的 TALENT 运行时设备。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CountermeasureDeviceDirectoryService {

    static final String JAMMING_TYPE = "JAMMING";
    static final String SPOOFING_TYPE = "SPOOFING";
    static final String TALENT_TYPE = "TALENT";
    static final String COUNTERMEASURE_CLASS = "反制设备";

    static final String ADAPTER_MAODE_OMNI = "adapter:jamming:maode-omni";
    static final String ADAPTER_HG_DUAL_IP = "adapter:jamming:hg-dual-ip";
    static final String ADAPTER_WEIXIANG_OMNI = "adapter:jamming:weixiang-omni";

    private final CountermeasureConfigService countermeasureConfigService;
    private final DeviceManagerService deviceManagerService;
    private final ConfigService configService;

    public CountermeasureModeOptionsResponse buildModeOptions() {
        List<CountermeasureRuntimeDevice> runtimeDevices = listRuntimeDevices();
        EnumSet<CountermeasureOmcFlag> supportedFlags = resolveSupportedOmcFlags(runtimeDevices);
        List<CountermeasureModeOptionDevice> devices = runtimeDevices.stream()
                .map(device -> new CountermeasureModeOptionDevice(
                        device.id(),
                        device.type(),
                        device.brand(),
                        device.model(),
                        device.collectFlag(),
                        device.name(),
                        device.location(),
                        device.coverage(),
                        device.direction(),
                        device.executable(),
                        device.executableReason()
                ))
                .toList();
        return new CountermeasureModeOptionsResponse(
                countermeasureConfigService.getMode(),
                countermeasureConfigService.getLocation(),
                countermeasureConfigService.getOmcFlag(),
                devices,
                supportedFlags.stream().toList()
        );
    }

    public List<CountermeasureRuntimeDevice> resolveExecutableJammingDevices(CountermeasureOmcFlag omcFlag) {
        CountermeasureOmcFlag effectiveFlag = omcFlag == null ? CountermeasureOmcFlag.NONE : omcFlag;
        return listRuntimeDevices().stream()
                .filter(CountermeasureRuntimeDevice::isJamming)
                .filter(CountermeasureRuntimeDevice::executable)
                .filter(device -> switch (effectiveFlag) {
                    case ALL, JAMMING -> true;
                    case NONE, SPOOFING -> device.direction() == CountermeasureDeviceDirection.DIRECTIONAL;
                })
                .toList();
    }

    public List<CountermeasureRuntimeDevice> resolveExecutableSpoofingDevices(CountermeasureOmcFlag omcFlag) {
        CountermeasureOmcFlag effectiveFlag = omcFlag == null ? CountermeasureOmcFlag.NONE : omcFlag;
        return listRuntimeDevices().stream()
                .filter(CountermeasureRuntimeDevice::isSpoofing)
                .filter(CountermeasureRuntimeDevice::executable)
                .filter(device -> switch (effectiveFlag) {
                    case ALL, SPOOFING -> true;
                    case NONE, JAMMING -> device.direction() == CountermeasureDeviceDirection.DIRECTIONAL;
                })
                .toList();
    }

    public String resolveDeceptionRuntimeDeviceId() {
        try {
            com.example.coreserver.grpc.config.DeviceConfig config = configService.getConfig();
            return config.getDevicesList().stream()
                    .filter(device -> TALENT_TYPE.equals(device.hasDeviceType() ? device.getDeviceType() : null))
                    .filter(com.example.coreserver.grpc.config.Device::hasDeviceId)
                    .map(com.example.coreserver.grpc.config.Device::getDeviceId)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.warn("解析运行时诱骗设备失败: {}", e.getMessage(), e);
            return null;
        }
    }

    EnumSet<CountermeasureOmcFlag> resolveSupportedOmcFlags(List<CountermeasureRuntimeDevice> runtimeDevices) {
        boolean omniJammingSupported = runtimeDevices.stream()
                .anyMatch(device -> device.isJamming()
                        && device.direction() == CountermeasureDeviceDirection.OMNI
                        && device.executable());
        boolean omniSpoofingSupported = runtimeDevices.stream()
                .anyMatch(device -> device.isSpoofing()
                        && device.direction() == CountermeasureDeviceDirection.OMNI
                        && device.executable());

        EnumSet<CountermeasureOmcFlag> flags = EnumSet.of(CountermeasureOmcFlag.NONE);
        if (omniJammingSupported) {
            flags.add(CountermeasureOmcFlag.JAMMING);
        }
        if (omniSpoofingSupported) {
            flags.add(CountermeasureOmcFlag.SPOOFING);
        }
        if (omniJammingSupported && omniSpoofingSupported) {
            flags.add(CountermeasureOmcFlag.ALL);
        }
        return flags;
    }

    List<CountermeasureRuntimeDevice> listRuntimeDevices() {
        String location = countermeasureConfigService.getLocation();
        if (location == null || location.isBlank()) {
            log.warn("自动处置反制设备目录解析失败: sys.location 未配置");
            return List.of();
        }

        String deceptionRuntimeDeviceId = resolveDeceptionRuntimeDeviceId();
        LambdaQueryWrapper<Device> queryWrapper = new LambdaQueryWrapper<Device>()
                .eq(Device::getLocation, location)
                .eq(Device::getClasses, COUNTERMEASURE_CLASS)
                .orderByAsc(Device::getOrderNum)
                .orderByAsc(Device::getId);
        return deviceManagerService.list(queryWrapper).stream()
                .map(device -> classifyDevice(device, deceptionRuntimeDeviceId))
                .toList();
    }

    private CountermeasureRuntimeDevice classifyDevice(Device device, String deceptionRuntimeDeviceId) {
        String type = device.getType() == null ? "" : device.getType().trim().toUpperCase();
        return switch (type) {
            case JAMMING_TYPE -> classifyJamming(device);
            case SPOOFING_TYPE -> classifySpoofing(device, deceptionRuntimeDeviceId);
            default -> new CountermeasureRuntimeDevice(
                    device.getId(),
                    type,
                    device.getBrand(),
                    device.getModel(),
                    device.getCollectFlag(),
                    device.getName(),
                    device.getLocation(),
                    normalize(device.getCoverage()),
                    CountermeasureDeviceDirection.DIRECTIONAL,
                    false,
                    "自动处置暂不支持该反制设备类型",
                    null
            );
        };
    }

    private CountermeasureRuntimeDevice classifyJamming(Device device) {
        String collectFlag = normalize(device.getCollectFlag());
        String brand = normalize(device.getBrand());
        String model = normalize(device.getModel());
        CountermeasureDeviceDirection direction = resolveDirection(device, collectFlag, brand, model);

        if ("ELINT_PROMOS_UAV".equals(collectFlag)) {
            return buildRuntimeDevice(device, direction, true, null, ADAPTER_MAODE_OMNI);
        }
        if ("JAMMING_HG_CQRF".equals(collectFlag)) {
            return buildRuntimeDevice(device, direction, true, null, ADAPTER_HG_DUAL_IP);
        }
        if ("ELINT_NJWX_DGR".equals(collectFlag)) {
            return buildRuntimeDevice(device, direction, true, null, ADAPTER_WEIXIANG_OMNI);
        }
        if ("海格".equals(brand) && "CQ-RF单IP".equals(model)) {
            return buildRuntimeDevice(device, direction, false, "海格CQ-RF单IP协议开发中", null);
        }
        if ("海格".equals(brand) && "CQ-RF双IP".equals(model) && collectFlag == null) {
            return buildRuntimeDevice(device, direction, false, "海格CQ-RF双IP缺少collect_flag", null);
        }
        if (collectFlag == null) {
            return buildRuntimeDevice(device, direction, false, "设备缺少collect_flag", null);
        }
        return buildRuntimeDevice(device, direction, false, "暂未接入该定向干扰设备协议", null);
    }

    private CountermeasureRuntimeDevice classifySpoofing(Device device, String deceptionRuntimeDeviceId) {
        boolean executable = deceptionRuntimeDeviceId != null && !deceptionRuntimeDeviceId.isBlank();
        String reason = executable ? null : "未找到TALENT运行时设备";
        CountermeasureDeviceDirection direction = resolveDirection(
                device,
                normalize(device.getCollectFlag()),
                normalize(device.getBrand()),
                normalize(device.getModel())
        );
        return buildRuntimeDevice(device, direction, executable, reason, null);
    }

    private CountermeasureRuntimeDevice buildRuntimeDevice(
            Device device,
            CountermeasureDeviceDirection direction,
            boolean executable,
            String executableReason,
            String adapterId
    ) {
        return new CountermeasureRuntimeDevice(
                device.getId(),
                normalize(device.getType()),
                normalize(device.getBrand()),
                normalize(device.getModel()),
                normalize(device.getCollectFlag()),
                normalize(device.getName()),
                normalize(device.getLocation()),
                normalize(device.getCoverage()),
                direction,
                executable,
                executableReason,
                adapterId
        );
    }

    private CountermeasureDeviceDirection resolveDirection(Device device, String collectFlag, String brand, String model) {
        String coverage = normalize(device.getCoverage());
        if ("全向".equals(coverage)) {
            return CountermeasureDeviceDirection.OMNI;
        }
        if ("定向".equals(coverage)) {
            return CountermeasureDeviceDirection.DIRECTIONAL;
        }
        if ("ELINT_PROMOS_UAV".equals(collectFlag)
                || "JAMMING_HG_CQRF".equals(collectFlag)
                || "ELINT_NJWX_DGR".equals(collectFlag)
                || ("海格".equals(brand) && "CQ-RF单IP".equals(model))
                || ("海格".equals(brand) && "CQ-RF双IP".equals(model))) {
            return CountermeasureDeviceDirection.OMNI;
        }
        return CountermeasureDeviceDirection.DIRECTIONAL;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record CountermeasureRuntimeDevice(
            Long id,
            String type,
            String brand,
            String model,
            String collectFlag,
            String name,
            String location,
            String coverage,
            CountermeasureDeviceDirection direction,
            boolean executable,
            String executableReason,
            String adapterId
    ) {
        boolean isJamming() {
            return JAMMING_TYPE.equalsIgnoreCase(type);
        }

        boolean isSpoofing() {
            return SPOOFING_TYPE.equalsIgnoreCase(type);
        }
    }
}

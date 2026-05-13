package com.example.coreserver.service.device;

import com.example.coreserver.grpc.camera.CameraServiceGrpc;
import com.example.coreserver.grpc.camera.DeviceListResponse;
import com.example.coreserver.grpc.common.Empty;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class CameraService {

    @GrpcClient("device-server")
    private CameraServiceGrpc.CameraServiceBlockingStub stub;

    public DeviceListResponse getCameraDevices() {
        return stub.getCameraDevices(Empty.newBuilder().build());
    }
}

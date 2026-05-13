package com.example.coreserver.service.device;

import com.example.coreserver.grpc.common.DeviceId;
import com.example.coreserver.grpc.common.Empty;
import com.example.coreserver.grpc.common.Response;
import com.example.coreserver.grpc.talent.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class TalentService {

    @GrpcClient("device-server")
    private TalentServiceGrpc.TalentServiceBlockingStub stub;

    public DeviceListResponse getTalentDevices() {
        DeviceListResponse response = stub.getTalentDevices(Empty.newBuilder().build());
        return response;
    }

    public Response UpdateConnectSetting(DeceptionRequest deceptionRequest) {
        Response response = stub.updateConnectSetting(deceptionRequest);
        return response;
    }

    public Response UpdateCommand(DeceptionRequest deceptionRequest) {
        Response response = stub.updateCommand(deceptionRequest);
        return response;
    }


    public SimulationStatus getSimulationStatus(DeviceId deviceId) {
        SimulationStatus status = stub.getSimulationStatus(deviceId);
        return status;
    }

    public Response sendCaptureCommand(CaptureRequest captureRequest) {
        Response response = stub.sendCaptureCommand(captureRequest);
        return response;
    }

    public Response sendDriveAngleCommand(DriveAngleRequest driveAngleRequest) {
        Response response = stub.sendDriveAngleCommand(driveAngleRequest);
        return response;
    }

    public Response sendBootstrapPositionCommand(PositionRequest positionRequest) {
        Response response = stub.sendBootstrapPositionCommand(positionRequest);
        return response;
    }

    public Response sendDefenseCommand(DeviceId deviceId) {
        Response response = stub.sendDefenseCommand(deviceId);
        return response;
    }

    public Response sendInterferenceCommand(DeviceId deviceId) {
        Response response = stub.sendInterferenceCommand(deviceId);
        return response;
    }

    public Response sendTransmitPowerCommand(TransmitPowerRequest transmitPowerRequest) {
        Response response = stub.sendTransmitPowerCommand(transmitPowerRequest);
        return response;
    }

    public Response stopLaunch(DeviceId deviceId) {
        Response response = stub.stopLaunch(deviceId);
        return response;
    }

    public Response sendNoFly(DeviceId deviceId) {
        Response response = stub.sendNoFlyCommand(deviceId);
        return response;
    }

    public ConnectionStatus isConnected(DeviceId deviceId) {
        ConnectionStatus status = stub.isConnected(deviceId);
        return status;
    }

}

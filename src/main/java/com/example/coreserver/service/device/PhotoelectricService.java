package com.example.coreserver.service.device;

import com.example.coreserver.grpc.common.DeviceId;
import com.example.coreserver.grpc.common.Empty;
import com.example.coreserver.grpc.common.Response;
import com.example.coreserver.grpc.photoelectric.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class PhotoelectricService {

    @GrpcClient("device-server")
    private PhotoelectricServiceGrpc.PhotoelectricServiceBlockingStub stub;

    public DeviceList getPhotoelectricDevices() {
        DeviceList list = stub.getPhotoelectricDevices(Empty.newBuilder().build());
        return list;
    }

    public Response setTargetParameters(TargetParameters target) {
        Response response = stub.setTargetParameters(target);
        return response;
    }

    public Response setCorrections(Corrections corrections) {
        Response response = stub.setCorrections(corrections);
        return response;
    }

    public Response setNavigationParameters(NavigationParameters navigation) {
        Response response = stub.setNavigationParameters(navigation);
        return response;
    }

    public Response controlServo(ServoControl control) {
        Response response = stub.controlServo(control);
        return response;
    }

    public Response setWorkMode(WorkMode mode) {
        Response response = stub.setWorkMode(mode);
        return response;
    }

    public Response setIRPower(PowerControl power) {
        Response response = stub.setIRPower(power);
        return response;
    }

    public Response setLaserPower(PowerControl power) {
        Response response = stub.setLaserPower(power);
        return response;
    }

    public Response setLaserEnergy(LaserEnergy energy) {
        Response response = stub.setLaserEnergy(energy);
        return response;
    }

    public Response launchOrStop(LaunchControl launchControl) {
        Response response = stub.launchOrStop(launchControl);
        return response;
    }

    public Response setZeroPosition(DeviceId deviceId) {
        Response response = stub.setZeroPosition(deviceId);
        return response;
    }

    public Response setTrackingMode(TrackingMode mode) {
        Response response = stub.setTrackingMode(mode);
        return response;
    }

    public Response setCaptureMode(CaptureMode mode) {
        Response response = stub.setCaptureMode(mode);
        return response;
    }

    public Response setRadarGuidanceMode(RadarGuidanceParameters guidance) {
        Response response = stub.setRadarGuidanceMode(guidance);
        return response;
    }

    public Response setMixGuidanceMode(MixGuidanceParameters guidance) {
        Response response = stub.setMixGuidanceMode(guidance);
        return response;
    }

    public Response cancelGuidanceMode(DeviceId deviceId) {
        Response response = stub.cancelGuidanceMode(deviceId);
        return response;
    }

    public Response manipulate(ManipulateCommand manipulateCommand) {
        Response response = stub.manipulate(manipulateCommand);
        return response;
    }

    public Response setChannel(ChannelControl channelControl) {
        Response response = stub.setChannel(channelControl);
        return response;
    }

    public Response setTargetPolarity(PolarityControl polarityControl) {
        Response response = stub.setTargetPolarity(polarityControl);
        return response;
    }

    public Response setIRPolarity(PolarityControl polarityControl) {
        Response response = stub.setIRPolarity(polarityControl);
        return response;
    }

    public Response initNorth(InitNorthRequest request) {
        Response response = stub.initNorth(request);
        return response;
    }

    public AngleData getAngleData(AngleRequest angleRequest) {
        return stub.getAngleData(angleRequest);
    }

    public Response trackerManualTracking(ManualTrackingCommand manualtrackingcommand){
        Response response = stub.trackerManualTracking(manualtrackingcommand);
        return response;
    }

    public Response setStandby(DeviceId request){
        Response response = stub.setStandby(request);
        return response;
    }

    public Response singleSelfCheck(DeviceId request){
        Response response = stub.singleSelfCheck(request);
        return response;
    }

    public Response setTargetRecognition(RecognitionControl request){
        Response response = stub.setTargetRecognition(request);
        return response;
    }

    /**
     * 停止引导
     */
    public void stopGuidance(GuidanceStopParams params) {
        stub.stopGuidance(params);
    }
}

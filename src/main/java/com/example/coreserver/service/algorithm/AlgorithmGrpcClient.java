package com.example.coreserver.service.algorithm;

import com.example.coreserver.grpc.algorithm.*;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import io.grpc.stub.StreamObserver;
import com.example.coreserver.grpc.algorithm.Result;
import com.example.coreserver.grpc.algorithm.Empty;
import javax.annotation.PostConstruct;


@Slf4j
@Component
public class AlgorithmGrpcClient {

    @GrpcClient("algorithm-server")
    private AntiDroneAlgorithmServiceGrpc.AntiDroneAlgorithmServiceBlockingStub stub;

    @GrpcClient("algorithm-server")
    private AntiDroneAlgorithmServiceGrpc.AntiDroneAlgorithmServiceStub asyncStub;

    public void PushFusionData(String jsonData) {
        try {
            log.info("===============发送给算法的数据 start========================");
            log.info(jsonData);
            log.info("===============发送给算法的数据 end  ========================");
            RawData request = RawData.newBuilder()
                    .setJsonData(ByteString.copyFrom(jsonData, StandardCharsets.UTF_8))
                    .build();
            stub.pushFusionData(request);
            log.debug("Successfully sent raw data to fusion");
        } catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            if (status.getCode() == Status.UNAVAILABLE.getCode()) {
                log.warn("Waiting for gRPC connection");
            } else {
                log.error("RPC failed: {}", status);
            }
        }
    }

    public void PushTrackData(String jsonData) {
        try {
//            RawData request = RawData.newBuilder()
//                    .setJsonData(ByteString.copyFrom(jsonData, StandardCharsets.UTF_8))
//                    .build();
//            stub.pushTrackData(request);
//            log.debug("Successfully sent raw data to track");
        } catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            if (status.getCode() == Status.UNAVAILABLE.getCode()) {
                log.warn("Waiting for gRPC connection");
            } else {
                log.error("RPC failed: {}", status);
            }
        }
    }

    public void PushTdoaData(String jsonData) {
        try {
            RawData request = RawData.newBuilder()
                    .setJsonData(ByteString.copyFrom(jsonData, StandardCharsets.UTF_8))
                    .build();
            stub.pushTrackData(request);
            log.debug("Successfully sent tdoa  data to track");
        } catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            if (status.getCode() == Status.UNAVAILABLE.getCode()) {
                log.warn("Waiting for gRPC connection");
            } else {
                log.error("RPC failed: {}", status);
            }
        }
    }




    /**
     * 推送图像数据
     * @param timestamp 图像时间戳
     */
    public void pushImageData(String timestamp, byte[] imageData) {
        try {
//            Image request = Image.newBuilder()
//                    .setTimestamp(timestamp)
//                    .setData(ByteString.copyFrom(imageData))
//                    .build();
//            stub.pushImageData(request);
//            log.debug("Successfully sent image data to algorithm service");
//            log.info(String.valueOf(request));
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus());
        }
    }

//     /**
//      * 拉取融合算法结果数据
//      * @return 融合算法返回的结果数据
//      */
//     public String PullResultFusion() {
//         try {
//             Result result = stub.pullResultFusion(Empty.newBuilder().build());
// //            log.info("Received timestamp: {}", System.currentTimeMillis());
// //                    result.getJsonData() != null ? result.getJsonData().size() : 0);

//             if (result.getJsonData() == null || result.getJsonData().isEmpty()) {
//                 log.warn("Received empty JSON data from algorithm server");
//                 return null;
//             }

//             String jsonString = result.getJsonData().toStringUtf8();
//             log.debug("JSON data content: {}", jsonString);
//             return jsonString;
//         } catch (StatusRuntimeException e) {
//             log.error("RPC failed: {} - {}", e.getStatus(), e.getMessage());
//             if (e.getStatus().getCode() == io.grpc.Status.Code.UNIMPLEMENTED) {
//                 log.error("Method not implemented on server side. Please check if the server has implemented the PullResultFusion method.");
//             }
//             return null;
//         } catch (Exception e) {
//             log.error("Unexpected error while pulling fusion data: {}", e.getMessage(), e);
//             return null;
//         }
//     }

//     /**
//      * 拉取轨迹预测算法结果数据
//      * @return 轨迹预测算法返回的结果数据
//      */
//     public String PullResultTrack() {
//         try {
//             Result result = stub.pullResultTrack(Empty.newBuilder().build());
// //            log.info("Received timestamp: {}", System.currentTimeMillis());
// //                    result.getJsonData() != null ? result.getJsonData().size() : 0);

//             if (result.getJsonData() == null || result.getJsonData().isEmpty()) {
//                 log.warn("Received empty JSON data from algorithm server");
//                 return null;
//             }

//             String jsonString = result.getJsonData().toStringUtf8();
//             log.debug("JSON data content: {}", jsonString);
//             return jsonString;
//         } catch (StatusRuntimeException e) {
//             log.error("RPC failed: {} - {}", e.getStatus(), e.getMessage());
//             if (e.getStatus().getCode() == io.grpc.Status.Code.UNIMPLEMENTED) {
//                 log.error("Method not implemented on server side. Please check if the server has implemented the PullResultTrack method.");
//             }
//             return null;
//         } catch (Exception e) {
//             log.error("Unexpected error while pulling track data: {}", e.getMessage(), e);
//             return null;
//         }
//     }

//     /**
//      * 拉取目标识别算法结果数据
//      * @return 目标识别算法返回的结果数据
//      */
//     public String PullResultImage() {
//         try {
//             Result result = stub.pullResultImage(Empty.newBuilder().build());
// //            log.info("Received timestamp: {}", System.currentTimeMillis());
// //                    result.getJsonData() != null ? result.getJsonData().size() : 0);

//             if (result.getJsonData() == null || result.getJsonData().isEmpty()) {
//                 log.warn("Received empty JSON data from algorithm server");
//                 return null;
//             }

//             String jsonString = result.getJsonData().toStringUtf8();
//             log.debug("JSON data content: {}", jsonString);
//             return jsonString;
//         } catch (StatusRuntimeException e) {
//             log.error("RPC failed: {} - {}", e.getStatus(), e.getMessage());
//             if (e.getStatus().getCode() == io.grpc.Status.Code.UNIMPLEMENTED) {
//                 log.error("Method not implemented on server side. Please check if the server has implemented the PullResultImage method.");
//             }
//             return null;
//         } catch (Exception e) {
//             log.error("Unexpected error while pulling image data: {}", e.getMessage(), e);
//             return null;
//         }
//     }

    /**
     * 订阅融合数据流
     */
    public void subscribeFusionStream(StreamObserver<Result> responseObserver) {
        asyncStub.subscribeFusion(Empty.newBuilder().build(), responseObserver);
    }

    /**
     * 订阅轨迹预测数据流
     */
    public void subscribeTrackStream(StreamObserver<Result> responseObserver) {
        asyncStub.subscribeTrack(Empty.newBuilder().build(), responseObserver);
    }

    /**
     * 订阅图像数据流
     */
    public void subscribeImageStream(StreamObserver<Result> responseObserver) {
        asyncStub.subscribeImage(Empty.newBuilder().build(), responseObserver);
    }
}
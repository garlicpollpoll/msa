package com.database.cache.inbound.adapter;

import com.database.cache.domain.CacheDomain;
import com.database.cache.dto.ResponseDto;
import com.database.cache.inbound.port.RedisService;
import com.database.cache.outbound.port.CacheStorage;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;
import redis.Redis;
import redis.RedisServiceGrpc;

@GrpcService
@RequiredArgsConstructor
public class RedisGrpcService extends RedisServiceGrpc.RedisServiceImplBase {

    private final RedisService redisService;

    @Override
    public void save(Redis.CommonRequest request, StreamObserver<Redis.CommonResponse> responseObserver) {
        String key = request.getKey();
        String value = request.getValue();

        ResponseDto<?> response = redisService.saveKey(key, value);

        Redis.CommonResponse builderResponse = Redis.CommonResponse.newBuilder()
                .setResponse(response.getResponse().toString())
                .build();

        responseObserver.onNext(builderResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void hasKey(Redis.CommonRequest request, StreamObserver<Redis.HasKeyResponse> responseObserver) {
        String key = request.getKey();

        boolean isExist = redisService.hasKey(key);

        Redis.HasKeyResponse builderResponse;

        if (isExist) {
            builderResponse = Redis.HasKeyResponse.newBuilder()
                    .setResponse(true)
                    .build();
        }
        else {
            builderResponse = Redis.HasKeyResponse.newBuilder()
                    .setResponse(false)
                    .build();
        }

        responseObserver.onNext(builderResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void get(Redis.CommonRequest request, StreamObserver<Redis.CommonResponse> responseObserver) {
        String key = request.getKey();
        String type = request.getType();

        ResponseDto<?> response = redisService.getValue(key, type);
        Boolean success = response.getSuccess();

        Redis.CommonResponse builderResponse;

        if (success) {
            builderResponse = Redis.CommonResponse.newBuilder()
                    .setResponse(response.getResponse().toString())
                    .build();
        }
        else {
            builderResponse = Redis.CommonResponse.newBuilder()
                    .setResponse("")
                    .build();
        }

        responseObserver.onNext(builderResponse);
        responseObserver.onCompleted();
    }
}

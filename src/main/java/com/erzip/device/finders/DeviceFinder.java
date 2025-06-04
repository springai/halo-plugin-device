package com.erzip.device.finders;



import com.erzip.device.vo.DeviceGroupVo;
import com.erzip.device.vo.DeviceVo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;

public interface DeviceFinder {

    Flux<DeviceVo> listAll();

    Mono<ListResult<DeviceVo>> list(Integer page, Integer size);

    Mono<ListResult<DeviceVo>> list(Integer page, Integer size, String group);

    Flux<DeviceVo> listBy(String groupName);

    Flux<DeviceGroupVo> groupBy();

    Mono<DeviceGroupVo> groupBy(String groupName);

}

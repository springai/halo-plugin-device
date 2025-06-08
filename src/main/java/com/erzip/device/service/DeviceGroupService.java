package com.erzip.device.service;


import com.erzip.device.extension.DeviceGroup;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.router.IListRequest.QueryListRequest;

public interface DeviceGroupService {
    Mono<ListResult<DeviceGroup>> listDeviceGroup(QueryListRequest query);

    Mono<DeviceGroup> deleteDeviceGroup(String name);
}

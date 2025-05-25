package com.erzip.device.service;

import com.erzip.device.DeviceQuery;
import com.erzip.device.extension.Device;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;

public interface DeviceService {
    Mono<ListResult<Device>> listDevice(DeviceQuery query);
}

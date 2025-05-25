package com.erzip.device.service.impl;

import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import com.erzip.device.extension.Device;
import com.erzip.device.extension.DeviceGroup;
import com.erzip.device.service.DeviceGroupService;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.IListRequest.QueryListRequest;

@Component
public class DeviceGroupServiceImpl implements DeviceGroupService {
    private final ReactiveExtensionClient client;

    public DeviceGroupServiceImpl(ReactiveExtensionClient client) {
        this.client = client;
    }


    private Predicate<DeviceGroup> deviceListPredicate(QueryListRequest query) {
        return labelAndFieldSelectorToPredicate(query.getLabelSelector(),
            query.getFieldSelector()
        );
    }

    @Override
    public Mono<ListResult<DeviceGroup>> listDeviceGroup(QueryListRequest query) {
        return this.client.list(DeviceGroup.class, deviceListPredicate(query),
            null, query.getPage(), query.getSize()
        ).flatMap(listResult -> Flux.fromStream(
                listResult.get().map(this::populateDevices))
            .concatMap(Function.identity())
            .collectList()
            .map(groups -> new ListResult<>(listResult.getPage(),
                listResult.getSize(), listResult.getTotal(), groups
            )));
    }

    @Override
    public Mono<DeviceGroup> deleteDeviceGroup(String name) {
        return this.client.fetch(DeviceGroup.class, name).flatMap(
            deviceGroup -> this.client.delete(deviceGroup)
                .flatMap(deleted -> this.client.list(Device.class,
                    (device) -> StringUtils.equals(name,
                        device.getSpec().getGroupName()
                    ), null
                ).flatMap(this.client::delete).then(Mono.just(deleted))));
    }

    private Mono<DeviceGroup> populateDevices(DeviceGroup deviceGroup) {
        return Mono.just(deviceGroup).flatMap(fg -> fetchDeviceCount(fg).doOnNext(
                count -> fg.getStatusOrDefault().setDeviceCount(count))
            .thenReturn(fg));
    }
    Mono<Integer> fetchDeviceCount(DeviceGroup deviceGroup) {
        Assert.notNull(deviceGroup, "设备分组不能为null");
        String name = deviceGroup.getMetadata().getName();
        return client.list(Device.class, device -> !device.isDeleted()
                && device.getSpec().getGroupName().equals(name), null)
            .count()
            .defaultIfEmpty(0L)
            .map(Long::intValue);
    }
}

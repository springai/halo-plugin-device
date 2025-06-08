package com.erzip.device.service.impl;

import static run.halo.app.extension.router.selector.SelectorUtil.
    labelAndFieldSelectorToListOptions;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.index.query.QueryFactory;


import com.erzip.device.extension.Device;
import com.erzip.device.extension.DeviceGroup;
import com.erzip.device.service.DeviceGroupService;
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


    private ListOptions toListOptions(QueryListRequest query) {
        return labelAndFieldSelectorToListOptions(
            query.getLabelSelector(), query.getFieldSelector()
        );
    }

    @Override
    public Mono<ListResult<DeviceGroup>> listDeviceGroup(QueryListRequest query) {
        return this.client.listBy(
                DeviceGroup.class,
                toListOptions(query),
                PageRequestImpl.of(query.getPage(), query.getSize())
            )
            .flatMap(listResult -> Flux.fromStream(listResult.get())
                .flatMap(this::populateDevices)
                .collectList()
                .map(groups -> new ListResult<>(
                    listResult.getPage(),
                    listResult.getSize(),
                    listResult.getTotal(),
                    groups
                ))
            );
    }

    @Override
    public Mono<DeviceGroup> deleteDeviceGroup(String name) {
        return this.client.fetch(DeviceGroup.class, name)
            .flatMap(this.client::delete)
            .flatMap(deleted -> {
                    var listOptions = ListOptions.builder()
                        .andQuery(QueryFactory.equal("spec.groupName", name))
                        .build();
                    return this.client.listAll(Device.class, listOptions, Sort.unsorted())
                        .flatMap(this.client::delete)
                        .then()
                        .thenReturn(deleted);
                }
            );
    }

    private Mono<DeviceGroup> populateDevices(DeviceGroup deviceGroup) {
        return fetchDeviceCount(deviceGroup)
            .doOnNext(count -> deviceGroup.getStatusOrDefault().setDeviceCount(count))
            .thenReturn(deviceGroup);
    }

    private Mono<Integer> fetchDeviceCount(DeviceGroup deviceGroup) {
        Assert.notNull(deviceGroup, "设备分组不能为null");
        String name = deviceGroup.getMetadata().getName();

        return client.list(
                Device.class,
                device -> !device.isDeleted()
                    &&
                    device.getSpec().getGroupName().equals(name),
                null
            )
            .count()
            .defaultIfEmpty(0L)
            .map(Long::intValue);
    }
}

package com.erzip.device.finders.impl;

import static org.springframework.data.domain.Sort.Order.asc;
import static org.springframework.data.domain.Sort.Order.desc;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.index.query.QueryFactory;

import com.erzip.device.extension.Device;
import com.erzip.device.extension.DeviceGroup;
import com.erzip.device.finders.DeviceFinder;
import com.erzip.device.vo.DeviceGroupVo;
import com.erzip.device.vo.DeviceVo;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.Finder;

@Finder("deviceFinder")
public class DeviceFinderImpl implements DeviceFinder {
    private final ReactiveExtensionClient client;

    public DeviceFinderImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public Flux<DeviceVo> listAll() {
        return this.client.listAll(
            Device.class,
            ListOptions.builder().build(),
            defaultSort()
            )
            .map(DeviceVo::new);
    }


    @Override
    public Mono<ListResult<DeviceVo>> list(Integer page, Integer size) {
        return list(page, size, null);
    }

    @Override
    public Mono<ListResult<DeviceVo>> list(Integer page, Integer size, String group) {
        return pageDevice(page, size, group);
    }
    private Mono<ListResult<DeviceVo>> pageDevice(Integer page, Integer size,String group){
        var builder = ListOptions.builder();
        if (StringUtils.isNotEmpty(group)) {
            builder.andQuery(QueryFactory.equal("spec.groupName", group));
        }
        return client.listBy(Device.class, builder.build(),
                PageRequestImpl.of(page, size, defaultSort()))
            .flatMap(listResult -> Flux.fromStream(listResult.get())
                .map(DeviceVo::new)
                .collectList()
                .map(list -> new ListResult<>(
                    listResult.getPage(), listResult.getSize(), listResult.getTotal(), list
                ))
            );
    }


    @Override
    public Flux<DeviceVo> listBy(String groupName) {
        var options = ListOptions.builder()
            .andQuery(QueryFactory.equal("spec.groupName", groupName))
            .build();
        return client.listAll(Device.class, options, defaultSort()).map(DeviceVo::new);
    }

    @Override
    public Flux<DeviceGroupVo> groupBy() {
        return this.client.listAll(DeviceGroup.class,
                ListOptions.builder().build(), defaultSort())
            .concatMap(group -> {
                return this.listBy(group.getMetadata().getName())
                    .collectList()
                    .map(
                        devices -> {
                            DeviceGroup.PostGroupStatus status = group.getStatus();
                            status.setDeviceCount(devices.size());
                            return new DeviceGroupVo(group.getMetadata(), group.getSpec(),status,devices);
                        });
            });
    }

    @Override
    public Mono<DeviceGroupVo> groupBy(String groupName) {
        return this.client.listAll(DeviceGroup.class,
                ListOptions.builder().build(), defaultSort())
            .filter(group -> group.getMetadata().getName().equals(groupName))
            .next()
            .flatMap(group ->
                this.listBy(group.getMetadata().getName())
                    .collectList()
                    .map(devices -> {
                        DeviceGroup.PostGroupStatus status = group.getStatus();
                        status.setDeviceCount(devices.size());
                        return new DeviceGroupVo(
                            group.getMetadata(),
                            group.getSpec(),
                            status,
                            devices
                        );
                    })
            );
    }

    private static Sort defaultSort() {
        return Sort.by(
            asc("spec.priority"),
            desc("metadata.creationTimestamp"),
            asc("metadata.name")
        );
    }
}

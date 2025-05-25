package com.erzip.device.finders.impl;

import com.erzip.device.extension.Device;
import com.erzip.device.extension.DeviceGroup;
import com.erzip.device.finders.DeviceFinder;

import com.erzip.device.vo.DeviceGroupVo;
import com.erzip.device.vo.DeviceVo;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.comparator.Comparators;
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
        return this.client.list(Device.class, null, defaultDeviceComparator())
            .flatMap(device -> Mono.just(new DeviceVo(device)));
    }



    @Override
    public Mono<ListResult<DeviceVo>> list(Integer page, Integer size) {
        return list(page, size, null);
    }

    @Override
    public Mono<ListResult<DeviceVo>> list(Integer page, Integer size, String group) {
        return pageDevice(page, size, group, null, defaultDeviceComparator());
    }
    private Mono<ListResult<DeviceVo>> pageDevice(Integer page, Integer size,
        String group, Predicate<Device> devicePredicate,
        Comparator<Device> comparator) {
        Predicate<Device> predicate = devicePredicate == null ? device -> true
            : devicePredicate;
        if (StringUtils.isNotEmpty(group)) {
            predicate = predicate.and(device -> {
                String groupName = device.getSpec().getGroupName();
                return StringUtils.equals(groupName, group);
            });
        }
        return client.list(Device.class, predicate, comparator,
            pageNullSafe(page), sizeNullSafe(size)
        ).flatMap(list -> Flux.fromStream(list.get())
            .concatMap(device -> Mono.just(new DeviceVo(device)))
            .collectList()
            .map(momentVos -> new ListResult<>(list.getPage(), list.getSize(),
                list.getTotal(), momentVos
            ))).defaultIfEmpty(new ListResult<>(page, size, 0L, List.of()));
    }


    @Override
    public Flux<DeviceVo> listBy(String groupName) {
        return client.list(Device.class, device -> {
            String group = device.getSpec().getGroupName();
            return StringUtils.equals(group, groupName);
        }, defaultDeviceComparator()).flatMap(
            device -> Mono.just(new DeviceVo(device)));
    }

    @Override
    public Flux<DeviceGroupVo> groupBy() {
        return this.client.list(DeviceGroup.class, null,
            defaultGroupComparator()
        ).concatMap(group -> {
            return this.listBy(group.getMetadata().getName()).collectList().map(
                devices -> {
                    DeviceGroup.PostGroupStatus status = group.getStatus();
                    status.setDeviceCount(devices.size());
                    return new DeviceGroupVo(group.getMetadata(), group.getSpec(),status,devices);
                });
        });
    }



    private Comparator<Device> defaultDeviceComparator() {
        Function<Device, Integer> priority = link -> link.getSpec()
            .getPriority();
        Function<Device, Instant> createTime = link -> link.getMetadata()
            .getCreationTimestamp();
        Function<Device, String> name = link -> link.getMetadata().getName();
        return Comparator.comparing(priority, Comparators.nullsLow())
            .thenComparing(Comparator.comparing(createTime).reversed())
            .thenComparing(name);
    }
    static Comparator<DeviceGroup> defaultGroupComparator() {
        Function<DeviceGroup, Integer> priority = group -> group.getSpec()
            .getPriority();
        Function<DeviceGroup, Instant> createTime = group -> group.getMetadata()
            .getCreationTimestamp();
        Function<DeviceGroup, String> name = group -> group.getMetadata()
            .getName();
        return Comparator.comparing(priority, Comparators.nullsLow())
            .thenComparing(createTime)
            .thenComparing(name);
    }


    int pageNullSafe(Integer page) {
        return ObjectUtils.defaultIfNull(page, 1);
    }

    int sizeNullSafe(Integer size) {
        return ObjectUtils.defaultIfNull(size, 10);
    }

}

package com.erzip.device.service.impl;

import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import com.erzip.device.DeviceQuery;
import com.erzip.device.DeviceSorter;
import com.erzip.device.extension.Device;
import com.erzip.device.service.DeviceService;
import java.util.Comparator;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;

@Component
public class DeviceServiceImpl implements DeviceService {

    private final ReactiveExtensionClient client;

    public DeviceServiceImpl(ReactiveExtensionClient client){
        this.client = client;
    }

    @Override
    public Mono<ListResult<Device>> listDevice(DeviceQuery query) {
        Comparator<Device> comparator = DeviceSorter.from(query.getSort(),
            query.getSortOrder()
        );
        return this.client.list(Device.class, deviceListPredicate(query),
            comparator, query.getPage(), query.getSize()
        );
    }

    private Predicate<Device> deviceListPredicate(DeviceQuery query) {
        Predicate<Device> predicate = device -> true;
        String keyword = query.getKeyword();

        if (keyword != null) {
            predicate = predicate.and(device -> {
                String displayName = device.getSpec().getDisplayName();
                return StringUtils.containsIgnoreCase(displayName, keyword);
            });
        }

        String groupName = query.getGroup();
        if (groupName != null) {
            predicate = predicate.and(device -> {
                String group = device.getSpec().getGroupName();
                return StringUtils.equals(group, groupName);
            });
        }

        Predicate<Extension> labelAndFieldSelectorPredicate
            = labelAndFieldSelectorToPredicate(query.getLabelSelector(),
            query.getFieldSelector()
        );
        return predicate.and(labelAndFieldSelectorPredicate);
    }
}

package com.erzip.device.service.impl;

import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToListOptions;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.index.query.QueryFactory;

import com.erzip.device.DeviceQuery;
import com.erzip.device.extension.Device;
import com.erzip.device.service.DeviceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
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
        return this.client.listBy(
            Device.class,
            toListOptions(query),
            PageRequestImpl.of(query.getPage(), query.getSize(), query.getSort()));
    }

    private ListOptions toListOptions(DeviceQuery query) {
        var builder = ListOptions.builder(labelAndFieldSelectorToListOptions(
            query.getLabelSelector(), query.getFieldSelector())
        );

        if (StringUtils.isNotBlank(query.getKeyword())) {
            builder.andQuery(QueryFactory.contains("spec.displayName", query.getKeyword()));
        }
        if (StringUtils.isNotBlank(query.getGroup())) {
            builder.andQuery(QueryFactory.equal("spec.groupName", query.getGroup()));
        }
        return builder.build();
    }

}

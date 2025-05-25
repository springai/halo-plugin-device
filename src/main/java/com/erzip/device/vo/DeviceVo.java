package com.erzip.device.vo;

import com.erzip.device.extension.Device;
import run.halo.app.extension.MetadataOperator;

public record DeviceVo(MetadataOperator metadata, Device.DeviceSpec spec) {
    public DeviceVo(Device device){
        this(device.getMetadata(), device.getSpec());
    }
}

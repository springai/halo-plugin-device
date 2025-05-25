package com.erzip.device.vo;

import com.erzip.device.extension.DeviceGroup;
import run.halo.app.extension.MetadataOperator;

import java.util.List;

public record DeviceGroupVo(MetadataOperator metadata, DeviceGroup.DeviceGroupSpec spec,
                            DeviceGroup.PostGroupStatus status,
                            List<DeviceVo> devices) {
}

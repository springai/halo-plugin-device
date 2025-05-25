package com.erzip.device.extension;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import java.util.Objects;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "core.erzip.com",version = "v1alpha1", kind = "Device", plural = "devices", singular = "deivce")
public class Device extends AbstractExtension {
    private DeviceSpec spec;

    @Data
    public class DeviceSpec {

        @Schema(requiredMode = REQUIRED, description = "设备名称", example = "MacBook Pro")
        private String displayName;

        @Schema(requiredMode = REQUIRED,description = "设备标签", example = "M1Pro 32G / 1TB")
        private String label;

        @Schema(requiredMode = REQUIRED,description = "设备描述", example = "屏幕显示效果...")
        private String description;

        @Schema(requiredMode = REQUIRED, description = "设备封面地址")
        private String cover;

        @Schema(requiredMode = REQUIRED, description = "查看详情链接", example = "https://www.apple.com/cn/macbook-pro/")
        private String url;

        private Integer priority;

        @Schema(requiredMode = REQUIRED,pattern = "^\\S+$")
        private String groupName;
    }

    @JsonIgnore
    public boolean isDeleted(){
        return Objects.equals(true,getMetadata().getDeletionTimestamp() != null);
    }
}

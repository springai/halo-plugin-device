package com.erzip.device.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "core.erzip.com", version = "v1alpha1", kind = "DeviceGroup", plural = "devicegroups",
    singular = "devicegroup")
public class DeviceGroup extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private DeviceGroupSpec spec;

    @Schema
    private PostGroupStatus status;

    @Data
    public static class DeviceGroupSpec{

        @Schema(requiredMode = REQUIRED, description = "设备分类名称", example = "办公设备")
        private String displayName;

        @Schema(description = "设备分类描述", example = "提升自己生产效率的硬件设备")
        private String description;

        private Integer priority;
    }

    @JsonIgnore
    public PostGroupStatus getStatusOrDefault(){
        if (this.status == null){
            this.status = new PostGroupStatus();
        }
        return this.status;
    }

    @Data
    public static class PostGroupStatus{
        public Integer deviceCount;
    }
}

package com.erzip.device.extension;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "core.erzip.com",version = "v1alpha1", kind = "DeviceComment", plural = "devicecomments", singular = "deivcecomment")
public class DeviceComment extends AbstractExtension {

    @Data
    public class DeviceCommentSpec{
        private Boolean haloCommentEnabled;
    }
}

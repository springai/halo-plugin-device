package com.erzip.device;

import com.erzip.device.extension.Device;
import com.erzip.device.extension.DeviceComment;
import com.erzip.device.extension.DeviceGroup;
import org.springframework.stereotype.Component;
import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;


@Component
public class DevicePlugin extends BasePlugin {
    private final SchemeManager schemeManager;
    public DevicePlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        System.out.println("插件启动成功！");
        schemeManager.register(Device.class);
        schemeManager.register(DeviceGroup.class);
        schemeManager.register(DeviceComment.class);
    }

    @Override
    public void stop() {
        System.out.println("插件停止！");
        schemeManager.unregister(schemeManager.get(Device.class));
        schemeManager.unregister(schemeManager.get(DeviceGroup.class));
        schemeManager.unregister(schemeManager.get(DeviceComment.class));
    }
}

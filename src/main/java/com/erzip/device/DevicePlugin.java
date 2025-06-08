package com.erzip.device;

import com.erzip.device.extension.Device;
import com.erzip.device.extension.DeviceComment;
import com.erzip.device.extension.DeviceGroup;
import org.springframework.stereotype.Component;
import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

import static run.halo.app.extension.index.IndexAttributeFactory.simpleAttribute;
import run.halo.app.extension.index.IndexSpec;

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
        schemeManager.register(Device.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.groupName")
                .setIndexFunc(simpleAttribute(Device.class, device->
                    device.getSpec() == null ? "" : device.getSpec().getGroupName()
                ))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.displayName")
                .setIndexFunc(simpleAttribute(Device.class, device ->
                    device.getSpec() == null ? "" : device.getSpec().getDisplayName()
                ))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.priority")
                .setIndexFunc(simpleAttribute(Device.class, device ->
                    device.getSpec() == null || device.getSpec().getPriority() == null
                        ? String.valueOf(0) : device.getSpec().getPriority().toString()
                ))
            );
        });
        schemeManager.register(DeviceGroup.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.priority")
                .setIndexFunc(simpleAttribute(DeviceGroup.class, group ->
                    group.getSpec() == null || group.getSpec().getPriority() == null
                        ? String.valueOf(0) : group.getSpec().getPriority().toString()
                ))
            );
        });
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

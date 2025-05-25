package com.erzip.device;


import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

import com.erzip.device.extension.DeviceGroup;
import com.erzip.device.service.DeviceGroupService;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.router.IListRequest;

@Component
@RequiredArgsConstructor
public class DeviceGroupEndpoint implements CustomEndpoint {

    private final DeviceGroupService deviceGroupService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return route()
            .GET("devicegroups", this::listDeviceGroup,
                builder -> {
                    builder.operationId("ListDevices")
                        .description("List devices.")
                        .response(responseBuilder().implementation(
                            ListResult.generateGenericClass(DeviceGroup.class))
                        );

                    DeviceQuery.buildParameters(builder);
                }
            )
            .DELETE("devicegroups/{name}", this::deleteDeviceGroup,
                builder -> builder.operationId("DeleteDeviceGroup")
                    .description("Delete device group.")
                    .parameter(parameterBuilder()
                        .name("name")
                        .in(ParameterIn.PATH)
                        .description("Device group name")
                        .implementation(String.class)
                        .required(true)
                    )
                    .response(responseBuilder().implementation(DeviceGroup.class))
            )
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("console.api.device.erzip.com/v1alpha1");
    }

    private Mono<ServerResponse> deleteDeviceGroup(ServerRequest serverRequest) {
        String name = serverRequest.pathVariable("name");
        if (StringUtils.isBlank(name)) {
            throw new ServerWebInputException("Device group name must not be blank.");
        }
        return deviceGroupService.deleteDeviceGroup(name)
            .flatMap(deviceGroup -> ServerResponse.ok().bodyValue(deviceGroup));
    }

    private Mono<ServerResponse> listDeviceGroup(ServerRequest serverRequest) {
        IListRequest.QueryListRequest request = new DeviceQuery(serverRequest.queryParams());
        return deviceGroupService.listDeviceGroup(request)
            .flatMap(deviceGroups -> ServerResponse.ok().bodyValue(deviceGroups));
    }
}

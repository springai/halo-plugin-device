package com.erzip.device;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;

import com.erzip.device.finders.DeviceFinder;

import com.erzip.device.vo.DeviceGroupVo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.erzip.device.vo.DeviceVo;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.app.theme.TemplateNameResolver;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.UrlContextListResult;

@Component
@AllArgsConstructor
public class DeviceRouter {
    private static final String GROUP_PARAM = "group";

    private DeviceFinder deviceFinder;

    private final ReactiveSettingFetcher settingFetcher;

    private final TemplateNameResolver templateNameResolver;

    @Bean
    RouterFunction<ServerResponse> deviceTemplateRouter() {
        return route(GET("/devices"), this::renderDevicePage);
    }

    Mono<ServerResponse> renderDevicePage(ServerRequest request) {
        // 或许你需要准备你需要提供给模板的默认数据，非必须
        return templateNameResolver.resolveTemplateNameOrDefault(request.exchange(), "devices")
            .flatMap(templateName -> ServerResponse.ok().render(templateName,
                Map.of("groups", deviceGroups(),
                    "devices", deviceList(request),
                    ModelConst.TEMPLATE_ID, "devices",
                    "title", getDevicesTitle()
                )
            ));
    }


    @Bean
    RouterFunction<ServerResponse> deviceRouter() {
        return route(GET("/devices").or(GET("/devices/page/{page:\\d+}")),
            handlerFunction()
        );
    }

    private HandlerFunction<ServerResponse> handlerFunction() {
        return request -> ServerResponse.ok().render("devices",
            Map.of("groups", deviceGroups(),
                "devices", deviceList(request),
                ModelConst.TEMPLATE_ID, "devices",
                "title", getDevicesTitle()
            )
        );
    }

    private Mono<UrlContextListResult<DeviceVo>> deviceList(ServerRequest request) {
        String path = request.path();
        int pageNum = pageNumInPathVariable(request);
        String group = groupPathQueryParam(request);
        return this.settingFetcher.get("base")
            .map(item -> item.get("pageSize").asInt(10))
            .defaultIfEmpty(10)
            .flatMap(pageSize -> deviceFinder.list(pageNum, pageSize, group)
                .map(list -> new UrlContextListResult.Builder<DeviceVo>()
                    .listResult(list)
                    .nextUrl(appendGroupParam(
                        PageUrlUtils.nextPageUrl(path, totalPage(list)), group)
                    )
                    .prevUrl(appendGroupParam(PageUrlUtils.prevPageUrl(path), group))
                    .build()
                )
            );

    }

    private static String appendGroupParam(String path, String group) {
        return UriComponentsBuilder.fromPath(path)
            .queryParamIfPresent(GROUP_PARAM, Optional.ofNullable(group))
            .build()
            .toString();
    }

    private int pageNumInPathVariable(ServerRequest request) {
        String page = request.pathVariables().get("page");
        return NumberUtils.toInt(page, 1);
    }

    private String groupPathQueryParam(ServerRequest request) {
        return request.queryParam(GROUP_PARAM)
            .filter(StringUtils::isNotBlank)
            .orElse(null);
    }

    Mono<String> getDevicesTitle() {
        return this.settingFetcher.get("base").map(
            setting -> setting.get("title").asText("个人设备")).defaultIfEmpty(
            "个人设备");
    }

    private Mono<List<DeviceGroupVo>> deviceGroups() {
        return deviceFinder.groupBy().collectList();
    }

}

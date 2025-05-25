package com.erzip.device;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.extension.router.QueryParamBuildUtil.sortParameter;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.util.MultiValueMap;
import run.halo.app.extension.router.IListRequest;

public class DeviceQuery extends IListRequest.QueryListRequest {

    public DeviceQuery(MultiValueMap<String, String> queryParams) {
        super(queryParams);
    }

    @Schema(description = "按分组查询")
    public String getGroup(){
        return StringUtils.defaultIfBlank(queryParams.getFirst("group"),null);
    }

    @Nullable
    @Schema(description = "按关键字查询")
    public String getKeyword(){
        return StringUtils.defaultIfBlank(queryParams.getFirst("keyword"), null);
    }

    @Schema(description = "设备排序")
    public DeviceSorter getSort(){
        String sort = queryParams.getFirst("sort");
        return DeviceSorter.convertFrom(sort);
    }

    @Schema(description = "是否降序排列")
    public Boolean getSortOrder(){
        String sortOrder = queryParams.getFirst("sortOrder");
        return convertBooleanOrNull(sortOrder);
    }
    private Boolean convertBooleanOrNull(String value) {
        return StringUtils.isBlank(value) ? null : Boolean.parseBoolean(value);
    }

    public static void buildParameters(Builder builder) {
        IListRequest.buildParameters(builder);
        builder.parameter(sortParameter())
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("keyword")
                .description("按关键字筛选的设备")
                .implementation(String.class)
                .required(false))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("group")
                .description("设备分组名称")
                .implementation(String.class)
                .required(false))
        ;
    }

}

# Halo-Plugin-Device
这是一款专为Halo建站系统设计的个人设备可视化展示插件，支持高效管理并呈现用户的设备收藏与使用场景。 该插件配置了Halo FinderAPI，并内置了个人设备页面，用户也可根据Finder API文档自行设计个人设备展示页。

## 特性
- 设备分类管理：可自定义分类标题与描述，构建多层级设备展示体系。
- 设备详情配置：支持上传设备图片、命名设备名称、添加个性化标签、撰写详细描述，并支持嵌入查看详情链接，可以是文章链接，也可以是商品链接。
- 内置个人设备展示页，PC端每行最多3个设备，手机端自动切换为单列显示。
- 实现了自定义评论主体，方便收集和展示评论来源。
- 支持Halo FinderAPI，提供标准数据接口，用户可自主对接API自己实现个人设备展示页。

## 特别鸣谢
本插件基于Halo官方发布的图库插件改编而来，感谢Halo官方的开源精神。

### 路由信息
- 模板路径: /templates/devices.html
- 访问路径：/devices


### Finder API

#### groupBy()
#### 描述
获取全部分组列表
##### 参数
无
##### 返回值
List<[#DeviceGroupVo](#devicegroupvo)>
##### 示例
```html
<div class="device-container" th:each="group : ${deviceFinder.groupBy()}" >
    <h1 class="device-title" th:text="${group.spec.displayName}"></h1>
    <p class="device-subtitle" th:text="${group.spec.description}"></p>
    <div class="device-product-grid">
        <div class="device-product-card"  th:each="device : ${group.devices}">
            <img class="device-product-image" data-fancybox="gallery" th:src="${device.spec.cover}" th:alt="${device.spec.displayName}">
            <h2 class="device-product-title" th:text="${device.spec.displayName}"></h2>
            <p class="device-product-subtitle" th:text="${device.spec.label}"></p>
            <p class="device-product-description" th:text="${device.spec.description}"></p>
            <div class="device-buttons">
                <a th:href="${#strings.startsWith(device.spec.url, 'http://')
        || #strings.startsWith(device.spec.url, 'https://')
            ? device.spec.url
            : 'http://' + device.spec.url}"
                   class="device-details-button">查看详情</a>
            </div>
        </div>
    </div>
</div>
```

#### listAll()
##### 描述
获取全部设备内容
##### 参数
无
##### 返回值
List<[#DeviceVo](#devicevo)>
##### 示例
```html
<ul>
    <li th:each="device : ${deviceFinder.listAll()}">
        <img th:src="${device.spec.url}" th:alt="${device.spec.displayName}" width="280">
        <h2 th:text="${device.spec.displayName}"></h2>
        <p th:text="${device.spec.description}"></p>
        <p th:text="${device.spec.label}"></p>
        <a th:href="${device.spec.url}">查看详情</a>
    </li>
</ul>
```

#### listBy(group)
##### 描述
根据分组获取设备列表
##### 参数
1. `group: string` - 设备分组名称, 对应 DeviceGroupVo.metadata.name
##### 返回值
List<[#DeviceVo](#devicevo)>
##### 示例
```html
<ul>
    <li th:each="device : ${deviceFinder.listBy('device-group-05lytpbm')}">
        <img th:src="${device.spec.url}" th:alt="${device.spec.displayName}" width="280">
        <h2 th:text="${device.spec.displayName}"></h2>
        <p th:text="${device.spec.description}"></p>
        <p th:text="${device.spec.label}"></p>
        <a th:href="${device.spec.url}">查看详情</a>
    </li>
</ul>
```

### 自定义评论主体
##### 描述
后台自定义评论主体
##### 示例
```html
<div th:if="${haloCommentEnabled}">
    <halo:comment
            group="core.erzip.com"
            kind="DeviceComment"
            name="plugin-device-comment"
    />
</div>
```


### 类型定义
#### DeviceVo
```json
{
    "metadata": {
        "name": "string",                                   // 唯一标识
        "labels": {
            "additionalProp1": "string"
        },
        "annotations": {
            "additionalProp1": "string"
        },
        "creationTimestamp": "2025-05-25T09:48:11.115504917Z"   // 创建时间
    },
    "spec": {
        "displayName": "string",                            // 设备名称
        "label": "string",                                  // 设备标签                   
        "description": "string",                            // 设备描述
        "cover": "string",                                  // 详情链接
        "url": "string",                                    // 设备封面图片
        "priority": 0,                                      // 优先级
        "groupName": "string"                             // 分组名称，对应分组 metadata.name
    }
}
```
#### DeviceGroupVo
```json
{
  "metadata": {
    "name": "string",                                   // 唯一标识
    "labels": {
      "additionalProp1": "string"
    },
    "annotations": {
      "additionalProp1": "string"
    },
    "creationTimestamp": "2025-05-25T09:45:44.978360237Z"    // 创建时间
  },
  "spec": {
    "displayName": "string",                            // 分组名称
    "description": "string",                            // 分组描述
    "priority": 0                                    // 分组优先级
  },
  "status": {
    "deviceCount": 0                                    // 分组下设备数量
  },
  "devices": "List<#DeviceVo>"                           // 分组下所有设备列表
}
```
#### DeviceComment
```json
{
    "apiVersion": "core.erzip.com/v1alpha1",
    "kind": "DeviceComment",
    "metadata": {
        "name": "plugin-device-comment",                                   // 唯一标识
        "labels": {
            "plugin.halo.run/plugin-name": "plugin-device-comment"
        },
        "version": 0,
        "creationTimestamp": "2025-05-25T10:49:07.927867444Z"    // 创建时间
    },
    "spec": {
        "displayName": "string",                            // 分组名称
        "description": "string",                            // 分组描述
        "priority": 0                                    // 分组优先级
    },
    "status": {
        "deviceCount": 0                                    // 分组下设备数量
    },
    "devices": "List<#DeviceVo>"                           // 分组下所有设备列表
}
```


### 开发环境

插件开发的详细文档请查阅：<https://docs.halo.run/developer-guide/plugin/introduction>

所需环境：

1. Java 17
2. Node 20
3. pnpm 9
4. Docker (可选)

克隆项目：

```bash
git clone git@github.com:halo-sigs/plugin-starter.git

# 或者当你 fork 之后

git clone git@github.com:{your_github_id}/plugin-starter.git
```

```bash
cd path/to/plugin-starter
```

### 运行方式 1（推荐）

> 此方式需要本地安装 Docker

```bash
# macOS / Linux
./gradlew pnpmInstall

# Windows
./gradlew.bat pnpmInstall
```

```bash
# macOS / Linux
./gradlew haloServer

# Windows
./gradlew.bat haloServer
```

执行此命令后，会自动创建一个 Halo 的 Docker 容器并加载当前的插件，更多文档可查阅：<https://docs.halo.run/developer-guide/plugin/basics/devtools>

### 运行方式 2

> 此方式需要使用源码运行 Halo

编译插件：

```bash
# macOS / Linux
./gradlew build

# Windows
./gradlew.bat build
```

修改 Halo 配置文件：

```yaml
halo:
  plugin:
    runtime-mode: development
    fixedPluginPath:
      - "/path/to/plugin-starter"
```

最后重启 Halo 项目即可。

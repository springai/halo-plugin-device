<script lang="ts" setup>
import LazyImage from "@/components/LazyImage.vue";
import DeviceEditingModal from "@/components/DeviceEditingModal.vue";

import type { Device, DeviceList } from "@/types";
import { axiosInstance } from "@halo-dev/api-client";
import {
  Dialog,
  IconAddCircle,
  IconArrowLeft,
  IconArrowRight,
  IconCheckboxFill,
  Toast,
  VButton,
  VCard,
  VDropdown,
  VDropdownItem,
  VEmpty,
  VLoading,
  VPageHeader,
  VPagination,
  VSpace,
} from "@halo-dev/components";
import type { AttachmentLike } from "@halo-dev/console-shared";
import { useQuery } from "@tanstack/vue-query";
import Fuse from "fuse.js";

import { computed, nextTick, ref, watch } from "vue";

import RiHardDrive2Line from "~icons/ri/hard-drive-2-line"


import DeviceGroupList from "../components/DeviceGroupList.vue"


const selectedDevice = ref<Device | undefined>();
const selectedDevices = ref<Set<Device>>(new Set<Device>());
const selectedGroup = ref<string>();
const editingModal = ref(false);
const checkedAll = ref(false);
const groupListRef = ref();

const page = ref(1);
const size = ref(20);
const total = ref(0);
const keyword = ref("");

const {
  data: devices,
  isLoading,
  refetch,
} = useQuery<Device[]>({
  queryKey: ["plugin:devices:data", page, size, keyword, selectedGroup],
  queryFn: async () => {
    if (!selectedGroup.value) {
      return [];
    }
    const { data } = await axiosInstance.get<DeviceList>("/apis/console.api.device.erzip.com/v1alpha1/devices", {
      params: {
        page: page.value,
        size: size.value,
        keyword: keyword.value,
        group: selectedGroup.value,
      },
    });
    total.value = data.total;
    return data.items
      .map((group) => {
        if (group.spec) {
          group.spec.priority = group.spec.priority || 0;
        }
        return group;
      })
      .sort((a, b) => {
        return (a.spec?.priority || 0) - (b.spec?.priority || 0);
      });
  },
  refetchInterval(data) {
    const hasDeletingGroup = data?.some((group) => !!group.metadata.deletionTimestamp);
    return hasDeletingGroup ? 1000 : false;
  },
  refetchOnWindowFocus: false,
});

const handleSelectPrevious = () => {
  if (!devices.value) {
    return;
  }

  const currentIndex = devices.value.findIndex((device) => device.metadata.name === selectedDevice.value?.metadata.name);

  if (currentIndex > 0) {
    selectedDevice.value = devices.value[currentIndex - 1];
    return;
  }

  if (currentIndex <= 0) {
    selectedDevice.value = undefined;
  }
};

const handleSelectNext = () => {
  if (!devices.value) {
    return;
  }

  if (!selectedDevice.value) {
    selectedDevice.value = devices.value[0];
    return;
  }
  const currentIndex = devices.value.findIndex((device) => device.metadata.name === selectedDevice.value?.metadata.name);
  if (currentIndex !== devices.value.length - 1) {
    selectedDevice.value = devices.value[currentIndex + 1];
  }
};

const handleOpenEditingModal = (device?: Device) => {
  selectedDevice.value = device;
  editingModal.value = true;
};

const handleDeleteInBatch = () => {
  Dialog.warning({
    title: "是否确认删除所选的设备？",
    description: "删除之后将无法恢复。",
    confirmType: "danger",
    onConfirm: async () => {
      try {
        const promises = Array.from(selectedDevices.value).map((device) => {
          return axiosInstance.delete(`/apis/core.erzip.com/v1alpha1/devices/${device.metadata.name}`);
        });
        await Promise.all(promises);
      } catch (e) {
        console.error(e);
      } finally {
        pageRefetch();
      }
    },
  });
};

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;
  handleCheckAll(checked);
};

const handleCheckAll = (checkAll: boolean) => {
  if (checkAll) {
    devices.value?.forEach((device) => {
      selectedDevices.value.add(device);
    });
  } else {
    selectedDevices.value.clear();
  }
};

const isChecked = (device: Device) => {
  return (
    device.metadata.name === selectedDevice.value?.metadata.name ||
    Array.from(selectedDevices.value)
      .map((item) => item.metadata.name)
      .includes(device.metadata.name)
  );
};

watch(
  () => selectedDevices.value.size,
  (newValue) => {
    checkedAll.value = newValue === devices.value?.length;
  }
);

// search
let fuse: Fuse<Device> | undefined = undefined;

watch(
  () => devices.value,
  () => {
    if (!devices.value) {
      return;
    }

    fuse = new Fuse(devices.value, {
      keys: ["spec.displayName", "metadata.name", "spec.description", "spec.url"],
      useExtendedSearch: true,
    });
  }
);

const searchResults = computed({
  get() {
    if (!fuse || !keyword.value) {
      return devices.value || [];
    }

    return fuse?.search(keyword.value).map((item) => item.item);
  },
  set(value) {
    devices.value = value;
  },
});

// create by attachments
const attachmentModal = ref(false);

const onAttachmentsSelect = async (attachments: AttachmentLike[]) => {
  const devices: {
    url: string;
    cover?: string;
    displayName?: string;
    type?: string;
  }[] = attachments
    .map((attachment) => {
      const post = {
        groupName: selectedGroup.value || "",
      };

      if (typeof attachment === "string") {
        return {
          ...post,
          url: attachment,
          cover: attachment,
        };
      }
      if ("url" in attachment) {
        return {
          ...post,
          url: attachment.url,
          cover: attachment.url,
        };
      }
      if ("spec" in attachment) {
        return {
          ...post,
          url: attachment.status?.permalink,
          cover: attachment.status?.permalink,
          displayName: attachment.spec.displayName,
          type: attachment.spec.mediaType,
        };
      }
    })
    .filter(Boolean) as {
    url: string;
    cover?: string;
    displayName?: string;
    type?: string;
  }[];

  for (const device of devices) {
    const type = device.type;
    if (!type) {
      Toast.error("只支持选择图片");
      nextTick(() => {
        attachmentModal.value = true;
      });

      return;
    }
    const fileType = type.split("/")[0];
    if (fileType !== "image") {
      Toast.error("只支持选择图片");
      nextTick(() => {
        attachmentModal.value = true;
      });
      return;
    }
  }

  const createRequests = devices.map((device) => {
    return axiosInstance.post<Device>("/apis/core.erzip.com/v1alpha1/devices", {
      metadata: {
        name: "",
        generateName: "device-",
      },
      spec: device,
      kind: "Device",
      apiVersion: "core.erzip.com/v1alpha1",
    });
  });

  await Promise.all(createRequests);

  Toast.success(`新建成功，一共创建了 ${devices.length} 张设备。`);
  pageRefetch();
};

const groupSelectHandle = (group?: string) => {
  selectedGroup.value = group;
};

const pageRefetch = async () => {
  await groupListRef.value.refetch();
  await refetch();
  selectedDevices.value = new Set<Device>();
};
const onEditingModalClose = () => {
  editingModal.value = false;
  refetch();
};

</script>
<template>
  <DeviceEditingModal
    v-if="editingModal"
    :device="selectedDevice"
    :group="selectedGroup"
    @close="onEditingModalClose"
    @saved="pageRefetch"
  >
    <template #append-actions>
      <span @click="handleSelectPrevious">
        <IconArrowLeft />
      </span>
      <span @click="handleSelectNext">
        <IconArrowRight />
      </span>
    </template>
  </DeviceEditingModal>
  <AttachmentSelectorModal v-model:visible="attachmentModal" :accepts="['image/*']" @select="onAttachmentsSelect" />
  <VPageHeader title="设备库">
    <template #icon>
      <RiHardDrive2Line />
    </template>
  </VPageHeader>
  <div class=":uno: p-4">
    <div class=":uno: flex flex-col gap-2 lg:flex-row">
      <div class=":uno: w-full flex-none lg:w-96">
        <DeviceGroupList ref="groupListRef" @select="groupSelectHandle" />
      </div>
      <div class=":uno: min-w-0 flex-1 shrink">
        <VCard>
          <template #header>
            <div class=":uno: block w-full bg-gray-50 px-4 py-3">
              <div class=":uno: relative flex flex-col items-start sm:flex-row sm:items-center">
                <div class=":uno: mr-4 hidden items-center sm:flex">
                  <input v-model="checkedAll" type="checkbox" @change="handleCheckAllChange" />
                </div>
                <div class=":uno: w-full flex flex-1 sm:w-auto">
                  <SearchInput v-if="!selectedDevices.size" v-model="keyword" />
                  <VSpace v-else>
                    <VButton type="danger" @click="handleDeleteInBatch"> 删除 </VButton>
                  </VSpace>
                </div>
                <div v-if="selectedGroup" v-permission="['plugin:devices:manage']" class=":uno: mt-4 flex sm:mt-0">
                  <VDropdown>
                    <VButton size="xs"> 新增 </VButton>
                    <template #popper>
                      <VDropdownItem @click="handleOpenEditingModal()"> 新增 </VDropdownItem>
                    </template>
                  </VDropdown>
                </div>
              </div>
            </div>
          </template>
          <VLoading v-if="isLoading" />
          <Transition v-else-if="!selectedGroup" appear name="fade">
            <VEmpty message="请选择或新建分组" title="未选择分组"></VEmpty>
          </Transition>
          <Transition v-else-if="!searchResults.length" appear name="fade">
            <VEmpty message="你可以尝试刷新或者新建设备" title="当前没有设备">
              <template #actions>
                <VSpace>
                  <VButton @click="refetch"> 刷新</VButton>
                  <VButton v-permission="['plugin:devices:manage']" type="primary" @click="handleOpenEditingModal()">
                    <template #icon>
                      <IconAddCircle class=":uno: size-full" />
                    </template>
                    新增设备
                  </VButton>
                </VSpace>
              </template>
            </VEmpty>
          </Transition>
          <Transition v-else appear name="fade">
            <div
              class=":uno: grid grid-cols-1 mt-2 gap-x-2 gap-y-3 lg:grid-cols-3 sm:grid-cols-2 xl:grid-cols-5"
              role="list"
            >
              <VCard
                v-for="device in devices"
                :key="device.metadata.name"
                :body-class="[':uno: !p-0']"
                :class="{
                  ':uno: ring-primary ring-1': isChecked(device),
                  ':uno: ring-1 ring-red-600': device.metadata.deletionTimestamp,
                }"
                class=":uno: hover:shadow"
                @click="handleOpenEditingModal(device)"
              >
                <div class=":uno: group relative bg-white">
                  <div class=":uno: block aspect-16/9 size-full cursor-pointer overflow-hidden bg-gray-100">
                    <LazyImage
                      :key="device.metadata.name"
                      :alt="device.spec.displayName"
                      :src="device.spec.cover || device.spec.url"
                      classes="size-full pointer-events-none group-hover:opacity-75"
                    >
                      <template #loading>
                        <div class=":uno: h-full flex justify-center">
                          <VLoading></VLoading>
                        </div>
                      </template>
                      <template #error>
                        <div class=":uno: h-full flex items-center justify-center object-cover">
                          <span class=":uno: text-xs text-red-400"> 加载异常 </span>
                        </div>
                      </template>
                    </LazyImage>
                  </div>

                  <p
                    v-tooltip="device.spec.displayName"
                    class=":uno: block cursor-pointer truncate px-2 py-1 text-center text-xs text-gray-700 font-medium"
                  >
                    {{ device.spec.displayName }}
                  </p>

                  <div v-if="device.metadata.deletionTimestamp" class=":uno: absolute right-1 top-1 text-xs text-red-300">
                    删除中...
                  </div>

                  <div
                    v-if="!device.metadata.deletionTimestamp"
                    v-permission="['plugin:photos:manage']"
                    :class="{ ':uno: !flex': selectedDevices.has(device) }"
                    class=":uno: absolute left-0 top-0 hidden h-1/3 w-full cursor-pointer justify-end from-gray-300 to-transparent bg-gradient-to-b ease-in-out group-hover:flex"
                    @click.stop="selectedDevices.has(device) ? selectedDevices.delete(device) : selectedDevices.add(device)"
                  >
                    <IconCheckboxFill
                      :class="{
                        ':uno: !text-primary': selectedDevices.has(device),
                      }"
                      class=":uno: hover:text-primary mr-1 mt-1 h-6 w-6 cursor-pointer text-white transition-all"
                    />
                  </div>
                </div>
              </VCard>
            </div>
          </Transition>

          <template #footer>
            <VPagination v-model:page="page" v-model:size="size" :total="total" :size-options="[20, 30, 50, 100]" />
          </template>
        </VCard>
      </div>
    </div>
  </div>
</template>

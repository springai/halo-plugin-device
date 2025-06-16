<script lang="ts" setup>
import type {DeviceGroup, DeviceGroupList} from "../types/index";
import { axiosInstance } from "@halo-dev/api-client";
import {
  Dialog,
  IconList,
  VButton,
  VCard,
  VDropdownItem,
  VEmpty,
  VEntity,
  VEntityField,
  VLoading,
  VStatusDot,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import { ref } from "vue";
import { VueDraggable } from "vue-draggable-plus";
import DeviceGroupEditingModal from "./DeviceGroupEditingModal.vue";

const emit = defineEmits<{
  (event: "select", group?: string): void;
}>();

const deviceGroupEditingModal = ref(false);
const updateGroup = ref<DeviceGroup>();
const selectedGroup = useRouteQuery<string>("device-group");
const groups = ref<DeviceGroup[]>([]);

const { refetch, isLoading } = useQuery<DeviceGroup[]>({
  queryKey: ["plugin:photos:groups"],
  queryFn: async () => {
    const { data } = await axiosInstance.get<DeviceGroupList>("/apis/console.api.device.erzip.com/v1alpha1/devicegroups");
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
  onSuccess(data) {
    groups.value = data;

    if (selectedGroup.value) {
      const groupNames = data.map((group) => group.metadata.name);
      if (groupNames.includes(selectedGroup.value)) {
        emit("select", selectedGroup.value);
        return;
      }
    }

    if (data.length) {
      handleSelectedClick(data[0]);
    } else {
      selectedGroup.value = "";
      emit("select", "");
    }
  },
  refetchOnWindowFocus: false,
});

const handleSaveInBatch = async () => {
  try {
    const promises = groups.value?.map((group: DeviceGroup, index) => {
      if (group.spec) {
        group.spec.priority = index;
      }
      return axiosInstance.put(`/apis/core.erzip.com/v1alpha1/devicegroups/${group.metadata.name}`, group);
    });

    if (promises) {
      await Promise.all(promises);
    }
  } catch (e) {
    console.error("保存顺序时发生错误", e);
  } finally {
    refetch();
  }
};

const handleDelete = async (group: DeviceGroup) => {
  Dialog.warning({
    title: "确定要删除该分组吗？",
    description: "将同时删除该分组下的所有设备，该操作不可恢复。",
    confirmType: "danger",
    onConfirm: async () => {
      try {
        await axiosInstance.delete(`/apis/console.api.device.erzip.com/v1alpha1/devicegroups/${group.metadata.name}`);
        refetch();
      } catch (e) {
        console.error("Failed to delete devicegroup", e);
      }
    },
  });
};

const handleOpenEditingModal = (group?: DeviceGroup) => {
  deviceGroupEditingModal.value = true;
  updateGroup.value = group;
};

const handleSelectedClick = (group: DeviceGroup) => {
  selectedGroup.value = group.metadata.name;
  emit("select", group.metadata.name);
};

defineExpose({
  refetch,
});

function onGroupEditingModalClose() {
  deviceGroupEditingModal.value = false;
  refetch();
}

</script>

<template>
  <DeviceGroupEditingModal v-if="deviceGroupEditingModal" :group="updateGroup" @close="onGroupEditingModalClose" />
  <VCard :body-class="[':uno: !p-0']" title="分组">
    <VLoading v-if="isLoading" />
    <Transition v-else-if="!groups || !groups.length" appear name="fade">
      <VEmpty message="你可以尝试刷新或者新建分组" title="当前没有分组">
        <template #actions>
          <VSpace>
            <VButton size="sm" @click="refetch()"> 刷新</VButton>
          </VSpace>
        </template>
      </VEmpty>
    </Transition>
    <Transition v-else appear name="fade">
      <div class=":uno: w-full overflow-x-auto">
        <table class=":uno: w-full border-spacing-0">
          <VueDraggable
            v-model="groups"
            class=":uno: divide-y divide-gray-100"
            group="group"
            handle=".drag-element"
            item-key="metadata.name"
            tag="tbody"
            @update="handleSaveInBatch"
          >
            <VEntity
              v-for="group in groups"
              :key="group.metadata.name"
              :is-selected="selectedGroup === group.metadata.name"
              class=":uno: group"
              @click="handleSelectedClick(group)"
            >
              <template #prepend>
                <div
                  class=":uno: drag-element absolute inset-y-0 left-0 hidden w-3.5 cursor-move items-center bg-gray-100 transition-all group-hover:flex hover:bg-gray-200"
                >
                  <IconList class=":uno: size-3.5" />
                </div>
              </template>

              <template #start>
                <VEntityField
                  :title="group.spec?.displayName"
                  :description="`${group.status.deviceCount || 0} 个设备`"
                ></VEntityField>
              </template>

              <template #end>
                <VEntityField v-if="group.metadata.deletionTimestamp">
                  <template #description>
                    <VStatusDot v-tooltip="`删除中`" state="warning" animate />
                  </template>
                </VEntityField>
              </template>

              <template #dropdownItems>
                <VDropdownItem @click="handleOpenEditingModal(group)"> 修改 </VDropdownItem>
                <VDropdownItem type="danger" @click="handleDelete(group)"> 删除 </VDropdownItem>
              </template>
            </VEntity>
          </VueDraggable>
        </table>
      </div>
    </Transition>

    <template v-if="!isLoading" #footer>
      <!-- @unocss-skip-start -->
      <VButton
        v-permission="['plugin:devices:manage']"
        block
        type="secondary"
        @click="handleOpenEditingModal(undefined)"
      >
        新增分组
      </VButton>
      <!-- @unocss-skip-end -->
    </template>
  </VCard>
</template>

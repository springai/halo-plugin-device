<script lang="ts" setup>
import type { Device } from "@/types";
import { reset, submitForm } from "@formkit/core";
import { axiosInstance } from "@halo-dev/api-client";
import { IconSave, VButton, VModal } from "@halo-dev/components";
import { cloneDeep } from "lodash-es";
import { computed, nextTick, ref, watch } from "vue";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    device?: Device;
    group?: string;
  }>(),
  {
    visible: false,
    device: undefined,
    group: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", value: boolean): void;
  (event: "close"): void;
  (event: "saved", device: Device): void;
}>();

const initialFormState: Device = {
  metadata: {
    name: "",
    generateName: "device-",
  },
  spec: {
    cover: "",
    displayName: "",
    url: "",
    groupName: props.group || "",
  },
  kind: "Device",
  apiVersion: "core.erzip.com/v1alpha1",
} as Device;

const formState = ref<Device>(cloneDeep(initialFormState));

const saving = ref<boolean>(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const modalTitle = computed(() => {
  return isUpdateMode.value ? "编辑设备" : "添加设备";
});

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleResetForm = () => {
  formState.value = cloneDeep(initialFormState);
  reset("device-form");
};

watch(
  () => props.visible,
  (visible) => {
    if (!visible && !props.device) {
      handleResetForm();
    }
  }
);

watch(
  () => props.device,
  (device) => {
    if (device) {
      formState.value = cloneDeep(device);
    } else {
      handleResetForm();
    }
  }
);
const annotationsFormRef = ref();

const handleSaveDevice = async () => {
  annotationsFormRef.value?.handleSubmit();
  await nextTick();
  const { customAnnotations, annotations, customFormInvalid, specFormInvalid } = annotationsFormRef.value || {};
  if (customFormInvalid || specFormInvalid) {
    return;
  }
  formState.value.metadata.annotations = {
    ...annotations,
    ...customAnnotations,
  };
  try {
    saving.value = true;
    if (isUpdateMode.value) {
      await axiosInstance.put<Device>(
        `/apis/core.erzip.com/v1alpha1/devices/${formState.value.metadata.name}`,
        formState.value
      );
    } else {
      if (props.group) {
        formState.value.spec.groupName = props.group;
      }
      const { data } = await axiosInstance.post<Device>(`/apis/core.erzip.com/v1alpha1/devices`, formState.value);
      emit("saved", data);
    }
    onVisibleChange(false);
  } catch (e) {
    console.error(e);
  } finally {
    saving.value = false;
  }
};
</script>
<template>
  <VModal :title="modalTitle" :visible="visible" :width="650" @update:visible="onVisibleChange">
    <template #actions>
      <slot name="append-actions" />
    </template>

    <FormKit
      id="device-form"
      v-model="formState.spec"
      name="device-form"
      :actions="false"
      :config="{ validationVisibility: 'submit' }"
      type="form"
      @submit="handleSaveDevice"
    >
      <div class="md:grid md:grid-cols-4 md:gap-6">
        <div class="md:col-span-1">
          <div class="sticky top-0">
            <span class="text-base font-medium text-gray-900"> 常规 </span>
          </div>
        </div>
        <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
          <FormKit name="displayName" label="名称" type="text" validation="required" help="例如: MacBook Pro"></FormKit>
          <FormKit name="label" label="标签" type="textarea" validation="required" help="例如: M1Pro 32G / 1TB "></FormKit>
          <FormKit name="description" label="描述" type="textarea" validation="required" help="例如: 屏幕显示效果..."></FormKit>
          <FormKit name="cover" label="封面" type="attachment" :accepts="['image/*']" validation="required" help="设备封面图"></FormKit>
          <FormKit name="url" label="详情地址" type="text" validation="required" help="可以是文章链接、商品购买链接等"></FormKit>
          <FormKit name="priority" label="优先级" type="text" validation="required" help="用于排序，0优先级最大"></FormKit>
        </div>
      </div>
    </FormKit>
    <div class="py-5">
      <div class="border-t border-gray-200"></div>
    </div>
    <div class="md:grid md:grid-cols-4 md:gap-6">
      <div class="md:col-span-1">
        <div class="sticky top-0">
          <span class="text-base font-medium text-gray-900"> 元数据 </span>
        </div>
      </div>
      <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
        <AnnotationsForm
          v-if="visible"
          :key="formState.metadata.name"
          ref="annotationsFormRef"
          :value="formState.metadata.annotations"
          kind="Device"
          group="core.erzip.com"
        />
      </div>
    </div>
    <template #footer>
      <VButton :loading="saving" type="secondary" @click="submitForm('device-form')">
        <template #icon>
          <IconSave class="size-full" />
        </template>
        保存
      </VButton>
    </template>
  </VModal>
</template>

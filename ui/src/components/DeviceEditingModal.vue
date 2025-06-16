<script lang="ts" setup>
import type {Device} from "@/types";
import { submitForm } from "@formkit/core";
import {axiosInstance} from "@halo-dev/api-client";
import { VButton, VModal, VSpace } from "@halo-dev/components";
import {cloneDeep} from "lodash-es";
import { computed, nextTick, onMounted, ref, useTemplateRef } from "vue";

const props = withDefaults(
  defineProps<{
    device?: Device;
    group?: string;
  }>(),
  {
    device: undefined,
    group: undefined,
  }
);

const emit = defineEmits<{
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

const isSubmitting = ref<boolean>(false);
const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const modalTitle = computed(() => {
  return isUpdateMode.value ? "编辑设备" : "添加设备";
});

onMounted(() => {
  if (props.device) {
    formState.value = cloneDeep(props.device);
  }
});

const annotationsFormRef = ref();

const handleSaveDevice = async () => {
  annotationsFormRef.value?.handleSubmit();
  await nextTick();
  const {customAnnotations, annotations, customFormInvalid, specFormInvalid} = annotationsFormRef.value || {};
  if (customFormInvalid || specFormInvalid) {
    return;
  }
  formState.value.metadata.annotations = {
    ...annotations,
    ...customAnnotations,
  };
  try {
    isSubmitting.value = true;
    if (isUpdateMode.value) {
      await axiosInstance.put<Device>(
        `/apis/core.erzip.com/v1alpha1/devices/${formState.value.metadata.name}`,
        formState.value
      );
    } else {
      if (props.group) {
        formState.value.spec.groupName = props.group;
      }
      const {data} = await axiosInstance.post<Device>(`/apis/core.erzip.com/v1alpha1/devices`, formState.value);
      emit("saved", data);
    }
    modal.value?.close();
  } catch (e) {
    console.error(e);
  } finally {
    isSubmitting.value = false;
  }
};
</script>
<template>
  <VModal ref="modal" :title="modalTitle" :width="650" @close="emit('close')">
    <template #actions>
      <slot name="append-actions"/>
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
      <div class=":uno: md:grid md:grid-cols-4 md:gap-6">
        <div class=":uno: md:col-span-1">
          <div class=":uno: sticky top-0">
            <span class=":uno: text-base text-gray-900 font-medium"> 常规 </span>
          </div>
        </div>
        <div class=":uno: mt-5 md:col-span-3 md:mt-0 divide-y divide-gray-100">
          <FormKit name="displayName" label="名称" type="text" validation="required" help="例如: MacBook Pro"></FormKit>
          <FormKit name="label" label="标签" type="textarea" validation="required"
                   help="例如: M1Pro 32G / 1TB "></FormKit>
          <FormKit name="description" label="描述" type="textarea" validation="required"
                   help="例如: 屏幕显示效果..."></FormKit>
          <FormKit name="cover" label="封面" type="attachment" :accepts="['image/*']" validation="required"
                   help="设备封面图"></FormKit>
          <FormKit name="url" label="详情地址" type="text" validation="required"
                   help="可以是文章链接、商品购买链接等"></FormKit>
          <FormKit name="priority" label="优先级" type="number" number="integer"
                   validation="min:0" step="1" min="0"
                   :validation-messages="{
                      integer: '必须输入整数',
                      min: '不能小于0'
                    }"
                   validation-visibility="live"
                   help="用于排序，0优先级最大"></FormKit>
        </div>
      </div>
    </FormKit>
    <div class=":uno: py-5">
      <div class=":uno: border-t border-gray-200"></div>
    </div>
    <div class=":uno: md:grid md:grid-cols-4 md:gap-6">
      <div class=":uno: md:col-span-1">
        <div class=":uno: sticky top-0">
          <span class=":uno: text-base text-gray-900 font-medium"> 元数据 </span>
        </div>
      </div>
      <div class=":uno: mt-5 md:col-span-3 md:mt-0 divide-y divide-gray-100">
        <AnnotationsForm
          :key="formState.metadata.name"
          ref="annotationsFormRef"
          :value="formState.metadata.annotations"
          kind="Device"
          group="core.erzip.com"
        />
      </div>
    </div>

    <template #footer>
      <VSpace>
        <VButton :loading="isSubmitting" type="secondary" @click="submitForm('device-form')"> 保存 </VButton>
        <VButton @click="modal?.close()">取消</VButton>
      </VSpace>
    </template>
  </VModal>
</template>

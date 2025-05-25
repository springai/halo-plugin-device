export interface Metadata {
  name: string;
  generateName?: string;
  labels?: {
    [key: string]: string;
  } | null;
  annotations?: {
    [key: string]: string;
  } | null;
  version?: number | null;
  creationTimestamp?: string | null;
  deletionTimestamp?: string | null;
}

export interface DeviceGroupSpec {
  displayName: string;
  description: string;
  priority?: number;
}

export interface PostGroupStatus2 {
  deviceCount: number;
}

export interface DeviceSpec {
  cover: string;
  displayName: string;
  label: string;
  description: string;
  url: string;
  priority?: number;
  groupName: string;
}
export interface Device {
  spec: DeviceSpec;
  apiVersion: string;
  kind: string;
  metadata: Metadata;
}
export interface DeviceGroup {
  spec: DeviceGroupSpec;
  apiVersion: string;
  kind: string;
  metadata: Metadata;
  status: PostGroupStatus2;
}
export interface DeviceList {
  page: number;
  size: number;
  total: number;
  totalPages: number;
  items: Array<Device>;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}
export interface DeviceGroupList {
  page: number;
  size: number;
  total: number;
  items: Array<DeviceGroup>;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}

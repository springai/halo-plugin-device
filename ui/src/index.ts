import { definePlugin } from "@halo-dev/console-shared";
import { defineAsyncComponent, markRaw } from "vue";
import RiHardDrive2Line from "~icons/ri/hard-drive-2-line"
import "uno.css";
import { VLoading } from "@halo-dev/components";

export default definePlugin({
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/devices",
        name: "Devices",
        component: defineAsyncComponent({
          loader: () => import("@/views/DeviceList.vue"),
          loadingComponent: VLoading,
        }),
        meta: {
          permissions: ["plugin:devices:view"],
          menu: {
            name: "设备库",
            group: "content",
            icon: markRaw(RiHardDrive2Line),
          },
        },
      },
    },
  ],
});

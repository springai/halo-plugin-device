import { definePlugin, type CommentSubjectRefProvider, type CommentSubjectRefResult } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import DeviceList from "@/views/DeviceList.vue";
import RiImage2Line from "~icons/ri/image-2-line";
import RiHardDrive2Line from "~icons/ri/hard-drive-2-line"
import type { Extension } from "@halo-dev/api-client";

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/devices",
        name: "Devices",
        component: DeviceList,
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
  extensionPoints: {
    "comment:subject-ref:create": (): CommentSubjectRefProvider[] => {
      return [
        {
          kind: "DeviceComment",
          group: "core.erzip.com",
          resolve: (subject: Extension): CommentSubjectRefResult => {
            return {
              label: "设备库",
              title: "设备库页面",
              externalUrl: "/devices",
              route: {
                name: "Devices",
              },
            };
          },
        },
      ];
    },
  },
});

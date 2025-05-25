package com.erzip.device;

import com.erzip.device.extension.DeviceComment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import run.halo.app.content.comment.CommentSubject;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;

@Component
@RequiredArgsConstructor
public class DeviceCommentSubject implements CommentSubject<DeviceComment> {
    private final ReactiveExtensionClient client;
    private static final GroupVersionKind DEVICE_COMMENT_GVK =
        GroupVersionKind.fromExtension(DeviceComment.class);

    private static final SubjectDisplay FIXED_SUBJECT_DISPLAY =
        new SubjectDisplay("设备库", "/devices", "设备库");

    @Override
    public Mono<DeviceComment> get(String name) {
        return client.get(DeviceComment.class, name);
    }

    @Override
    public Mono<SubjectDisplay> getSubjectDisplay(String name) {
        return Mono.just(FIXED_SUBJECT_DISPLAY);
    }

    @Override
    public boolean supports(Ref ref) {
        Assert.notNull(ref, "Subject ref must not be null.");
        GroupVersionKind groupVersionKind = new GroupVersionKind(ref.getGroup(),
            ref.getVersion(), ref.getKind()
        );
        return DEVICE_COMMENT_GVK.equals(groupVersionKind)
            && "plugin-device-comment".equals(ref.getName());
    }
}
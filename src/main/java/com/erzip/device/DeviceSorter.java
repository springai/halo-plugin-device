package com.erzip.device;

import com.erzip.device.extension.Device;
import org.springframework.util.comparator.Comparators;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

public enum DeviceSorter {
    DISPLAY_NAME,

    CREATE_TIME;

    static final Function<Device, String> name = device -> device.getMetadata()
        .getName();


    public static Comparator<Device> from(DeviceSorter sorter,
        Boolean ascending) {
        if (Objects.equals(true, ascending)) {
            return from(sorter);
        }
        return from(sorter).reversed();
    }


    static Comparator<Device> from(DeviceSorter sorter) {
        if (sorter == null) {
            return createTimeComparator();
        }
        if (CREATE_TIME.equals(sorter)) {
            Function<Device, Instant> comparatorFunc
                = device -> device.getMetadata().getCreationTimestamp();
            return Comparator.comparing(comparatorFunc).thenComparing(name);
        }

        if (DISPLAY_NAME.equals(sorter)) {
            Function<Device, String> comparatorFunc = moment -> moment.getSpec()
                .getDisplayName();
            return Comparator.comparing(comparatorFunc, Comparators.nullsLow())
                .thenComparing(name);
        }

        throw new IllegalStateException("Unsupported sort value: " + sorter);
    }


    static DeviceSorter convertFrom(String sort) {
        for (DeviceSorter sorter : values()) {
            if (sorter.name().equalsIgnoreCase(sort)) {
                return sorter;
            }
        }
        return null;
    }


    static Comparator<Device> createTimeComparator() {
        Function<Device, Instant> comparatorFunc = device -> device.getMetadata()
            .getCreationTimestamp();
        return Comparator.comparing(comparatorFunc).thenComparing(name);
    }
}

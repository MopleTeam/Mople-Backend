package com.mople.global.utils.batch;

import java.util.List;
import java.util.function.Consumer;

public final class Batching {

    private Batching() {}

    public static <T> void chunk(List<T> list, int size, Consumer<List<T>> consumer) {
        if (list == null || list.isEmpty()) return;
        for (int i = 0; i < list.size(); i += size) {
            consumer.accept(list.subList(i, Math.min(i + size, list.size())));
        }
    }
}

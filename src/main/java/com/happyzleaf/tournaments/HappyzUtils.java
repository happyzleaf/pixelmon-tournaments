package com.happyzleaf.tournaments;

import java.util.Collections;
import java.util.List;

public class HappyzUtils {
    public static <E> List<E> subListSafe(List<E> list, int from, int to) {
        if (from < 0 || from >= list.size() || to < 0) {
            return Collections.emptyList();
        }

        if (to > list.size()) {
            to = list.size();
        }

        return list.subList(from, to);
    }
}

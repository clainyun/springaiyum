package com.ssafy.yumyum.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

public final class SortUtils {

    private SortUtils() {
    }

    public static <T> List<T> quickSort(List<T> source, Comparator<T> comparator) {
        List<T> items = new ArrayList<>(source);
        if (items.size() < 2) {
            return items;
        }
        sortRange(items, 0, items.size() - 1, comparator);
        return items;
    }

    private static <T> void sortRange(List<T> items, int left, int right, Comparator<T> comparator) {
        int i = left;
        int j = right;
        T pivot = items.get((left + right) / 2);

        while (i <= j) {
            while (comparator.compare(items.get(i), pivot) < 0) {
                i++;
            }
            while (comparator.compare(items.get(j), pivot) > 0) {
                j--;
            }
            if (i <= j) {
                T tmp = items.get(i);
                items.set(i, items.get(j));
                items.set(j, tmp);
                i++;
                j--;
            }
        }

        if (left < j) {
            sortRange(items, left, j, comparator);
        }
        if (i < right) {
            sortRange(items, i, right, comparator);
        }
    }

    public static <T> List<T> selectionSort(List<T> source, Comparator<T> comparator) {
        List<T> items = new ArrayList<>(source);
        for (int i = 0; i < items.size() - 1; i++) {
            int selectedIndex = i;
            for (int j = i + 1; j < items.size(); j++) {
                if (comparator.compare(items.get(j), items.get(selectedIndex)) < 0) {
                    selectedIndex = j;
                }
            }
            if (selectedIndex != i) {
                T tmp = items.get(i);
                items.set(i, items.get(selectedIndex));
                items.set(selectedIndex, tmp);
            }
        }
        return items;
    }

    public static <T> List<T> countingSort(List<T> source, ToIntFunction<T> keyExtractor) {
        if (source.isEmpty()) {
            return new ArrayList<>();
        }

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (T item : source) {
            int key = keyExtractor.applyAsInt(item);
            min = Math.min(min, key);
            max = Math.max(max, key);
        }

        int[] counts = new int[max - min + 1];
        for (T item : source) {
            counts[keyExtractor.applyAsInt(item) - min]++;
        }

        for (int i = 1; i < counts.length; i++) {
            counts[i] += counts[i - 1];
        }

        List<T> output = new ArrayList<>(source.size());
        for (int i = 0; i < source.size(); i++) {
            output.add(null);
        }
        for (int i = source.size() - 1; i >= 0; i--) {
            T item = source.get(i);
            int offset = keyExtractor.applyAsInt(item) - min;
            counts[offset]--;
            output.set(counts[offset], item);
        }
        return output;
    }
}

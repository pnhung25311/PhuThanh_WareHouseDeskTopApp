package com.phuthanh.business.helper;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



public class ColumnSorter {

    private ColumnSorter(){}

    public static Map<String, Object> reorder(
            Map<String, Object> input,
            List<String> columnOrder
    ) {
        Map<String, Object> sorted = new LinkedHashMap<>();

        // 1. add theo thứ tự người dùng muốn
        for (String key : columnOrder) {
            if (input.containsKey(key)) {
                sorted.put(key, input.get(key));
            }
        }

        // 2. add các cột còn dư của API
        for (String key : input.keySet()) {
            if (!sorted.containsKey(key)) {
                sorted.put(key, input.get(key));
            }
        }

        return sorted;
    }
}

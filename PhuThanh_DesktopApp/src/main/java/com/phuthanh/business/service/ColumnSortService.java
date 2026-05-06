package com.phuthanh.business.service;

import java.util.stream.Collectors;

import com.phuthanh.business.helper.ColumnSorter;
import java.util.List;
import java.util.Map;


public class ColumnSortService {

    private ColumnSortService(){}

    public static List<Map<String, Object>> reorderList(
            List<Map<String, Object>> data,
            List<String> columnOrder
    ) {
        return data.stream()
                .map(row -> ColumnSorter.reorder(row, columnOrder))
                .collect(Collectors.toList());
    }
}

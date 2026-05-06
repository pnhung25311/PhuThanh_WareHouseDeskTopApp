package com.phuthanh.business.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.phuthanh.model.business.StockResult;

public class StockAllocator {

    private final Map<String, LinkedHashMap<String, Map<String, Integer>>> memo = new HashMap<>();

    private LinkedHashMap<String, Map<String, Integer>> dfs(
            List<Map.Entry<String, Integer>> items,
            int index,
            int remaining) {

        // hết kho -> trả map rỗng (KHÔNG BAO GIỜ NULL)
        if (index == items.size())
            return new LinkedHashMap<>();

        String key = buildKey(items, index, remaining);
        if (memo.containsKey(key))
            return memo.get(key);

        String name = items.get(index).getKey();
        int value = items.get(index).getValue();

        LinkedHashMap<String, Map<String, Integer>> best = new LinkedHashMap<>();
        int bestRemain = Integer.MAX_VALUE;

        for (int co = 0; co <= Math.min(value, remaining); co++) {

            int newRemaining = remaining - co;

            LinkedHashMap<String, Map<String, Integer>> sub = dfs(items, index + 1, newRemaining);

            // 🔥 CHỐNG NULL TUYỆT ĐỐI
            if (sub == null)
                sub = new LinkedHashMap<>();

            LinkedHashMap<String, Map<String, Integer>> current = new LinkedHashMap<>();

            Map<String, Integer> r = new HashMap<>();
            r.put("co", co);
            r.put("ko", value - co);

            current.put(name, r);
            current.putAll(sub);

            // chọn phương án dư ít nhất
            if (newRemaining < bestRemain) {
                bestRemain = newRemaining;
                best = current;
            }
        }

        memo.put(key, best);
        return best;
    }

    private String buildKey(List<Map.Entry<String, Integer>> items,
            int index,
            int remaining) {

        StringBuilder sb = new StringBuilder();
        sb.append(index).append("_").append(remaining);

        // 🔥 thêm stock của từng kho vào key
        for (var e : items)
            sb.append("_").append(e.getValue());

        return sb.toString();
    }

    public Map<String, StockResult> allocate(Map<String, Integer> input, int total) {

        memo.clear();

        List<Map.Entry<String, Integer>> items = new ArrayList<>(input.entrySet());
        LinkedHashMap<String, Map<String, Integer>> raw = dfs(items, 0, total);
        if (raw == null)
            raw = new LinkedHashMap<>();
        Map<String, StockResult> result = new LinkedHashMap<>();

        for (var e : raw.entrySet()) {
            result.put(e.getKey(),
                    new StockResult(
                            e.getValue().get("co"),
                            e.getValue().get("ko")));
        }

        return result;
    }
}
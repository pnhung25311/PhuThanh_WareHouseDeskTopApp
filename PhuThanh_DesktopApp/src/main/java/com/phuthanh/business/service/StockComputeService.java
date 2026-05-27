package com.phuthanh.business.service;

import java.util.LinkedHashMap;
import java.util.Map;

import com.phuthanh.business.helper.StockAllocator;
import com.phuthanh.model.business.ProductBusiness;
import com.phuthanh.model.business.StockResult;

public class StockComputeService {

    private final StockAllocator allocator = new StockAllocator();
    private final Map<ProductBusiness, Map<String, StockResult>> cache = new LinkedHashMap<>();

    public Map<String, StockResult> compute(ProductBusiness p) {

        if (cache.containsKey(p))
            return cache.get(p);

        // 🔥 parse STOCK
        int vietY = toInt(p.vatVietY);
        int phuThanh = toInt(p.vatPhuThanh);

        Map<String, Integer> stock = new LinkedHashMap<>();
        stock.put("VAT Phú Thành", phuThanh);
        stock.put("VAT Việt Ý", vietY);

        // 🔥 parse NEED
        int need = toInt(p.khoChinh) +
                toInt(p.kho397) +
                toInt(p.khoKheDay) +
                toInt(p.khoKhoangSan) +
                toInt(p.khoLangKhanh);

        // Map<String, StockResult> rs = need <= 0 ? new LinkedHashMap<>()
        // : allocator.allocate(stock, need);

        Map<String, StockResult> rs = allocator.allocate(stock, need);

        cache.put(p, rs);
        return rs;
    }

    private int toInt(String s) {

        // null thật
        if (s == null)
            return 0;

        // trim
        s = s.trim();

        // rỗng
        if (s.isBlank())
            return 0;

        // 🔥 API trả chữ "null"
        if (s.equalsIgnoreCase("null"))
            return 0;

        if (Double.parseDouble(s) < 0) {
            return 0;
        }

        // 🔥 có thể có dấu phẩy 1,234
        s = s.replace(",", "");

        try {
            return (int) Double.parseDouble(s);
        } catch (Exception ex) {
            return 0; // chống mọi dữ liệu bẩn
        }
    }

    public void clearCache() {
        cache.clear();
    }
}

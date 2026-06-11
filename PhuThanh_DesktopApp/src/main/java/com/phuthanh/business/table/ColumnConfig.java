package com.phuthanh.business.table;

import java.util.function.Function;
import com.phuthanh.model.business.ProductBusiness;

public class ColumnConfig {
    public final String id;
    public final String header;
    public final Function<ProductBusiness, String> mapper;
    public final boolean isNumber;
    public final double width;

    public ColumnConfig(String id, String header, Function<ProductBusiness, String> mapper, boolean isNumber, double width) {
        this.id = id;
        this.header = header;
        this.mapper = mapper;
        this.isNumber = isNumber;
        this.width = width;
    }
}
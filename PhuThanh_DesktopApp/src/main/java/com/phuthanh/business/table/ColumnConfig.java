package com.phuthanh.business.table;

import java.util.function.Function;
import com.phuthanh.model.business.ProductBusiness;

public class ColumnConfig {

    public final String header;
    public final Function<ProductBusiness, String> mapper;
    public final boolean isNumber;
    public final double width;

    public ColumnConfig(String header, Function<ProductBusiness, String> mapper, boolean isNumber, double width) {
        this.header = header;
        this.mapper = mapper;
        this.isNumber = isNumber;
        this.width = width;
    }
}
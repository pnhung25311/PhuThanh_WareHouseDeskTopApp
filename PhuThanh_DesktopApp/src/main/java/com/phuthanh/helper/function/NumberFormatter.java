package com.phuthanh.helper.function;

import java.text.DecimalFormat;

public class NumberFormatter {

    public String formatIfNumber(String value) {
        DecimalFormat INT_FORMAT = new DecimalFormat("#,###");
        DecimalFormat DEC_FORMAT = new DecimalFormat("#,###.##");
        if (value == null || value.isBlank())
            return "";

        try {
            String clean = value.replace(",", "").trim();
            double number = Double.parseDouble(clean);

            if (number == Math.floor(number))
                return INT_FORMAT.format(number);

            return DEC_FORMAT.format(number);

        } catch (Exception e) {
            return value; // không phải số
        }
    }
}

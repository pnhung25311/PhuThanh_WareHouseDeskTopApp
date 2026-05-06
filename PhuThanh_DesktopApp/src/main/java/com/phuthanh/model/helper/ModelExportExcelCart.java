package com.phuthanh.model.helper;

import java.time.LocalDate;

import com.phuthanh.model.enums.ExcelTemplateType;

public class ModelExportExcelCart {
    private LocalDate date;
    private ExcelTemplateType excelTemplateType;

    public ModelExportExcelCart() {
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ExcelTemplateType getExcelTemplateType() {
        return excelTemplateType;
    }

    public void setExcelTemplateType(ExcelTemplateType excelTemplateType) {
        this.excelTemplateType = excelTemplateType;
    }

}

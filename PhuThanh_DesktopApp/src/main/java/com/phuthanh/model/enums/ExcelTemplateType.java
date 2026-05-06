package com.phuthanh.model.enums;

public enum ExcelTemplateType {
    RECORDSIMPORTWH("Mẫu biên bản Nhập kho"),
    RECORDSEXPORTWH("Mẫu biên bản Xuất kho"),
    RECORDSTHL("Mẫu biên bản THL"),
    RECORDSDEONAI("Mẫu biên bản Đèo Nai"),
    RECORDSKTKS("Mẫu biên bản KTKS"),
    RECORDSCNMLK("Mẫu biên bản CNMLK"),
    RECORDSCNMKD("Mẫu biên bản CNMKD"),
    RECORDSDONGBAC("Mẫu biên bản Đông Bắc"),
    RECORDSPHUONGSON("Mẫu biên bản Phương Sơn"),
    RECORDSCBT("Mẫu biên bản CBT"),
    RECORDSCAOSON("Mẫu biên bản Cao Sơn"),
    RECORDSKHESIM("Mẫu biên bản Khe Sim"),
    RECORDSVIETBAC("Mẫu biên bản Việt Bắc"),
    RECORDSAPLUC("Mẫu biên bản Áp Lực"),
    RECORDSBANLE("Mẫu biên bản Bán Lẻ"),
    RECORDSUONGBI("Mẫu biên bản Uông bí");

    private final String displayName;

    ExcelTemplateType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

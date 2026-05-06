package com.phuthanh.warehouse.helper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.collections.ObservableList;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class CartFilterManager {

    private DatePicker dpFromDate;
    private DatePicker dpToDate;
    private TextField txtSearch;

    public CartFilterManager(TextField txtSearch, DatePicker from, DatePicker to) {
        this.txtSearch = txtSearch;
        this.dpFromDate = from;
        this.dpToDate = to;
    }

    public void attachAutoFilter(Runnable applyFilter) {
        txtSearch.textProperty().addListener((obs, o, n) -> applyFilter.run());
        dpFromDate.valueProperty().addListener((obs, o, n) -> applyFilter.run());
        dpToDate.valueProperty().addListener((obs, o, n) -> applyFilter.run());
    }

    public boolean matchRow(ObservableList<String> row, String keyword) {

        String kw = keyword == null ? "" : keyword.toLowerCase().trim();

        // SEARCH
        boolean matchSearch = kw.isBlank();
        if (!matchSearch) {
            for (String cell : row) {
                if (cell != null && cell.toLowerCase().contains(kw)) {
                    matchSearch = true;
                    break;
                }
            }
        }

        // DATE
        boolean matchDate = true;
        int dateColumn = 36;

        try {
            if (dateColumn < row.size()) {
                LocalDate rowDate = parseDate(row.get(dateColumn));

                LocalDate from = dpFromDate.getValue();
                LocalDate to = dpToDate.getValue();

                if (rowDate != null) {
                    if (from != null && rowDate.isBefore(from))
                        matchDate = false;
                    if (to != null && rowDate.isAfter(to))
                        matchDate = false;
                }
            }
        } catch (Exception ignored) {
        }

        return matchSearch && matchDate;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank())
            return null;

        if (dateStr.length() >= 10)
            dateStr = dateStr.substring(0, 10);

        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateStr,
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public boolean matchDate(ObservableList<String> row,
            LocalDate fromDate,
            LocalDate toDate,
            int indexDelivery) {

        if (fromDate == null || toDate == null)
            return true;

        try {
            // ⚠️ CHỈNH INDEX CỘT NGÀY Ở ĐÂY
            // Ví dụ cột LastTime nằm ở index 3
            String dateStr = row.get(indexDelivery);

            if (dateStr == null || dateStr.isBlank())
                return false;

            // format theo dữ liệu SQL của bạn
            LocalDate rowDate = LocalDate.parse(dateStr.substring(0, 10));

            return !rowDate.isBefore(fromDate) && !rowDate.isAfter(toDate);

        } catch (Exception e) {
            return true;
        }
    }

    public boolean matchKeyword(ObservableList<String> row, String keyword) {
        if (keyword == null || keyword.isBlank())
            return true;

        String lower = keyword.toLowerCase();

        for (String cell : row) {
            if (cell != null && cell.toLowerCase().contains(lower)) {
                return true;
            }
        }
        return false;
    }
}

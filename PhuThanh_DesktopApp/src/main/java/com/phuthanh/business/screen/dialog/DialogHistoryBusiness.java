package com.phuthanh.business.screen.dialog;

import com.phuthanh.helper.DBCallDataBusiness;

import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class DialogHistoryBusiness {

    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnReload;
    @FXML
    private TabPane tabPane;

    @FXML
    private TableView<ObservableList<String>> tableSale;
    @FXML
    private TableView<ObservableList<String>> tableImport;

    private ObservableList<ObservableList<String>> masterSaleData;
    private ObservableList<ObservableList<String>> masterImportData;

    private FilteredList<ObservableList<String>> filteredSale;
    private FilteredList<ObservableList<String>> filteredImport;

    private static final DBCallDataBusiness dbCallDataBusiness = new DBCallDataBusiness();

    private String sqlImport = "SELECT * FROM vwct70y WHERE sl_nhap > 0 ORDER BY ngay_ct DESC";
    private String sqlExport = "SELECT * FROM vwct90 WHERE sl_nhap > 0 ORDER BY ngay_ct DESC";

    @FXML
    public void initialize() {
        setupSearch();
    }

    // =====================================================
    // LOAD DATA
    // =====================================================
    private void loadData(String sqlEx, String sqlIm) {

        System.out.println("Loading Export: " + sqlEx);
        System.out.println("Loading Import: " + sqlIm);

        masterSaleData = dbCallDataBusiness.getDataSQL(tableSale, sqlEx);
        masterImportData = dbCallDataBusiness.getDataSQL(tableImport, sqlIm);

        // format số
        formatAllTableData(masterSaleData);
        formatAllTableData(masterImportData);

        // FilteredList
        filteredSale = new FilteredList<>(masterSaleData, p -> true);
        filteredImport = new FilteredList<>(masterImportData, p -> true);

        tableSale.setItems(filteredSale);
        tableImport.setItems(filteredImport);

        // ⭐⭐ DÒNG QUAN TRỌNG NHẤT ⭐⭐
        tableSale.refresh();
        tableImport.refresh();
    }

    public void initData(String sqlEx, String sqlIm) {
        this.sqlExport = sqlEx;
        this.sqlImport = sqlIm;
        loadData(sqlExport, sqlImport);
    }

    // =====================================================
    // FORMAT NUMBER
    // =====================================================
    private String formatNumberIfPossible(String value) {
        if (value == null || value.isBlank())
            return "";

        try {
            // bỏ dấu phẩy nếu có
            String clean = value.replace(",", "").trim();

            // parse số thập phân từ SQL
            double number = Double.parseDouble(clean);

            // nếu là số nguyên (27360000.00 -> 27360000)
            if (number == Math.floor(number)) {
                return new java.text.DecimalFormat("#,###")
                        .format(number);
            }

            // nếu còn phần lẻ (ví dụ 12.5)
            return new java.text.DecimalFormat("#,###.##")
                    .format(number);

        } catch (Exception e) {
            return value; // không phải số
        }
    }

    private void formatAllTableData(ObservableList<ObservableList<String>> data) {
        for (ObservableList<String> row : data) {
            for (int i = 0; i < row.size(); i++) {
                row.set(i, formatNumberIfPossible(row.get(i)));
            }
        }
    }

    // =====================================================
    // SEARCH (có hỗ trợ số có/không dấu phẩy)
    // =====================================================
    private void setupSearch() {

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {

            String keyword = newVal.toLowerCase().trim();

            filteredSale.setPredicate(row -> rowContainsKeyword(row, keyword));
            filteredImport.setPredicate(row -> rowContainsKeyword(row, keyword));

        });
    }

    private boolean rowContainsKeyword(ObservableList<String> row, String keyword) {

        if (keyword.isEmpty())
            return true;

        // bỏ dấu phẩy khi search
        String keywordClean = keyword.replace(",", "");

        for (String cell : row) {
            if (cell == null)
                continue;

            String cellClean = cell.toLowerCase().replace(",", "");

            if (cellClean.contains(keywordClean))
                return true;
        }
        return false;
    }
}
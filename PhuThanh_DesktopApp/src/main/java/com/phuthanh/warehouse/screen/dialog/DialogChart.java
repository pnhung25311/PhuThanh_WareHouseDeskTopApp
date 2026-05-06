package com.phuthanh.warehouse.screen.dialog;

import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;

public class DialogChart {
    @FXML
    private PieChart pieChart;

    @FXML
    public void initialize() {
    }

    private ObservableList<ObservableList<String>> data;

    // =====================================================
    // ✅ NHẬN DATA TỪ DialogStatistical
    // =====================================================
    public void setData(ObservableList<ObservableList<String>> data) {
        this.data = data;
        buildPieChart(); // load luôn
    }

    // =====================================================
    // ✅ BUILD PIECHART
    // =====================================================
    private void buildPieChart() {

        if (data == null || data.isEmpty())
            return;

        Map<String, Double> map = new HashMap<>();

        double total = 0; // ✅ tổng để tính %

        for (ObservableList<String> row : data) {

            String name = row.get(1); // cột NAME
            double value = Double.parseDouble(row.get(2)); // cột QTY

            map.put(name, map.getOrDefault(name, 0.0) + value);
            total += value;
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        double finalTotal = total;

        map.forEach((k, v) -> {

            double percent = (v / finalTotal) * 100.0;

            // ✅ hiển thị: NAME (xx.xx%)
            String label = String.format("%s (%.2f%%)", k, percent);

            pieData.add(new PieChart.Data(label, v));
        });

        pieChart.setData(pieData);
    }

    @FXML
    private void handleLoadData() {
        buildPieChart(); // bấm load lại nếu cần
    }
}

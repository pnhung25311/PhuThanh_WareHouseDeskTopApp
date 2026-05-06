package com.phuthanh.manager;

import com.phuthanh.helper.DbCRUDHelper;
// import com.phuthanh.helper.DbInfoHelper;
import com.phuthanh.helper.DbTableHelper;
import com.phuthanh.helper.TabViewHelper;
import com.phuthanh.model.warehouse.DrawerItem;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

public class TabContentManager {
    // private static final DbInfoHelper dbInfoHelper = new DbInfoHelper();
    private static final DbCRUDHelper dbCRUDHelper = new DbCRUDHelper();
    private static final DbTableHelper dbTableHelper = new DbTableHelper();
    private static final TabViewHelper tabViewHelper = new TabViewHelper();

    public void loadTabDetails(
            Tab tab,
            TableView<ObservableList<String>> table,
            DrawerItem drawerItem) {
        if (!tab.isSelected())
            return;

        String aid = tabViewHelper.getSelectedAID();
        if (aid == null || aid.isEmpty()) {
            setEmptyContent(tab, "Chưa chọn dòng để hiển thị dữ liệu");
            return;
        }

        if (drawerItem == null || drawerItem.getWareHouseHistory() == null) {
            setEmptyContent(tab, "Không có dữ liệu từ Drawer");
            return;
        }

        try {
            dbTableHelper.loadTableDetails(table,
                    drawerItem.getWareHouseHistory(),
                    "DataWareHouseAID",
                    aid);

            if (table.getItems().isEmpty()) {
                setEmptyContent(tab, "Không có dữ liệu cho AID: " + aid);
            } else {
                tab.setContent(table);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            setEmptyContent(tab, "Lỗi khi load dữ liệu!");
        }
    }

    public void loadTabHistoryToRow(
            Tab tab,
            TableView<ObservableList<String>> table,
            DrawerItem drawerItem) {

        try {
            if (!tab.isSelected())
                return;

            String aid = tabViewHelper.getSelectedAID();

            if (aid == null || aid.isEmpty()) {
                setEmptyContent(tab, "Chưa chọn dòng để hiển thị dữ liệu");
                return;
            }

            if (drawerItem == null || drawerItem.getWareHouseHistory() == null) {
                setEmptyContent(tab, "Không có dữ liệu từ Drawer");
                return;
            }
            System.out.println("Loading Row History for AID: " + aid);
            String whAID = dbCRUDHelper.returnAID(drawerItem.getWareHouseHistory(), "DataWareHouseAID",
                    "HistoryAID", aid);
            System.out.println("Loading Row History for WH AID: " + whAID);
            dbTableHelper.loadTableDetails(table,
                    drawerItem.getWareHouseHistory(),
                    "DataWareHouseAID",
                    whAID);

            if (table.getItems().isEmpty()) {
                setEmptyContent(tab, "Không có dữ liệu cho AID: " + whAID);
            } else {
                tab.setContent(table);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            setEmptyContent(tab, "Lỗi khi load dữ liệu!");
        }
    }

    public void loadTabHistoryToRowOntabmenu(
            Tab tab,
            TableView<ObservableList<String>> table,
            DrawerItem drawerItem) {

        try {
            if (!tab.isSelected())
                return;

            table.getSelectionModel().selectFirst();
            String aid = tabViewHelper.getSelectedAID();

            if (aid == null || aid.isEmpty()) {
                setEmptyContent(tab, "Chưa chọn dòng để hiển thị dữ liệu");
                return;
            }

            if (drawerItem == null || drawerItem.getWareHouseHistory() == null) {
                setEmptyContent(tab, "Không có dữ liệu từ Drawer");
                return;
            }
            // System.out.println("Loading Row History for AID: " + aid);
            // String whAID = DbCRUDHelper.returnAID(drawerItem.getWareHouseHistory(),
            // "DataWareHouseAID",
            // "HistoryAID", aid);
            // System.out.println("Loading Row History for WH AID: " + whAID);
            dbTableHelper.loadTableDetails(table,
                    drawerItem.getWareHouseHistory(),
                    "DataWareHouseAID",
                    aid);

            if (table.getItems().isEmpty()) {
                setEmptyContent(tab, "Không có dữ liệu cho AID: " + aid);
            } else {
                tab.setContent(table);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            setEmptyContent(tab, "Lỗi khi load dữ liệu!");
        }
    }

    public void loadTabHistoryToRowOntabmenuToRow(
            Tab tab,
            TableView<ObservableList<String>> table,
            DrawerItem drawerItem, String codeAID) {

        try {
            if (!tab.isSelected())
                return;

            String aid = codeAID;

            if (aid == null || aid.isEmpty()) {
                setEmptyContent(tab, "Chưa chọn dòng để hiển thị dữ liệu");
                return;
            }

            if (drawerItem == null || drawerItem.getWareHouseHistory() == null) {
                setEmptyContent(tab, "Không có dữ liệu từ Drawer");
                return;
            }
            // System.out.println("Loading Row History for AID: " + aid);
            // String whAID = DbCRUDHelper.returnAID(drawerItem.getWareHouseHistory(),
            // "DataWareHouseAID",
            // "HistoryAID", aid);
            // System.out.println("Loading Row History for WH AID: " + whAID);
            dbTableHelper.loadTableDetails(table,
                    drawerItem.getWareHouseHistory(),
                    "DataWareHouseAID",
                    aid);

            if (table.getItems().isEmpty()) {
                setEmptyContent(tab, "Không có dữ liệu cho AID: " + aid);
            } else {
                tab.setContent(table);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            setEmptyContent(tab, "Lỗi khi load dữ liệu!");
        }
    }

    public void loadTabHistoryUpdateToRow(
            Tab tab,
            TableView<ObservableList<String>> table,
            DrawerItem drawerItem) {

        try {
            if (!tab.isSelected())
                return;
            System.out.println("Loading Update History");

            if (drawerItem == null || drawerItem.getWareHouseHistory() == null) {
                setEmptyContent(tab, "Không có dữ liệu từ Drawer");
                return;
            }
            dbTableHelper.loadTableRequestConvert(table,
                    drawerItem.getWareHouseUpdateHistory());

            if (table.getItems().isEmpty()) {
                setEmptyContent(tab, "Không có dữ liệu cho ");
            } else {
                tab.setContent(table);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            setEmptyContent(tab, "Lỗi khi load dữ liệu!");
        }
    }

    private void setEmptyContent(Tab tab, String msg) {
        Label label = new Label(msg);
        label.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");
        StackPane box = new StackPane(label);
        box.setAlignment(Pos.CENTER);
        tab.setContent(box);
    }

    public void loadTabRequest(
            Tab tab,
            TableView<ObservableList<String>> table,
            DrawerItem drawerItem) {

        if (!tab.isSelected())
            return;

        if (drawerItem == null || drawerItem.getWareHouseRequest() == null) {
            setEmptyContent(tab, "Không có dữ liệu từ Drawer");
            return;
        }

        try {
            // ⬅️ Load toàn bộ bảng, KHÔNG lọc theo AID nữa

            dbTableHelper.loadTableRequestConvert(
                    table,
                    drawerItem.getWareHouseRequest());
            if (table.getItems().isEmpty()) {
                setEmptyContent(tab, "Bảng không có dữ liệu!");
            } else {
                tab.setContent(table);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            setEmptyContent(tab, "Lỗi khi load dữ liệu!");
        }
    }

}

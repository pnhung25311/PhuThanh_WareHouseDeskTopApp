package com.phuthanh.warehouse.helper;

import com.phuthanh.helper.DbTableHelper;
import com.phuthanh.model.info.Account;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class CartRepository {

    private final DbTableHelper db = new DbTableHelper();

    public ObservableList<ObservableList<String>> loadCart(
            TableView<ObservableList<String>> table,
            Integer typeCartID,
            Account account,
            String from,
            String to) {

        String roleFilter = buildRoleFilter(account);
        String typeFilter = typeCartID == null ? "" : " AND TypeCartID = " + typeCartID;

        String sql = """
            SELECT * FROM vwCart
            WHERE 1=1
            """ + typeFilter + roleFilter + """
            AND dbo.fnFromDateToDate(DeliveryTime, '%s', '%s') = 1
            ORDER BY LastTime DESC, CartAID DESC
            """.formatted(from, to);

        return db.loadDataTable(table, sql);
    }

    public String buildRoleFilter(Account acc) {
        if (acc.getRole().equals("WAREHOUSE") || acc.getRole().equals("ADMIN"))
            return "";

        return " AND (AccountID = " + acc.getAccountID()
                + " OR EmployeeID = " + acc.getEmployeeID() + ")";
    }
}

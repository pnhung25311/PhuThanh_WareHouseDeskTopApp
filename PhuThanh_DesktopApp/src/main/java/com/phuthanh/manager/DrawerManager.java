package com.phuthanh.manager;

import javafx.animation.TranslateTransition;
import javafx.scene.layout.AnchorPane;
// import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class DrawerManager {

    public void showDrawer(AnchorPane drawer, AnchorPane overlay) {
        overlay.setVisible(true);
        overlay.setMouseTransparent(false);
        overlay.toFront();
        drawer.toFront();

        TranslateTransition tt = new TranslateTransition(Duration.millis(250), drawer);
        tt.setToX(0);
        tt.play();
    }

    public void hideDrawer(AnchorPane drawer, AnchorPane overlay, double drawerWidth) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(250), drawer);
        tt.setToX(-drawerWidth);
        tt.setOnFinished(e -> {
            overlay.setVisible(false);
            overlay.setMouseTransparent(true);
        });
        tt.play();
    }

    public void toggleDrawer(AnchorPane drawer, AnchorPane overlay, double drawerWidth) {
        if (drawer.getTranslateX() < 0) showDrawer(drawer, overlay);
        else hideDrawer(drawer, overlay, drawerWidth);
    }
}

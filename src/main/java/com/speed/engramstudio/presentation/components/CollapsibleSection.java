package com.speed.engramstudio.presentation.components;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class CollapsibleSection extends VBox {

    private final Label headerLabel;
    private final Label chevron;
    private final VBox contentBox;
    private boolean expanded = true;
    private Animation animation;

    public CollapsibleSection(String title, Node... content) {
        setStyle("-fx-background-color: transparent;");

        headerLabel = new Label(title);
        headerLabel.setStyle("-fx-text-fill: #858585; -fx-font-size: 11px; -fx-font-weight: bold;");

        chevron = new Label("\u25BC");
        chevron.setStyle("-fx-text-fill: #555555; -fx-font-size: 10px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(6, chevron, headerLabel, spacer);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(8, 12, 8, 12));
        header.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
        header.setOnMouseEntered(e -> header.setStyle("-fx-cursor: hand; -fx-background-color: #1E1E1E;"));
        header.setOnMouseExited(e -> header.setStyle("-fx-cursor: hand; -fx-background-color: transparent;"));
        header.setOnMouseClicked(e -> toggle());

        contentBox = new VBox(4);
        contentBox.setPadding(new Insets(0, 12, 8, 12));
        contentBox.getChildren().addAll(content);

        getChildren().addAll(header, contentBox);
    }

    public void toggle() {
        if (expanded) {
            collapse();
        } else {
            expand();
        }
    }

    public void expand() {
        expanded = true;
        chevron.setText("\u25BC");
        if (animation != null) animation.stop();
        animation = new Transition() {
            {
                setCycleDuration(Duration.millis(150));
                setOnFinished(e -> contentBox.setVisible(true));
            }
            @Override
            protected void interpolate(double frac) {
                contentBox.setMaxHeight(contentBox.prefHeight(-1) * frac);
                contentBox.setVisible(frac > 0);
            }
        };
        contentBox.setVisible(true);
        contentBox.setManaged(true);
        animation.play();
    }

    public void collapse() {
        expanded = false;
        chevron.setText("\u25B6");
        if (animation != null) animation.stop();
        animation = new Transition() {
            {
                setCycleDuration(Duration.millis(150));
                setOnFinished(e -> {
                    contentBox.setVisible(false);
                    contentBox.setManaged(false);
                });
            }
            @Override
            protected void interpolate(double frac) {
                contentBox.setMaxHeight(contentBox.prefHeight(-1) * (1 - frac));
                contentBox.setVisible(frac < 1);
            }
        };
        animation.play();
    }

    public boolean isExpanded() {
        return expanded;
    }
}

package com.speed.engramstudio.presentation.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DetailWindow {

    private static Stage currentStage;
    private double dragOffsetX, dragOffsetY;

    public void show(String title, TextFlow content) {
        if (currentStage != null && currentStage.isShowing()) {
            currentStage.close();
        }

        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        currentStage = stage;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0A0A0A;");

        HBox titleBar = createTitleBar(stage, title);
        root.setTop(titleBar);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setStyle("-fx-background-color: #1E1E1E; -fx-border-color: #2A2A2A;");
        scrollPane.setMinHeight(0);

        BorderPane.setMargin(scrollPane, new Insets(0));
        root.setCenter(scrollPane);

        javafx.geometry.Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
        scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        scene.setFill(javafx.scene.paint.Color.web("#0A0A0A"));

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.setScene(scene);
        stage.show();
    }

    private HBox createTitleBar(Stage stage, String title) {
        HBox titleBar = new HBox();
        titleBar.getStyleClass().add("title-bar");
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(0, 8, 0, 0));
        titleBar.setPrefHeight(32);
        titleBar.setMinHeight(32);
        titleBar.setMaxHeight(32);

        Label icon = new Label("\u25C8");
        icon.getStyleClass().add("title-bar-icon");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title-bar-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label minBtn = createWindowButton("\u2500");
        Label maxBtn = createWindowButton("\u25A1");
        Label closeBtn = createWindowButton("\u2715");

        minBtn.setOnMouseClicked(e -> stage.setIconified(true));
        maxBtn.setOnMouseClicked(e -> {
            javafx.geometry.Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            if (stage.getWidth() < bounds.getWidth() - 10 || stage.getHeight() < bounds.getHeight() - 10) {
                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());
                stage.setWidth(bounds.getWidth());
                stage.setHeight(bounds.getHeight());
                maxBtn.setText("\u25A3");
            } else {
                stage.setWidth(800);
                stage.setHeight(600);
                stage.centerOnScreen();
                maxBtn.setText("\u25A1");
            }
        });
        closeBtn.setOnMouseClicked(e -> stage.close());
        closeBtn.setOnMouseEntered(e -> closeBtn.getStyleClass().add("title-bar-btn-close"));
        closeBtn.setOnMouseExited(e -> closeBtn.getStyleClass().remove("title-bar-btn-close"));

        titleBar.getChildren().addAll(icon, titleLabel, spacer, minBtn, maxBtn, closeBtn);

        titleBar.setOnMousePressed(e -> {
            if (e.getTarget() == titleBar || e.getTarget() == titleLabel || e.getTarget() == icon) {
                dragOffsetX = e.getScreenX() - stage.getX();
                dragOffsetY = e.getScreenY() - stage.getY();
            }
        });
        titleBar.setOnMouseDragged(e -> {
            if (e.getTarget() == titleBar || e.getTarget() == titleLabel || e.getTarget() == icon) {
                stage.setX(e.getScreenX() - dragOffsetX);
                stage.setY(e.getScreenY() - dragOffsetY);
            }
        });
        titleBar.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && (e.getTarget() == titleBar || e.getTarget() == titleLabel || e.getTarget() == icon)) {
                javafx.geometry.Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
                if (stage.getWidth() < bounds.getWidth() - 10 || stage.getHeight() < bounds.getHeight() - 10) {
                    stage.setX(bounds.getMinX());
                    stage.setY(bounds.getMinY());
                    stage.setWidth(bounds.getWidth());
                    stage.setHeight(bounds.getHeight());
                    maxBtn.setText("\u25A3");
                } else {
                    stage.setWidth(800);
                    stage.setHeight(600);
                    stage.centerOnScreen();
                    maxBtn.setText("\u25A1");
                }
            }
        });

        return titleBar;
    }

    private Label createWindowButton(String text) {
        Label btn = new Label(text);
        btn.getStyleClass().add("title-bar-btn");
        btn.setPrefSize(32, 32);
        btn.setAlignment(Pos.CENTER);
        return btn;
    }
}

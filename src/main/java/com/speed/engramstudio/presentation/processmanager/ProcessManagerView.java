package com.speed.engramstudio.presentation.processmanager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ProcessManagerView {

    private final ProcessManagerViewModel viewModel;
    private VBox root;

    public ProcessManagerView(ProcessManagerViewModel viewModel) {
        this.viewModel = viewModel;
        buildUI();
    }

    private void buildUI() {
        root = new VBox(0);
        root.getStyleClass().add("content-area");

        HBox header = createHeader();
        root.getChildren().add(header);

        VBox content = createContent();
        VBox.setVgrow(content, Priority.ALWAYS);
        root.getChildren().add(content);
    }

    private HBox createHeader() {
        HBox header = new HBox(16);
        header.getStyleClass().add("header");
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label title = new Label("PROCESS MANAGER");
        title.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button detectBtn = new Button("REFRESH");
        detectBtn.setOnAction(e -> viewModel.detect());

        header.getChildren().addAll(title, spacer, detectBtn);
        return header;
    }

    private VBox createContent() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(20));

        // Status card
        VBox statusCard = new VBox(8);
        statusCard.getStyleClass().add("stat-card");
        statusCard.setPadding(new Insets(16));

        Label statusTitle = new Label("STATUS");
        statusTitle.getStyleClass().add("stat-label");

        Label statusValue = new Label();
        statusValue.getStyleClass().add("stat-value");
        statusValue.textProperty().bind(viewModel.processStatusProperty());
        statusValue.textProperty().addListener((obs, old, val) -> {
            statusValue.getStyleClass().removeAll("status-connected", "status-disconnected");
            statusValue.getStyleClass().add("RUNNING".equals(val) ? "status-connected" : "status-disconnected");
        });

        statusCard.getChildren().addAll(statusTitle, statusValue);

        // Process info card
        VBox infoCard = new VBox(8);
        infoCard.getStyleClass().add("stat-card");
        infoCard.setPadding(new Insets(16));

        Label infoTitle = new Label("PROCESS INFO");
        infoTitle.getStyleClass().add("stat-label");

        TextArea infoArea = new TextArea();
        infoArea.setEditable(false);
        infoArea.setWrapText(true);
        infoArea.getStyleClass().add("content-text-area");
        infoArea.setPrefHeight(120);
        infoArea.textProperty().bind(viewModel.processInfoProperty());

        infoCard.getChildren().addAll(infoTitle, infoArea);

        // Action result
        Label resultLabel = new Label();
        resultLabel.getStyleClass().add("status-text");
        resultLabel.textProperty().bind(viewModel.actionResultProperty());

        // Control buttons
        HBox buttons = new HBox(12);
        buttons.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button startBtn = new Button("START");
        startBtn.getStyleClass().add("button-accent");
        startBtn.setOnAction(e -> viewModel.start());

        Button stopBtn = new Button("STOP");
        stopBtn.setStyle("-fx-text-fill: #FF5252;");
        stopBtn.setOnAction(e -> viewModel.stop());

        Button restartBtn = new Button("RESTART");
        restartBtn.setOnAction(e -> viewModel.restart());

        buttons.getChildren().addAll(startBtn, stopBtn, restartBtn);

        // Info text
        Label infoText = new Label("Engram binary: resolved from PATH, GOBIN or the user Go bin directory");
        infoText.getStyleClass().add("sidebar-label");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        content.getChildren().addAll(statusCard, infoCard, resultLabel, buttons, spacer, infoText);
        return content;
    }

    public VBox getView() {
        return root;
    }

    public void refresh() {
        viewModel.detect();
    }
}

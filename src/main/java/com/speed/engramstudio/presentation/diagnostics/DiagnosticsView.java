package com.speed.engramstudio.presentation.diagnostics;

import com.speed.engramstudio.domain.model.CheckResult;
import com.speed.engramstudio.domain.model.DiagnosticsReport;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class DiagnosticsView {

    private final DiagnosticsViewModel viewModel;
    private VBox root;
    private SplitPane splitPane;
    private TextArea detailArea;

    public DiagnosticsView(DiagnosticsViewModel viewModel) {
        this.viewModel = viewModel;
        buildUI();
    }

    private void buildUI() {
        root = new VBox(0);
        root.getStyleClass().add("content-area");

        HBox header = createHeader();
        root.getChildren().add(header);

        splitPane = new SplitPane();
        splitPane.setDividerPositions(0.5);

        VBox listPanel = createListPanel();
        VBox detailPanel = createDetailPanel();

        splitPane.getItems().addAll(listPanel, detailPanel);

        VBox.setVgrow(splitPane, Priority.ALWAYS);
        root.getChildren().add(splitPane);
    }

    private HBox createHeader() {
        HBox header = new HBox(16);
        header.getStyleClass().add("header");
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label title = new Label("DIAGNOSTICS");
        title.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button runBtn = new Button("RUN DOCTOR");
        runBtn.getStyleClass().add("button-accent");
        runBtn.setOnAction(e -> viewModel.load());

        Label status = new Label();
        status.getStyleClass().addAll("status-text", "status-connected");
        viewModel.statusMessageProperty().addListener((obs, old, val) -> {
            status.setText("[" + val + "]");
        });

        header.getChildren().addAll(title, spacer, runBtn, status);
        return header;
    }

    private VBox createListPanel() {
        VBox panel = new VBox(0);
        panel.getStyleClass().add("master-panel");

        // Summary cards
        HBox summaryBar = new HBox(12);
        summaryBar.setPadding(new Insets(12));
        summaryBar.getStyleClass().add("filter-bar");

        Label totalLabel = new Label();
        totalLabel.getStyleClass().add("tag");
        Label okLabel = new Label();
        okLabel.getStyleClass().addAll("tag", "tag-type");
        Label warnLabel = new Label();
        warnLabel.getStyleClass().add("tag");
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("tag");

        viewModel.reportProperty().addListener((obs, old, val) -> {
            if (val != null) {
                totalLabel.setText("TOTAL: " + val.summary().total());
                okLabel.setText("OK: " + val.summary().ok());
                warnLabel.setText("WARNINGS: " + val.summary().warnings());
                errorLabel.setText("ERRORS: " + val.summary().errors());
            }
        });

        summaryBar.getChildren().addAll(totalLabel, okLabel, warnLabel, errorLabel);

        // Checks table
        TableView<CheckResult> table = new TableView<>(viewModel.checks());
        table.setPlaceholder(new Label("Run diagnostics to see results"));
        table.getStyleClass().add("observation-table");

        TableColumn<CheckResult, String> idCol = new TableColumn<>("CHECK");
        idCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().checkId()));
        idCol.setPrefWidth(280);

        TableColumn<CheckResult, String> resultCol = new TableColumn<>("RESULT");
        resultCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().result()));
        resultCol.setPrefWidth(80);
        resultCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label tag = new Label(item.toUpperCase());
                    tag.getStyleClass().add("tag");
                    if ("ok".equalsIgnoreCase(item)) {
                        tag.getStyleClass().add("tag-type");
                    } else if ("warning".equalsIgnoreCase(item)) {
                        tag.setStyle("-fx-text-fill: #FFB300; -fx-background-color: #FFB30020;");
                    } else if ("error".equalsIgnoreCase(item) || "blocked".equalsIgnoreCase(item)) {
                        tag.setStyle("-fx-text-fill: #FF5252; -fx-background-color: #FF525220;");
                    }
                    setGraphic(tag);
                    setText(null);
                }
            }
        });

        TableColumn<CheckResult, String> severityCol = new TableColumn<>("SEVERITY");
        severityCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().severity()));
        severityCol.setPrefWidth(80);

        TableColumn<CheckResult, String> msgCol = new TableColumn<>("MESSAGE");
        msgCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().message()));
        msgCol.setPrefWidth(300);

        table.getColumns().addAll(idCol, resultCol, severityCol, msgCol);

        // Selection shows detail
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("CHECK: ").append(val.checkId()).append("\n");
                sb.append("RESULT: ").append(val.result()).append("\n");
                sb.append("SEVERITY: ").append(val.severity()).append("\n");
                sb.append("REASON: ").append(val.reasonCode()).append("\n\n");
                sb.append("MESSAGE:\n").append(val.message()).append("\n\n");
                sb.append("WHY:\n").append(val.why()).append("\n\n");
                sb.append("EVIDENCE:\n");
                val.evidence().forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
                sb.append("\nSAFE NEXT STEP:\n").append(val.safeNextStep());
                detailArea.setText(sb.toString());
            }
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        panel.getChildren().addAll(summaryBar, table);
        return panel;
    }

    private VBox createDetailPanel() {
        VBox panel = new VBox(0);
        panel.getStyleClass().add("detail-panel");

        HBox detailHeader = new HBox(12);
        detailHeader.setPadding(new Insets(12));
        detailHeader.getStyleClass().add("detail-header");
        detailHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label detailTitle = new Label("CHECK DETAIL");
        detailTitle.getStyleClass().add("stat-label");

        detailHeader.getChildren().add(detailTitle);

        detailArea = new TextArea();
        detailArea.setEditable(false);
        detailArea.setWrapText(true);
        detailArea.getStyleClass().add("content-text-area");
        detailArea.setPromptText("Select a check to view its details...");

        VBox.setVgrow(detailArea, Priority.ALWAYS);
        panel.getChildren().addAll(detailHeader, detailArea);
        return panel;
    }

    public VBox getView() {
        return root;
    }

    public void refresh() {
        viewModel.load();
    }
}

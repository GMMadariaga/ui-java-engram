package com.speed.engramstudio.presentation.conflicts;

import com.speed.engramstudio.domain.model.Conflict;
import com.speed.engramstudio.infrastructure.markdown.MarkdownRenderer;
import com.speed.engramstudio.presentation.components.DetailWindow;
import com.speed.engramstudio.presentation.components.DateTimeDisplay;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;

public class ConflictsView {

    private final ConflictsViewModel viewModel;
    private VBox root;
    private DetailWindow detailWindow = new DetailWindow();

    public ConflictsView(ConflictsViewModel viewModel) {
        this.viewModel = viewModel;
        buildUI();
    }

    private void buildUI() {
        root = new VBox(0);
        root.getStyleClass().add("content-area");

        HBox header = createHeader();
        root.getChildren().add(header);

        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.35);

        VBox masterPanel = createMasterPanel();
        VBox detailPanel = createDetailPanel();

        splitPane.getItems().addAll(masterPanel, detailPanel);
        SplitPane.setResizableWithParent(masterPanel, true);
        SplitPane.setResizableWithParent(detailPanel, true);

        VBox.setVgrow(splitPane, Priority.ALWAYS);
        root.getChildren().add(splitPane);
    }

    private HBox createHeader() {
        HBox header = new HBox(16);
        header.getStyleClass().add("header");
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label title = new Label("CONFLICTS & RELATIONS");
        title.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("REFRESH");
        refreshBtn.setOnAction(e -> viewModel.load());

        Label status = new Label();
        status.getStyleClass().addAll("status-text", "status-connected");
        viewModel.statusMessageProperty().addListener((obs, old, val) -> status.setText("[" + val + "]"));

        header.getChildren().addAll(title, spacer, refreshBtn, status);
        return header;
    }

    private VBox createMasterPanel() {
        VBox panel = new VBox(0);
        panel.getStyleClass().add("master-panel");

        TableView<Conflict> table = new TableView<>(viewModel.conflicts());
        table.setPlaceholder(new Label("No relations loaded"));
        table.getStyleClass().add("observation-table");

        TableColumn<Conflict, Long> idCol = new TableColumn<>("#");
        idCol.setCellValueFactory(p -> new javafx.beans.property.SimpleLongProperty(p.getValue().id()).asObject());
        idCol.setPrefWidth(50);

        TableColumn<Conflict, String> relationCol = new TableColumn<>("RELATION");
        relationCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().relation().name()));
        relationCol.setPrefWidth(120);
        relationCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label tag = new Label(item);
                    tag.getStyleClass().add("tag");
                    switch (item) {
                        case "NOT_CONFLICT" -> tag.getStyleClass().add("tag-read");
                        case "CONFLICTS_WITH" -> tag.getStyleClass().add("tag-project");
                        case "RELATED" -> tag.getStyleClass().add("tag-type");
                        case "SCOPED" -> tag.getStyleClass().add("tag-new");
                        default -> tag.getStyleClass().add("tag-type");
                    }
                    setGraphic(tag);
                    setText(null);
                }
            }
        });

        TableColumn<Conflict, String> statusCol = new TableColumn<>("STATUS");
        statusCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().judgmentStatus().name()));
        statusCol.setPrefWidth(80);

        TableColumn<Conflict, String> sourceCol = new TableColumn<>("SOURCE");
        sourceCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().sourceTitle()));
        sourceCol.setPrefWidth(250);

        TableColumn<Conflict, String> targetCol = new TableColumn<>("TARGET");
        targetCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().targetTitle()));
        targetCol.setPrefWidth(250);

        TableColumn<Conflict, String> dateCol = new TableColumn<>("UPDATED");
        dateCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(
            DateTimeDisplay.format(p.getValue().updatedAt())));
        dateCol.setPrefWidth(140);

        TableColumn<Conflict, Void> openCol = new TableColumn<>("ACTION");
        openCol.setPrefWidth(70);
        openCol.setCellFactory(col -> new TableCell<>() {
            private final Button openBtn = new Button("OPEN");
            {
                openBtn.getStyleClass().addAll("button", "button-accent");
                openBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 8 2 8;");
                openBtn.setOnAction(e -> {
                    Conflict c = getTableView().getItems().get(getIndex());
                    if (c != null) openConflictDetail(c);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : openBtn);
            }
        });

        table.getColumns().addAll(idCol, openCol, relationCol, statusCol, sourceCol, targetCol, dateCol);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) viewModel.selectedConflictProperty().set(val);
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        panel.getChildren().add(table);
        return panel;
    }

    private VBox createDetailPanel() {
        VBox panel = new VBox(0);
        panel.getStyleClass().add("detail-panel");

        HBox detailHeader = new HBox(12);
        detailHeader.setPadding(new Insets(12));
        detailHeader.getStyleClass().add("detail-header");
        detailHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label detailTitle = new Label("RELATION DETAIL");
        detailTitle.getStyleClass().add("stat-label");

        Label relationTag = new Label();
        relationTag.getStyleClass().addAll("tag", "tag-type");
        Label statusTag = new Label();
        statusTag.getStyleClass().add("tag");

        viewModel.selectedConflictProperty().addListener((obs, old, val) -> {
            if (val != null) {
                relationTag.setText(val.relation().name());
                statusTag.setText(val.judgmentStatus().name());
            } else {
                relationTag.setText("");
                statusTag.setText("");
            }
        });

        detailHeader.getChildren().addAll(detailTitle, relationTag, statusTag);

        Label sourceLabel = new Label("SOURCE");
        sourceLabel.getStyleClass().add("stat-label");
        sourceLabel.setPadding(new Insets(4, 12, 0, 12));

        Label sourceContent = new Label();
        sourceContent.getStyleClass().add("sidebar-label");
        sourceContent.setWrapText(true);
        sourceContent.setPadding(new Insets(0, 12, 8, 12));

        Label targetLabel = new Label("TARGET");
        targetLabel.getStyleClass().add("stat-label");
        targetLabel.setPadding(new Insets(4, 12, 0, 12));

        Label targetContent = new Label();
        targetContent.getStyleClass().add("sidebar-label");
        targetContent.setWrapText(true);
        targetContent.setPadding(new Insets(0, 12, 8, 12));

        Label dateLabel = new Label("DATES");
        dateLabel.getStyleClass().add("stat-label");
        dateLabel.setPadding(new Insets(4, 12, 0, 12));

        Label dateContent = new Label();
        dateContent.getStyleClass().add("sidebar-label");
        dateContent.setWrapText(true);
        dateContent.setPadding(new Insets(0, 12, 8, 12));

        viewModel.selectedConflictProperty().addListener((obs, old, val) -> {
            if (val != null) {
                sourceContent.setText(val.sourceTitle() != null ? val.sourceTitle() : val.sourceId());
                targetContent.setText(val.targetTitle() != null ? val.targetTitle() : val.targetId());
                String created = DateTimeDisplay.formatOr(val.createdAt(), "N/A");
                String updated = DateTimeDisplay.formatOr(val.updatedAt(), "N/A");
                dateContent.setText("Created: " + created + "\nUpdated: " + updated);
            } else {
                sourceContent.setText("Select a relation to view details");
                targetContent.setText("");
                dateContent.setText("");
            }
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        panel.getChildren().addAll(detailHeader, sourceLabel, sourceContent, targetLabel, targetContent, dateLabel, dateContent, spacer);
        return panel;
    }

    private void openConflictDetail(Conflict conflict) {
        TextFlow content = MarkdownRenderer.render(
            "## Relation: " + conflict.relation().name() + "\n\n" +
            "**Status:** " + conflict.judgmentStatus().name() + "\n\n" +
            "**Source:** " + conflict.sourceTitle() + "\n\n" +
            "**Target:** " + conflict.targetTitle() + "\n\n" +
            "**Created:** " + DateTimeDisplay.formatOr(conflict.createdAt(), "N/A") + "\n\n" +
            "**Updated:** " + DateTimeDisplay.formatOr(conflict.updatedAt(), "N/A")
        );
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #1E1E1E;");
        content.setLineSpacing(3);
        detailWindow.show(conflict.sourceTitle() + " → " + conflict.targetTitle(), content);
    }

    public VBox getView() {
        return root;
    }

    public void refresh() {
        viewModel.load();
    }
}

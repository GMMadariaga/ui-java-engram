package com.speed.engramstudio.presentation.sessions;

import com.speed.engramstudio.domain.model.Observation;
import com.speed.engramstudio.domain.model.Session;
import com.speed.engramstudio.infrastructure.markdown.MarkdownRenderer;
import com.speed.engramstudio.presentation.components.DetailWindow;
import com.speed.engramstudio.presentation.components.DateTimeDisplay;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;

public class SessionTimelineView {

    private final SessionTimelineViewModel viewModel;
    private VBox root;
    private DetailWindow detailWindow = new DetailWindow();

    public SessionTimelineView(SessionTimelineViewModel viewModel) {
        this.viewModel = viewModel;
        buildUI();
    }

    private void buildUI() {
        root = new VBox(0);
        root.getStyleClass().add("content-area");

        HBox header = createHeader();
        root.getChildren().add(header);

        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.3);

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

        Label title = new Label("SESSIONS");
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

        TableView<Session> table = new TableView<>(viewModel.sessions());
        table.setPlaceholder(new Label("No sessions loaded"));
        table.getStyleClass().add("observation-table");

        TableColumn<Session, String> idCol = new TableColumn<>("SESSION");
        idCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().id()));
        idCol.setPrefWidth(250);

        TableColumn<Session, String> projCol = new TableColumn<>("PROJECT");
        projCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().project()));
        projCol.setPrefWidth(150);

        TableColumn<Session, String> dateCol = new TableColumn<>("STARTED");
        dateCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(
            DateTimeDisplay.format(p.getValue().startedAt())));
        dateCol.setPrefWidth(150);

        TableColumn<Session, Integer> countCol = new TableColumn<>("OBS");
        countCol.setCellValueFactory(p -> new javafx.beans.property.SimpleIntegerProperty(p.getValue().observationCount()).asObject());
        countCol.setPrefWidth(60);

        TableColumn<Session, Void> openCol = new TableColumn<>("ACTION");
        openCol.setPrefWidth(70);
        openCol.setCellFactory(col -> new TableCell<>() {
            private final Button openBtn = new Button("OPEN");
            {
                openBtn.getStyleClass().addAll("button", "button-accent");
                openBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 8 2 8;");
                openBtn.setOnAction(e -> {
                    Session s = getTableView().getItems().get(getIndex());
                    if (s != null) openSessionDetail(s);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : openBtn);
            }
        });

        table.getColumns().addAll(idCol, openCol, projCol, dateCol, countCol);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) viewModel.selectSession(val);
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

        Label detailTitle = new Label("SESSION DETAIL");
        detailTitle.getStyleClass().add("stat-label");

        Label projMeta = new Label();
        projMeta.getStyleClass().addAll("tag", "tag-project");
        Label dateMeta = new Label();
        dateMeta.getStyleClass().add("tag");
        Label countMeta = new Label();
        countMeta.getStyleClass().add("tag");

        viewModel.selectedSessionProperty().addListener((obs, old, val) -> {
            if (val != null) {
                projMeta.setText(val.project() != null ? val.project() : "N/A");
                dateMeta.setText(DateTimeDisplay.formatOr(val.startedAt(), "N/A"));
                countMeta.setText("Observations: " + val.observationCount());
            } else {
                projMeta.setText("");
                dateMeta.setText("");
                countMeta.setText("");
            }
        });

        detailHeader.getChildren().addAll(detailTitle, projMeta, dateMeta, countMeta);

        Label obsTitle = new Label("SESSION OBSERVATIONS");
        obsTitle.getStyleClass().add("stat-label");
        obsTitle.setPadding(new Insets(0, 12, 4, 12));

        TableView<Observation> obsTable = new TableView<>(viewModel.sessionObservations());
        obsTable.setPlaceholder(new Label("Select a session to view observations"));
        obsTable.getStyleClass().add("observation-table");

        TableColumn<Observation, Long> idCol = new TableColumn<>("#");
        idCol.setCellValueFactory(p -> new javafx.beans.property.SimpleLongProperty(p.getValue().id()).asObject());
        idCol.setPrefWidth(50);

        TableColumn<Observation, String> typeCol = new TableColumn<>("TYPE");
        typeCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().type().name()));
        typeCol.setPrefWidth(80);

        TableColumn<Observation, String> titleCol = new TableColumn<>("TITLE");
        titleCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().title()));
        titleCol.setPrefWidth(300);

        TableColumn<Observation, String> dateCol = new TableColumn<>("CREATED");
        dateCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(
            DateTimeDisplay.format(p.getValue().createdAt())));
        dateCol.setPrefWidth(140);

        TableColumn<Observation, Void> openCol = new TableColumn<>("ACTION");
        openCol.setPrefWidth(70);
        openCol.setCellFactory(col -> new TableCell<>() {
            private final Button openBtn = new Button("OPEN");
            {
                openBtn.getStyleClass().addAll("button", "button-accent");
                openBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 8 2 8;");
                openBtn.setOnAction(e -> {
                    Observation obs = getTableView().getItems().get(getIndex());
                    if (obs != null) openObservationDetail(obs);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : openBtn);
            }
        });

        obsTable.getColumns().addAll(idCol, openCol, typeCol, titleCol, dateCol);

        Label contentLabel = new Label("CONTENT");
        contentLabel.getStyleClass().add("stat-label");
        contentLabel.setPadding(new Insets(4, 12, 4, 12));

        TextFlow contentFlow = new TextFlow();
        contentFlow.setPadding(new Insets(12));
        contentFlow.setStyle("-fx-background-color: #1E1E1E;");
        contentFlow.setLineSpacing(3);

        Label placeholder = new Label("Select an observation to view content...");
        placeholder.getStyleClass().add("sidebar-label");
        contentFlow.getChildren().add(placeholder);

        obsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            contentFlow.getChildren().clear();
            if (val != null && val.content() != null && !val.content().isEmpty()) {
                contentFlow.getChildren().add(MarkdownRenderer.render(val.content()));
            } else {
                Label empty = new Label("No content");
                empty.getStyleClass().add("sidebar-label");
                contentFlow.getChildren().add(empty);
            }
        });

        ScrollPane scrollPane = new ScrollPane(contentFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setStyle("-fx-background-color: #1E1E1E; -fx-border-color: #2A2A2A;");
        scrollPane.setMinHeight(0);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        panel.getChildren().addAll(detailHeader, obsTitle, obsTable, contentLabel, scrollPane);
        return panel;
    }

    private void openSessionDetail(Session session) {
        TextFlow content = MarkdownRenderer.render(
            "## Session: " + session.id() + "\n\n" +
            "**Project:** " + session.project() + "\n\n" +
            "**Started:** " + session.startedAt() + "\n\n" +
            "**Observations:** " + session.observationCount()
        );
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #1E1E1E;");
        content.setLineSpacing(3);
        detailWindow.show("Session: " + session.id(), content);
    }

    private void openObservationDetail(Observation obs) {
        TextFlow content = MarkdownRenderer.render(obs.content() != null ? obs.content() : "");
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #1E1E1E;");
        content.setLineSpacing(3);
        detailWindow.show(obs.title() != null ? obs.title() : "Observation #" + obs.id(), content);
    }

    public VBox getView() {
        return root;
    }

    public void refresh() {
        viewModel.load();
    }
}

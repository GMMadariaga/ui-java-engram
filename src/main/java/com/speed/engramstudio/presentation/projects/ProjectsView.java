package com.speed.engramstudio.presentation.projects;

import com.speed.engramstudio.domain.model.Observation;
import com.speed.engramstudio.domain.model.Project;
import com.speed.engramstudio.infrastructure.markdown.MarkdownRenderer;
import com.speed.engramstudio.presentation.components.DetailWindow;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;

public class ProjectsView {

    private final ProjectsViewModel viewModel;
    private VBox root;
    private DetailWindow detailWindow = new DetailWindow();

    public ProjectsView(ProjectsViewModel viewModel) {
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

        Label title = new Label("PROJECTS");
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

        TableView<Project> table = new TableView<>(viewModel.projects());
        table.setPlaceholder(new Label("No projects loaded"));
        table.getStyleClass().add("observation-table");

        TableColumn<Project, String> nameCol = new TableColumn<>("PROJECT");
        nameCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().name()));
        nameCol.setPrefWidth(200);

        TableColumn<Project, Integer> obsCol = new TableColumn<>("OBS");
        obsCol.setCellValueFactory(p -> new javafx.beans.property.SimpleIntegerProperty(p.getValue().observationCount()).asObject());
        obsCol.setPrefWidth(60);

        TableColumn<Project, Integer> sessCol = new TableColumn<>("SESSIONS");
        sessCol.setCellValueFactory(p -> new javafx.beans.property.SimpleIntegerProperty(p.getValue().sessionCount()).asObject());
        sessCol.setPrefWidth(80);

        TableColumn<Project, Void> openCol = new TableColumn<>("ACTION");
        openCol.setPrefWidth(70);
        openCol.setCellFactory(col -> new TableCell<>() {
            private final Button openBtn = new Button("OPEN");
            {
                openBtn.getStyleClass().addAll("button", "button-accent");
                openBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 8 2 8;");
                openBtn.setOnAction(e -> {
                    Project p = getTableView().getItems().get(getIndex());
                    if (p != null) openProjectDetail(p);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : openBtn);
            }
        });

        table.getColumns().addAll(nameCol, openCol, obsCol, sessCol);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) viewModel.selectProject(val);
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

        Label detailTitle = new Label("PROJECT OBSERVATIONS");
        detailTitle.getStyleClass().add("stat-label");

        Label projMeta = new Label();
        projMeta.getStyleClass().addAll("tag", "tag-project");
        Label obsCountMeta = new Label();
        obsCountMeta.getStyleClass().add("tag");
        Label sessCountMeta = new Label();
        sessCountMeta.getStyleClass().add("tag");

        viewModel.selectedProjectProperty().addListener((obs, old, val) -> {
            if (val != null) {
                projMeta.setText(val.name());
                obsCountMeta.setText("Obs: " + val.observationCount());
                sessCountMeta.setText("Sessions: " + val.sessionCount());
            } else {
                projMeta.setText("");
                obsCountMeta.setText("");
                sessCountMeta.setText("");
            }
        });

        detailHeader.getChildren().addAll(detailTitle, projMeta, obsCountMeta, sessCountMeta);

        TableView<Observation> obsTable = new TableView<>(viewModel.projectObservations());
        obsTable.setPlaceholder(new Label("Select a project to view observations"));
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
            p.getValue().createdAt() != null ? p.getValue().createdAt().toString().substring(0, 19) : ""));
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
        panel.getChildren().addAll(detailHeader, obsTable, contentLabel, scrollPane);
        return panel;
    }

    private void openProjectDetail(Project project) {
        TextFlow content = MarkdownRenderer.render(
            "## Project: " + project.name() + "\n\n" +
            "**Observations:** " + project.observationCount() + "\n\n" +
            "**Sessions:** " + project.sessionCount()
        );
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #1E1E1E;");
        content.setLineSpacing(3);
        detailWindow.show("Project: " + project.name(), content);
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

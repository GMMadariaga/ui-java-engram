package com.speed.engramstudio.presentation.dashboard;

import com.speed.engramstudio.infrastructure.engram.dto.ObservationDto;
import com.speed.engramstudio.infrastructure.engram.dto.SessionDto;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DashboardView {

    private final DashboardViewModel viewModel;
    private VBox root;

    public DashboardView(DashboardViewModel viewModel) {
        this.viewModel = viewModel;
        buildUI();
    }

    private void buildUI() {
        root = new VBox(0);
        root.getStyleClass().add("content-area");

        HBox header = createHeader();
        root.getChildren().add(header);

        GridPane statsGrid = createStatsGrid();
        statsGrid.setPadding(new Insets(20));

        VBox activitySection = createActivitySection();

        VBox.setVgrow(activitySection, Priority.ALWAYS);
        root.getChildren().addAll(statsGrid, activitySection);
    }

    private HBox createHeader() {
        HBox header = new HBox(16);
        header.getStyleClass().add("header");
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label title = new Label("DASHBOARD");
        title.getStyleClass().add("header-title");

        Label status = new Label();
        status.getStyleClass().addAll("status-text", "status-connected");
        viewModel.statusMessageProperty().addListener((obs, old, val) -> {
            status.setText("[" + val + "]");
        });

        header.getChildren().addAll(title, status);
        return header;
    }

    private GridPane createStatsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        VBox obsCard = createStatCard("OBSERVATIONS", viewModel.observationsProperty().asString());
        VBox sessCard = createStatCard("SESSIONS", viewModel.sessionsProperty().asString());
        VBox promptCard = createStatCard("PROMPTS", viewModel.promptsProperty().asString());
        VBox projCard = createStatCard("PROJECTS", viewModel.projectsTextProperty());

        GridPane.setHgrow(obsCard, Priority.ALWAYS);
        GridPane.setHgrow(sessCard, Priority.ALWAYS);
        GridPane.setHgrow(promptCard, Priority.ALWAYS);
        GridPane.setHgrow(projCard, Priority.ALWAYS);

        grid.add(obsCard, 0, 0);
        grid.add(sessCard, 1, 0);
        grid.add(promptCard, 2, 0);
        grid.add(projCard, 3, 0);

        return grid;
    }

    private VBox createStatCard(String label, javafx.beans.value.ObservableValue<? extends String> value) {
        Label valueLabel = new Label();
        valueLabel.getStyleClass().add("stat-value");
        valueLabel.textProperty().bind(value);

        Label titleLabel = new Label(label);
        titleLabel.getStyleClass().add("stat-label");

        VBox card = new VBox(4, valueLabel, titleLabel);
        card.getStyleClass().add("stat-card");
        card.setMinWidth(150);
        return card;
    }

    private VBox createActivitySection() {
        VBox section = new VBox(12);
        section.setPadding(new Insets(0, 20, 20, 20));

        Label obsTitle = new Label("RECENT OBSERVATIONS");
        obsTitle.getStyleClass().add("stat-label");

        TableView<ObservationDto> obsTable = new TableView<>(viewModel.recentObservations());
        obsTable.setPrefHeight(250);
        obsTable.setPlaceholder(new Label("No observations"));

        TableColumn<ObservationDto, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().id()));
        idCol.setPrefWidth(50);

        TableColumn<ObservationDto, String> typeCol = new TableColumn<>("TYPE");
        typeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().type()));
        typeCol.setPrefWidth(100);

        TableColumn<ObservationDto, String> titleCol = new TableColumn<>("TITLE");
        titleCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().title()));
        titleCol.setPrefWidth(350);

        TableColumn<ObservationDto, String> projectCol = new TableColumn<>("PROJECT");
        projectCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().project()));
        projectCol.setPrefWidth(120);

        TableColumn<ObservationDto, String> dateCol = new TableColumn<>("CREATED");
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().createdAt()));
        dateCol.setPrefWidth(150);

        obsTable.getColumns().addAll(idCol, typeCol, titleCol, projectCol, dateCol);

        Label sessTitle = new Label("RECENT SESSIONS");
        sessTitle.getStyleClass().add("stat-label");

        TableView<SessionDto> sessTable = new TableView<>(viewModel.recentSessions());
        sessTable.setPrefHeight(150);
        sessTable.setPlaceholder(new Label("No sessions"));

        TableColumn<SessionDto, String> sessIdCol = new TableColumn<>("SESSION");
        sessIdCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().id()));
        sessIdCol.setPrefWidth(250);

        TableColumn<SessionDto, String> sessProjCol = new TableColumn<>("PROJECT");
        sessProjCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().project()));
        sessProjCol.setPrefWidth(150);

        TableColumn<SessionDto, String> sessDateCol = new TableColumn<>("STARTED");
        sessDateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().startedAt()));
        sessDateCol.setPrefWidth(150);

        TableColumn<SessionDto, Number> sessCountCol = new TableColumn<>("OBSCOUNT");
        sessCountCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().observationCount()));
        sessCountCol.setPrefWidth(80);

        sessTable.getColumns().addAll(sessIdCol, sessProjCol, sessDateCol, sessCountCol);

        section.getChildren().addAll(obsTitle, obsTable, sessTitle, sessTable);
        return section;
    }

    public VBox getView() {
        return root;
    }

    public void refresh() {
        viewModel.load();
    }
}

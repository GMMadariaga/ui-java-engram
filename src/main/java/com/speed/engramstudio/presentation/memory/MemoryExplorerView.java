package com.speed.engramstudio.presentation.memory;

import com.speed.engramstudio.domain.model.Observation;
import com.speed.engramstudio.domain.model.ObservationType;
import com.speed.engramstudio.infrastructure.markdown.MarkdownRenderer;
import com.speed.engramstudio.presentation.components.CollapsibleSection;
import com.speed.engramstudio.presentation.components.DateTimeDisplay;
import com.speed.engramstudio.presentation.components.DetailWindow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;

public class MemoryExplorerView {

    private final MemoryExplorerViewModel viewModel;
    private VBox root;
    private TableView<Observation> table;
    private final ObservableSet<Long> readIds = FXCollections.observableSet();
    private DetailWindow detailWindow = new DetailWindow();

    public MemoryExplorerView(MemoryExplorerViewModel viewModel) {
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

        Label title = new Label("MEMORY EXPLORER");
        title.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Search observations...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(300);
        searchField.textProperty().bindBidirectional(viewModel.searchQueryProperty());

        Button searchBtn = new Button("SEARCH");
        searchBtn.getStyleClass().add("button-accent");
        searchBtn.setOnAction(e -> viewModel.search());

        Button clearBtn = new Button("CLEAR");
        clearBtn.setOnAction(e -> viewModel.clearSearch());

        Button refreshBtn = new Button("REFRESH");
        refreshBtn.setOnAction(e -> viewModel.load());

        Label status = new Label();
        status.getStyleClass().addAll("status-text", "status-connected");
        viewModel.statusMessageProperty().addListener((obs, old, val) -> status.setText("[" + val + "]"));

        header.getChildren().addAll(title, spacer, searchField, searchBtn, clearBtn, refreshBtn, status);
        return header;
    }

    private VBox createMasterPanel() {
        VBox panel = new VBox(0);
        panel.getStyleClass().add("master-panel");

        HBox filterBar = new HBox(4);
        filterBar.setPadding(new Insets(4));
        filterBar.getStyleClass().add("filter-bar");

        ToggleGroup filterGroup = new ToggleGroup();
        RadioButton allBtn = createFilterButton("ALL", filterGroup, true);
        RadioButton archBtn = createFilterButton("ARCH", filterGroup, false);
        RadioButton bugBtn = createFilterButton("BUG", filterGroup, false);
        RadioButton decBtn = createFilterButton("DEC", filterGroup, false);
        RadioButton discBtn = createFilterButton("DISC", filterGroup, false);
        RadioButton learnBtn = createFilterButton("LEARN", filterGroup, false);
        RadioButton prefBtn = createFilterButton("PREF", filterGroup, false);

        filterBar.getChildren().addAll(allBtn, archBtn, bugBtn, decBtn, discBtn, learnBtn, prefBtn);
        CollapsibleSection filters = new CollapsibleSection("FILTERS", filterBar);

        Label statsLabel = new Label();
        statsLabel.getStyleClass().add("sidebar-label");
        statsLabel.setPadding(new Insets(4, 12, 4, 12));
        CollapsibleSection statsSection = new CollapsibleSection("STATISTICS", statsLabel);
        viewModel.observations().addListener((javafx.collections.ListChangeListener<Observation>) c -> updateStats(statsLabel));
        updateStats(statsLabel);

        table = new TableView<>(viewModel.observations());
        table.setPlaceholder(new Label("No observations loaded"));
        table.getStyleClass().add("observation-table");

        TableColumn<Observation, Long> idCol = new TableColumn<>("#");
        idCol.setCellValueFactory(p -> new javafx.beans.property.SimpleLongProperty(p.getValue().id()).asObject());
        idCol.setPrefWidth(50);
        idCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    boolean isRead = readIds.contains(item);
                    setStyle(isRead ? "-fx-background-color: #141414;" : "-fx-background-color: #1E1E1E;");
                    setText(String.valueOf(item));
                }
            }
        });

        TableColumn<Observation, ObservationType> typeCol = new TableColumn<>("TYPE");
        typeCol.setCellValueFactory(p -> new javafx.beans.property.SimpleObjectProperty<>(p.getValue().type()));
        typeCol.setPrefWidth(80);
        typeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ObservationType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label tag = new Label(item.name());
                    tag.getStyleClass().addAll("tag", "tag-type");
                    setGraphic(tag);
                    setText(null);
                }
            }
        });

        TableColumn<Observation, String> titleCol = new TableColumn<>("TITLE");
        titleCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().title()));
        titleCol.setPrefWidth(350);

        TableColumn<Observation, String> projectCol = new TableColumn<>("PROJECT");
        projectCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().project()));
        projectCol.setPrefWidth(100);

        TableColumn<Observation, String> dateCol = new TableColumn<>("CREATED");
        dateCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(
            DateTimeDisplay.format(p.getValue().createdAt())));
        dateCol.setPrefWidth(140);

        TableColumn<Observation, String> readCol = new TableColumn<>("STATUS");
        readCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(""));
        readCol.setPrefWidth(60);
        readCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    boolean isRead = readIds.contains(getTableRow().getItem().id());
                    Label badge = new Label(isRead ? "READ" : "NEW");
                    badge.getStyleClass().addAll("tag", isRead ? "tag-read" : "tag-new");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<Observation, Void> openCol = new TableColumn<>("ACTION");
        openCol.setPrefWidth(70);
        openCol.setCellFactory(col -> new TableCell<>() {
            private final Button openBtn = new Button("OPEN");
            {
                openBtn.getStyleClass().addAll("button", "button-accent");
                openBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 8 2 8;");
                openBtn.setOnAction(e -> {
                    Observation obs = getTableView().getItems().get(getIndex());
                    if (obs != null) openDetailWindow(obs);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : openBtn);
            }
        });

        table.getColumns().addAll(idCol, openCol, typeCol, titleCol, projectCol, dateCol, readCol);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) {
                readIds.add(val.id());
                table.refresh();
            }
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        panel.getChildren().addAll(filters, statsSection, table);
        return panel;
    }

    private VBox createDetailPanel() {
        VBox panel = new VBox(0);
        panel.getStyleClass().add("detail-panel");

        HBox detailHeader = new HBox(12);
        detailHeader.setPadding(new Insets(12));
        detailHeader.getStyleClass().add("detail-header");
        detailHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label detailTitle = new Label("OBSERVATION DETAIL");
        detailTitle.getStyleClass().add("stat-label");

        Label idTag = new Label();
        idTag.getStyleClass().addAll("tag", "tag-type");
        Label typeTag = new Label();
        typeTag.getStyleClass().addAll("tag", "tag-type");
        Label projectTag = new Label();
        projectTag.getStyleClass().addAll("tag", "tag-project");
        Label dateTag = new Label();
        dateTag.getStyleClass().add("tag");

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) {
                idTag.setText("#" + val.id());
                typeTag.setText(val.type() != null ? val.type().name() : "");
                projectTag.setText(val.project() != null ? val.project() : "N/A");
                dateTag.setText(DateTimeDisplay.formatOr(val.createdAt(), "N/A"));
            } else {
                idTag.setText("");
                typeTag.setText("");
                projectTag.setText("");
                dateTag.setText("");
            }
        });

        detailHeader.getChildren().addAll(detailTitle, idTag, typeTag, projectTag, dateTag);

        Label titleLabel = new Label();
        titleLabel.getStyleClass().add("header-title");
        titleLabel.setPadding(new Insets(0, 12, 4, 12));
        titleLabel.setWrapText(true);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            titleLabel.setText(val != null ? val.title() : "");
        });

        TextFlow contentFlow = new TextFlow();
        contentFlow.setPadding(new Insets(12));
        contentFlow.setStyle("-fx-background-color: #1E1E1E;");
        contentFlow.setLineSpacing(3);

        Label placeholder = new Label("Select an observation to view content...");
        placeholder.getStyleClass().add("sidebar-label");
        contentFlow.getChildren().add(placeholder);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
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
        panel.getChildren().addAll(detailHeader, titleLabel, scrollPane);
        return panel;
    }

    private void updateStats(Label statsLabel) {
        int total = viewModel.observations().size();
        long read = readIds.size();
        long unread = total - read;
        statsLabel.setText("Total: " + total + "  |  Read: " + read + "  |  Unread: " + unread);
    }

    private RadioButton createFilterButton(String text, ToggleGroup group, boolean selected) {
        RadioButton btn = new RadioButton(text);
        btn.setToggleGroup(group);
        btn.getStyleClass().add("filter-button");
        btn.setSelected(selected);
        btn.setPadding(new Insets(2, 8, 2, 8));
        return btn;
    }

    private void openDetailWindow(Observation obs) {
        readIds.add(obs.id());
        table.refresh();

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

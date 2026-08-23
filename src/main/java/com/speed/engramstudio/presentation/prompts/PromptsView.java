package com.speed.engramstudio.presentation.prompts;

import com.speed.engramstudio.domain.model.Prompt;
import com.speed.engramstudio.infrastructure.markdown.MarkdownRenderer;
import com.speed.engramstudio.presentation.components.DetailWindow;
import com.speed.engramstudio.presentation.components.DateTimeDisplay;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;

public class PromptsView {

    private final PromptsViewModel viewModel;
    private VBox root;
    private DetailWindow detailWindow = new DetailWindow();

    public PromptsView(PromptsViewModel viewModel) {
        this.viewModel = viewModel;
        buildUI();
    }

    private void buildUI() {
        root = new VBox(0);
        root.getStyleClass().add("content-area");

        HBox header = createHeader();
        root.getChildren().add(header);

        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.4);

        VBox listPanel = createListPanel();
        VBox detailPanel = createDetailPanel();

        splitPane.getItems().addAll(listPanel, detailPanel);
        SplitPane.setResizableWithParent(listPanel, true);
        SplitPane.setResizableWithParent(detailPanel, true);

        VBox.setVgrow(splitPane, Priority.ALWAYS);
        root.getChildren().add(splitPane);
    }

    private HBox createHeader() {
        HBox header = new HBox(16);
        header.getStyleClass().add("header");
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label title = new Label("PROMPTS");
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

    private VBox createListPanel() {
        VBox panel = new VBox(0);
        panel.getStyleClass().add("master-panel");

        TableView<Prompt> table = new TableView<>(viewModel.prompts());
        table.setPlaceholder(new Label("No prompts loaded"));
        table.getStyleClass().add("observation-table");

        TableColumn<Prompt, Long> idCol = new TableColumn<>("#");
        idCol.setCellValueFactory(p -> new javafx.beans.property.SimpleLongProperty(p.getValue().id()).asObject());
        idCol.setPrefWidth(50);

        TableColumn<Prompt, String> contentCol = new TableColumn<>("CONTENT");
        contentCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().content()));
        contentCol.setPrefWidth(350);

        TableColumn<Prompt, String> projectCol = new TableColumn<>("PROJECT");
        projectCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().project()));
        projectCol.setPrefWidth(150);

        TableColumn<Prompt, String> dateCol = new TableColumn<>("CREATED");
        dateCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(
            DateTimeDisplay.format(p.getValue().createdAt())));
        dateCol.setPrefWidth(140);

        TableColumn<Prompt, Void> openCol = new TableColumn<>("ACTION");
        openCol.setPrefWidth(70);
        openCol.setCellFactory(col -> new TableCell<>() {
            private final Button openBtn = new Button("OPEN");
            {
                openBtn.getStyleClass().addAll("button", "button-accent");
                openBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 8 2 8;");
                openBtn.setOnAction(e -> {
                    Prompt p = getTableView().getItems().get(getIndex());
                    if (p != null) openPromptDetail(p);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : openBtn);
            }
        });

        table.getColumns().addAll(idCol, openCol, contentCol, projectCol, dateCol);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) viewModel.selectedPromptProperty().set(val);
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

        Label detailTitle = new Label("PROMPT DETAIL");
        detailTitle.getStyleClass().add("stat-label");

        Label promptIdLabel = new Label();
        promptIdLabel.getStyleClass().add("header-title");
        Label projectMeta = new Label();
        projectMeta.getStyleClass().addAll("tag", "tag-project");
        Label dateMeta = new Label();
        dateMeta.getStyleClass().add("tag");

        viewModel.selectedPromptProperty().addListener((obs, old, val) -> {
            if (val != null) {
                promptIdLabel.setText("#" + val.id());
                projectMeta.setText(val.project() != null ? val.project() : "N/A");
                dateMeta.setText(DateTimeDisplay.formatOr(val.createdAt(), "N/A"));
            } else {
                promptIdLabel.setText("");
                projectMeta.setText("");
                dateMeta.setText("");
            }
        });

        detailHeader.getChildren().addAll(detailTitle, promptIdLabel, projectMeta, dateMeta);

        Label contentLabel = new Label("CONTENT");
        contentLabel.getStyleClass().add("stat-label");
        contentLabel.setPadding(new Insets(4, 12, 4, 12));

        TextFlow contentFlow = new TextFlow();
        contentFlow.setPadding(new Insets(12));
        contentFlow.setStyle("-fx-background-color: #1E1E1E;");
        contentFlow.setLineSpacing(3);

        Label placeholder = new Label("Select a prompt to view content...");
        placeholder.getStyleClass().add("sidebar-label");
        contentFlow.getChildren().add(placeholder);

        viewModel.selectedPromptProperty().addListener((obs, old, val) -> {
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
        panel.getChildren().addAll(detailHeader, contentLabel, scrollPane);
        return panel;
    }

    private void openPromptDetail(Prompt prompt) {
        TextFlow content = MarkdownRenderer.render(
            "## Prompt #" + prompt.id() + "\n\n" +
            "**Project:** " + prompt.project() + "\n\n" +
            "**Created:** " + prompt.createdAt() + "\n\n" +
            "### Content\n\n" + prompt.content()
        );
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #1E1E1E;");
        content.setLineSpacing(3);
        detailWindow.show("Prompt #" + prompt.id(), content);
    }

    public VBox getView() {
        return root;
    }

    public void refresh() {
        viewModel.load();
    }
}

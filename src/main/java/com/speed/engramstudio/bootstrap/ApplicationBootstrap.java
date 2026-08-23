package com.speed.engramstudio.bootstrap;

import com.speed.engramstudio.application.conflicts.GetConflicts;
import com.speed.engramstudio.application.connection.CheckConnection;
import com.speed.engramstudio.application.dashboard.GetDashboardStats;
import com.speed.engramstudio.application.diagnostics.RunDiagnostics;
import com.speed.engramstudio.application.memory.GetRecentMemories;
import com.speed.engramstudio.application.memory.SearchMemories;
import com.speed.engramstudio.application.processmanager.DetectEngram;
import com.speed.engramstudio.application.processmanager.RestartEngram;
import com.speed.engramstudio.application.processmanager.StartEngram;
import com.speed.engramstudio.application.processmanager.StopEngram;
import com.speed.engramstudio.application.projects.GetProjectObservations;
import com.speed.engramstudio.application.projects.GetProjects;
import com.speed.engramstudio.application.prompts.GetRecentPrompts;
import com.speed.engramstudio.application.sessions.GetRecentSessions;
import com.speed.engramstudio.application.sessions.GetSessionDetail;
import com.speed.engramstudio.domain.enums.ConnectionState;
import com.speed.engramstudio.infrastructure.config.AppConfiguration;
import com.speed.engramstudio.infrastructure.engram.EngramConnectionAdapter;
import com.speed.engramstudio.infrastructure.engram.ConflictRepositoryImpl;
import com.speed.engramstudio.infrastructure.engram.DashboardRepositoryImpl;
import com.speed.engramstudio.infrastructure.engram.DiagnosticsRepositoryImpl;
import com.speed.engramstudio.infrastructure.engram.ObservationRepositoryImpl;
import com.speed.engramstudio.infrastructure.engram.ProjectRepositoryImpl;
import com.speed.engramstudio.infrastructure.engram.PromptRepositoryImpl;
import com.speed.engramstudio.infrastructure.engram.SessionRepositoryImpl;
import com.speed.engramstudio.infrastructure.engram.api.ConflictsApi;
import com.speed.engramstudio.infrastructure.engram.api.DiagnosticsApi;
import com.speed.engramstudio.infrastructure.engram.api.ObservationsApi;
import com.speed.engramstudio.infrastructure.engram.api.PromptsApi;
import com.speed.engramstudio.infrastructure.engram.api.SearchApi;
import com.speed.engramstudio.infrastructure.engram.api.SessionsApi;
import com.speed.engramstudio.infrastructure.engram.api.StatsApi;
import com.speed.engramstudio.infrastructure.process.EngramProcessManager;
import com.speed.engramstudio.infrastructure.process.EngramExecutableResolver;
import com.speed.engramstudio.infrastructure.process.AgentExecutableResolver;
import com.speed.engramstudio.presentation.conflicts.ConflictsView;
import com.speed.engramstudio.presentation.conflicts.ConflictsViewModel;
import com.speed.engramstudio.presentation.connection.ConnectionViewModel;
import com.speed.engramstudio.presentation.dashboard.DashboardView;
import com.speed.engramstudio.presentation.dashboard.DashboardViewModel;
import com.speed.engramstudio.presentation.diagnostics.DiagnosticsView;
import com.speed.engramstudio.presentation.diagnostics.DiagnosticsViewModel;
import com.speed.engramstudio.presentation.agents.AgentCliView;
import com.speed.engramstudio.presentation.memory.MemoryExplorerView;
import com.speed.engramstudio.presentation.memory.MemoryExplorerViewModel;
import com.speed.engramstudio.presentation.processmanager.ProcessManagerView;
import com.speed.engramstudio.presentation.processmanager.ProcessManagerViewModel;
import com.speed.engramstudio.presentation.projects.ProjectsView;
import com.speed.engramstudio.presentation.projects.ProjectsViewModel;
import com.speed.engramstudio.presentation.prompts.PromptsView;
import com.speed.engramstudio.presentation.prompts.PromptsViewModel;
import com.speed.engramstudio.presentation.sessions.SessionTimelineView;
import com.speed.engramstudio.presentation.sessions.SessionTimelineViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Polygon;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ApplicationBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationBootstrap.class);

    private double dragOffsetX, dragOffsetY;
    private AppConfiguration appConfig;

    public void start(Stage primaryStage) {
        logger.info("Starting Engram Studio...");
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("Engram Studio");
        primaryStage.getIcons().add(loadIcon("/icon.png"));

        AppConfiguration config = new AppConfiguration();
        this.appConfig = config;
        EngramConnectionAdapter connectionAdapter = new EngramConnectionAdapter(config);
        EngramProcessManager processManager = new EngramProcessManager(config.getEngramUrl());

        StatsApi statsApi = new StatsApi(connectionAdapter.getHttpClient());
        ObservationsApi observationsApi = new ObservationsApi(connectionAdapter.getHttpClient());
        SessionsApi sessionsApi = new SessionsApi(connectionAdapter.getHttpClient());
        SearchApi searchApi = new SearchApi(connectionAdapter.getHttpClient());
        PromptsApi promptsApi = new PromptsApi(connectionAdapter.getHttpClient());
        ConflictsApi conflictsApi = new ConflictsApi(connectionAdapter.getHttpClient());
        DiagnosticsApi diagnosticsApi = new DiagnosticsApi(connectionAdapter.getHttpClient());

        DashboardRepositoryImpl dashboardRepo = new DashboardRepositoryImpl(statsApi, observationsApi, sessionsApi);
        ObservationRepositoryImpl observationRepo = new ObservationRepositoryImpl(observationsApi, searchApi);
        SessionRepositoryImpl sessionRepo = new SessionRepositoryImpl(sessionsApi);
        ProjectRepositoryImpl projectRepo = new ProjectRepositoryImpl(statsApi, observationsApi, sessionsApi);
        PromptRepositoryImpl promptRepo = new PromptRepositoryImpl(promptsApi);
        ConflictRepositoryImpl conflictRepo = new ConflictRepositoryImpl(conflictsApi);
        DiagnosticsRepositoryImpl diagnosticsRepo = new DiagnosticsRepositoryImpl(diagnosticsApi);

        CheckConnection checkConnection = new CheckConnection(connectionAdapter);
        GetDashboardStats getDashboardStats = new GetDashboardStats(dashboardRepo);
        GetRecentMemories getRecentMemories = new GetRecentMemories(observationRepo);
        SearchMemories searchMemories = new SearchMemories(observationRepo);
        GetRecentSessions getRecentSessions = new GetRecentSessions(sessionRepo);
        GetSessionDetail getSessionDetail = new GetSessionDetail(sessionRepo);
        GetProjects getProjects = new GetProjects(projectRepo);
        GetProjectObservations getProjectObservations = new GetProjectObservations(projectRepo);
        GetRecentPrompts getRecentPrompts = new GetRecentPrompts(promptRepo);
        GetConflicts getConflicts = new GetConflicts(conflictRepo);
        RunDiagnostics runDiagnostics = new RunDiagnostics(diagnosticsRepo);
        DetectEngram detectEngram = new DetectEngram(processManager);
        StartEngram startEngram = new StartEngram(processManager);
        StopEngram stopEngram = new StopEngram(processManager);
        RestartEngram restartEngram = new RestartEngram(processManager);

        ConnectionViewModel connectionViewModel = new ConnectionViewModel(checkConnection);
        DashboardViewModel dashboardViewModel = new DashboardViewModel(getDashboardStats);
        MemoryExplorerViewModel memoryExplorerViewModel = new MemoryExplorerViewModel(getRecentMemories, searchMemories);
        SessionTimelineViewModel sessionTimelineViewModel = new SessionTimelineViewModel(getRecentSessions, getSessionDetail, getRecentMemories);
        ProjectsViewModel projectsViewModel = new ProjectsViewModel(getProjects, getProjectObservations);
        PromptsViewModel promptsViewModel = new PromptsViewModel(getRecentPrompts);
        ConflictsViewModel conflictsViewModel = new ConflictsViewModel(getConflicts);
        DiagnosticsViewModel diagnosticsViewModel = new DiagnosticsViewModel(runDiagnostics);
        ProcessManagerViewModel processManagerViewModel = new ProcessManagerViewModel(detectEngram, startEngram, stopEngram, restartEngram);

        DashboardView dashboardView = new DashboardView(dashboardViewModel);
        MemoryExplorerView memoryExplorerView = new MemoryExplorerView(memoryExplorerViewModel);
        SessionTimelineView sessionTimelineView = new SessionTimelineView(sessionTimelineViewModel);
        ProjectsView projectsView = new ProjectsView(projectsViewModel);
        PromptsView promptsView = new PromptsView(promptsViewModel);
        ConflictsView conflictsView = new ConflictsView(conflictsViewModel);
        DiagnosticsView diagnosticsView = new DiagnosticsView(diagnosticsViewModel);
        ProcessManagerView processManagerView = new ProcessManagerView(processManagerViewModel);
        AgentCliView agentCliView = new AgentCliView();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("content-area");

        // Custom title bar
        HBox titleBar = createTitleBar(primaryStage);
        root.setTop(titleBar);

        // Sidebar
        java.util.function.Consumer<Node> switchView = new java.util.function.Consumer<>() {
            @Override
            public void accept(Node view) {
                root.setCenter(view);
            }
        };

        VBox sidebar = createSidebar(switchView, connectionViewModel, dashboardView, memoryExplorerView,
                sessionTimelineView, projectsView, promptsView, conflictsView, diagnosticsView, processManagerView,
                agentCliView);
        root.setLeft(sidebar);

        root.setCenter(dashboardView.getView());

        HBox statusBar = createStatusBar(connectionViewModel);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 1200, 750);
        scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        scene.setFill(javafx.scene.paint.Color.web("#0A0A0A"));
        primaryStage.setScene(scene);
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());
        primaryStage.setOnCloseRequest(event -> agentCliView.close());
        primaryStage.show();

        initializeEngram(processManager, connectionViewModel, dashboardViewModel);

        logger.info("Engram Studio started.");
    }

    private void initializeEngram(EngramProcessManager processManager,
                                  ConnectionViewModel connectionViewModel,
                                  DashboardViewModel dashboardViewModel) {
        if (!appConfig.isAutoConnect()) {
            logger.info("Auto-connect disabled; Engram will not be queried on startup.");
            return;
        }

        if (!appConfig.isAutoStart() || !isLocalEngramUrl(appConfig.getEngramUrl())) {
            connectionViewModel.checkConnection();
            dashboardViewModel.load();
            return;
        }

        logger.info("Auto-starting Engram at {}", appConfig.getEngramUrl());
        processManager.start().whenComplete((result, throwable) ->
            javafx.application.Platform.runLater(() -> {
                if (throwable != null) {
                    logger.error("Auto-start failed", throwable);
                } else {
                    logger.info("Auto-start result: {}", result.message());
                }
                connectionViewModel.checkConnection();
                dashboardViewModel.load();
            }));
    }

    private boolean isLocalEngramUrl(String url) {
        try {
            String host = java.net.URI.create(url).getHost();
            return "127.0.0.1".equals(host)
                || "localhost".equalsIgnoreCase(host)
                || "[::1]".equalsIgnoreCase(host)
                || "::1".equals(host);
        } catch (Exception e) {
            return false;
        }
    }

    private HBox createTitleBar(Stage stage) {
        HBox titleBar = new HBox();
        titleBar.getStyleClass().add("title-bar");
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(0, 8, 0, 0));
        titleBar.setPrefHeight(32);
        titleBar.setMinHeight(32);
        titleBar.setMaxHeight(32);

        Label icon = new Label("\u25C8");
        icon.getStyleClass().add("title-bar-icon");

        Label title = new Label("Engram Studio");
        title.getStyleClass().add("title-bar-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label minBtn = createWindowButton("\u2500", "minimize");
        Label maxBtn = createWindowButton("\u25A1", "maximize");
        Label closeBtn = createWindowButton("\u2715", "close");

        minBtn.setOnMouseClicked(e -> stage.setIconified(true));
        maxBtn.setOnMouseClicked(e -> {
            if (stage.isMaximized()) {
                stage.setMaximized(false);
                maxBtn.setText("\u25A1");
            } else {
                stage.setMaximized(true);
                maxBtn.setText("\u25A3");
            }
        });
        closeBtn.setOnMouseClicked(e -> stage.close());

        titleBar.getChildren().addAll(icon, title, spacer, minBtn, maxBtn, closeBtn);

        // Drag to move
        titleBar.setOnMousePressed(e -> {
            if (e.getTarget() == titleBar || e.getTarget() == title || e.getTarget() == icon) {
                dragOffsetX = e.getScreenX() - stage.getX();
                dragOffsetY = e.getScreenY() - stage.getY();
            }
        });
        titleBar.setOnMouseDragged(e -> {
            if (e.getTarget() == titleBar || e.getTarget() == title || e.getTarget() == icon) {
                stage.setX(e.getScreenX() - dragOffsetX);
                stage.setY(e.getScreenY() - dragOffsetY);
            }
        });
        titleBar.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && (e.getTarget() == titleBar || e.getTarget() == title || e.getTarget() == icon)) {
                if (stage.isMaximized()) {
                    stage.setMaximized(false);
                    maxBtn.setText("\u25A1");
                } else {
                    stage.setMaximized(true);
                    maxBtn.setText("\u25A3");
                }
            }
        });

        return titleBar;
    }

    private Label createWindowButton(String text, String type) {
        Label btn = new Label(text);
        btn.getStyleClass().add("title-bar-btn");
        btn.setPrefSize(32, 32);
        btn.setAlignment(Pos.CENTER);
        btn.setOnMouseEntered(e -> btn.getStyleClass().add("title-bar-btn-" + type));
        btn.setOnMouseExited(e -> btn.getStyleClass().remove("title-bar-btn-" + type));
        return btn;
    }

    private VBox createSidebar(java.util.function.Consumer<Node> switchView,
                               ConnectionViewModel connectionViewModel,
                               DashboardView dashboardView, MemoryExplorerView memoryExplorerView,
                               SessionTimelineView sessionTimelineView, ProjectsView projectsView,
                               PromptsView promptsView, ConflictsView conflictsView,
                               DiagnosticsView diagnosticsView, ProcessManagerView processManagerView,
                               AgentCliView agentCliView) {

        VBox sidebar = new VBox(0);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setMinWidth(52);
        sidebar.setPrefWidth(184);
        sidebar.setMaxWidth(184);
        boolean[] sidebarCollapsed = {false};

        List<Label> allItems = new ArrayList<>();

        // Logo
        Label logo = new Label("\u25C8 ENGRAM");
        logo.getStyleClass().add("logo-text");
        logo.setMaxWidth(Double.MAX_VALUE);

        Label version = new Label("STUDIO v1.0");
        version.getStyleClass().add("sidebar-label");
        version.setPadding(new Insets(0, 16, 16, 16));

        Label sep1 = new Label("NAVIGATION");
        sep1.getStyleClass().add("sidebar-label");
        sep1.setPadding(new Insets(12, 16, 4, 16));

        Label dashItem = createSidebarItem("\u25A3 Dashboard");
        dashItem.getStyleClass().add("active");
        Label memItem = createSidebarItem("\u25C9 Memories");
        Label sessItem = createSidebarItem("\u25B7 Sessions");
        Label projItem = createSidebarItem("\u25CB Projects");
        Label timelineItem = createSidebarItem("\u25F7 Timeline");
        Label promptItem = createSidebarItem("\u25BA Prompts");
        Label conflictsItem = createSidebarItem("\u26A0 Conflicts");

        allItems.addAll(List.of(dashItem, memItem, sessItem, projItem, timelineItem, promptItem, conflictsItem));

        Label sep2 = new Label("SYSTEM");
        sep2.getStyleClass().add("sidebar-label");
        sep2.setPadding(new Insets(12, 16, 4, 16));

        Label diagItem = createSidebarItem("\u2699 Diagnostics");
        Label processItem = createSidebarItem("\u25B6 Process");
        Label agentCliItem = createSidebarItem("\u25A4 Agents");
        Label settingsItem = createSidebarItem("\u2692 Settings");

        allItems.addAll(List.of(diagItem, processItem, agentCliItem, settingsItem));

        Label connStatus = new Label();
        connStatus.getStyleClass().add("sidebar-label");
        connStatus.setPadding(new Insets(16, 16, 8, 16));
        connectionViewModel.stateProperty().addListener((obs, old, val) -> {
            String prefix = val == ConnectionState.CONNECTED ? "●" : "○";
            connStatus.setText(sidebarCollapsed[0] ? prefix : prefix + " Engram");
            connStatus.getStyleClass().removeAll("status-connected", "status-disconnected");
            connStatus.getStyleClass().add(val == ConnectionState.CONNECTED ? "status-connected" : "status-disconnected");
        });
        String initialPrefix = connectionViewModel.stateProperty().get() == ConnectionState.CONNECTED ? "●" : "○";
        connStatus.setText(initialPrefix + " Engram");

        Polygon collapseIcon = new Polygon(15.0, 4.0, 7.0, 12.0, 15.0, 20.0);
        collapseIcon.getStyleClass().add("sidebar-toggle-icon");
        javafx.scene.control.Button collapseButton = new javafx.scene.control.Button();
        collapseButton.getStyleClass().add("sidebar-toggle");
        collapseButton.setFocusTraversable(false);
        collapseButton.setMinSize(36, 32);
        collapseButton.setPrefSize(36, 32);
        collapseButton.setGraphic(collapseIcon);
        collapseButton.setAccessibleText("Contraer o desplegar menú lateral");
        VBox logoHeader = new VBox(4);
        logoHeader.getStyleClass().add("sidebar-header");
        logoHeader.getChildren().addAll(logo, collapseButton);

        java.util.function.Consumer<Boolean> setCollapsed = collapsed -> {
            sidebarCollapsed[0] = collapsed;
            sidebar.setPrefWidth(collapsed ? 52 : 184);
            sidebar.setMaxWidth(collapsed ? 52 : 184);
            sidebar.getStyleClass().removeAll("sidebar-collapsed");
            if (collapsed) sidebar.getStyleClass().add("sidebar-collapsed");
            logo.setText(collapsed ? "◈" : "◈ ENGRAM");
            logo.setAlignment(collapsed ? Pos.CENTER : Pos.CENTER_LEFT);
            version.setVisible(!collapsed);
            version.setManaged(!collapsed);
            sep1.setVisible(!collapsed);
            sep1.setManaged(!collapsed);
            sep2.setVisible(!collapsed);
            sep2.setManaged(!collapsed);
            for (Label item : allItems) {
                item.setText(collapsed
                    ? (String) item.getProperties().get("collapsedText")
                    : (String) item.getProperties().get("expandedText"));
                item.setAlignment(collapsed ? Pos.CENTER : Pos.CENTER_LEFT);
            }
            connStatus.setText(collapsed
                ? (connectionViewModel.stateProperty().get() == ConnectionState.CONNECTED ? "●" : "○")
                : (connectionViewModel.stateProperty().get() == ConnectionState.CONNECTED ? "● Engram" : "○ Engram"));
            if (collapsed) {
                collapseIcon.getPoints().setAll(9.0, 4.0, 17.0, 12.0, 9.0, 20.0);
            } else {
                collapseIcon.getPoints().setAll(15.0, 4.0, 7.0, 12.0, 15.0, 20.0);
            }
        };
        collapseButton.setOnAction(event -> setCollapsed.accept(!sidebarCollapsed[0]));

        // Navigation: select + switch + auto-load
        dashItem.setOnMouseClicked(e -> { select(dashItem, allItems); switchView.accept(dashboardView.getView()); dashboardView.refresh(); });
        memItem.setOnMouseClicked(e -> { select(memItem, allItems); switchView.accept(memoryExplorerView.getView()); memoryExplorerView.refresh(); });
        sessItem.setOnMouseClicked(e -> { select(sessItem, allItems); switchView.accept(sessionTimelineView.getView()); sessionTimelineView.refresh(); });
        projItem.setOnMouseClicked(e -> { select(projItem, allItems); switchView.accept(projectsView.getView()); projectsView.refresh(); });
        timelineItem.setOnMouseClicked(e -> { select(timelineItem, allItems); switchView.accept(sessionTimelineView.getView()); sessionTimelineView.refresh(); });
        promptItem.setOnMouseClicked(e -> { select(promptItem, allItems); switchView.accept(promptsView.getView()); promptsView.refresh(); });
        conflictsItem.setOnMouseClicked(e -> { select(conflictsItem, allItems); switchView.accept(conflictsView.getView()); conflictsView.refresh(); });
        diagItem.setOnMouseClicked(e -> { select(diagItem, allItems); switchView.accept(diagnosticsView.getView()); diagnosticsView.refresh(); });
        processItem.setOnMouseClicked(e -> { select(processItem, allItems); switchView.accept(processManagerView.getView()); processManagerView.refresh(); });
        agentCliItem.setOnMouseClicked(e -> { select(agentCliItem, allItems); switchView.accept(agentCliView.getView()); });
        settingsItem.setOnMouseClicked(e -> { select(settingsItem, allItems); switchView.accept(createSettingsView()); });

        sidebar.getChildren().addAll(
                logoHeader, version,
                sep1,
                dashItem, memItem, sessItem, projItem, timelineItem, promptItem, conflictsItem,
                sep2,
                diagItem, processItem, agentCliItem, settingsItem,
                new Spacer(),
                connStatus
        );

        return sidebar;
    }

    private void select(Label selected, List<Label> allItems) {
        for (Label item : allItems) {
            item.getStyleClass().remove("active");
        }
        selected.getStyleClass().add("active");
    }

    private Node createSettingsView() {
        VBox settings = new VBox(16);
        settings.setPadding(new Insets(20));
        settings.getStyleClass().add("content-area");

        Label title = new Label("SETTINGS");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Connection, runtime and agent configuration");
        subtitle.getStyleClass().add("status-text");

        // --- SERVER CARD ---
        VBox serverCard = createSettingsCard("SERVER", "Configure how Studio connects to Engram");
        javafx.scene.control.TextField urlField = new javafx.scene.control.TextField(appConfig.getEngramUrl());
        urlField.getStyleClass().add("search-field");
        HBox.setHgrow(urlField, Priority.ALWAYS);

        javafx.scene.control.TextField timeoutField = new javafx.scene.control.TextField(String.valueOf(appConfig.getTimeout()));
        timeoutField.getStyleClass().add("search-field");
        timeoutField.setPrefWidth(120);

        javafx.scene.control.CheckBox autoCheck = new javafx.scene.control.CheckBox("Auto-connect on startup");
        autoCheck.setSelected(appConfig.isAutoConnect());

        javafx.scene.control.CheckBox autoStartCheck = new javafx.scene.control.CheckBox("Start Engram if stopped");
        autoStartCheck.setSelected(appConfig.isAutoStart());

        serverCard.getChildren().addAll(
            settingsField("Engram Server URL", urlField),
            new HBox(12, settingsField("Timeout (ms)", timeoutField), autoCheck, autoStartCheck)
        );

        // --- BINARY CARD ---
        VBox binaryCard = createSettingsCard("ENGRAM BINARY", "Detect, install or update the local executable");
        Label pathValue = new Label(findEngramBinary());
        pathValue.getStyleClass().add("settings-value");
        pathValue.setWrapText(true);
        Label versionValue = new Label("Not checked");
        versionValue.getStyleClass().add("settings-value");

        javafx.scene.control.Button checkUpdateBtn = new javafx.scene.control.Button("CHECK UPDATE");
        checkUpdateBtn.setOnAction(e -> {
            checkUpdateBtn.setDisable(true);
            checkUpdateBtn.setText("CHECKING...");
            checkGitHubVersion(versionValue, checkUpdateBtn);
        });

        javafx.scene.control.Button installBtn = new javafx.scene.control.Button("INSTALL / UPDATE");
        installBtn.setOnAction(e -> {
            installBtn.setDisable(true);
            installBtn.setText("INSTALLING...");
            installEngram(installBtn, versionValue);
        });

        binaryCard.getChildren().addAll(
            settingsValueRow("Binary path", pathValue),
            settingsValueRow("Version", versionValue),
            new HBox(8, checkUpdateBtn, installBtn)
        );

        // --- AGENT CARD ---
        VBox agentCard = createSettingsCard("AGENT PROFILES", "Apply Engram setup to the selected integration");
        javafx.scene.control.ComboBox<String> profileList = new javafx.scene.control.ComboBox<>();
        profileList.getStyleClass().add("search-field");
        profileList.setMaxWidth(Double.MAX_VALUE);
        profileList.getItems().addAll("opencode", "codex", "antigravity", "cursor");
        profileList.getSelectionModel().selectFirst();
        HBox.setHgrow(profileList, Priority.ALWAYS);

        Label setupStatus = new Label("Select a profile and run its setup command.");
        setupStatus.getStyleClass().add("status-text");
        setupStatus.setWrapText(true);

        javafx.scene.control.Button setupBtn = new javafx.scene.control.Button("RUN SETUP");
        setupBtn.setOnAction(e -> runAgentSetup(profileList.getValue(), setupBtn, setupStatus));
        agentCard.getChildren().addAll(new HBox(10, profileList, setupBtn), setupStatus);

        // --- AGENT EXECUTABLES CARD ---
        VBox executableCard = createSettingsCard("AGENT EXECUTABLES",
            "Configure the exact CLI path or search the current PATH. Changes apply immediately to Agent CLI.");
        java.util.Map<String, javafx.scene.control.TextField> executableFields = new java.util.LinkedHashMap<>();
        Label executableStatus = new Label("Use FIND to detect PATH installations, or BROWSE to select a .exe/.cmd/.ps1 file.");
        executableStatus.getStyleClass().add("status-text");
        executableStatus.setWrapText(true);

        String[][] agentExecutables = {
            {"claude", "Claude"},
            {"opencode", "OpenCode"},
            {"codex", "Codex"},
            {"agy", "Antigravity"}
        };
        for (String[] agent : agentExecutables) {
            String configured = appConfig.getAgentExecutable(agent[0]);
            javafx.scene.control.TextField executableField = new javafx.scene.control.TextField(configured);
            if (configured.isBlank()) {
                AgentExecutableResolver.findOnPath(agent[0]).ifPresent(executableField::setText);
            }
            executableField.getStyleClass().add("search-field");
            HBox.setHgrow(executableField, Priority.ALWAYS);

            javafx.scene.control.Button findButton = new javafx.scene.control.Button("FIND");
            findButton.setOnAction(e -> findAgentExecutable(agent[0], executableField, executableStatus));
            javafx.scene.control.Button browseButton = new javafx.scene.control.Button("BROWSE");
            browseButton.setOnAction(e -> browseAgentExecutable(agent[0], executableField, executableStatus));

            HBox row = new HBox(8, executableField, findButton, browseButton);
            row.setAlignment(Pos.CENTER_LEFT);
            executableCard.getChildren().add(settingsField(agent[1] + " executable", row));
            executableFields.put(agent[0], executableField);
        }
        executableCard.getChildren().add(executableStatus);

        // Two columns keep the page compact while the full-width profile card gets priority.
        GridPane cards = new GridPane();
        cards.setHgap(16);
        cards.setVgap(16);
        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(50);
        left.setHgrow(Priority.ALWAYS);
        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(50);
        right.setHgrow(Priority.ALWAYS);
        cards.getColumnConstraints().addAll(left, right);
        cards.add(serverCard, 0, 0);
        cards.add(binaryCard, 1, 0);
        cards.add(agentCard, 0, 1, 2, 1);
        cards.add(executableCard, 0, 2, 2, 1);

        // --- FOOTER ---
        javafx.scene.control.Button saveBtn = new javafx.scene.control.Button("SAVE CHANGES");
        saveBtn.getStyleClass().add("button-accent");
        saveBtn.setOnAction(e -> {
            appConfig.setEngramUrl(urlField.getText().trim());
            try { appConfig.setTimeout(Integer.parseInt(timeoutField.getText().trim())); } catch (NumberFormatException ignored) {}
            appConfig.setAutoConnect(autoCheck.isSelected());
            appConfig.setAutoStart(autoStartCheck.isSelected());
            executableFields.forEach((agent, field) -> appConfig.setAgentExecutable(agent, field.getText()));
            appConfig.save();
            saveBtn.setText("SAVED");
            javafx.application.Platform.runLater(() -> {
                try { Thread.sleep(1200); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                javafx.application.Platform.runLater(() -> saveBtn.setText("SAVE CHANGES"));
            });
        });

        Label configPath = new Label("Config file: " + System.getProperty("user.home") + "\\.engram-studio\\engram-studio.properties");
        configPath.getStyleClass().add("status-text");
        HBox footer = new HBox(12, saveBtn, configPath);
        footer.setAlignment(Pos.CENTER_LEFT);

        settings.getChildren().addAll(title, subtitle, cards, footer);
        VBox.setVgrow(cards, Priority.ALWAYS);

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(settings);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("settings-scroll");
        return scroll;
    }

    private VBox createSettingsCard(String titleText, String description) {
        VBox card = new VBox(10);
        card.getStyleClass().add("stat-card");
        card.setMaxWidth(Double.MAX_VALUE);
        Label title = new Label(titleText);
        title.getStyleClass().add("stat-label");
        Label hint = new Label(description);
        hint.getStyleClass().add("status-text");
        hint.setWrapText(true);
        card.getChildren().addAll(title, hint, new Separator());
        return card;
    }

    private VBox settingsField(String labelText, javafx.scene.Node field) {
        VBox wrapper = new VBox(4);
        Label label = new Label(labelText);
        label.getStyleClass().add("sidebar-label");
        wrapper.getChildren().addAll(label, field);
        VBox.setVgrow(field, Priority.NEVER);
        return wrapper;
    }

    private HBox settingsValueRow(String labelText, Label value) {
        Label label = new Label(labelText);
        label.getStyleClass().add("sidebar-label");
        HBox row = new HBox(8, label, value);
        row.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(value, Priority.ALWAYS);
        return row;
    }

    private void runAgentSetup(String agent, javafx.scene.control.Button button, Label status) {
        if (agent == null || agent.isBlank()) return;
        String executable = EngramExecutableResolver.resolve().orElse(null);
        if (executable == null) {
            status.setText("Engram executable not found. Install or update it first.");
            status.setStyle("-fx-text-fill: #D06B6B;");
            return;
        }

        button.setDisable(true);
        button.setText("RUNNING...");
        status.setText("Running " + executable + " setup " + agent + "...");
        status.setStyle("");
        Thread.startVirtualThread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(executable, "setup", agent);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                String output;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    output = reader.lines().reduce("", (all, line) -> all.isEmpty() ? line : all + "\\n" + line);
                }
                int exitCode = process.waitFor();
                javafx.application.Platform.runLater(() -> {
                    button.setDisable(false);
                    button.setText("RUN SETUP");
                    status.setText(exitCode == 0 ? "Setup completed for " + agent : "Setup failed (exit " + exitCode + "): " + output);
                    status.setStyle(exitCode == 0 ? "-fx-text-fill: #5FBF7F;" : "-fx-text-fill: #D06B6B;");
                });
            } catch (Exception ex) {
                logger.error("Failed to run Engram setup for {}", agent, ex);
                javafx.application.Platform.runLater(() -> {
                    button.setDisable(false);
                    button.setText("RUN SETUP");
                    status.setText("Setup error: " + ex.getMessage());
                    status.setStyle("-fx-text-fill: #D06B6B;");
                });
            }
        });
    }

    private void findAgentExecutable(String agent,
                                     javafx.scene.control.TextField field,
                                     Label status) {
        AgentExecutableResolver.findOnPath(agent).ifPresentOrElse(path -> {
            field.setText(path);
            status.setText("Found " + agent + " at " + path);
            status.setStyle("-fx-text-fill: #5FBF7F;");
        }, () -> {
            status.setText(agent + " was not found on PATH. Use BROWSE or paste its full path.");
            status.setStyle("-fx-text-fill: #D06B6B;");
        });
    }

    private void browseAgentExecutable(String agent,
                                       javafx.scene.control.TextField field,
                                       Label status) {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Select " + agent + " executable");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter(
            "CLI files", "*.exe", "*.cmd", "*.bat", "*.ps1", "*.*"));
        java.io.File selected = chooser.showOpenDialog(field.getScene().getWindow());
        if (selected != null) {
            field.setText(selected.getAbsolutePath());
            status.setText("Selected " + agent + ": " + selected.getAbsolutePath() + ". Press SAVE CHANGES.");
            status.setStyle("-fx-text-fill: #62A7FF;");
        }
    }

    private String findEngramBinary() {
        return EngramExecutableResolver.resolve().orElse("Not found in PATH or Go bin");
    }

    private void checkGitHubVersion(Label versionValue, javafx.scene.control.Button btn) {
        java.net.http.HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(5)).build()
            .sendAsync(
                java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.github.com/repos/Gentleman-Programming/engram/releases/latest"))
                    .header("Accept", "application/vnd.github.v3+json")
                    .GET().build(),
                java.net.http.HttpResponse.BodyHandlers.ofString()
            )
            .thenAccept(resp -> {
                String body = resp.body();
                String tag = extractJsonString(body, "tag_name");
                String name = extractJsonString(body, "name");
                javafx.application.Platform.runLater(() -> {
                    if (tag != null) {
                        versionValue.setText(tag + " (" + (name != null ? name : "latest") + ")");
                        versionValue.setStyle("-fx-text-fill: #62A7FF;");
                    } else {
                        versionValue.setText("Could not fetch");
                        versionValue.setStyle("-fx-text-fill: #D06B6B;");
                    }
                    btn.setDisable(false);
                    btn.setText("CHECK UPDATE");
                });
            })
            .exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> {
                    versionValue.setText("Network error");
                    versionValue.setStyle("-fx-text-fill: #D06B6B;");
                    btn.setDisable(false);
                    btn.setText("CHECK UPDATE");
                });
                return null;
            });
    }

    private void installEngram(javafx.scene.control.Button btn, Label versionValue) {
        // Try go install first
        try {
            String goPath = findGoBinary();
            if (goPath != null) {
                ProcessBuilder pb = new ProcessBuilder(goPath, "install",
                    "github.com/Gentleman-Programming/engram/cmd/engram@latest");
                pb.redirectErrorStream(true);
                Process proc = pb.start();

                Thread.startVirtualThread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            logger.info("go install: {}", line);
                        }
                    } catch (Exception ignored) {}
                });

                boolean ok = proc.waitFor() == 0;
                javafx.application.Platform.runLater(() -> {
                    btn.setDisable(false);
                    btn.setText("INSTALL / UPDATE");
                    if (ok) {
                        versionValue.setText("Installed successfully");
                        versionValue.setStyle("-fx-text-fill: #5FBF7F;");
                    } else {
                        versionValue.setText("Install failed (exit code " + proc.exitValue() + ")");
                        versionValue.setStyle("-fx-text-fill: #D06B6B;");
                    }
                });
            } else {
                // Fallback to download zip from GitHub
                downloadEngramZip(btn, versionValue);
            }
        } catch (Exception e) {
            javafx.application.Platform.runLater(() -> {
                btn.setDisable(false);
                btn.setText("INSTALL / UPDATE");
                versionValue.setText("Error: " + e.getMessage());
                versionValue.setStyle("-fx-text-fill: #D06B6B;");
            });
        }
    }

    private String findGoBinary() {
        String[] paths = {"go", "go.exe", "C:\\Program Files\\Go\\bin\\go.exe"};
        for (String p : paths) {
            try {
                ProcessBuilder pb = new ProcessBuilder(p, "version");
                pb.redirectErrorStream(true);
                Process proc = pb.start();
                if (proc.waitFor() == 0) return p;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void downloadEngramZip(javafx.scene.control.Button btn, Label versionValue) {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        String suffix = os.contains("win") ? "windows_amd64" : os.contains("mac") ? "darwin_" + (arch.contains("arm") ? "arm64" : "amd64") : "linux_amd64";

        java.net.http.HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(10)).build()
            .sendAsync(
                java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.github.com/repos/Gentleman-Programming/engram/releases/latest"))
                    .header("Accept", "application/vnd.github.v3+json")
                    .GET().build(),
                java.net.http.HttpResponse.BodyHandlers.ofString()
            )
            .thenAccept(resp -> {
                String body = resp.body();
                String tag = extractJsonString(body, "tag_name");
                String zipName = "engram_" + (tag != null ? tag.replace("v", "") : "latest") + "_" + suffix + ".zip";
                String downloadUrl = "https://github.com/Gentleman-Programming/engram/releases/download/" + (tag != null ? tag : "latest") + "/" + zipName;

                downloadAndInstall(downloadUrl, btn, versionValue);
            })
            .exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> {
                    btn.setDisable(false);
                    btn.setText("INSTALL / UPDATE");
                    versionValue.setText("Network error");
                    versionValue.setStyle("-fx-text-fill: #D06B6B;");
                });
                return null;
            });
    }

    private void downloadAndInstall(String url, javafx.scene.control.Button btn, Label versionValue) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(30))
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .build();

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .GET().build();

            client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofByteArray())
                .thenAccept(resp -> {
                    try {
                        java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("engram-install");
                        java.nio.file.Path zipFile = tempDir.resolve("engram.zip");
                        java.nio.file.Files.write(zipFile, resp.body());

                        // Extract
                        java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(
                            new java.io.FileInputStream(zipFile.toFile()));
                        java.util.zip.ZipEntry entry;
                        String targetDir = "C:\\Users\\gmadariaga\\go\\bin";
                        java.nio.file.Files.createDirectories(java.nio.file.Path.of(targetDir));

                        while ((entry = zis.getNextEntry()) != null) {
                            if (!entry.isDirectory()) {
                                java.nio.file.Path out = java.nio.file.Path.of(targetDir, entry.getName());
                                java.nio.file.Files.copy(zis, out, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                out.toFile().setExecutable(true);
                            }
                        }
                        zis.close();
                        java.nio.file.Files.deleteIfExists(zipFile);
                        java.nio.file.Files.deleteIfExists(tempDir);

                        javafx.application.Platform.runLater(() -> {
                            btn.setDisable(false);
                            btn.setText("INSTALL / REINSTALL");
                            versionValue.setText("Installed to " + targetDir);
                            versionValue.setStyle("-fx-text-fill: #5FBF7F;");
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            btn.setDisable(false);
                            btn.setText("INSTALL / REINSTALL");
                            versionValue.setText("Install error: " + e.getMessage());
                            versionValue.setStyle("-fx-text-fill: #D06B6B;");
                        });
                    }
                })
                .exceptionally(ex -> {
                    javafx.application.Platform.runLater(() -> {
                        btn.setDisable(false);
                        btn.setText("INSTALL / REINSTALL");
                        versionValue.setText("Download failed");
                        versionValue.setStyle("-fx-text-fill: #D06B6B;");
                    });
                    return null;
                });
        } catch (Exception e) {
            btn.setDisable(false);
            btn.setText("INSTALL / REINSTALL");
            versionValue.setText("Error: " + e.getMessage());
            versionValue.setStyle("-fx-text-fill: #D06B6B;");
        }
    }

    private String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int start = idx + search.length();
        int end = json.indexOf("\"", start);
        if (end <= start) return null;
        return json.substring(start, end);
    }

    private Label createSidebarItem(String text) {
        String expandedText = "  " + text;
        String collapsedText = text.substring(0, 1);
        Label label = new Label(expandedText);
        label.getStyleClass().add("sidebar-item");
        label.setMaxWidth(Double.MAX_VALUE);
        label.getProperties().put("expandedText", expandedText);
        label.getProperties().put("collapsedText", collapsedText);
        return label;
    }

    private HBox createStatusBar(ConnectionViewModel connectionViewModel) {
        HBox statusBar = new HBox(16);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setAlignment(Pos.CENTER_LEFT);

        Label indicator = new Label();
        indicator.getStyleClass().add("status-indicator");
        connectionViewModel.stateProperty().addListener((obs, old, val) -> {
            indicator.setText(val == ConnectionState.CONNECTED ? "\u25CF" : "\u25CB");
            indicator.getStyleClass().removeAll("status-connected", "status-disconnected");
            indicator.getStyleClass().add(val == ConnectionState.CONNECTED ? "status-connected" : "status-disconnected");
        });

        Label urlLabel = new Label();
        urlLabel.getStyleClass().add("status-text");
        urlLabel.textProperty().bind(connectionViewModel.urlProperty());

        Label versionLabel = new Label();
        versionLabel.getStyleClass().add("status-text");
        connectionViewModel.versionProperty().addListener((obs, old, val) -> {
            versionLabel.setText("v" + val);
        });

        Label projectLabel = new Label();
        projectLabel.getStyleClass().add("status-text");
        projectLabel.setText("SQLite \u2022 FTS5");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label rightLabel = new Label("Engram Studio 1.0");
        rightLabel.getStyleClass().add("status-text");

        statusBar.getChildren().addAll(indicator, urlLabel, versionLabel, projectLabel, spacer, rightLabel);
        return statusBar;
    }

    private Image loadIcon(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) return new Image(is);
        } catch (Exception e) {
            logger.warn("Icon not found: {}", path);
        }
        return null;
    }

    private static class Spacer extends Region {
        public Spacer() {
            setMaxHeight(Double.MAX_VALUE);
            setPrefHeight(20);
        }
    }
}

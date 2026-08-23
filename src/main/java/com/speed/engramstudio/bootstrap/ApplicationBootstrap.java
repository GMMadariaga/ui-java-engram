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
import com.speed.engramstudio.presentation.conflicts.ConflictsView;
import com.speed.engramstudio.presentation.conflicts.ConflictsViewModel;
import com.speed.engramstudio.presentation.connection.ConnectionViewModel;
import com.speed.engramstudio.presentation.dashboard.DashboardView;
import com.speed.engramstudio.presentation.dashboard.DashboardViewModel;
import com.speed.engramstudio.presentation.diagnostics.DiagnosticsView;
import com.speed.engramstudio.presentation.diagnostics.DiagnosticsViewModel;
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
        EngramProcessManager processManager = new EngramProcessManager();

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
                sessionTimelineView, projectsView, promptsView, conflictsView, diagnosticsView, processManagerView);
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
        primaryStage.show();

        connectionViewModel.checkConnection();
        dashboardViewModel.load();

        logger.info("Engram Studio started.");
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
                               DiagnosticsView diagnosticsView, ProcessManagerView processManagerView) {

        VBox sidebar = new VBox(0);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(200);

        List<Label> allItems = new ArrayList<>();

        // Logo
        Label logo = new Label("\u25C8 ENGRAM");
        logo.getStyleClass().add("logo-text");
        logo.setPadding(new Insets(8, 16, 4, 16));

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
        Label settingsItem = createSidebarItem("\u2692 Settings");

        allItems.addAll(List.of(diagItem, processItem, settingsItem));

        // Navigation: select + switch + auto-load
        Runnable[] selectItem = new Runnable[1];
        selectItem[0] = () -> {};

        Runnable select = () -> {};

        dashItem.setOnMouseClicked(e -> { select(dashItem, allItems); switchView.accept(dashboardView.getView()); dashboardView.refresh(); });
        memItem.setOnMouseClicked(e -> { select(memItem, allItems); switchView.accept(memoryExplorerView.getView()); memoryExplorerView.refresh(); });
        sessItem.setOnMouseClicked(e -> { select(sessItem, allItems); switchView.accept(sessionTimelineView.getView()); sessionTimelineView.refresh(); });
        projItem.setOnMouseClicked(e -> { select(projItem, allItems); switchView.accept(projectsView.getView()); projectsView.refresh(); });
        timelineItem.setOnMouseClicked(e -> { select(timelineItem, allItems); switchView.accept(sessionTimelineView.getView()); sessionTimelineView.refresh(); });
        promptItem.setOnMouseClicked(e -> { select(promptItem, allItems); switchView.accept(promptsView.getView()); promptsView.refresh(); });
        conflictsItem.setOnMouseClicked(e -> { select(conflictsItem, allItems); switchView.accept(conflictsView.getView()); conflictsView.refresh(); });
        diagItem.setOnMouseClicked(e -> { select(diagItem, allItems); switchView.accept(diagnosticsView.getView()); diagnosticsView.refresh(); });
        processItem.setOnMouseClicked(e -> { select(processItem, allItems); switchView.accept(processManagerView.getView()); processManagerView.refresh(); });
        settingsItem.setOnMouseClicked(e -> { select(settingsItem, allItems); switchView.accept(createSettingsView()); });

        // Connection status
        Label connStatus = new Label();
        connStatus.getStyleClass().add("sidebar-label");
        connStatus.setPadding(new Insets(16, 16, 8, 16));
        connectionViewModel.stateProperty().addListener((obs, old, val) -> {
            String prefix = val == ConnectionState.CONNECTED ? "[OK]" : "[--]";
            connStatus.setText(prefix + " Engram");
            connStatus.getStyleClass().removeAll("status-connected", "status-disconnected");
            connStatus.getStyleClass().add(val == ConnectionState.CONNECTED ? "status-connected" : "status-disconnected");
        });

        sidebar.getChildren().addAll(
                logo, version,
                sep1,
                dashItem, memItem, sessItem, projItem, timelineItem, promptItem, conflictsItem,
                sep2,
                diagItem, processItem, settingsItem,
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

        // --- SERVER SECTION ---
        Label serverSection = new Label("SERVER");
        serverSection.getStyleClass().add("stat-label");

        Label urlLabel = new Label("Engram Server URL");
        urlLabel.getStyleClass().add("sidebar-label");
        javafx.scene.control.TextField urlField = new javafx.scene.control.TextField(appConfig.getEngramUrl());
        urlField.getStyleClass().add("search-field");
        urlField.setMaxWidth(400);

        Label timeoutLabel = new Label("Connection Timeout (ms)");
        timeoutLabel.getStyleClass().add("sidebar-label");
        javafx.scene.control.TextField timeoutField = new javafx.scene.control.TextField(String.valueOf(appConfig.getTimeout()));
        timeoutField.getStyleClass().add("search-field");
        timeoutField.setMaxWidth(200);

        Label autoLabel = new Label("Auto-connect on startup");
        autoLabel.getStyleClass().add("sidebar-label");
        javafx.scene.control.CheckBox autoCheck = new javafx.scene.control.CheckBox();
        autoCheck.setSelected(appConfig.isAutoConnect());

        // --- ENGRAM BINARY SECTION ---
        Label binarySection = new Label("ENGRAM BINARY");
        binarySection.getStyleClass().add("stat-label");

        Label pathLabel = new Label("Binary path:");
        pathLabel.getStyleClass().add("sidebar-label");
        Label pathValue = new Label(findEngramBinary());
        pathValue.getStyleClass().add("sidebar-label");
        pathValue.setStyle("-fx-text-fill: #C8C8C8;");

        Label versionLabel = new Label("Version:");
        versionLabel.getStyleClass().add("sidebar-label");
        Label versionValue = new Label("Checking...");
        versionValue.getStyleClass().add("sidebar-label");
        versionValue.setStyle("-fx-text-fill: #C8C8C8;");

        javafx.scene.control.Button checkUpdateBtn = new javafx.scene.control.Button("CHECK UPDATE");
        checkUpdateBtn.getStyleClass().addAll("button");
        checkUpdateBtn.setOnAction(e -> {
            checkUpdateBtn.setDisable(true);
            checkUpdateBtn.setText("CHECKING...");
            checkGitHubVersion(versionValue, checkUpdateBtn);
        });

        javafx.scene.control.Button installBtn = new javafx.scene.control.Button("INSTALL / UPDATE");
        installBtn.getStyleClass().addAll("button");
        installBtn.setOnAction(e -> {
            installBtn.setDisable(true);
            installBtn.setText("INSTALLING...");
            installEngram(installBtn, versionValue);
        });

        Label installHint = new Label("Uses: go install github.com/Gentleman-Programming/engram/cmd/engram@latest");
        installHint.getStyleClass().add("sidebar-label");
        installHint.setWrapText(true);

        // --- AGENT PROFILES SECTION ---
        Label agentSection = new Label("AGENT PROFILES");
        agentSection.getStyleClass().add("stat-label");

        Label agentHint = new Label("Run 'engram setup <agent>' to configure per agent:");
        agentHint.getStyleClass().add("sidebar-label");
        agentHint.setWrapText(true);

        Label agentCommands = new Label("  engram setup opencode\n  engram setup codex\n  engram setup gemini-cli\n  engram setup cursor\n  engram setup vscode-copilot");
        agentCommands.getStyleClass().add("sidebar-label");
        agentCommands.setStyle("-fx-text-fill: #858585;");

        javafx.scene.control.ListView<String> profileList = new javafx.scene.control.ListView<>();
        profileList.getStyleClass().add("table-view");
        profileList.setPrefHeight(100);
        profileList.getItems().addAll(
            "opencode (port 7437)",
            "codex (port 7438)",
            "gemini-cli (port 7439)",
            "cursor (port 7440)",
            "vscode-copilot (port 7441)"
        );
        profileList.getSelectionModel().selectFirst();

        javafx.scene.control.Button setupBtn = new javafx.scene.control.Button("engram setup");
        setupBtn.getStyleClass().addAll("button");
        setupBtn.setOnAction(e -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("engram", "setup");
                pb.redirectErrorStream(true);
                pb.start();
            } catch (Exception ex) {
                logger.error("Failed to run engram setup", ex);
            }
        });

        // --- SAVE ---
        javafx.scene.control.Button saveBtn = new javafx.scene.control.Button("SAVE");
        saveBtn.getStyleClass().addAll("button", "button-accent");
        saveBtn.setOnAction(e -> {
            appConfig.setEngramUrl(urlField.getText().trim());
            try { appConfig.setTimeout(Integer.parseInt(timeoutField.getText().trim())); } catch (NumberFormatException ignored) {}
            appConfig.setAutoConnect(autoCheck.isSelected());
            appConfig.save();
            saveBtn.setText("SAVED");
            saveBtn.setStyle("-fx-background-color: #5FBF7F;");
            javafx.application.Platform.runLater(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> {
                    saveBtn.setText("SAVE");
                    saveBtn.setStyle("");
                });
            });
        });

        Label configPath = new Label("Config: " + System.getProperty("user.home") + "\\.engram-studio\\engram-studio.properties");
        configPath.getStyleClass().add("sidebar-label");

        VBox form = new VBox(6);
        form.getChildren().addAll(
            serverSection,
            urlLabel, urlField,
            timeoutLabel, timeoutField,
            autoLabel, autoCheck,
            new Separator(),
            binarySection,
            pathLabel, pathValue,
            versionLabel, versionValue,
            new HBox(8, checkUpdateBtn, installBtn),
            installHint,
            new Separator(),
            agentSection,
            agentHint, agentCommands,
            profileList, setupBtn,
            new Separator(),
            saveBtn, configPath
        );

        settings.getChildren().addAll(title, form);
        return settings;
    }

    private String findEngramBinary() {
        // Check common locations
        String[] paths = {
            "C:\\Users\\gmadariaga\\go\\bin\\engram.exe",
            System.getProperty("user.home") + "\\go\\bin\\engram.exe",
            "engram.exe"
        };
        for (String p : paths) {
            if (new java.io.File(p).exists()) return p;
        }
        return "engram.exe (not found in PATH)";
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
        Label label = new Label("  " + text);
        label.getStyleClass().add("sidebar-item");
        label.setMaxWidth(Double.MAX_VALUE);
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

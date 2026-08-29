package com.speed.engramstudio.presentation.agents;

import com.sun.javafx.webkit.WebConsoleListener;
import com.speed.engramstudio.infrastructure.config.AgentSessionSetting;
import com.speed.engramstudio.infrastructure.config.AppConfiguration;
import com.speed.engramstudio.infrastructure.process.AgentExecutableResolver;
import com.speed.engramstudio.infrastructure.process.EngramExecutableResolver;
import com.speed.engramstudio.infrastructure.process.ProcessTreeInspector;
import com.speed.engramstudio.infrastructure.process.PtyTerminalSession;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import netscape.javascript.JSObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Full-size xterm.js terminal for PowerShell and common agent CLIs. */
public final class AgentCliView {

    private final List<AgentCommand> agents = new ArrayList<>(List.of(
        new AgentCommand("powershell", "PowerShell", List.of(), true, false),
        new AgentCommand("engram", "engram tui", List.of("engram", "tui"), false, false),
        new AgentCommand("claude", "claude", List.of("claude"), false, false),
        new AgentCommand("codex", "codex", List.of("codex"), false, false),
        new AgentCommand("opencode", "opencode", List.of("opencode"), false, false),
        new AgentCommand("agy", "Antigravity", List.of("agy"), false, false)
    ));

    private static final Set<String> MODEL_AGENT_IDS = Set.of("claude", "codex", "opencode", "agy");
    private static final Duration AGENT_ACTIVITY_INTERVAL = Duration.seconds(3);

    private static final List<TabColor> TAB_COLORS = List.of(
        new TabColor("green", "Verde", "#4EC9B0"),
        new TabColor("yellow", "Amarillo", "#DCDCAA"),
        new TabColor("orange", "Naranja", "#E8A33D"),
        new TabColor("red", "Rojo", "#F14C4C"),
        new TabColor("purple", "Morado", "#C586C0"),
        new TabColor("pink", "Rosa", "#FF7AB6"));

    private final VBox root = new VBox();
    private final StackPane terminalPane = new StackPane();
    private final List<AgentSession> sessions = new ArrayList<>();
    private final ToggleGroup agentGroup = new ToggleGroup();
    private final HBox tabs = new HBox(6);
    private final ReadOnlyIntegerWrapper runningAgentCount = new ReadOnlyIntegerWrapper(0);
    private final Timeline agentActivityWatch = new Timeline();
    private final AppConfiguration configuration;
    private Button addAgentButton;
    private AgentSession activeSession;
    private int customAgentSequence;
    private boolean agentActivityScanRunning;

    public AgentCliView() {
        this(new AppConfiguration());
    }

    public AgentCliView(AppConfiguration configuration) {
        this.configuration = configuration;
        buildUi();
    }

    private void buildUi() {
        root.getStyleClass().add("terminal-view");
        root.setFillWidth(true);

        tabs.setAlignment(Pos.CENTER_LEFT);
        tabs.getStyleClass().add("agent-tabs");

        addAgentButton = new Button("+");
        addAgentButton.setFocusTraversable(false);
        addAgentButton.getStyleClass().add("agent-tab");
        addAgentButton.setOnAction(event -> promptForAgent());
        tabs.getChildren().add(addAgentButton);

        ScrollPane tabsScroll = new ScrollPane(tabs);
        tabsScroll.setFitToHeight(true);
        tabsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        tabsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tabsScroll.setPannable(true);
        tabsScroll.getStyleClass().add("agent-tabs-scroll");

        WebConsoleListener.setDefaultListener((view, message, lineNumber, sourceId) ->
            System.err.println("JS Console [" + sourceId + ":" + lineNumber + "]: " + message));
        terminalPane.getStyleClass().add("terminal-pane");

        restoreSessions();

        root.getChildren().addAll(tabsScroll, terminalPane);
        VBox.setVgrow(terminalPane, Priority.ALWAYS);
        activateSession(sessions.getFirst());
        startAgentActivityWatch();
    }

    /** Number of tabs whose agent CLI is running right now. */
    public ReadOnlyIntegerProperty runningAgentCountProperty() {
        return runningAgentCount.getReadOnlyProperty();
    }

    private void startAgentActivityWatch() {
        agentActivityWatch.getKeyFrames().add(
            new KeyFrame(AGENT_ACTIVITY_INTERVAL, event -> refreshRunningAgentCount()));
        agentActivityWatch.setCycleCount(Animation.INDEFINITE);
        agentActivityWatch.play();
    }

    /**
     * The pty child is always the shell, so a live terminal says nothing about
     * the agent. What counts is whether the CLI still runs below that shell.
     */
    private void refreshRunningAgentCount() {
        if (agentActivityScanRunning) return;

        List<AgentActivityProbe> probes = new ArrayList<>();
        for (AgentSession session : sessions) {
            if (!MODEL_AGENT_IDS.contains(session.agent.id) || session.agent.command.isEmpty()) continue;
            session.terminal.pid().ifPresent(pid ->
                probes.add(new AgentActivityProbe(pid, session.agent.command.getFirst())));
        }
        if (probes.isEmpty()) {
            runningAgentCount.set(0);
            return;
        }

        agentActivityScanRunning = true;
        Thread.ofVirtual().start(() -> {
            int running = 0;
            try {
                for (AgentActivityProbe probe : probes) {
                    if (ProcessTreeInspector.hasDescendantMatching(probe.shellPid(), probe.marker())) running++;
                }
            } catch (RuntimeException ex) {
                System.err.println("Agent activity scan error: " + ex.getMessage());
            }
            int total = running;
            Platform.runLater(() -> {
                runningAgentCount.set(total);
                agentActivityScanRunning = false;
            });
        });
    }

    private void configureWebEngineUserDataDirectory(WebEngine engine) {
        try {
            // WebEngine locks its profile directory. Use an isolated profile
            // per launch so a previous/still-running app cannot make this
            // terminal fail with "user data directory ... already in use".
            engine.setUserDataDirectory(Files.createTempDirectory("engram-studio-webview-").toFile());
        } catch (IOException ex) {
            System.err.println("WebEngine user data directory error: " + ex.getMessage());
        }
    }

    private void attachBridge(AgentSession session) {
        JSObject window = (JSObject) session.webEngine.executeScript("window");
        window.setMember("javaBridge", session.bridge);
        // With several WebViews loading at once, JavaFX may report SUCCEEDED
        // before the final inline script has installed the startup function.
        // Let the page retry until that function is available.
        session.webEngine.executeScript("""
            (function waitForEmbeddedTerminal() {
              if (typeof window.startEmbeddedTerminal === 'function') {
                window.startEmbeddedTerminal();
              } else {
                window.setTimeout(waitForEmbeddedTerminal, 50);
              }
            })();
            """);
    }

    private String xtermHtml() {
        String xtermCss = terminalAsset(
            "/web/xterm.css", "text/css");
        String xtermScript = terminalAsset(
            "/web/xterm.js", "text/javascript");
        String fitScript = terminalAsset(
            "/web/xterm-addon-fit.js", "text/javascript");

        String template = """
            <!doctype html>
            <html><head>
              <meta charset="UTF-8">
              <link rel="stylesheet" href="{{XTERM_CSS}}">
              <script src="{{XTERM_JS}}"></script>
              <script src="{{FIT_JS}}"></script>
              <style>
                html, body, #terminal { width:100%; height:100%; margin:0; padding:0; overflow:hidden; }
                body { background:#050505; }
                .xterm { height:100%; padding:14px; box-sizing:border-box; }
                /* WebView paints no visible scrollbar for the xterm viewport unless it is styled. */
                .xterm .xterm-viewport::-webkit-scrollbar { width:14px; }
                .xterm .xterm-viewport::-webkit-scrollbar-track { background:#141414; }
                .xterm .xterm-viewport::-webkit-scrollbar-thumb {
                  background:#7A7A7A; border:3px solid #141414;
                  border-radius:7px; background-clip:padding-box;
                }
                .xterm .xterm-viewport::-webkit-scrollbar-thumb:hover { background:#62A7FF; }
              </style>
            </head><body>
              <div id="terminal"></div>
              <script>
                window.startEmbeddedTerminal = function() {
                  if (window.term || window.terminalStarting) return;
                  if (typeof Terminal === 'undefined' || typeof FitAddon === 'undefined') {
                    // WebView can report the document as loaded before its
                    // external scripts are ready. Retry instead of losing
                    // the terminal during startup.
                    window.setTimeout(window.startEmbeddedTerminal, 50);
                    return;
                  }
                  window.terminalStarting = true;
                  window.fit = new FitAddon.FitAddon();
                  window.term = new Terminal({
                    cursorBlink:true, cursorStyle:'block', scrollback:10000,
                    convertEol:true, windowsMode:true,
                    fontFamily:'Consolas, monospace', fontSize:14,
                    theme: {
                      background:'#050505', foreground:'#E0E0E0',
                      cursor:'#FFFFFF', selectionBackground:'#264F78',
                      black:'#000000', red:'#CD3131', green:'#0DBC79',
                      yellow:'#E5E510', blue:'#2472C8', magenta:'#BC3FBC',
                      cyan:'#11A8CD', white:'#E5E5E5',
                      brightBlack:'#666666', brightRed:'#F14C4C',
                      brightGreen:'#23D18B', brightYellow:'#F5F543',
                      brightBlue:'#3B8EEA', brightMagenta:'#D670D6',
                      brightCyan:'#29B8DB', brightWhite:'#FFFFFF'
                    }
                  });
                  window.term.loadAddon(window.fit);
                  window.term.open(document.getElementById('terminal'));
                  window.term.onData(data => javaBridge.sendData(data));
                  window.pasteFromClipboard = function() {
                    const text = javaBridge.readClipboardText();
                    if (!text) return;
                    if (typeof window.term.paste === 'function') window.term.paste(text);
                    else javaBridge.sendData(text);
                  };
                  window.term.attachCustomKeyEventHandler(event => {
                    if (event.type !== 'keydown') return true;
                    const modifier = event.ctrlKey || event.metaKey;
                    const key = event.key.toLowerCase();
                    if (modifier && key === 'c' && window.term.hasSelection()) {
                      javaBridge.copyText(window.term.getSelection());
                      window.term.clearSelection();
                      return false;
                    }
                    if (modifier && key === 'v') {
                      window.pasteFromClipboard();
                      return false;
                    }
                    return true;
                  });
                  window.fit.fit();
                  javaBridge.onResize(window.term.cols, window.term.rows);
                  javaBridge.onReady();
                  window.addEventListener('resize', () => {
                    window.fit.fit();
                    javaBridge.onResize(window.term.cols, window.term.rows);
                  });
                  window.term.focus();
                };
                window.writeTerminalBytes = function(base64) {
                  if (!window.term) return;
                  const raw = atob(base64), bytes = new Uint8Array(raw.length);
                  for (let i=0; i<raw.length; i++) bytes[i] = raw.charCodeAt(i);
                  window.term.write(bytes);
                };
                window.resizeTerminal = function() {
                  if (!window.term || !window.fit) return;
                  window.fit.fit();
                  javaBridge.onResize(window.term.cols, window.term.rows);
                };
                window.focusTerminal = function() { if (window.term) window.term.focus(); };
              </script>
            </body></html>
            """;

        return template
            .replace("{{XTERM_CSS}}", xtermCss)
            .replace("{{XTERM_JS}}", xtermScript)
            .replace("{{FIT_JS}}", fitScript);
    }

    private String terminalAsset(String resourcePath, String mimeType) {
        try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
            if (stream != null) {
                String base64 = Base64.getEncoder().encodeToString(stream.readAllBytes());
                return "data:" + mimeType + ";base64," + base64;
            }
        } catch (IOException ex) {
            System.err.println("Error cargando recurso: " + resourcePath + " -> " + ex.getMessage());
        }
        System.err.println("ADVERTENCIA: No se encontró el recurso embebido: " + resourcePath);
        return "";
    }

    private AgentSession addAgentSession(AgentCommand agent, boolean removable) {
        AgentSession session = new AgentSession(agent, sessionLabel(agent), removable);
        sessions.add(session);
        addAgentTab(session);
        terminalPane.getChildren().add(session.webView);
        session.webView.setVisible(false);
        session.webView.setManaged(false);
        return session;
    }

    private void restoreSessions() {
        for (AgentSessionSetting setting : configuration.getAgentSessions()) {
            AgentCommand agent = resolveAgent(setting);
            if (agent == null) continue;
            AgentSession session = addAgentSession(agent, setting.removable());
            if (!setting.label().isBlank()) {
                session.label = setting.label();
                session.tab.setText(setting.label());
            }
            session.colorId = setting.color();
            applyTabColor(session);
        }
        for (AgentCommand agent : List.copyOf(agents)) {
            boolean alreadyOpen = sessions.stream()
                .anyMatch(session -> !session.removable && session.agent.id.equals(agent.id));
            if (!alreadyOpen) addAgentSession(agent, false);
        }
    }

    private AgentCommand resolveAgent(AgentSessionSetting setting) {
        for (AgentCommand agent : agents) {
            if (agent.id.equals(setting.agentId())) return agent;
        }
        if (setting.command().isBlank()) return null;
        try {
            AgentCommand agent = new AgentCommand(setting.agentId(), setting.agentName(),
                parseCommandLine(setting.command()), false, true);
            agents.add(agent);
            rememberCustomAgentId(setting.agentId());
            return agent;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void rememberCustomAgentId(String agentId) {
        if (!agentId.startsWith("custom-")) return;
        try {
            customAgentSequence = Math.max(customAgentSequence,
                Integer.parseInt(agentId.substring("custom-".length())));
        } catch (NumberFormatException ignored) {
            // A hand-edited id simply stays out of the generated sequence.
        }
    }

    private void persistSessions() {
        configuration.saveAgentSessions(sessions.stream()
            .map(session -> new AgentSessionSetting(
                session.agent.id,
                session.agent.name,
                session.agent.rawPowerShellCommand ? String.join(" ", session.agent.command) : "",
                session.label,
                session.colorId,
                session.removable))
            .toList());
    }

    private String sessionLabel(AgentCommand agent) {
        long sameAgentCount = sessions.stream()
            .filter(session -> session.agent.id.equals(agent.id))
            .count();
        return sameAgentCount == 0 ? agent.name : agent.name + " #" + (sameAgentCount + 1);
    }

    private void addAgentTab(AgentSession session) {
        ToggleButton tab = new ToggleButton(session.label);
        tab.setToggleGroup(agentGroup);
        tab.setMnemonicParsing(false);
        tab.setFocusTraversable(false);
        tab.getStyleClass().add("agent-tab");
        tab.setOnAction(event -> activateSession(session));
        session.tab = tab;
        HBox tabContainer = new HBox(0);
        tabContainer.setAlignment(Pos.CENTER);
        tabContainer.getStyleClass().add("agent-tab-container");
        tabContainer.getChildren().add(tab);
        if (session.removable) {
            Button closeButton = new Button("×");
            closeButton.setFocusTraversable(false);
            closeButton.setMnemonicParsing(false);
            closeButton.setTooltip(new javafx.scene.control.Tooltip("Cerrar sesión"));
            closeButton.getStyleClass().add("agent-tab-close");
            closeButton.setOnAction(event -> removeSession(session));
            tabContainer.getChildren().add(closeButton);
        }
        tab.setContextMenu(buildTabMenu(session));
        applyTabColor(session);
        session.tabContainer = tabContainer;
        int insertAt = tabs.getChildren().indexOf(addAgentButton);
        tabs.getChildren().add(insertAt, tabContainer);
    }

    private ContextMenu buildTabMenu(AgentSession session) {
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("agent-tab-menu");

        MenuItem renameItem = new MenuItem("Renombrar...");
        renameItem.setOnAction(event -> renameSession(session));

        CustomMenuItem colorItem = new CustomMenuItem(buildColorRow(session, menu), false);
        menu.getItems().addAll(renameItem, colorItem);

        if (session.removable) {
            MenuItem closeItem = new MenuItem("Cerrar sesión");
            closeItem.setOnAction(event -> removeSession(session));
            menu.getItems().addAll(new SeparatorMenuItem(), closeItem);
        }
        return menu;
    }

    private Node buildColorRow(AgentSession session, ContextMenu menu) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("agent-color-row");
        row.getChildren().add(colorSwatch(session, menu, null));
        for (TabColor color : TAB_COLORS) row.getChildren().add(colorSwatch(session, menu, color));
        return row;
    }

    private Button colorSwatch(AgentSession session, ContextMenu menu, TabColor color) {
        Button swatch = new Button();
        swatch.setFocusTraversable(false);
        swatch.getStyleClass().add("agent-color-swatch");
        if (color == null) {
            swatch.getStyleClass().add("agent-color-none");
            swatch.setTooltip(new Tooltip("Sin color"));
        } else {
            swatch.setStyle("-fx-background-color: " + color.hex() + ";");
            swatch.setTooltip(new Tooltip(color.name()));
        }
        swatch.setOnAction(event -> {
            session.colorId = color == null ? "" : color.id();
            applyTabColor(session);
            persistSessions();
            menu.hide();
        });
        return swatch;
    }

    private void applyTabColor(AgentSession session) {
        if (session.tab == null) return;
        for (TabColor color : TAB_COLORS) session.tab.getStyleClass().remove(color.styleClass());
        TAB_COLORS.stream()
            .filter(color -> color.id().equals(session.colorId))
            .findFirst()
            .ifPresent(color -> session.tab.getStyleClass().add(color.styleClass()));
    }

    private void renameSession(AgentSession session) {
        TextInputDialog dialog = new TextInputDialog(session.label);
        dialog.setTitle("Renombrar sesión");
        dialog.setHeaderText("Nombre de la pestaña");
        dialog.setContentText("Nombre:");
        dialog.showAndWait().map(String::trim).filter(value -> !value.isEmpty()).ifPresent(value -> {
            session.label = value;
            session.tab.setText(value);
            persistSessions();
        });
    }

    private void promptForAgent() {
        List<CliChoice> choices = agents.stream()
            .map(agent -> new CliChoice(agent.name, agent))
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        choices.add(new CliChoice("Comando personalizado...", null));

        ChoiceDialog<CliChoice> dialog = new ChoiceDialog<>(choices.getFirst(), choices);
        dialog.setTitle("Nueva sesión");
        dialog.setHeaderText("Elige qué CLI quieres ejecutar");
        dialog.setContentText("CLI:");
        dialog.showAndWait().ifPresent(choice -> {
            if (choice.agent == null) {
                promptForCustomAgent();
            } else {
                activateSession(addAgentSession(choice.agent, true));
                persistSessions();
            }
        });
    }

    private void promptForCustomAgent() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nueva sesión personalizada");
        dialog.setHeaderText("Agregar una CLI o comando PowerShell");
        dialog.setContentText("Comando (ej.: git, docker, Get-Process):");
        dialog.getEditor().setPromptText("agy");
        dialog.showAndWait().map(String::trim).filter(value -> !value.isEmpty()).ifPresent(value -> {
            try {
                List<String> command = parseCommandLine(value);
                AgentCommand agent = new AgentCommand(
                    "custom-" + (++customAgentSequence), command.getFirst(), command, false, true);
                agents.add(agent);
                activateSession(addAgentSession(agent, true));
                persistSessions();
            } catch (IllegalArgumentException ex) {
                // Keep the terminal itself free of auxiliary dialogs/messages.
            }
        });
    }

    private void removeSession(AgentSession session) {
        if (!session.removable || !sessions.contains(session)) return;

        boolean wasActive = activeSession == session;
        int tabIndex = tabs.getChildren().indexOf(session.tabContainer);
        sessions.remove(session);
        agentGroup.getToggles().remove(session.tab);
        tabs.getChildren().remove(session.tabContainer);
        terminalPane.getChildren().remove(session.webView);
        session.close();
        if (session.agent.rawPowerShellCommand
            && sessions.stream().noneMatch(candidate -> candidate.agent == session.agent)) {
            agents.remove(session.agent);
        }

        if (wasActive && !sessions.isEmpty()) {
            AgentSession replacement = sessions.get(Math.min(tabIndex, sessions.size() - 1));
            activateSession(replacement);
        }
        persistSessions();
    }

    private List<String> parseCommandLine(String value) {
        List<String> arguments = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        char quote = 0;
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character == '\'' || character == '"') {
                if (quote == 0) quote = character;
                else if (quote == character) quote = 0;
                else current.append(character);
            } else if (Character.isWhitespace(character) && quote == 0) {
                if (!current.isEmpty()) {
                    arguments.add(current.toString());
                    current.setLength(0);
                }
            } else current.append(character);
        }
        if (quote != 0) throw new IllegalArgumentException("La línea tiene comillas sin cerrar.");
        if (!current.isEmpty()) arguments.add(current.toString());
        if (arguments.isEmpty()) throw new IllegalArgumentException("Escribe un comando válido.");
        return arguments;
    }

    private void activateSession(AgentSession session) {
        activeSession = session;
        for (AgentSession candidate : sessions) {
            boolean active = candidate == session;
            candidate.webView.setVisible(active);
            candidate.webView.setManaged(active);
        }
        if (session.tab != null) session.tab.setSelected(true);
        if (session.webTerminalReady) {
            Platform.runLater(() -> {
                if (activeSession != session) return;
                session.webEngine.executeScript("window.resizeTerminal()");
                startProcessWhenReady(session);
                focusSession(session);
            });
        }
    }

    private void startProcessWhenReady(AgentSession session) {
        if (activeSession != session || !session.webTerminalReady || session.processStarted) return;
        session.processStarted = true;
        run(session);
    }

    private void focusSession(AgentSession session) {
        session.webEngine.executeScript("window.focusTerminal()");
    }

    private void run(AgentSession session) {
        if (session.closed) return;
        long request = ++session.launchNumber;
        try {
            String command = session.agent.resolveCommand();
            if (session.terminal.isAlive()) {
                focusSession(session);
                return;
            }

            Map<String, String> environment = new HashMap<>(System.getenv());
            environment.put("TERM", "xterm-256color");
            environment.put("COLORTERM", "truecolor");
            AgentExecutableResolver.addUserLocalPaths(environment);
            session.terminal.startRaw(AgentExecutableResolver.embeddedPowerShellCommand(), "",
                    environment,
                    session::writeTerminalOutput,
                    exitCode -> Platform.runLater(() -> {
                        if (!session.closed) session.processStarted = false;
                    }));
            if (!session.agent.powershell) sendAfterStartup(session, command, request);
        } catch (Exception ex) {
            session.processStarted = false;
            String message = "No se pudo iniciar " + session.label + ": "
                + (ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
            System.err.println(message);
            session.showSystemMessage(message);
        }
    }

    private void sendAfterStartup(AgentSession session, String command, long request) {
        PauseTransition pause = new PauseTransition(Duration.millis(650));
        pause.setOnFinished(event -> {
            if (request == session.launchNumber && session.terminal.isAlive()) {
                writeInput(session, command + "\r");
            }
        });
        pause.play();
    }

    private void writeInput(AgentSession session, String value) {
        try {
            session.terminal.write(value);
        } catch (IOException ignored) {
            // The PTY may finish between a browser key event and this write.
        }
    }

    public VBox getView() {
        return root;
    }

    public void close() {
        agentActivityWatch.stop();
        sessions.forEach(AgentSession::close);
    }

    private final class AgentSession {
        private final AgentCommand agent;
        private final boolean removable;
        private String label;
        private String colorId = "";
        private final PtyTerminalSession terminal = new PtyTerminalSession();
        private final WebView webView = new WebView();
        private final WebEngine webEngine = webView.getEngine();
        private final TerminalBridge bridge = new TerminalBridge(this);
        private final ContextMenu terminalContextMenu = new ContextMenu();
        private final Object terminalOutputLock = new Object();
        private final ByteArrayOutputStream pendingTerminalOutput = new ByteArrayOutputStream();
        private ToggleButton tab;
        private HBox tabContainer;
        private boolean webTerminalReady;
        private boolean processStarted;
        private boolean terminalOutputFlushScheduled;
        private boolean closed;
        private long launchNumber;

        private AgentSession(AgentCommand agent, String label, boolean removable) {
            this.agent = agent;
            this.label = label;
            this.removable = removable;
            // xterm.js owns the terminal interaction; use its custom menu so
            // JavaFX does not intercept right-click before the bridge can
            // access the system clipboard.
            webView.setContextMenuEnabled(false);
            webView.setFocusTraversable(true);
            configureClipboardSupport();
            configureWebEngineUserDataDirectory(webEngine);
            webEngine.setJavaScriptEnabled(true);
            webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    attachBridge(this);
                } else if (newState == javafx.concurrent.Worker.State.FAILED) {
                    System.err.println("Error al cargar la terminal " + label + ": "
                        + webEngine.getLoadWorker().getException());
                }
            });
            webEngine.setOnError(event ->
                System.err.println("WebEngine Error [" + label + "]: " + event.getMessage()));
            webEngine.loadContent(xtermHtml(), "text/html");
        }

        private void configureClipboardSupport() {
            MenuItem copyItem = new MenuItem("Copiar");
            copyItem.setOnAction(event -> copySelectedText());

            MenuItem pasteItem = new MenuItem("Pegar");
            pasteItem.setOnAction(event -> pasteClipboard());

            MenuItem selectAllItem = new MenuItem("Seleccionar todo");
            selectAllItem.setOnAction(event -> {
                if (webTerminalReady && !closed) {
                    webEngine.executeScript("window.term.selectAll()");
                    focusSession(this);
                }
            });

            terminalContextMenu.getItems().addAll(copyItem, pasteItem, selectAllItem);
            terminalContextMenu.setOnShowing(event ->
                copyItem.setDisable(selectedText().isEmpty()));

            // JavaFX receives the key before WebView. This makes clipboard
            // shortcuts work even when WebView does not forward Ctrl+C/V to
            // the JavaScript keyboard handler.
            webView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (!(event.isControlDown() || event.isMetaDown()) || event.isAltDown()) return;
                if (event.getCode() == KeyCode.C) {
                    if (!selectedText().isEmpty()) {
                        copySelectedText();
                        event.consume();
                    }
                } else if (event.getCode() == KeyCode.V) {
                    pasteClipboard();
                    event.consume();
                }
            });

            // Do not rely on WebView's browser context menu. It can be
            // disabled on JavaFX platforms, so provide a native JavaFX menu.
            webView.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                showTerminalContextMenu(event.getScreenX(), event.getScreenY());
                event.consume();
            });
            // WebView versions that do not emit ContextMenuEvent still emit
            // the secondary mouse release, which is a reliable fallback.
            webView.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
                if (event.getButton() != MouseButton.SECONDARY) return;
                showTerminalContextMenu(event.getScreenX(), event.getScreenY());
                event.consume();
            });
        }

        private void showTerminalContextMenu(double screenX, double screenY) {
            if (!webTerminalReady || closed) return;
            if (!terminalContextMenu.isShowing()) {
                terminalContextMenu.show(webView, screenX, screenY);
            }
        }

        private String selectedText() {
            if (!webTerminalReady || closed) return "";
            Object selection = webEngine.executeScript(
                "window.term && window.term.getSelection ? window.term.getSelection() : ''");
            return selection == null ? "" : selection.toString();
        }

        private void copySelectedText() {
            String selection = selectedText();
            if (selection.isEmpty()) return;
            bridge.copyText(selection);
            webEngine.executeScript("window.term.clearSelection()");
            focusSession(this);
        }

        private void pasteClipboard() {
            if (webTerminalReady && !closed) {
                webEngine.executeScript("window.pasteFromClipboard()");
                focusSession(this);
            }
        }

        private void writeTerminalOutput(byte[] output) {
            if (closed || output == null || output.length == 0) return;

            boolean scheduleFlush;
            synchronized (terminalOutputLock) {
                pendingTerminalOutput.writeBytes(output);
                scheduleFlush = !terminalOutputFlushScheduled;
                terminalOutputFlushScheduled = true;
            }
            if (scheduleFlush) Platform.runLater(this::flushTerminalOutput);
        }

        /**
         * Coalesces PTY chunks before crossing the Java/JavaScript bridge. The
         * bytes remain untouched; batching simply prevents a busy TUI from
         * flooding the JavaFX event queue with one script call per read().
         */
        private void flushTerminalOutput() {
            if (closed) return;
            byte[] output;
            synchronized (terminalOutputLock) {
                if (pendingTerminalOutput.size() == 0) {
                    terminalOutputFlushScheduled = false;
                    return;
                }
                output = pendingTerminalOutput.toByteArray();
                pendingTerminalOutput.reset();
            }

            if (webTerminalReady) {
                String base64 = Base64.getEncoder().encodeToString(output);
                webEngine.executeScript("window.writeTerminalBytes('" + base64 + "')");
            }

            boolean scheduleNext;
            synchronized (terminalOutputLock) {
                scheduleNext = pendingTerminalOutput.size() > 0;
                if (!scheduleNext) terminalOutputFlushScheduled = false;
            }
            if (scheduleNext) Platform.runLater(this::flushTerminalOutput);
        }

        private void showSystemMessage(String message) {
            if (!webTerminalReady || closed) return;
            String output = "\r\n\u001b[31m[Agent CLI] " + message
                + "\u001b[0m\r\n";
            writeTerminalOutput(output.getBytes(StandardCharsets.UTF_8));
        }

        private void close() {
            closed = true;
            processStarted = false;
            terminalContextMenu.hide();
            terminal.close();
            synchronized (terminalOutputLock) {
                pendingTerminalOutput.reset();
                terminalOutputFlushScheduled = false;
            }
        }
    }

    public final class TerminalBridge {
        private final AgentSession session;

        private TerminalBridge(AgentSession session) {
            this.session = session;
        }

        public void sendData(String data) {
            writeInput(session, data);
        }

                public void onResize(int columns, int rows) {
            session.terminal.resize(columns, rows);
        }

        public void copyText(String text) {
            if (text == null || text.isEmpty()) return;
            ClipboardContent content = new ClipboardContent();
            content.putString(text);
            Clipboard.getSystemClipboard().setContent(content);
        }

        public String readClipboardText() {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            return clipboard.hasString() ? clipboard.getString() : "";
        }

        public void onReady() {
            Platform.runLater(() -> {
                session.webTerminalReady = true;
                if (activeSession == session) {
                    session.webEngine.executeScript("window.resizeTerminal()");
                    startProcessWhenReady(session);
                    focusSession(session);
                }
            });
        }
    }

    private record AgentActivityProbe(long shellPid, String marker) {
    }

    private record TabColor(String id, String name, String hex) {
        private String styleClass() {
            return "agent-tab-" + id;
        }
    }

    private final class CliChoice {
        private final String label;
        private final AgentCommand agent;

        private CliChoice(String label, AgentCommand agent) {
            this.label = label;
            this.agent = agent;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final class AgentCommand {
        private final String id;
        private final String name;
        private final List<String> command;
        private final boolean powershell;
        private final boolean rawPowerShellCommand;

        private AgentCommand(String id, String name, List<String> command,
                             boolean powershell, boolean rawPowerShellCommand) {
            this.id = id;
            this.name = name;
            this.command = command;
            this.powershell = powershell;
            this.rawPowerShellCommand = rawPowerShellCommand;
        }

        private String resolveCommand() {
            if (powershell) return "Write-Output ''";
            if (rawPowerShellCommand) return String.join(" ", command);
            String executable;
            if (id.equals("engram")) {
                executable = EngramExecutableResolver.resolve().orElse(command.getFirst());
            } else {
                // Resolve the executable in Java instead of relying only on
                // the embedded PowerShell PATH. IDE-launched Java processes
                // can inherit a stale PATH, while pnpm/agy are installed in
                // user-local directories that are not always visible there.
                executable = AgentExecutableResolver.resolve(command.getFirst(), configuration.getAgentExecutable(id))
                    .orElse(command.getFirst());
            }
            return AgentExecutableResolver.buildShellCommand(executable, command.subList(1, command.size()));
        }
    }
}

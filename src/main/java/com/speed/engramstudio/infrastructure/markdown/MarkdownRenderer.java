package com.speed.engramstudio.infrastructure.markdown;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownRenderer {

    private static final String COLOR_TEXT = "#C8C8C8";
    private static final String COLOR_HEADING = "#62A7FF";
    private static final String COLOR_BOLD = "#E0E0E0";
    private static final String COLOR_CODE = "#5FBF7F";
    private static final String COLOR_CODE_BG = "#1E1E1E";
    private static final String COLOR_LIST = "#858585";

    public static TextFlow render(String markdown) {
        TextFlow flow = new TextFlow();
        flow.setStyle("-fx-padding: 12; -fx-background-color: #1E1E1E;");
        flow.setLineSpacing(3);

        if (markdown == null || markdown.isEmpty()) {
            flow.getChildren().add(createText(""));
            return flow;
        }

        String[] lines = markdown.split("\n");
        List<Node> nodes = new ArrayList<>();

        for (String line : lines) {
            if (line.startsWith("## ")) {
                nodes.add(createHeading(line.substring(3)));
                nodes.add(createText("\n"));
            } else if (line.startsWith("### ")) {
                nodes.add(createSubHeading(line.substring(4)));
                nodes.add(createText("\n"));
            } else if (line.startsWith("# ")) {
                nodes.add(createHeading(line.substring(2)));
                nodes.add(createText("\n"));
            } else if (line.startsWith("- ") || line.startsWith("* ")) {
                nodes.add(createBullet(line.substring(2)));
                nodes.add(createText("\n"));
            } else if (line.startsWith("```")) {
                // code block marker
            } else if (line.startsWith("  ") || line.startsWith("\t")) {
                nodes.add(createCodeLine(line));
                nodes.add(createText("\n"));
            } else if (line.isBlank()) {
                nodes.add(createText("\n"));
            } else {
                nodes.addAll(parseInline(line));
                nodes.add(createText("\n"));
            }
        }

        flow.getChildren().addAll(nodes);
        return flow;
    }

    private static Text createText(String content) {
        Text t = new Text(content);
        t.setFill(Color.web(COLOR_TEXT));
        t.setFont(Font.font("Consolas", 13));
        return t;
    }

    private static Text createHeading(String content) {
        Text t = new Text(content);
        t.setFill(Color.web(COLOR_HEADING));
        t.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        return t;
    }

    private static Text createSubHeading(String content) {
        Text t = new Text(content);
        t.setFill(Color.web(COLOR_HEADING));
        t.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        return t;
    }

    private static Text createBullet(String content) {
        Text t = new Text("  \u2022 " + content);
        t.setFill(Color.web(COLOR_LIST));
        t.setFont(Font.font("Consolas", 13));
        return t;
    }

    private static Text createCodeLine(String content) {
        Text t = new Text(content);
        t.setFill(Color.web(COLOR_CODE));
        t.setFont(Font.font("Consolas", 12));
        return t;
    }

    private static List<Node> parseInline(String line) {
        List<Node> nodes = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\*\\*(.+?)\\*\\*|`(.+?)`|\\*(.+?)\\*");
        Matcher matcher = pattern.matcher(line);

        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                nodes.add(createText(line.substring(lastEnd, matcher.start())));
            }

            if (matcher.group(1) != null) {
                Text t = new Text(matcher.group(1));
                t.setFill(Color.web(COLOR_BOLD));
                t.setFont(Font.font("Consolas", FontWeight.BOLD, 13));
                nodes.add(t);
            } else if (matcher.group(2) != null) {
                Text t = new Text(matcher.group(2));
                t.setFill(Color.web(COLOR_CODE));
                t.setFont(Font.font("Consolas", 12));
                nodes.add(t);
            } else if (matcher.group(3) != null) {
                Text t = new Text(matcher.group(3));
                t.setFill(Color.web(COLOR_TEXT));
                t.setFont(Font.font("Consolas", FontWeight.NORMAL, 13));
                t.setUnderline(true);
                nodes.add(t);
            }

            lastEnd = matcher.end();
        }

        if (lastEnd < line.length()) {
            nodes.add(createText(line.substring(lastEnd)));
        }

        if (nodes.isEmpty()) {
            nodes.add(createText(line));
        }

        return nodes;
    }
}

package com.speed.engramstudio.presentation.projects;

import com.speed.engramstudio.application.projects.GetProjectObservations;
import com.speed.engramstudio.application.projects.GetProjects;
import com.speed.engramstudio.domain.model.Observation;
import com.speed.engramstudio.domain.model.Project;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProjectsViewModel {

    private final GetProjects getProjects;
    private final GetProjectObservations getProjectObservations;

    private final ObservableList<Project> projects = FXCollections.observableArrayList();
    private final ObservableList<Observation> projectObservations = FXCollections.observableArrayList();
    private final ObjectProperty<Project> selectedProject = new SimpleObjectProperty<>();
    private final StringProperty statusMessage = new SimpleStringProperty("Ready");

    public ProjectsViewModel(GetProjects getProjects, GetProjectObservations getProjectObservations) {
        this.getProjects = getProjects;
        this.getProjectObservations = getProjectObservations;
    }

    public void load() {
        statusMessage.set("Loading projects...");
        getProjects.execute()
            .thenAccept(list -> Platform.runLater(() -> {
                projects.clear();
                projects.addAll(list);
                statusMessage.set("Loaded " + list.size() + " projects");
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> statusMessage.set("Error: " + throwable.getMessage()));
                return null;
            });
    }

    public void selectProject(Project project) {
        if (project == null) {
            projectObservations.clear();
            return;
        }
        selectedProject.set(project);
        statusMessage.set("Loading observations for: " + project.name() + "...");

        getProjectObservations.execute(project.name())
            .thenAccept(list -> Platform.runLater(() -> {
                projectObservations.clear();
                projectObservations.addAll(list);
                statusMessage.set(project.name() + ": " + list.size() + " observations");
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> statusMessage.set("Error: " + throwable.getMessage()));
                return null;
            });
    }

    public ObservableList<Project> projects() { return projects; }
    public ObservableList<Observation> projectObservations() { return projectObservations; }
    public ObjectProperty<Project> selectedProjectProperty() { return selectedProject; }
    public StringProperty statusMessageProperty() { return statusMessage; }
}

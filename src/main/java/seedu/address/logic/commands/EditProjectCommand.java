package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;

/**
 * Updates one existing project while retaining its stable ID and unspecified fields.
 */
public class EditProjectCommand extends Command {
    public static final String COMMAND_WORD = "edit-project";
    public static final String MESSAGE_USAGE = "Usage: edit-project CLIENT_INDEX PROJECT_ID [title/TITLE] "
            + "[deadline/DATE] [status/STATUS] [priority/PRIORITY]";
    public static final String MESSAGE_NO_FIELD = "Specify at least one project field to update.";
    public static final String MESSAGE_SAVE_FAILURE = "Project could not be saved. No changes were made.";
    public static final String MESSAGE_DUPLICATE = "Update would duplicate another project for this client.";

    private final Index clientIndex;
    private final int projectId;
    private final EditProjectDescriptor descriptor;

    /**
     * Creates an update for an existing project belonging to the displayed client.
     */
    public EditProjectCommand(Index clientIndex, int projectId, EditProjectDescriptor descriptor) {
        this.clientIndex = requireNonNull(clientIndex);
        checkArgument(projectId > 0, Project.MESSAGE_ID_CONSTRAINTS);
        this.projectId = projectId;
        this.descriptor = requireNonNull(descriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> clients = model.getFilteredPersonList();
        if (clientIndex.getZeroBased() >= clients.size()) {
            throw new CommandException("No client at index " + clientIndex.getOneBased() + ".");
        }
        Person client = clients.get(clientIndex.getZeroBased());
        Project original = client.getProjects().stream().filter(project -> project.getId() == projectId)
                .findFirst().orElseThrow(() -> new CommandException("No project with ID " + projectId
                        + " for " + client.getName() + "."));
        Project updated = descriptor.applyTo(original);
        assert updated.getId() == original.getId() : "Editing a project must preserve its ID.";
        if (updated.equals(original)) {
            return new CommandResult("No changes needed for project #" + projectId + ".");
        }
        if (client.getProjects().stream()
                .anyMatch(project -> project.getId() != projectId && project.isSameProject(updated))) {
            throw new CommandException(MESSAGE_DUPLICATE);
        }
        List<Project> projects = new ArrayList<>(client.getProjects());
        projects.set(projects.indexOf(original), updated);
        Person updatedClient = client.withProjects(projects, client.getNextProjectId());
        model.setPerson(client, updatedClient);
        return new CommandResult("Updated project #" + projectId + " for " + client.getName() + ": "
                + updated.getTitle() + " | Deadline: " + updated.getDeadline()
                + " | Status: " + updated.getStatus() + " | Priority: " + updated.getPriority(), updatedClient);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof EditProjectCommand command
                && clientIndex.equals(command.clientIndex) && projectId == command.projectId
                && descriptor.equals(command.descriptor));
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientIndex.getZeroBased(), projectId, descriptor);
    }

    /**
     * Immutable optional field values; null means that the existing field is retained.
     */
    public record EditProjectDescriptor(String title, LocalDate deadline, ProjectStatus status,
            ProjectPriority priority) {
        /**
         * Validates supplied fields without requiring values for unchanged fields.
         */
        public EditProjectDescriptor {
            checkArgument(title != null || deadline != null || status != null || priority != null, MESSAGE_NO_FIELD);
            if (title != null) {
                checkArgument(ProjectTitle.isValidTitle(title), ProjectTitle.MESSAGE_CONSTRAINTS);
                title = new ProjectTitle(title).value;
            }
            if (deadline != null) {
                checkArgument(deadline.getYear() >= 1 && deadline.getYear() <= 9999,
                        ProjectDeadline.MESSAGE_CONSTRAINTS);
            }
        }

        /**
         * Replaces supplied values and preserves both the project ID and omitted values.
         */
        public Project applyTo(Project original) {
            requireNonNull(original);
            return new Project(original.getId(), title == null ? original.getTitle() : new ProjectTitle(title),
                    deadline == null ? original.getDeadline() : new ProjectDeadline(deadline.toString()),
                    status == null ? original.getStatus() : status,
                    priority == null ? original.getPriority() : priority);
        }
    }
}

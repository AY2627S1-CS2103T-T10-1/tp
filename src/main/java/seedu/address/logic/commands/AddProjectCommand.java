package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;

/**
 * Adds a project to the client at the currently displayed index.
 */
public class AddProjectCommand extends Command {

    public static final String COMMAND_WORD = "add-project";
    public static final String MESSAGE_USAGE = "Usage: add-project CLIENT_INDEX title/TITLE deadline/DATE "
            + "[status/STATUS] [priority/PRIORITY]";
    public static final String MESSAGE_INVALID_INDEX = "Client index must be a displayed positive number.";
    public static final String MESSAGE_NEW_STATUS = "New project status must be not-started or in-progress.";
    public static final String MESSAGE_SUCCESS = "Added project #%1$d to %2$s.";
    public static final String MESSAGE_DUPLICATE_PROJECT = "This project already exists for %1$s.";
    public static final String MESSAGE_SAVE_FAILURE = "Project could not be saved. No changes were made.";
    public static final String MESSAGE_ID_EXHAUSTED = "No more project IDs are available for this client.";
    public static final String MESSAGE_OVERDUE = "Warning: This project is overdue.";

    private final Index clientIndex;
    private final ProjectTitle title;
    private final ProjectDeadline deadline;
    private final ProjectStatus status;
    private final ProjectPriority priority;
    private final Clock clock;

    /**
     * Creates a command using the system's local date for overdue warnings.
     */
    public AddProjectCommand(Index clientIndex, ProjectTitle title, ProjectDeadline deadline,
            ProjectStatus status, ProjectPriority priority) {
        this(clientIndex, title, deadline, status, priority, Clock.systemDefaultZone());
    }

    /**
     * Creates a command with an explicit clock for deterministic date-boundary tests.
     */
    AddProjectCommand(Index clientIndex, ProjectTitle title, ProjectDeadline deadline,
            ProjectStatus status, ProjectPriority priority, Clock clock) {
        requireAllNonNull(clientIndex, title, deadline, status, priority, clock);
        if (status == ProjectStatus.COMPLETED) {
            throw new IllegalArgumentException(MESSAGE_NEW_STATUS);
        }
        this.clientIndex = clientIndex;
        this.title = title;
        this.deadline = deadline;
        this.status = status;
        this.priority = priority;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedClients = model.getFilteredPersonList();
        if (clientIndex.getZeroBased() >= displayedClients.size()) {
            throw new CommandException("No client at index " + clientIndex.getOneBased() + ".");
        }

        Person client = displayedClients.get(clientIndex.getZeroBased());
        if (client.getNextProjectId() > Integer.MAX_VALUE) {
            throw new CommandException(MESSAGE_ID_EXHAUSTED);
        }
        Project project = new Project((int) client.getNextProjectId(), title, deadline, status, priority);
        if (client.getProjects().stream().anyMatch(existing -> existing.isSameProject(project))) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_PROJECT, client.getName()));
        }

        List<Project> projects = new ArrayList<>(client.getProjects());
        projects.add(project);
        Person updatedClient = client.withProjects(projects, client.getNextProjectId() + 1);
        model.setPerson(client, updatedClient);
        String feedback = String.format(MESSAGE_SUCCESS, project.getId(), client.getName());
        if (deadline.getDate().isBefore(LocalDate.now(clock))) {
            feedback += "\n" + MESSAGE_OVERDUE;
        }
        return new CommandResult(feedback, updatedClient);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof AddProjectCommand otherCommand
                && clientIndex.equals(otherCommand.clientIndex)
                && title.equals(otherCommand.title)
                && deadline.equals(otherCommand.deadline)
                && status == otherCommand.status
                && priority == otherCommand.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientIndex.getZeroBased(), title, deadline, status, priority);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("clientIndex", clientIndex).add("title", title)
                .add("deadline", deadline).add("status", status).add("priority", priority).toString();
    }
}

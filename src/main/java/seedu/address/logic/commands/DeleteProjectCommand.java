package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;

/** Deletes a project from the client at a displayed index. */
public class DeleteProjectCommand extends Command {
    public static final String COMMAND_WORD = "delete-project";
    public static final String MESSAGE_USAGE = "Usage: delete-project CLIENT_INDEX PROJECT_ID";
    public static final String MESSAGE_INVALID_INDEX = "Client index must be a displayed positive number.";
    public static final String MESSAGE_SAVE_FAILURE = "Project could not be deleted. No changes were made.";
    public static final String MESSAGE_SUCCESS = "Deleted project #%1$d from %2$s.";

    private final Index clientIndex;
    private final int projectId;

    /** Creates a command to delete the given client-scoped project ID. */
    public DeleteProjectCommand(Index clientIndex, int projectId) {
        this.clientIndex = requireNonNull(clientIndex);
        if (projectId <= 0) {
            throw new IllegalArgumentException("Project ID must be positive.");
        }
        this.projectId = projectId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedClients = model.getFilteredPersonList();
        if (clientIndex.getZeroBased() >= displayedClients.size()) {
            throw new CommandException(MESSAGE_INVALID_INDEX);
        }
        Person client = displayedClients.get(clientIndex.getZeroBased());
        int projectIndex = findProjectIndex(client.getProjects());
        if (projectIndex < 0) {
            throw new CommandException("No project with ID " + projectId + " exists for this client.");
        }

        List<Project> remainingProjects = new ArrayList<>(client.getProjects());
        remainingProjects.remove(projectIndex);
        Person updatedClient = client.withProjects(remainingProjects, client.getNextProjectId());
        model.setPerson(client, updatedClient);
        return new CommandResult(String.format(MESSAGE_SUCCESS, projectId, client.getName()));
    }

    private int findProjectIndex(List<Project> projects) {
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getId() == projectId) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof DeleteProjectCommand command
                && clientIndex.equals(command.clientIndex) && projectId == command.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientIndex.getZeroBased(), projectId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("clientIndex", clientIndex).add("projectId", projectId).toString();
    }
}

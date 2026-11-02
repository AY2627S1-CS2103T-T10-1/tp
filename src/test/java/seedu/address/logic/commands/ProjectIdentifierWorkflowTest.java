package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.ModelManager;
import seedu.address.model.project.ProjectStatus;
import seedu.address.testutil.PersonBuilder;

public class ProjectIdentifierWorkflowTest {
    @Test
    public void execute_formattedProjectId_canBeReusedAcrossLocales() throws Exception {
        Locale previous = Locale.getDefault(Locale.Category.FORMAT);
        try {
            for (String language : List.of("ar-EG", "fa-IR", "th-TH", "en-SG")) {
                Locale.setDefault(Locale.Category.FORMAT, Locale.forLanguageTag(language));
                for (int id : List.of(1, 12, Integer.MAX_VALUE)) {
                    ModelManager model = new ModelManager();
                    model.addPerson(new PersonBuilder().withName("Acme")
                            .withProjects(List.of(), id).build());
                    AddressBookParser parser = new AddressBookParser();
                    CommandResult added = parser.parseCommand(
                            "add-project 1 title/Task deadline/2099-12-31").execute(model);
                    assertEquals("Added project #" + id + " to Acme.", added.getFeedbackToUser());
                    String displayedId = added.getFeedbackToUser().split("#")[1].split(" ")[0];

                    parser.parseCommand("edit-project 0001 " + displayedId + " status/completed").execute(model);
                    assertEquals(ProjectStatus.COMPLETED,
                            model.getFilteredPersonList().getFirst().getProjects().getFirst().getStatus());
                    CommandResult deleted = parser.parseCommand("delete-project 0001 " + displayedId).execute(model);
                    assertEquals("Deleted project #" + id + " from Acme.", deleted.getFeedbackToUser());
                    assertTrue(model.getFilteredPersonList().getFirst().getProjects().isEmpty());
                }
            }
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, previous);
        }
    }
}

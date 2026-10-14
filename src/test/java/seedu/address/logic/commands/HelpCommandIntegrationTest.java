package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Executes the examples users can copy from Help through the application command path.
 */
public class HelpCommandIntegrationTest {
    @TempDir
    Path testFolder;

    @Test
    public void execute_helpExamples_showsCurrentInstructions() throws Exception {
        for (String example : getExamples("help")) {
            Fixture fixture = createFixture();
            CommandResult result = fixture.logic().execute(example);
            assertTrue(result.isShowHelp());
            assertFalse(result.isExit());
            assertTrue(result.getFeedbackToUser().contains(example.equals("help")
                    ? "ClientDesk commands:" : "Format: " + example.substring(5)));
        }
    }

    @Test
    public void execute_addClientExamples_createsClientsWithContactFieldsAndTags() throws Exception {
        List<Person> expectedClients = List.of(
                new PersonBuilder().withName("Acme Studio").withPhone("6581234567")
                        .withEmail("hello@acme.sg").withAddress("Not provided").build(),
                new PersonBuilder().withName("Northstar Labs").withPhone("91234567")
                        .withEmail("alex@northstar.io").withAddress("Not provided")
                        .withTags("startup", "web-design").build());
        List<String> examples = getExamples("add-client");
        assertEquals(2, examples.size());
        for (int i = 0; i < examples.size(); i++) {
            Fixture fixture = createFixture();
            fixture.logic().execute(examples.get(i));
            assertEquals(13, fixture.model().getAddressBook().getPersonList().size());
            assertTrue(fixture.model().getAddressBook().getPersonList().contains(expectedClients.get(i)));
            assertEquals(fixture.model().getAddressBook(), fixture.storage().readAddressBook().orElseThrow());
        }
    }

    @Test
    public void execute_editClientExamples_updatesOnlyEmail() throws Exception {
        List<String> examples = getExamples("edit-client");
        assertEquals(2, examples.size());
        for (int i = 0; i < examples.size(); i++) {
            Fixture fixture = createFixture();
            Person before = fixture.model().getFilteredPersonList().get(i);
            CommandResult result = fixture.logic().execute(examples.get(i));
            Person expected = new PersonBuilder(before)
                    .withEmail(i == 0 ? "new@acme.sg" : "new@northstar.io").build();
            assertEquals(expected, fixture.model().getFilteredPersonList().get(i));
            assertEquals(i == 0 ? "Updated client: Acme Fixture." : "Updated client: Client 02.",
                    result.getFeedbackToUser());
            assertEquals(fixture.model().getAddressBook(), fixture.storage().readAddressBook().orElseThrow());
        }
    }

    @Test
    public void execute_remarkExamples_setsAndClearsRemarkAtFilteredIndex() throws Exception {
        List<String> examples = getExamples("remark");
        assertEquals(2, examples.size());
        for (int i = 0; i < examples.size(); i++) {
            Fixture fixture = createFixture();
            fixture.model().updateFilteredPersonList(client -> client.getName().fullName.equals("Northstar Fixture"));
            Person before = fixture.model().getFilteredPersonList().getFirst();
            CommandResult result = fixture.logic().execute(examples.get(i));
            Person expected = new PersonBuilder(before).withRemark(i == 0 ? "Prefers email contact." : "").build();
            assertEquals(expected, fixture.model().getFilteredPersonList().getLast());
            assertEquals(12, fixture.model().getFilteredPersonList().size());
            assertTrue(result.getFeedbackToUser().startsWith(i == 0
                    ? "Added remark to person: Northstar Fixture;" : "Removed remark from person: Northstar Fixture;"));
            assertEquals(fixture.model().getAddressBook(), fixture.storage().readAddressBook().orElseThrow());
        }
    }

    @Test
    public void execute_projectExamples_createsAndUpdatesSavedProjects() throws Exception {
        Fixture added = createFixture();
        added.logic().execute(getExamples("add-project").getFirst());
        Person addedClient = added.model().getFilteredPersonList().getFirst();
        assertEquals(4, addedClient.getNextProjectId());
        assertEquals(new Project(3, new ProjectTitle("Website redesign"), new ProjectDeadline("2026-10-15"),
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM), addedClient.getProjects().getLast());
        assertEquals(added.model().getAddressBook(), added.storage().readAddressBook().orElseThrow());

        Fixture edited = createFixture();
        edited.logic().execute(getExamples("edit-project").getFirst());
        Person editedClient = edited.model().getFilteredPersonList().getFirst();
        assertEquals(3, editedClient.getNextProjectId());
        assertEquals(new Project(2, new ProjectTitle("Logo concept"), new ProjectDeadline("2027-02-01"),
                ProjectStatus.COMPLETED, ProjectPriority.HIGH), editedClient.getProjects().getFirst());
        assertEquals(edited.model().getAddressBook(), edited.storage().readAddressBook().orElseThrow());
    }

    @Test
    public void execute_deleteProjectExample_removesProjectAndPreservesOtherClientData() throws Exception {
        Fixture fixture = createFixture();
        List<Person> displayedClients = fixture.model().getFilteredPersonList();
        Person expectedClient = new PersonBuilder(displayedClients.getFirst()).withProjects(List.of(), 3).build();
        List<Person> otherClients = List.copyOf(displayedClients.subList(1, displayedClients.size()));

        CommandResult result = fixture.logic().execute(getExamples("delete-project").getFirst());

        assertEquals("Deleted project #2 from Acme Fixture.", result.getFeedbackToUser());
        assertEquals(expectedClient, fixture.model().getFilteredPersonList().getFirst());
        List<Person> remainingClients = fixture.model().getFilteredPersonList();
        assertEquals(otherClients, remainingClients.subList(1, remainingClients.size()));
        assertEquals(fixture.model().getAddressBook(), fixture.storage().readAddressBook().orElseThrow());
    }

    @Test
    public void execute_listExamples_restoresSortedClientDisplay() throws Exception {
        for (String command : List.of("list", "list-clients")) {
            Fixture fixture = createFixture();
            fixture.model().updateFilteredPersonList(client -> client.getName().fullName.equals("Northstar Fixture"));
            CommandResult result = fixture.logic().execute(getExamples(command).getFirst());
            assertEquals(command.equals("list") ? "Listed all persons." : "Listed all clients.",
                    result.getFeedbackToUser());
            assertEquals(List.of("Acme Fixture", "Client 02", "Client 03", "Client 04", "Client 05", "Client 06",
                    "Client 07", "Client 08", "Client 09", "Client 10", "Client 11", "Northstar Fixture"),
                    fixture.model().getFilteredPersonList().stream().map(client -> client.getName().fullName).toList());
        }
    }

    @Test
    public void execute_viewExamples_displaysContactFieldsRemarkTagsAndProjects() throws Exception {
        List<String> examples = getExamples("view");
        assertEquals(2, examples.size());
        List<String> expected = List.of(
                "Name: Acme Fixture\nPhone: 88123456\nEmail: acme@example.com\nAddress: 1 Test Lane"
                        + "\nRemark: Initial remark\nTags: client\nProjects:\n#2 | Logo concept | Deadline: 2027-02-01"
                        + " | Status: not-started | Priority: medium",
                "Name: Northstar Fixture\nPhone: 90000012\nEmail: client12@example.com\nAddress: 1 Test Lane"
                        + "\nRemark: Initial remark\nTags: startup, web-design");
        for (int i = 0; i < examples.size(); i++) {
            Fixture fixture = createFixture();
            assertEquals(expected.get(i), fixture.logic().execute(examples.get(i)).getFeedbackToUser());
        }
    }

    @Test
    public void execute_findExamples_displaysMatchingClients() throws Exception {
        List<String> examples = getExamples("find");
        assertEquals(3, examples.size());
        for (int i = 0; i < examples.size(); i++) {
            Fixture fixture = createFixture();
            assertEquals("Found 1 matching clients.", fixture.logic().execute(examples.get(i)).getFeedbackToUser());
            assertEquals(List.of(i == 1 ? "Northstar Fixture" : "Acme Fixture"), fixture.model()
                    .getFilteredPersonList().stream().map(client -> client.getName().fullName).toList());
        }
    }

    @Test
    public void execute_deleteClientExample_removesDisplayedClient() throws Exception {
        Fixture fixture = createFixture();
        fixture.logic().execute(getExamples("delete-client").getFirst());
        assertEquals(List.of("Acme Fixture", "Client 02", "Client 04", "Client 05", "Client 06", "Client 07",
                "Client 08", "Client 09", "Client 10", "Client 11", "Northstar Fixture"), fixture.model()
                .getFilteredPersonList().stream().map(client -> client.getName().fullName).toList());
        assertEquals(fixture.model().getAddressBook(), fixture.storage().readAddressBook().orElseThrow());
    }

    private List<String> getExamples(String command) throws Exception {
        String instructions = new AddressBookParser().parseCommand("help " + command)
                .execute(new ModelManager()).getFeedbackToUser();
        return List.of(instructions.substring(instructions.indexOf("\nExamples: ") + 11).split("; "));
    }

    private Fixture createFixture() throws Exception {
        ModelManager model = new ModelManager();
        for (int i = 12; i >= 2; i--) {
            model.addPerson(new PersonBuilder().withName(i == 12 ? "Northstar Fixture" : "Client %02d".formatted(i))
                    .withPhone("900000%02d".formatted(i)).withEmail("client" + i + "@example.com")
                    .withAddress("1 Test Lane").withRemark("Initial remark")
                    .withTags(i == 12 ? new String[] {"startup", "web-design"} : new String[] {"client"}).build());
        }
        Person client = new PersonBuilder().withName("Acme Fixture").withPhone("88123456")
                .withEmail("acme@example.com").withAddress("1 Test Lane").withRemark("Initial remark")
                .withTags("client").build();
        Project project = new Project(2, new ProjectTitle("Logo concept"), new ProjectDeadline("2027-02-01"),
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM);
        model.addPerson(new Person(client.getName(), client.getPhone(), client.getEmail(), client.getAddress(),
                client.getRemark(), client.getTags(), List.of(project), 3));
        Path directory = Files.createTempDirectory(testFolder, "example-");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("clients.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        return new Fixture(model, logic, storage);
    }

    /**
     * Groups the model, command logic and temporary storage for one Help example.
     */
    private record Fixture(ModelManager model, LogicManager logic, JsonAddressBookStorage storage) {
    }
}

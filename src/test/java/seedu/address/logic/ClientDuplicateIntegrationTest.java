package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks Unicode duplicate validation without changing the validity of v1.4 stored data.
 */
public class ClientDuplicateIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("clients.json"));
        logic = createLogic();
    }

    @Test
    public void execute_canonicalNameDuplicates_rejectsNewAddsAndEditsWithoutChangingData() throws Exception {
        logic.execute("add-client n/José p/98765432 e/first@example.com");
        logic.execute("add-client n/Other p/87654321 e/second@example.com");
        logic.execute("find Other");
        assertRejectedWithoutChanges("add-client n/Jose\u0301 p/98765432 e/third@example.com",
                "This client already exists: Jose\u0301.");
        assertRejectedWithoutChanges("add n/Jose\u0301 p/98765432 e/third@example.com a/Office",
                AddCommand.MESSAGE_DUPLICATE_PERSON);
        assertRejectedWithoutChanges("edit 1 n/Jose\u0301 p/98765432", EditCommand.MESSAGE_DUPLICATE_PERSON);

        logic.execute("add-client n/Jose\u0301 p/81234567 e/third@example.com");
        assertEquals("Jose\u0301", storage.readAddressBook().orElseThrow()
                .getPersonList().getLast().getName().fullName);
    }

    @Test
    public void execute_legacyCanonicalPair_preservesSpellingAndProjectsAcrossEditsAndReload() throws Exception {
        Person first = new PersonBuilder().withName("José").withPhone("98765432")
                .withEmail("first@example.com").build();
        Person second = new PersonBuilder(first).withName("Jose\u0301").withEmail("second@example.com").build();
        model.addPerson(first);
        model.addPerson(second);
        storage.saveAddressBook(model.getAddressBook());
        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = createLogic();
        assertEquals(List.of(first, second), model.getAddressBook().getPersonList());

        logic.execute("edit " + indexOfEmail("first@example.com") + " a/New office");
        logic.execute("remark " + indexOfEmail("second@example.com") + " r/Keep this record");
        logic.execute("add-project " + indexOfEmail("second@example.com") + " title/Website deadline/2099-12-01");
        logic.execute("edit-client " + indexOfEmail("second@example.com") + " e/updated@example.com");
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        List<Person> saved = storage.readAddressBook().orElseThrow().getPersonList();
        assertEquals(List.of("José", "Jose\u0301"), saved.stream().map(person -> person.getName().fullName).toList());
        assertEquals("Keep this record", saved.get(1).getRemark().value);
        assertEquals(1, saved.get(1).getProjects().size());
        assertEquals(2, saved.get(1).getNextProjectId());
        assertRejectedWithoutChanges("edit " + indexOfEmail("first@example.com") + " n/Jose\u0301",
                EditCommand.MESSAGE_DUPLICATE_PERSON);
        assertRejectedWithoutChanges("add-client n/JOSE\u0301 p/98765432 e/third@example.com",
                "This client already exists: JOSE\u0301.");
    }

    @Test
    public void execute_caseEquivalentAdd_rejectsHiddenDuplicateAndPreservesData() throws Exception {
        logic.execute("add-client n/Işık p/81234567 e/first@example.com");
        model.updateFilteredPersonList(person -> false);
        assertRejectedWithoutChanges("add-client n/IŞIK p/81234567 e/second@example.com",
                "This client already exists: IŞIK.");
        assertRejectedWithoutChanges("add n/IŞIK p/81234567 e/second@example.com a/Office",
                AddCommand.MESSAGE_DUPLICATE_PERSON);

        logic.execute("add-client n/IŞIK p/82345678 e/second@example.com");
        assertEquals(2, storage.readAddressBook().orElseThrow().getPersonList().size());
        assertEquals("IŞIK", model.getAddressBook().getPersonList().get(1).getName().fullName);
        assertRejectedWithoutChanges("add-client n/Another p/83456789 e/FIRST@EXAMPLE.COM",
                "This client already exists: Another.");
    }

    @Test
    public void execute_editIntoHiddenCaseEquivalentClient_rejectsWithoutChangingData() throws Exception {
        logic.execute("add-client n/Işık p/81234567 e/first@example.com");
        logic.execute("add-client n/Other p/82345678 e/second@example.com");
        logic.execute("find Other");
        assertRejectedWithoutChanges("edit 1 n/IŞIK p/81234567", EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_legacyCasePair_remainsReadableEditableAndDeletable() throws Exception {
        AddressBook legacy = new AddressBook();
        Person first = new PersonBuilder().withName("Işık").withPhone("81234567")
                .withEmail("first@example.com").build();
        Person second = new PersonBuilder(first).withName("IŞIK").withEmail("second@example.com").build();
        legacy.addPerson(first);
        legacy.addPerson(second);
        storage.saveAddressBook(legacy);
        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = createLogic();

        assertEquals(List.of(first, second), model.getAddressBook().getPersonList());
        assertRejectedWithoutChanges("add-client n/ışık p/81234567 e/third@example.com",
                "This client already exists: ışık.");
        logic.execute("add-client n/ΟΣ p/82345678 e/third@example.com");
        logic.execute("find first@example.com");
        assertRejectedWithoutChanges("edit 1 n/ος p/82345678", EditCommand.MESSAGE_DUPLICATE_PERSON);
        logic.execute("list-clients");
        logic.execute("delete-client " + indexOfEmail("third@example.com"));
        logic.execute("edit " + indexOfEmail("first@example.com") + " a/New office t/client");
        logic.execute("remark " + indexOfEmail("first@example.com") + " r/Call Monday");
        logic.execute("edit-client " + indexOfEmail("first@example.com") + " e/updated@example.com");
        logic.execute("add-project " + indexOfEmail("updated@example.com")
                + " title/Website deadline/2026-12-01");
        logic.execute("edit-project " + indexOfEmail("updated@example.com") + " 1 status/in-progress");
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertEquals(2, storage.readAddressBook().orElseThrow().getPersonList().size());
        assertRejectedWithoutChanges("edit " + indexOfEmail("updated@example.com")
                + " e/SECOND@EXAMPLE.COM", EditCommand.MESSAGE_DUPLICATE_PERSON);
        assertRejectedWithoutChanges("edit " + indexOfEmail("updated@example.com")
                + " n/IŞIK", EditCommand.MESSAGE_DUPLICATE_PERSON);

        logic.execute("edit " + indexOfEmail("second@example.com") + " p/83456789");
        assertEquals("83456789", storage.readAddressBook().orElseThrow().getPersonList().get(1).getPhone().value);
        logic.execute("delete-client " + indexOfEmail("second@example.com"));
        assertEquals(1, storage.readAddressBook().orElseThrow().getPersonList().size());
        Person remaining = storage.readAddressBook().orElseThrow().getPersonList().get(0);
        assertEquals("Işık", remaining.getName().fullName);
        assertEquals("Call Monday", remaining.getRemark().value);
        assertEquals(1, remaining.getProjects().size());
        assertEquals(2, remaining.getNextProjectId());
    }

    private Logic createLogic() {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }

    private int indexOfEmail(String email) {
        Person person = model.getFilteredPersonList().stream()
                .filter(client -> client.getEmail().value.equals(email)).findFirst().orElseThrow();
        return model.getFilteredPersonList().indexOf(person) + 1;
    }

    private void assertRejectedWithoutChanges(String input, String message) throws Exception {
        AddressBook original = new AddressBook(model.getAddressBook());
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        String saved = Files.readString(storage.getAddressBookFilePath());
        assertEquals(message, assertThrows(CommandException.class, () -> logic.execute(input)).getMessage());
        assertEquals(original, model.getAddressBook());
        assertEquals(displayed, model.getFilteredPersonList());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
    }
}

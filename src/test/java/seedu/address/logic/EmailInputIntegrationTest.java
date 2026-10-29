package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Email;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class EmailInputIntegrationTest {
    @TempDir
    public Path testFolder;

    @Test
    public void execute_unicodeSpacesInEmail_rejectsAllInputPathsWithoutSaving() {
        ModelManager model = new ModelManager();
        model.addPerson(ALICE);
        Path dataFile = testFolder.resolve("clients.json");
        Logic logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
        for (String space : List.of("\u00a0", "\u2003", "\u202f", "\u3000")) {
            String email = "alice" + space + "smith@example.com";
            for (String command : List.of("add-client n/Space Test p/91234567 e/" + email,
                    "add n/Space Test p/91234567 e/" + email + " a/Somewhere",
                    "edit-client 1 e/" + email, "edit 1 e/" + email)) {
                ParseException error = assertThrows(ParseException.class, () -> logic.execute(command));
                assertEquals(Email.MESSAGE_CONSTRAINTS, error.getMessage());
                assertEquals(List.of(ALICE), model.getFilteredPersonList());
                assertFalse(Files.exists(dataFile));
            }
        }
    }

    @Test
    public void readAndSave_legacyUnicodeSpaceEmail_preservesRecord() throws Exception {
        Person legacyClient = new PersonBuilder(ALICE).withEmail("alice\u00a0smith@example.com").build();
        AddressBook original = new AddressBook();
        original.addPerson(legacyClient);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("legacy.json"));
        storage.saveAddressBook(original);

        AddressBook loaded = new AddressBook(storage.readAddressBook().orElseThrow());
        assertEquals(original, loaded);
        storage.saveAddressBook(loaded);
        assertEquals(original, storage.readAddressBook().orElseThrow());
    }
}

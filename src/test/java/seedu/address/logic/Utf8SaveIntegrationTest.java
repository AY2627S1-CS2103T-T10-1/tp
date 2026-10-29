package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.charset.CharacterCodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class Utf8SaveIntegrationTest {
    @TempDir
    public Path testFolder;

    @Test
    public void saveMalformedStoredEmail_failsWithoutChangingModelOrFile() throws Exception {
        Path dataFile = testFolder.resolve("clients.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);
        Person validClient = new PersonBuilder(ALICE).withEmail("alice@example.com").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(validClient);
        storage.saveAddressBook(addressBook);

        String malformedJson = Files.readString(dataFile).replace("alice@example.com", "\\ud800@example.com");
        Files.writeString(dataFile, malformedJson);
        byte[] originalFile = Files.readAllBytes(dataFile);
        ModelManager model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
        AddressBook originalModel = new AddressBook(model.getAddressBook());
        List<Person> originalDisplay = List.copyOf(model.getFilteredPersonList());
        assertEquals(0xD800, model.getAddressBook().getPersonList().getFirst().getEmail().value.codePointAt(0));

        CommandException saveFailure = assertThrows(CommandException.class, () -> logic.execute("remark 1 r/Updated"));
        assertInstanceOf(CharacterCodingException.class, saveFailure.getCause());

        assertEquals(originalModel, model.getAddressBook());
        assertEquals(originalDisplay, model.getFilteredPersonList());
        assertArrayEquals(originalFile, Files.readAllBytes(dataFile));
        assertEquals(0xD800, storage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getEmail().value.codePointAt(0));

        logic.execute("edit-client 1 e/alice-fixed@example.com");
        assertEquals("alice-fixed@example.com", storage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getEmail().value);
    }

    @Test
    public void saveValidSupplementaryUnicode_roundTripsWithoutReplacement() throws Exception {
        Path dataFile = testFolder.resolve("unicode.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);
        ModelManager model = new ModelManager();
        model.addPerson(ALICE);
        Logic logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
        String emoji = "\uD83D\uDE00";

        logic.execute("remark 1 r/Call " + emoji);

        assertEquals("Call " + emoji, storage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getRemark().value);
    }
}

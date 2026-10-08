package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class AddClientCommandTest {

    private final Person client = new PersonBuilder().withName("Acme Studio").withPhone("6591234567")
            .withEmail("bob@acme.com").withAddress(AddClientCommand.DEFAULT_ADDRESS).withTags().build();

    @Test
    public void execute_newClient_addsAndDisplaysClient() {
        Model model = new ModelManager();
        model.updateFilteredPersonList(person -> false);
        Model expectedModel = new ModelManager();
        expectedModel.addPerson(client);
        assertCommandSuccess(new AddClientCommand(client), model,
                new CommandResult("Added client: Acme Studio.", client), expectedModel);
        assertEquals(client, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_duplicateContactInHiddenList_failsWithoutChangingData() {
        Model model = new ModelManager();
        model.addPerson(client);
        model.updateFilteredPersonList(person -> false);

        Person duplicatePhone = new PersonBuilder(client)
                .withEmail("other@acme.com").build();
        assertCommandFailure(new AddClientCommand(duplicatePhone), model,
                String.format(AddClientCommand.MESSAGE_DUPLICATE_CLIENT, duplicatePhone.getName().fullName));

        Person duplicateEmail = new PersonBuilder(client).withName("Other Studio")
                .withPhone("6587654321").withEmail("BOB@ACME.COM").build();
        assertCommandFailure(new AddClientCommand(duplicateEmail), model,
                String.format(AddClientCommand.MESSAGE_DUPLICATE_CLIENT, duplicateEmail.getName().fullName));
    }

    @Test
    public void execute_sameNameWithDistinctContacts_success() {
        Model model = new ModelManager();
        model.addPerson(client);
        Person duplicateName = new PersonBuilder(client).withPhone("6587654321")
                .withEmail("other@acme.com").build();
        Model expectedModel = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        expectedModel.addPerson(duplicateName);
        assertCommandSuccess(new AddClientCommand(duplicateName), model,
                new CommandResult("Added client: Acme Studio.", duplicateName), expectedModel);
    }

    @Test
    public void execute_distinctClient_addsToExistingData() {
        Model model = new ModelManager();
        model.addPerson(client);
        Person otherClient = new PersonBuilder().withName("Beacon Design").build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(otherClient);
        assertCommandSuccess(new AddClientCommand(otherClient), model,
                new CommandResult("Added client: Beacon Design.", otherClient), expectedModel);
    }

    @Test
    public void constructor_nullClient_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddClientCommand(null));
    }

    @Test
    public void execute_samePhoneWithDifferentNameAndEmail_success() {
        Model model = new ModelManager();
        model.addPerson(client);
        Person sharedPhone = new PersonBuilder(client).withName("Other Studio").withEmail("other@acme.com").build();
        Model expected = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        expected.addPerson(sharedPhone);
        assertCommandSuccess(new AddClientCommand(sharedPhone), model,
                new CommandResult("Added client: Other Studio.", sharedPhone), expected);
    }

    @Test
    public void execute_normalizedNameAndPhoneDuplicate_rejected() {
        Model model = new ModelManager();
        model.addPerson(client);
        Person normalizedDuplicate = new PersonBuilder(client).withName("acme   STUDIO")
                .withEmail("other@acme.com").build();
        assertCommandFailure(new AddClientCommand(normalizedDuplicate), model,
                "This client already exists: acme   STUDIO.");
    }

    @Test
    public void equals_comparesClientValuesAndCommandType() {
        AddClientCommand command = new AddClientCommand(client);
        assertTrue(command.equals(command));
        assertTrue(command.equals(new AddClientCommand(new PersonBuilder(client).build())));
        assertFalse(command.equals(new AddClientCommand(new PersonBuilder(client).withPhone("87654321").build())));
        assertFalse(command.equals(new AddCommand(client)));
        assertFalse(command.equals(null));
    }

    @Test
    public void toString_includesClientForDiagnostics() {
        assertEquals(AddClientCommand.class.getCanonicalName() + "{toAdd=" + client + "}",
                new AddClientCommand(client).toString());
    }
}

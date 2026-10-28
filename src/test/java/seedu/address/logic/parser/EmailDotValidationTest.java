package seedu.address.logic.parser;

import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;

/** Verifies that every email entry command enforces the new-input dot rule. */
public class EmailDotValidationTest {
    @Test
    public void parse_invalidUnquotedDots_rejectedByAllEmailCommands() {
        AddressBookParser parser = new AddressBookParser();
        for (String email : java.util.List.of(".alice@example.com", "alice.@example.com", "a..b@example.com")) {
            for (String command : java.util.List.of(
                    "add-client n/Alice p/91234567 e/" + email,
                    "add n/Alice p/91234567 a/Somewhere e/" + email,
                    "edit 1 e/" + email, "edit-client 1 e/" + email)) {
                assertThrows(ParseException.class, () -> parser.parseCommand(command));
            }
        }
    }
}

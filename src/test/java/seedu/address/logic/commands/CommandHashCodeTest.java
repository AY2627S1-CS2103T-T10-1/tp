package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class CommandHashCodeTest {
    @Test
    public void hashCode_equalAddClients_supportsHashLookup() throws ParseException {
        assertCommandHashLookup("add-client n/Alice p/91234567 e/alice@example.com");
    }

    @Test
    public void hashCode_equalAdds_supportsHashLookup() throws ParseException {
        assertCommandHashLookup("add n/Alice p/91234567 e/alice@example.com a/Test address");
    }

    @Test
    public void hashCode_equalDeleteClients_supportsHashLookup() throws ParseException {
        assertCommandHashLookup("delete-client 1");
    }

    @Test
    public void hashCode_equalDeletes_supportsHashLookup() throws ParseException {
        assertCommandHashLookup("delete 1");
    }

    @Test
    public void hashCode_equalEdits_supportsHashLookup() throws ParseException {
        assertCommandHashLookup("edit 1 n/Alice t/friend t/client");
    }

    @Test
    public void hashCode_equalFinds_supportsHashLookup() throws ParseException {
        assertCommandHashLookup("find Alice client");
    }

    @Test
    public void hashCode_equalRemarks_supportsHashLookup() throws ParseException {
        assertCommandHashLookup("remark 1 r/Follow up tomorrow");
    }

    @Test
    public void hashCode_equalDescriptorsWithDifferentTagOrder_supportsHashLookup() {
        EditPersonDescriptor first = new EditPersonDescriptorBuilder().withName("Alice")
                .withTags("friend", "client").build();
        EditPersonDescriptor second = new EditPersonDescriptorBuilder().withName("Alice")
                .withTags("client", "friend").build();
        assertHashLookup(first, second);
    }

    @Test
    public void hashCode_emptyDescriptors_supportsHashLookup() {
        assertHashLookup(new EditPersonDescriptor(), new EditPersonDescriptor());
    }

    private void assertCommandHashLookup(String input) throws ParseException {
        AddressBookParser parser = new AddressBookParser();
        assertHashLookup(parser.parseCommand(input), parser.parseCommand(input));
    }

    private void assertHashLookup(Object first, Object second) {
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        Set<Object> values = new HashSet<>();
        values.add(first);
        assertTrue(values.contains(second));
        values.add(second);
        assertEquals(1, values.size());
        Map<Object, String> results = new HashMap<>();
        results.put(first, "saved");
        assertEquals("saved", results.get(second));
    }
}

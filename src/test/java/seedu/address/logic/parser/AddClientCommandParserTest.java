package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddClientCommand;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class AddClientCommandParserTest {

    private final AddClientCommandParser parser = new AddClientCommandParser();
    private final Person client = new PersonBuilder().withName("Acme Studio").withPhone("6591234567")
            .withEmail("Bob@acme.com").withAddress(AddClientCommand.DEFAULT_ADDRESS).withTags().build();

    @Test
    public void parse_validFields_normalizesPhoneAndPreservesValues() {
        assertParseSuccess(parser, " n/Acme Studio p/+65 9123-4567 e/Bob@acme.com",
                new AddClientCommand(client));
        assertParseSuccess(parser, "\tE/Bob@acme.com\tP/6591234567\tN/Acme Studio  ",
                new AddClientCommand(client));
        assertParseSuccess(parser, "n/Acme Studio p/6591234567 e/Bob@acme.com", new AddClientCommand(client));
    }

    @Test
    public void parse_missingFields_reportsMissingField() {
        assertParseFailure(parser, " p/91234567 e/bob@acme.com",
                AddClientCommand.MESSAGE_FORMAT);
        assertParseFailure(parser, " n/Acme e/bob@acme.com",
                AddClientCommand.MESSAGE_FORMAT);
        assertParseFailure(parser, " n/Acme p/91234567",
                AddClientCommand.MESSAGE_FORMAT);
    }

    @Test
    public void parse_repeatedPrefixes_rejectsInsteadOfOverwriting() {
        String validArgs = " n/Acme p/91234567 e/bob@acme.com";
        assertParseFailure(parser, validArgs + " N/Other", Messages.getErrorMessageForDuplicatePrefixes(
                CliSyntax.PREFIX_NAME));
        assertParseFailure(parser, validArgs + " p/87654321", Messages.getErrorMessageForDuplicatePrefixes(
                CliSyntax.PREFIX_PHONE));
        assertParseFailure(parser, validArgs + " e/other@acme.com", Messages.getErrorMessageForDuplicatePrefixes(
                CliSyntax.PREFIX_EMAIL));
    }

    @Test
    public void parse_unknownPrefixOrPreamble_rejectsExtraArguments() {
        assertParseFailure(parser, " n/Acme p/91234567 e/bob@acme.com a/address",
                "Unknown client parameter: a/.\n" + AddClientCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " n/Acme x/extra p/91234567 e/bob@acme.com",
                "Unknown client parameter: x/.\n" + AddClientCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " extra n/Acme p/91234567 e/bob@acme.com",
                AddClientCommand.MESSAGE_FORMAT);
    }

    @Test
    public void parse_invalidNameOrEmail_reportsFieldConstraint() {
        assertParseFailure(parser, " n/ p/91234567 e/bob@acme.com", Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " n/Acme p/91234567 e/invalid", Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " n/Acme p/91234567 e/", Email.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidPhone_reportsPhoneConstraint() {
        for (String phone : new String[] {"", "123456", "1234567890123456", "9123abc4567",
            "123.456", "+()-", "65+91234567"}) {
            assertParseFailure(parser, " n/Acme p/" + phone + " e/bob@acme.com",
                    AddClientCommandParser.MESSAGE_PHONE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_phoneLengthBoundaries_success() {
        for (String phone : new String[] {"1234567", "123456789012345"}) {
            Person expected = new PersonBuilder(client).withPhone(phone).build();
            assertParseSuccess(parser, " n/Acme Studio p/" + phone + " e/Bob@acme.com",
                    new AddClientCommand(expected));
        }
    }

    @Test
    public void parse_businessNamesAndNameLengthBounds_followSpec() {
        for (String name : new String[] {"O'Neil & Co. (SG)", "Northstar-Labs", "A", "A".repeat(80)}) {
            Person expected = new PersonBuilder(client).withName(name).build();
            assertParseSuccess(parser, " n/" + name + " p/6591234567 e/Bob@acme.com",
                    new AddClientCommand(expected));
        }
        for (String name : new String[] {"A".repeat(81), "() & . -", "Studio*"}) {
            assertParseFailure(parser, " n/" + name + " p/6591234567 e/Bob@acme.com", Name.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_repeatedAndHyphenatedTags_storesUniqueTags() {
        Person expected = new PersonBuilder(client).withTags("startup", "web-design").build();
        assertParseSuccess(parser, " n/Acme Studio p/6591234567 e/Bob@acme.com"
                + " t/startup t/web-design t/startup", new AddClientCommand(expected));
    }

    @Test
    public void parse_invalidTags_reportsConstraint() {
        for (String tag : new String[] {"", "Startup", "-startup", "startup-", "web design", "a".repeat(31)}) {
            assertParseFailure(parser, " n/Acme p/91234567 e/bob@acme.com t/" + tag,
                    Tag.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_emailRulesAndLengthBounds_followSpec() {
        String maximumEmail = "a".repeat(242) + "@example.com";
        Person expected = new PersonBuilder(client).withEmail(maximumEmail).build();
        assertParseSuccess(parser, " n/Acme Studio p/6591234567 e/" + maximumEmail, new AddClientCommand(expected));
        for (String email : new String[] {"bob@localhost", "bob@@acme.com", "bob@-acme.com", "bob@acme-.com",
            "bob @acme.com", "a" + maximumEmail}) {
            assertParseFailure(parser, " n/Acme p/91234567 e/" + email, Email.MESSAGE_CONSTRAINTS);
        }
    }
}

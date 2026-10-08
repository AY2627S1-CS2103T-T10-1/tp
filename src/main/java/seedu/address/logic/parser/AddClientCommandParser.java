package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.AddClientCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Remark;
import seedu.address.model.tag.Tag;

/**
 * Parses arguments for the first client creation increment.
 */
public class AddClientCommandParser implements Parser<AddClientCommand> {

    public static final String MESSAGE_PHONE_CONSTRAINTS =
            "Phone must contain 7–15 digits and may use a leading +, spaces, or hyphens.";

    private static final Pattern PARAMETER_PREFIX = Pattern.compile("(?<!\\S)([^\\s/]+/)");
    private static final Set<String> SUPPORTED_PREFIXES = Set.of("n/", "p/", "e/", "t/");

    @Override
    public AddClientCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(normalizePrefixes(args),
                PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_TAG);

        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException(AddClientCommand.MESSAGE_FORMAT);
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL);

        Name name = ParserUtil.parseName(requiredValue(arguments, PREFIX_NAME));
        Phone phone = parseClientPhone(requiredValue(arguments, PREFIX_PHONE));
        Email email = ParserUtil.parseEmail(requiredValue(arguments, PREFIX_EMAIL));
        Set<Tag> tags = new HashSet<>();
        for (String value : arguments.getAllValues(PREFIX_TAG)) {
            if (!value.matches("[a-z0-9]+(?:-+[a-z0-9]+)*") || value.length() > 30) {
                throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
            }
            tags.add(new Tag(value));
        }
        Person client = new Person(name, phone, email, new Address(AddClientCommand.DEFAULT_ADDRESS),
                new Remark(""), tags);
        return new AddClientCommand(client);
    }

    /**
     * Returns the required value or explains which client field is missing.
     */
    private String requiredValue(ArgumentMultimap arguments, Prefix prefix) throws ParseException {
        return arguments.getValue(prefix).orElseThrow(() -> new ParseException(AddClientCommand.MESSAGE_FORMAT));
    }

    /**
     * Normalizes supported prefixes without changing the capitalization of values.
     */
    private String normalizePrefixes(String args) throws ParseException {
        // The inherited tokenizer expects a space before each prefix.
        String spacedArgs = " " + args.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
        Matcher matcher = PARAMETER_PREFIX.matcher(spacedArgs);
        StringBuilder normalized = new StringBuilder();
        while (matcher.find()) {
            String prefix = matcher.group(1).toLowerCase(Locale.ROOT);
            if (!SUPPORTED_PREFIXES.contains(prefix)) {
                throw new ParseException("Unknown client parameter: " + matcher.group(1)
                        + ".\n" + AddClientCommand.MESSAGE_USAGE);
            }
            matcher.appendReplacement(normalized, Matcher.quoteReplacement(prefix));
        }
        matcher.appendTail(normalized);
        return normalized.toString();
    }

    /**
     * Parses a formatted phone number into its digit-only stored representation.
     */
    private Phone parseClientPhone(String value) throws ParseException {
        if (!value.matches("\\+?[0-9 -]+")) {
            throw new ParseException(MESSAGE_PHONE_CONSTRAINTS);
        }
        String digits = value.replaceAll("[ -]", "");
        if (digits.startsWith("+")) {
            digits = digits.substring(1);
        }
        if (!digits.matches("[0-9]{7,15}")) {
            throw new ParseException(MESSAGE_PHONE_CONSTRAINTS);
        }
        return new Phone(digits);
    }
}

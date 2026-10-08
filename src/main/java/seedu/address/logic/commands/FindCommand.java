package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.PersonContainsKeywordsPredicate;

/**
 * Finds clients matching every keyword across their name, phone, email, and tags.
 * Lists matches in name order, or restores the full list when there are no matches.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = "Usage: " + COMMAND_WORD + " KEYWORD [KEYWORD]...";
    public static final String MESSAGE_SUCCESS = "Found %1$d matching clients.";
    public static final String MESSAGE_NO_MATCHES = "No clients matched: %1$s.";

    private final PersonContainsKeywordsPredicate predicate;

    /**
     * Creates a command that searches using the given {@code predicate}.
     */
    public FindCommand(PersonContainsKeywordsPredicate predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        int matchingClientCount = model.getFilteredPersonList().size();
        if (matchingClientCount == 0) {
            model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
            return new CommandResult(String.format(MESSAGE_NO_MATCHES, String.join(" ", predicate.getKeywords())));
        }

        return new CommandResult(String.format(MESSAGE_SUCCESS, matchingClientCount));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}

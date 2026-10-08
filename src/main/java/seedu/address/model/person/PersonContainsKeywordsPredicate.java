package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether every search keyword occurs in a person's name, phone, email, or tags.
 * Keywords may match different fields of the same person. Matching uses case-insensitive substrings.
 */
public class PersonContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;
    private final List<String> normalizedKeywords;

    /**
     * Creates a predicate with a defensive copy of the given {@code keywords}.
     *
     * @throws NullPointerException if {@code keywords} or any keyword is null.
     */
    public PersonContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = List.copyOf(keywords);
        normalizedKeywords = this.keywords.stream()
                .map(keyword -> keyword.toLowerCase(Locale.ROOT))
                .toList();
    }

    /**
     * Returns the original keywords in an unmodifiable list for feedback messages.
     */
    public List<String> getKeywords() {
        return keywords;
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        return !normalizedKeywords.isEmpty()
                && normalizedKeywords.stream().allMatch(keyword -> hasMatchingField(person, keyword));
    }

    /**
     * Returns whether any searchable field contains the normalized {@code keyword}.
     */
    private boolean hasMatchingField(Person person, String keyword) {
        return containsKeyword(person.getName().fullName, keyword)
                || containsKeyword(person.getPhone().value, keyword)
                || containsKeyword(person.getEmail().value, keyword)
                || hasMatchingTag(person, keyword);
    }

    /**
     * Returns whether any tag contains the normalized {@code keyword}.
     */
    private boolean hasMatchingTag(Person person, String keyword) {
        return person.getTags().stream().anyMatch(tag -> containsKeyword(tag.tagName, keyword));
    }

    /**
     * Returns whether {@code value} contains the normalized {@code keyword}, independently of the default locale.
     */
    private boolean containsKeyword(String value, String keyword) {
        return value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof PersonContainsKeywordsPredicate otherPredicate)) {
            return false;
        }

        return keywords.equals(otherPredicate.keywords);
    }

    @Override
    public int hashCode() {
        return keywords.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}

package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether every search keyword occurs in a person's name, phone, email, or tags.
 * Keywords may match different fields of the same person. Matching uses case-insensitive substrings.
 */
public class PersonContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;
    private final List<Pattern> keywordPatterns;

    /**
     * Creates a predicate with a defensive copy of the given {@code keywords}.
     *
     * @throws NullPointerException if {@code keywords} or any keyword is null.
     */
    public PersonContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = List.copyOf(keywords);
        keywordPatterns = this.keywords.stream()
                .map(keyword -> Pattern.compile(keyword,
                        Pattern.LITERAL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))
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
        return !keywordPatterns.isEmpty()
                && keywordPatterns.stream().allMatch(keyword -> hasMatchingField(person, keyword));
    }

    /**
     * Returns whether any searchable field contains the literal {@code keyword}.
     */
    private boolean hasMatchingField(Person person, Pattern keyword) {
        return containsKeyword(person.getName().fullName, keyword)
                || hasMatchingPhone(person.getPhone().value, keyword)
                || containsKeyword(person.getEmail().value, keyword)
                || hasMatchingTag(person, keyword);
    }

    /**
     * Matches stored digits using a phone keyword with an optional plus and formatting hyphens.
     * Other fields still match the original keyword literally.
     */
    private boolean hasMatchingPhone(String phone, Pattern keyword) {
        if (containsKeyword(phone, keyword)) {
            return true;
        }
        String phoneKeyword = keyword.pattern();
        if (!phoneKeyword.matches("\\+?[0-9 -]+")) {
            return false;
        }
        String digits = phoneKeyword.replaceAll("[+ -]", "");
        return !digits.isEmpty() && phone.contains(digits);
    }

    /**
     * Returns whether any tag contains the literal {@code keyword}.
     */
    private boolean hasMatchingTag(Person person, Pattern keyword) {
        return person.getTags().stream().anyMatch(tag -> containsKeyword(tag.tagName, keyword));
    }

    /**
     * Returns whether {@code value} contains the literal {@code keyword}, using locale-independent Unicode case
     * matching.
     */
    private boolean containsKeyword(String value, Pattern keyword) {
        return keyword.matcher(value).find();
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

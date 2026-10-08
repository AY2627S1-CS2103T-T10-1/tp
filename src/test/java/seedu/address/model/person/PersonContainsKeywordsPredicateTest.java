package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonContainsKeywordsPredicateTest {
    private final Person client = new PersonBuilder().withName("Acme Studio").withPhone("98123456")
            .withEmail("hello@acme.com").withTags("startup", "webdesign").build();

    @Test
    public void test_partialKeywordsInEachField_returnsTrue() {
        for (String keyword : List.of("cme", "8123", "HELLO@", "ART", "web")) {
            assertTrue(new PersonContainsKeywordsPredicate(List.of(keyword)).test(client));
        }
    }

    @Test
    public void test_allKeywordsAcrossFieldsMatch_returnsTrue() {
        assertTrue(new PersonContainsKeywordsPredicate(List.of("STU", "8123", "HELLO", "WEB")).test(client));
        assertTrue(new PersonContainsKeywordsPredicate(List.of("startup", "web", "startup")).test(client));
    }

    @Test
    public void test_missingKeywords_returnsFalse() {
        assertFalse(new PersonContainsKeywordsPredicate(List.of()).test(client));
        assertFalse(new PersonContainsKeywordsPredicate(List.of("acme", "missing")).test(client));
        assertFalse(new PersonContainsKeywordsPredicate(List.of("acme.*")).test(client));
        assertFalse(new PersonContainsKeywordsPredicate(List.of("jurong")).test(client));
    }

    @Test
    public void constructor_mutableKeywords_copiesKeywords() {
        List<String> keywords = new ArrayList<>(List.of("acme"));
        PersonContainsKeywordsPredicate predicate = new PersonContainsKeywordsPredicate(keywords);
        keywords.clear();
        assertTrue(predicate.test(client));
        assertEquals(List.of("acme"), predicate.getKeywords());
        assertThrows(UnsupportedOperationException.class, () -> predicate.getKeywords().add("missing"));
    }

    @Test
    public void constructor_nullKeywords_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PersonContainsKeywordsPredicate(null));
    }

    @Test
    public void equals() {
        PersonContainsKeywordsPredicate predicate = new PersonContainsKeywordsPredicate(List.of("acme"));
        PersonContainsKeywordsPredicate copy = new PersonContainsKeywordsPredicate(List.of("acme"));
        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(copy));
        assertEquals(predicate.hashCode(), copy.hashCode());
        assertFalse(predicate.equals(new PersonContainsKeywordsPredicate(List.of("studio"))));
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals(1));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("acme");
        PersonContainsKeywordsPredicate predicate = new PersonContainsKeywordsPredicate(keywords);
        String expected = PersonContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}

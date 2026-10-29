package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    public void test_unicodeCaseVariantsInName_returnsTrue() {
        for (String name : List.of("Işık", "IŞIK")) {
            Person unicodeClient = new PersonBuilder().withName("Studio " + name + " Design").build();
            for (String keyword : List.of("IŞIK", "ışık", "şI")) {
                assertTrue(new PersonContainsKeywordsPredicate(List.of(keyword)).test(unicodeClient));
            }
        }
        for (String name : List.of("ΟΣ", "οσ", "ος")) {
            Person unicodeClient = new PersonBuilder().withName(name).build();
            for (String keyword : List.of("Σ", "σ", "ς")) {
                assertTrue(new PersonContainsKeywordsPredicate(List.of(keyword)).test(unicodeClient));
            }
        }
        for (String name : List.of("ß", "ẞ")) {
            Person unicodeClient = new PersonBuilder().withName(name).build();
            for (String keyword : List.of("ß", "ẞ")) {
                assertTrue(new PersonContainsKeywordsPredicate(List.of(keyword)).test(unicodeClient));
            }
        }
    }

    @Test
    public void test_supplementaryUnicodeCaseVariants_returnsTrue() {
        // Deseret upper and lower case letters each occupy a surrogate pair.
        String upperCase = "\uD801\uDC00";
        String lowerCase = "\uD801\uDC28";
        Person upperCaseClient = new PersonBuilder().withName("Studio " + upperCase + " Design").build();
        Person lowerCaseClient = new PersonBuilder().withName("Studio " + lowerCase + " Design").build();
        assertTrue(new PersonContainsKeywordsPredicate(List.of(lowerCase)).test(upperCaseClient));
        assertTrue(new PersonContainsKeywordsPredicate(List.of(upperCase)).test(lowerCaseClient));
    }

    @Test
    public void test_defaultLocale_doesNotAffectMatching() {
        Locale originalLocale = Locale.getDefault();
        try {
            for (Locale locale : List.of(Locale.ROOT, Locale.forLanguageTag("tr"))) {
                Locale.setDefault(locale);
                Person unicodeClient = new PersonBuilder().withName("Işık Studio").withTags("design").build();
                assertTrue(new PersonContainsKeywordsPredicate(List.of("IŞIK", "DESIGN")).test(unicodeClient));
            }
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void test_regexMetacharacters_matchLiterally() {
        Person namedClient = new PersonBuilder().withName("Acme (Studio)").build();
        assertTrue(new PersonContainsKeywordsPredicate(List.of("(STUDIO)")).test(namedClient));
        for (String keyword : List.of("acme.*", "acme|studio", "(?i)acme", "\\E")) {
            assertFalse(new PersonContainsKeywordsPredicate(List.of(keyword)).test(namedClient));
        }
    }

    @Test
    public void test_distinctLetters_doesNotRemoveAccentsOrExpandCaseMappings() {
        Person unicodeClient = new PersonBuilder().withName("Straße Café").build();
        assertTrue(new PersonContainsKeywordsPredicate(List.of("STRAẞE", "CAFÉ")).test(unicodeClient));
        assertFalse(new PersonContainsKeywordsPredicate(List.of("STRASSE")).test(unicodeClient));
        assertFalse(new PersonContainsKeywordsPredicate(List.of("CAFE")).test(unicodeClient));
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

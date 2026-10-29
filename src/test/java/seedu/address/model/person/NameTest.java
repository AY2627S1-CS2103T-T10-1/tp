package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains non-alphanumeric characters

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
    }

    @Test
    public void isValidName_combiningMarks_acceptsNamesAndPreservesSpelling() {
        for (String name : new String[] {"अनिल", "วิชัย", "Jose\u0301", "A\u0301\u0327", "A\u20dd",
            "\uD801\uDC00", "A\uD834\uDD65", "\u0301Alice"}) {
            assertTrue(Name.isValidName(name), name);
            assertEquals(name, new Name(name).fullName);
        }
    }

    @Test
    public void isValidName_marksWithoutLetterOrNumber_rejects() {
        for (String name : new String[] {"\u0301", "\u093f", "\u20dd", " \u0301 . &()"}) {
            assertFalse(Name.isValidName(name), name);
        }
    }

    @Test
    public void isValidName_unicodeLengthBoundaries_countsEachCodePoint() {
        for (String name : new String[] {"A".repeat(80), "A".repeat(79) + "\u0301", "A\u0301".repeat(40),
            "\uD801\uDC00".repeat(80), "A" + "\uD834\uDD65".repeat(79)}) {
            assertTrue(Name.isValidName(name));
        }
        for (String name : new String[] {"A".repeat(81), "A".repeat(80) + "\u0301", "A\u0301".repeat(41),
            "\uD801\uDC00".repeat(81), "A" + "\uD834\uDD65".repeat(80)}) {
            assertFalse(Name.isValidName(name));
        }
    }

    @Test
    public void isValidName_existingPunctuationAndInvalidCharacters_preservesRules() {
        assertTrue(Name.isValidName(" O'Neil & Co. (SG)-2 "));
        for (String name : new String[] {"() & . -", "Studio*", "A\u0000B", "A\tB", "A\nB", "A\u200bB",
            "A\u200dB", "A\u202eB", "A\uD800B", "A\uDC00B", "A\uD83D\uDE00"}) {
            assertFalse(Name.isValidName(name), name);
        }
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}

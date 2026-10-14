package seedu.address.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Duration;
import java.util.Locale;

import org.junit.jupiter.api.Test;

public class ProjectTitleTest {

    @Test
    public void constructor_invalidTitle_throwsIllegalArgumentException() {
        assertThrows(NullPointerException.class, () -> new ProjectTitle(null));
        String[] invalidTitles = {"", "   ", "---", "🎨", "A".repeat(101), "A\nB", "A\rB", "A\tB",
            "A\u0000B", "A\u007fB", "A\u0085B", "A\u200bB", "A\u2028B", "A\u2029B", "A\u0378B",
            "A\ud800B", "A\udc00B", "\nTitle", "Title\t"};
        for (String title : invalidTitles) {
            assertFalse(ProjectTitle.isValidTitle(title));
            assertThrows(IllegalArgumentException.class, ProjectTitle.MESSAGE_CONSTRAINTS, () ->
                    new ProjectTitle(title));
        }
    }

    @Test
    public void constructor_validTitle_preservesDisplayValue() {
        String[] validTitles = {"A", "7", "Ⅷ", "²", "網站設計", "Café redesign", "Design 🎨", "A".repeat(100),
            "𐐀".repeat(100), "Landing  page — phase 2", "title/status/in-progress"};
        for (String title : validTitles) {
            assertTrue(ProjectTitle.isValidTitle(title));
            assertEquals(title, new ProjectTitle(title).value);
        }
        assertEquals("Landing  page", new ProjectTitle(" \u00a0Landing  page\u2003 ").toString());
        assertThrows(IllegalArgumentException.class, () -> new ProjectTitle("𐐀".repeat(101)));
    }

    @Test
    public void normalizedValue_repeatedUnicodeSpaces_collapsesSpaces() {
        ProjectTitle title = new ProjectTitle(" \u00a0Website\u2003  REDESIGN ");
        assertEquals("website redesign", title.normalizedValue());
        assertEquals("Website\u2003  REDESIGN", title.value);
    }

    @Test
    public void constructor_unicodeSpaceSeparators_trimsEdgesOnly() {
        String[] spaces = {" ", "\u00a0", "\u1680", "\u2000", "\u2001", "\u2002", "\u2003", "\u2004",
            "\u2005", "\u2006", "\u2007", "\u2008", "\u2009", "\u200a", "\u202f", "\u205f", "\u3000"};
        for (String space : spaces) {
            ProjectTitle title = new ProjectTitle(space + "Website" + space + " redesign" + space);
            assertEquals("Website" + space + " redesign", title.value);
            assertEquals("website redesign", title.normalizedValue());
            assertFalse(ProjectTitle.isValidTitle(space));
        }
    }

    @Test
    public void isValidTitle_largeInternalSpaceRun_rejectsWithoutExcessiveProcessing() {
        String title = "A" + "\u00a0".repeat(200_000) + "B";
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertFalse(ProjectTitle.isValidTitle(title)));
    }

    @Test
    public void normalizedValue_turkishLocale_usesRootLocale() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("website redesign", new ProjectTitle("WEBSITE REDESIGN").normalizedValue());
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void equalsAndHashCode_compareDisplayValues() {
        ProjectTitle title = new ProjectTitle("Website redesign");
        assertEquals(title, title);
        assertEquals(title, new ProjectTitle("Website redesign"));
        assertEquals(title.hashCode(), new ProjectTitle("Website redesign").hashCode());
        assertFalse(title.equals(null));
        assertFalse(title.equals("Website redesign"));
        assertFalse(title.equals(new ProjectTitle("website redesign")));
        assertFalse(title.equals(new ProjectTitle("Website  redesign")));
    }
}

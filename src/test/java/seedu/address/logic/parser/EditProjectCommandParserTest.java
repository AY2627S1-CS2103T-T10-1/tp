package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditProjectCommand;
import seedu.address.logic.commands.EditProjectCommand.EditProjectDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;

/**
 * Tests field selection, strict dates, command routing and invalid edit syntax.
 */
public class EditProjectCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_multipleFieldsAnyOrderAndCase_preservesTitleCapitalization() throws Exception {
        EditProjectCommand expected = new EditProjectCommand(Index.fromOneBased(2), 11,
                new EditProjectDescriptor("New  Website", LocalDate.of(2024, 2, 29),
                        ProjectStatus.COMPLETED, ProjectPriority.HIGH));
        assertEquals(expected, parser.parseCommand("EDIT-PROJECT\t2 11 PRIORITY/HIGH TITLE/New  Website "
                + "STATUS/COMPLETED DEADLINE/2024-02-29"));
    }

    @Test
    public void parse_eachSingleField_succeedsWithoutRequiringOtherFields() throws Exception {
        List<String> fields = List.of("title/New name", "deadline/2000-01-01", "status/completed", "priority/low");
        List<EditProjectDescriptor> descriptors = List.of(
                new EditProjectDescriptor("New name", null, null, null),
                new EditProjectDescriptor(null, LocalDate.of(2000, 1, 1), null, null),
                new EditProjectDescriptor(null, null, ProjectStatus.COMPLETED, null),
                new EditProjectDescriptor(null, null, null, ProjectPriority.LOW));
        for (int i = 0; i < fields.size(); i++) {
            assertEquals(new EditProjectCommand(Index.fromOneBased(1), 3, descriptors.get(i)),
                    parser.parseCommand("edit-project 1 3 " + fields.get(i)));
        }
    }

    @Test
    public void parse_invalidIdentifiersAndMissingFields_rejects() {
        for (String args : List.of("", "1", "1 3", "1 3 extra status/completed", "0 3 status/completed",
                "-1 3 status/completed", "+1 3 status/completed", "1.5 3 status/completed",
                "1 0 status/completed", "1 -3 status/completed", "1 +3 status/completed",
                "1 3.5 status/completed", "1 2147483648 status/completed", "x 3 status/completed")) {
            assertThrows(ParseException.class, () -> parser.parseCommand("edit-project " + args), args);
        }
        assertEquals(EditProjectCommand.MESSAGE_NO_FIELD,
                assertThrows(ParseException.class, () -> parser.parseCommand("edit-project 1 3")).getMessage());
    }

    @Test
    public void parse_blankRepeatedUnknownAndInvalidFields_rejects() {
        for (String fields : List.of("title/", "title/---", "title/" + "a".repeat(101), "deadline/",
                "deadline/2026-02-29", "deadline/2026-1-01", "deadline/0000-01-01", "status/",
                "status/pending", "status/completed extra", "priority/", "priority/urgent", "foo/bar",
                "title/First title/Second", "deadline/2026-10-15 deadline/2026-10-16",
                "status/not-started STATUS/completed", "priority/low priority/high")) {
            assertThrows(ParseException.class, () -> parser.parseCommand("edit-project 1 3 " + fields), fields);
        }
        assertEquals(ProjectDeadline.MESSAGE_CONSTRAINTS, assertThrows(
                ParseException.class, () -> parser.parseCommand("edit-project 1 3 deadline/2026-02-29")).getMessage());
    }

    @Test
    public void parse_titleBoundary_acceptsOneAndOneHundredCharacters() throws Exception {
        for (String title : List.of("A", "a".repeat(100))) {
            assertEquals(new EditProjectCommand(Index.fromOneBased(1), 3,
                    new EditProjectDescriptor(title, null, null, null)),
                    parser.parseCommand("edit-project 1 3 title/" + title));
        }
    }

    @Test
    public void parse_slashesInTitle_preservesPrintableText() throws Exception {
        String title = "UX/UI redesign https://example.com";
        assertEquals(new EditProjectCommand(Index.fromOneBased(1), 3,
                new EditProjectDescriptor(title, null, null, null)),
                parser.parseCommand("edit-project 1 3 title/" + title));
    }

    @Test
    public void parse_controlCharactersInTitle_rejectsWithoutNormalizingThem() {
        EditProjectCommandParser commandParser = new EditProjectCommandParser();
        for (String title : List.of("\tWebsite", "Website\tlaunch", "Website\u0000", "Website\nlaunch")) {
            assertThrows(ParseException.class, () -> commandParser.parse("1 3 title/" + title));
        }
        assertThrows(ParseException.class, () -> parser.parseCommand("edit-project 1 3 title/Website\t"));
    }

    @Test
    public void parse_identifiers_acceptsAsciiBoundariesAndRejectsUnicodeDigits() throws Exception {
        assertEquals(new EditProjectCommand(Index.fromOneBased(1), Integer.MAX_VALUE,
                new EditProjectDescriptor(null, null, ProjectStatus.COMPLETED, null)),
                parser.parseCommand("edit-project 0001 2147483647 status/completed"));
        for (String identifiers : List.of("١ 3", "１ 3", "1 ٣", "1 ３", "2147483648 3")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(
                    "edit-project " + identifiers + " status/completed"));
        }
    }

    @Test
    public void parse_largeInternalSpaceRuns_rejectsWithoutExcessiveProcessing() {
        EditProjectCommandParser commandParser = new EditProjectCommandParser();
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            for (String space : List.of(" ", "\u00a0")) {
                String title = "A" + space.repeat(200_000) + "B";
                for (String args : List.of("1 3 title/" + title + " deadline/2026-10-15",
                        "1 3 deadline/2026-10-15 title/" + title)) {
                    assertEquals(ProjectTitle.MESSAGE_CONSTRAINTS,
                            assertThrows(ParseException.class, () -> commandParser.parse(args)).getMessage());
                }
            }
        });
    }

    @Test
    public void parse_mixedCaseUnderTurkishLocale_preservesSlashTextAndDelimiters() throws Exception {
        Locale previousLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals(new EditProjectCommand(Index.fromOneBased(1), 3,
                    new EditProjectDescriptor("UI/UX foo/bar", null, ProjectStatus.IN_PROGRESS,
                            ProjectPriority.HIGH)),
                    parser.parseCommand("EDIT-PROJECT 1 3 TITLE/UI/UX foo/bar STATUS/IN-PROGRESS PRIORITY/HIGH"));
        } finally {
            Locale.setDefault(previousLocale);
        }
    }


    @Test
    public void parse_spacesAndTabSeparators_trimsEdgesAndRetainsInternalTitleSpacing() throws Exception {
        assertEquals(new EditProjectCommand(Index.fromOneBased(1), 3,
                new EditProjectDescriptor("New  Website", LocalDate.of(2024, 2, 29),
                        ProjectStatus.COMPLETED, ProjectPriority.HIGH)),
                parser.parseCommand("  edit-project \t0001 0003 TITLE/   New  Website   \tDEADLINE/2024-02-29"
                        + "\tSTATUS/COMPLETED\tPRIORITY/HIGH  "));
    }

}

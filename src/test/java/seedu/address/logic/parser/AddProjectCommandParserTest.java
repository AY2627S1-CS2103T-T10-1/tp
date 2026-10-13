package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.time.LocalDate;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddProjectCommand;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;

public class AddProjectCommandParserTest {

    private static final String TITLE = "Website redesign";
    private static final String DEADLINE = "2026-10-15";
    private static final String REQUIRED_FIELDS = " title/" + TITLE + " deadline/" + DEADLINE;

    private final AddProjectCommandParser parser = new AddProjectCommandParser();

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_requiredFields_usesDefaults() {
        AddProjectCommand expected = expectedCommand(1, TITLE, DEADLINE,
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM);
        assertParseSuccess(parser, "1" + REQUIRED_FIELDS, expected);
        assertParseSuccess(parser, "   1   title/  " + TITLE + "  deadline/  " + DEADLINE + "   ", expected);
        assertParseSuccess(parser, "1 deadline/" + DEADLINE + " title/" + TITLE, expected);
    }

    @Test
    public void parse_allFieldsInDifferentOrders_success() {
        AddProjectCommand expected = expectedCommand(2, TITLE, DEADLINE,
                ProjectStatus.IN_PROGRESS, ProjectPriority.HIGH);
        for (String fields : new String[] {
            "title/Website redesign deadline/2026-10-15 status/in-progress priority/high",
            "priority/high status/in-progress deadline/2026-10-15 title/Website redesign",
            "deadline/2026-10-15 title/Website redesign priority/high status/in-progress",
            "status/in-progress deadline/2026-10-15 priority/high title/Website redesign"
        }) {
            assertParseSuccess(parser, "2 " + fields, expected);
        }
    }

    @Test
    public void parse_optionalFieldsIndividually_success() {
        assertParseSuccess(parser, "1" + REQUIRED_FIELDS + " status/in-progress",
                expectedCommand(1, TITLE, DEADLINE, ProjectStatus.IN_PROGRESS, ProjectPriority.MEDIUM));
        for (ProjectPriority priority : ProjectPriority.values()) {
            assertParseSuccess(parser, "1" + REQUIRED_FIELDS + " priority/" + priority,
                    expectedCommand(1, TITLE, DEADLINE, ProjectStatus.NOT_STARTED, priority));
        }
        assertParseSuccess(parser, "1" + REQUIRED_FIELDS + " status/not-started",
                expectedCommand(1, TITLE, DEADLINE, ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM));
    }

    @Test
    public void parse_mixedCasePrefixesAndValuesUnderTurkishLocale_success() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertParseSuccess(parser,
                    "1 TITLE/Website redesign DeAdLiNe/2026-10-15 STATUS/IN-PROGRESS PRIORITY/HIGH",
                    expectedCommand(1, TITLE, DEADLINE, ProjectStatus.IN_PROGRESS, ProjectPriority.HIGH));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void parse_tabsBetweenFields_success() {
        assertParseSuccess(parser,
                "\t1\ttitle/Website redesign\tdeadline/2026-10-15\tstatus/in-progress\tpriority/high",
                expectedCommand(1, TITLE, DEADLINE, ProjectStatus.IN_PROGRESS, ProjectPriority.HIGH));
    }

    @Test
    public void parse_missingIndexOrRequiredFields_reportsUsage() {
        for (String input : new String[] {"", "   ", "1", REQUIRED_FIELDS,
            "1 title/Website redesign", "1 deadline/2026-10-15", "1 status/not-started priority/medium"}) {
            assertParseFailure(parser, input, AddProjectCommand.MESSAGE_USAGE);
        }
    }

    @Test
    public void parse_garbagePreamble_reportsUsage() {
        for (String preamble : new String[] {"1 2", "1 extra", "extra 1", "1 x/value", "1 title = Website"}) {
            assertParseFailure(parser, preamble + REQUIRED_FIELDS, AddProjectCommand.MESSAGE_USAGE);
        }
    }

    @Test
    public void parse_invalidIndex_reportsIndexConstraint() {
        for (String index : new String[] {"0", "000", "-1", "+1", "1.0", "1e2", "1_000", "0x1", "one",
            "١", "１", "2147483648", "99999999999999999999999999999999"}) {
            assertParseFailure(parser, index + REQUIRED_FIELDS, AddProjectCommand.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_validIndexBoundaries_success() {
        for (String index : new String[] {"1", "0001", "2147483647"}) {
            assertParseSuccess(parser, index + REQUIRED_FIELDS,
                    expectedCommand(Integer.parseInt(index), TITLE, DEADLINE,
                            ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM));
        }
    }

    @Test
    public void parse_repeatedPrefixes_rejectsInsteadOfOverwriting() {
        assertParseFailure(parser, "1" + REQUIRED_FIELDS + " TITLE/Other",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_TITLE));
        assertParseFailure(parser, "1" + REQUIRED_FIELDS + " DEADLINE/2026-11-01",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_DEADLINE));
        assertParseFailure(parser, "1" + REQUIRED_FIELDS + " status/not-started STATUS/in-progress",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_STATUS));
        assertParseFailure(parser, "1" + REQUIRED_FIELDS + " priority/low PRIORITY/high",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_PRIORITY));
        assertParseFailure(parser, "1 title/ title/Website deadline/2026-10-15",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_TITLE));
    }

    @Test
    public void parse_blankValues_reportFieldConstraints() {
        assertParseFailure(parser, "1 title/   deadline/2026-10-15", ProjectTitle.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 title/Website deadline/  ", ProjectDeadline.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + REQUIRED_FIELDS + " status/   ", AddProjectCommand.MESSAGE_NEW_STATUS);
        assertParseFailure(parser, "1" + REQUIRED_FIELDS + " priority/   ", ProjectPriority.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_printableTitlesAndCodePointBoundaries_success() {
        for (String title : new String[] {"A", "7", "A".repeat(100), "𐐀".repeat(100),
            "Résumé – 更新 🛠", "A&B (Phase 2): 'Launch'!", "UI/UX refresh", "Review https://example.com",
            "Review foo/bar", "Alpha\u00a0Beta", "Full  spacing"}) {
            assertParseSuccess(parser, "1 title/" + title + " deadline/2026-10-15",
                    expectedCommand(1, title, DEADLINE, ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM));
        }
    }

    @Test
    public void parse_invalidTitles_reportTitleConstraint() {
        for (String title : new String[] {"A".repeat(101), "𐐀".repeat(101), "--- !!!", "🛠", "\u00a0",
            "Web\tsite", "Web\nsite", "Web\rsite", "Web\u0000site", "Web\u007fsite",
            "Web\u0085site", "Web\u200bsite", "Web\u2028site", "Web\u2029site", "Web\ud800site"}) {
            assertParseFailure(parser, "1 title/" + title + " deadline/2026-10-15", ProjectTitle.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_titleControlsAtValueEdges_areNotSilentlyTrimmed() {
        for (String title : new String[] {"\tWebsite", "\nWebsite", "\u0000Website", "Website\t",
            "Website\n", "Website\r", "Website\u0000"}) {
            assertParseFailure(parser, "1 deadline/2026-10-15 title/" + title, ProjectTitle.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_validDatesIncludingPastAndLeapDay_success() {
        LocalDate today = LocalDate.now();
        for (String date : new String[] {"0001-01-01", "9999-12-31", "2000-02-29", "2024-02-29",
            today.minusDays(1).toString(), today.toString(), today.plusDays(1).toString()}) {
            assertParseSuccess(parser, "1 title/Website redesign deadline/" + date,
                    expectedCommand(1, TITLE, date, ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM));
        }
    }

    @Test
    public void parse_invalidDates_reportDeadlineConstraint() {
        for (String date : new String[] {"2026-02-29", "1900-02-29", "2026-04-31", "2026-00-15", "2026-13-15",
            "2026-10-00", "2026-10-32", "0000-01-01", "10000-01-01", "+2026-10-15", "-2026-10-15",
            "2026-1-15", "2026-10-5", "15-10-2026", "2026/10/15", "２０２６-１０-１５", "٢٠٢٦-١٠-١٥",
            "2026-10-15T00:00:00", "2026-10-15Z", "2026-10-15 trailing", "2026-10-15\u0000"}) {
            assertParseFailure(parser, "1 title/Website deadline/" + date, ProjectDeadline.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidStatuses_reportNewProjectStatusConstraint() {
        for (String status : new String[] {"completed", "COMPLETED", "pending", "in progress", "not_started",
            "in-progress trailing", "in-progress\u0000"}) {
            assertParseFailure(parser, "1" + REQUIRED_FIELDS + " status/" + status,
                    AddProjectCommand.MESSAGE_NEW_STATUS);
        }
    }

    @Test
    public void parse_invalidPriorities_reportPriorityConstraint() {
        for (String priority : new String[] {"urgent", "normal", "1", "med", "high trailing", "high\u0000"}) {
            assertParseFailure(parser, "1" + REQUIRED_FIELDS + " priority/" + priority,
                    ProjectPriority.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_unknownPrefixesOutsideTitle_areNotSilentlyIgnored() {
        assertParseFailure(parser, "1 x/value" + REQUIRED_FIELDS, AddProjectCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "1" + REQUIRED_FIELDS + " x/value", ProjectDeadline.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + REQUIRED_FIELDS + " status/in-progress x/value",
                AddProjectCommand.MESSAGE_NEW_STATUS);
        assertParseFailure(parser, "1" + REQUIRED_FIELDS + " priority/high x/value",
                ProjectPriority.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 title/Website due/2026-10-15", AddProjectCommand.MESSAGE_USAGE);
    }

    /**
     * Creates the command expected after parsing the supplied field values.
     */
    private AddProjectCommand expectedCommand(int clientIndex, String title, String deadline,
            ProjectStatus status, ProjectPriority priority) {
        return new AddProjectCommand(Index.fromOneBased(clientIndex), new ProjectTitle(title),
                new ProjectDeadline(deadline), status, priority);
    }
}

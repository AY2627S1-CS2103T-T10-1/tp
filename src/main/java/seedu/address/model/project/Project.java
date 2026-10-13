package seedu.address.model.project;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;

/**
 * Immutable client work identified by a stable, client-local ID.
 */
public class Project {
    public static final String MESSAGE_TITLE_CONSTRAINTS =
            "Title must contain 1–100 printable characters and at least one letter or number.";
    public static final String MESSAGE_DEADLINE_CONSTRAINTS =
            "Deadline must be a real date in yyyy-MM-dd format (years 0001–9999).";
    public static final String MESSAGE_ID_CONSTRAINTS = "Project ID must be a positive integer.";

    private final int id;
    private final String title;
    private final LocalDate deadline;
    private final ProjectStatus status;
    private final ProjectPriority priority;

    /**
     * Creates validated project data, preserving the supplied stable ID.
     */
    public Project(int id, String title, LocalDate deadline, ProjectStatus status, ProjectPriority priority) {
        requireNonNull(title);
        requireNonNull(deadline);
        checkArgument(id > 0, MESSAGE_ID_CONSTRAINTS);
        checkArgument(isValidTitle(title), MESSAGE_TITLE_CONSTRAINTS);
        checkArgument(deadline.getYear() >= 1 && deadline.getYear() <= 9999, MESSAGE_DEADLINE_CONSTRAINTS);
        this.id = id;
        this.title = title.strip();
        this.deadline = deadline;
        this.status = requireNonNull(status);
        this.priority = requireNonNull(priority);
    }

    /**
     * Returns whether the trimmed title contains printable text within the length limit.
     */
    public static boolean isValidTitle(String title) {
        requireNonNull(title);
        String trimmed = title.strip();
        return !trimmed.isEmpty() && trimmed.codePointCount(0, trimmed.length()) <= 100
                && trimmed.codePoints().anyMatch(Character::isLetterOrDigit)
                && trimmed.codePoints().allMatch(Project::isPrintable);
    }

    private static boolean isPrintable(int codePoint) {
        int type = Character.getType(codePoint);
        return !Character.isISOControl(codePoint) && type != Character.FORMAT
                && type != Character.SURROGATE && type != Character.UNASSIGNED
                && type != Character.LINE_SEPARATOR && type != Character.PARAGRAPH_SEPARATOR;
    }

    /**
     * Parses a strict, real calendar date without silently correcting invalid days.
     */
    public static LocalDate parseDeadline(String value) {
        requireNonNull(value);
        checkArgument(value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"), MESSAGE_DEADLINE_CONSTRAINTS);
        try {
            LocalDate date = LocalDate.parse(value);
            checkArgument(date.getYear() >= 1, MESSAGE_DEADLINE_CONSTRAINTS);
            return date;
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(MESSAGE_DEADLINE_CONSTRAINTS, exception);
        }
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public ProjectPriority getPriority() {
        return priority;
    }

    /**
     * Detects equivalent client work independently of its ID, status and priority.
     */
    public boolean isSameWork(Project other) {
        requireNonNull(other);
        return title.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT)
                .equals(other.title.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT))
                && deadline.equals(other.deadline);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof Project project && id == project.id
                && title.equals(project.title) && deadline.equals(project.deadline)
                && status == project.status && priority == project.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, deadline, status, priority);
    }

    @Override
    public String toString() {
        return "#" + id + " " + title + " | Due: " + deadline + " | Status: " + status + " | Priority: " + priority;
    }
}

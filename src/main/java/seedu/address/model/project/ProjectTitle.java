package seedu.address.model.project;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * A printable project title. Display spacing is preserved; duplicate comparison ignores case and repeated spaces.
 */
public final class ProjectTitle {

    public static final String MESSAGE_CONSTRAINTS =
            "Project title must be 1–100 printable characters and contain a letter or number.";

    public final String value;

    /**
     * Constructs a title, removing only its surrounding printable spaces.
     */
    public ProjectTitle(String title) {
        requireNonNull(title);
        checkArgument(isValidTitle(title), MESSAGE_CONSTRAINTS);
        value = trimSpaces(title);
    }

    /**
     * Returns whether the title contains 1–100 printable Unicode code points and a letter or number.
     */
    public static boolean isValidTitle(String test) {
        requireNonNull(test);
        if (!test.codePoints().allMatch(ProjectTitle::isPrintable)) {
            return false;
        }
        String trimmed = trimSpaces(test);
        int length = trimmed.codePointCount(0, trimmed.length());
        return length >= 1 && length <= 100 && trimmed.matches(".*[\\p{L}\\p{N}].*");
    }

    /**
     * Returns a locale-independent title for duplicate comparison.
     */
    public String normalizedValue() {
        return value.replaceAll("\\p{Zs}+", " ").toLowerCase(Locale.ROOT);
    }

    private static boolean isPrintable(int codePoint) {
        int type = Character.getType(codePoint);
        return type != Character.CONTROL && type != Character.FORMAT && type != Character.UNASSIGNED
                && type != Character.SURROGATE && type != Character.LINE_SEPARATOR
                && type != Character.PARAGRAPH_SEPARATOR;
    }

    private static String trimSpaces(String title) {
        int start = 0;
        int end = title.length();
        while (start < end && Character.isSpaceChar(title.codePointAt(start))) {
            start += Character.charCount(title.codePointAt(start));
        }
        while (end > start && Character.isSpaceChar(title.codePointBefore(end))) {
            end -= Character.charCount(title.codePointBefore(end));
        }
        return title.substring(start, end);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof ProjectTitle otherTitle && value.equals(otherTitle.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

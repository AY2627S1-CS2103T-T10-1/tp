package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents an optional remark about a person. An empty value means no remark.
 * Remarks have no content constraints and are immutable.
 */
public class Remark {
    public final String value;

    /**
     * Creates a remark with the given value, which may be empty but not null.
     */
    public Remark(String remark) {
        requireNonNull(remark);
        value = remark;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof Remark otherRemark && value.equals(otherRemark.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

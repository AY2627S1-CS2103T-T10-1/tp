package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents an optional remark attached to a person.
 * Guarantees: immutable and non-null; an empty value means that no remark is set.
 */
public class Remark {

    public final String value;

    /**
     * Creates a {@code Remark} from the supplied text.
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
        return other == this
                || (other instanceof Remark otherRemark && value.equals(otherRemark.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

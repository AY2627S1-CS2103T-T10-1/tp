package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RemarkTest {
    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_unconstrainedValues_preservesValue() {
        assertEquals("", new Remark("").value);
        assertEquals("  日本語!\n", new Remark("  日本語!\n").value);
    }

    @Test
    public void equalsAndHashCode() {
        Remark remark = new Remark("note");
        Remark copy = new Remark("note");
        assertTrue(remark.equals(remark));
        assertTrue(remark.equals(copy));
        assertEquals(remark.hashCode(), copy.hashCode());
        assertFalse(remark.equals(null));
        assertFalse(remark.equals("note"));
        assertFalse(remark.equals(new Remark("other")));
    }

    @Test
    public void toStringMethod() {
        assertEquals("note", new Remark("note").toString());
    }
}

package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void valueMethods() {
        Remark remark = new Remark("Likes baseball");

        assertEquals("Likes baseball", remark.toString());
        assertEquals("Likes baseball".hashCode(), remark.hashCode());
        assertTrue(remark.equals(remark));
        assertTrue(remark.equals(new Remark("Likes baseball")));
        assertFalse(remark.equals(new Remark("Likes swimming")));
        assertFalse(remark.equals(null));
        assertFalse(remark.equals("Likes baseball"));
    }
}

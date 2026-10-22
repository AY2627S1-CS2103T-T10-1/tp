package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.tag.Tag;

public class JsonAdaptedTagTest {

    @Test
    public void toModelType_stringTag_returnsTag() throws Exception {
        JsonAdaptedTag tag = JsonUtil.fromJsonString("\"client\"", JsonAdaptedTag.class);
        assertEquals(new Tag("client"), tag.toModelType());
        assertEquals("\"client\"", JsonUtil.toJsonString(tag));
    }

    @Test
    public void toModelType_nonStringTag_throwsIllegalValueException() throws Exception {
        for (String value : new String[] {"123", "true", "{}", "[]"}) {
            JsonAdaptedTag tag = JsonUtil.fromJsonString(value, JsonAdaptedTag.class);
            assertThrows(IllegalValueException.class, Tag.MESSAGE_CONSTRAINTS, tag::toModelType);
        }
    }
}

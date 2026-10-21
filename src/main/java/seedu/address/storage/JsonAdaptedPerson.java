package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Remark;
import seedu.address.model.project.Project;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final JsonNode name;
    private final JsonNode phone;
    private final JsonNode email;
    private final JsonNode address;
    private final JsonNode remark;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();
    private final List<JsonAdaptedProject> projects = new ArrayList<>();
    private JsonNode nextProjectId;
    @JsonIgnore
    private boolean hasInvalidProjects;

    /**
     * Constructs stored client data. Legacy files may omit the projects and next project ID fields.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String address, String remark,
            List<JsonAdaptedTag> tags) {
        this(textNode(name), textNode(phone), textNode(email), textNode(address), textNode(remark), tags);
    }

    private JsonAdaptedPerson(JsonNode name, JsonNode phone, JsonNode email, JsonNode address, JsonNode remark,
            List<JsonAdaptedTag> tags) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.remark = remark;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = textNode(source.getName().fullName);
        phone = textNode(source.getPhone().value);
        email = textNode(source.getEmail().value);
        address = textNode(source.getAddress().value);
        remark = textNode(source.getRemark().value);
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
        projects.addAll(source.getProjects().stream()
                .map(JsonAdaptedProject::new)
                .collect(Collectors.toList()));
        nextProjectId = LongNode.valueOf(source.getNextProjectId());
        hasInvalidProjects = false;
    }

    @JsonCreator
    private static JsonAdaptedPerson fromJson(@JsonProperty("name") JsonNode name,
            @JsonProperty("phone") JsonNode phone, @JsonProperty("email") JsonNode email,
            @JsonProperty("address") JsonNode address, @JsonProperty("remark") JsonNode remark,
            @JsonProperty("tags") List<JsonAdaptedTag> tags) {
        return new JsonAdaptedPerson(name, phone, email, address, remark, tags);
    }

    /**
     * Reads projects only when the field is present. Missing fields in legacy files keep the empty default.
     * Jackson represents an explicit JSON null with a non-null {@code NullNode}, which is rejected as non-array data.
     */
    @JsonSetter("projects")
    private void setProjects(JsonNode storedProjects) {
        projects.clear();
        hasInvalidProjects = !storedProjects.isArray();
        if (hasInvalidProjects) {
            return;
        }
        for (JsonNode project : storedProjects) {
            if (!project.isObject()) {
                hasInvalidProjects = true;
                continue;
            }
            projects.add(new JsonAdaptedProject(project.get("id"), project.get("title"),
                    project.get("deadline"), project.get("status"), project.get("priority")));
        }
    }

    /**
     * Retains explicit null as invalid data while an omitted legacy counter remains absent.
     * Jackson supplies a non-null {@code NullNode} for an explicit JSON null.
     * Omitted properties do not invoke this setter.
     */
    @JsonSetter("nextProjectId")
    private void setNextProjectId(JsonNode storedNextProjectId) {
        nextProjectId = storedNextProjectId;
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        if (hasInvalidProjects) {
            throw new IllegalValueException("A client's projects must be an array of project objects.");
        }
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            if (tag == null) {
                throw new IllegalValueException("A client's tags cannot contain null entries.");
            }
            personTags.add(tag.toModelType());
        }

        String storedName = readText(name, Name.class);
        if (!Name.isValidName(storedName)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(storedName);

        String storedPhone = readText(phone, Phone.class);
        if (!Phone.isValidPhone(storedPhone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(storedPhone);

        String storedEmail = readText(email, Email.class);
        if (!Email.isValidEmail(storedEmail)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(storedEmail);

        String storedAddress = readText(address, Address.class);
        if (!Address.isValidAddress(storedAddress)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(storedAddress);

        final Remark modelRemark = new Remark(readText(remark, Remark.class));

        final Set<Tag> modelTags = new HashSet<>(personTags);
        final List<Project> modelProjects = new ArrayList<>();
        for (JsonAdaptedProject project : projects) {
            modelProjects.add(project.toModelType());
        }
        final long modelNextProjectId = readNextProjectId();
        try {
            return new Person(modelName, modelPhone, modelEmail, modelAddress, modelRemark, modelTags,
                    modelProjects, modelNextProjectId);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage(), e);
        }
    }

    private long readNextProjectId() throws IllegalValueException {
        if (nextProjectId == null) {
            return 1;
        }
        if (!nextProjectId.isIntegralNumber() || !nextProjectId.canConvertToLong()) {
            throw new IllegalValueException(Person.MESSAGE_PROJECT_ID_COUNTER_CONSTRAINTS);
        }
        return nextProjectId.longValue();
    }

    private static JsonNode textNode(String value) {
        return value == null ? null : TextNode.valueOf(value);
    }

    private static String readText(JsonNode field, Class<?> type) throws IllegalValueException {
        if (field == null || field.isNull()) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, type.getSimpleName()));
        }
        if (!field.isTextual()) {
            throw new IllegalValueException("Person's " + type.getSimpleName() + " field must be a string.");
        }
        return field.textValue();
    }
}

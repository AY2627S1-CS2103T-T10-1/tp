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

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final String remark;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();
    private final List<JsonAdaptedProject> projects = new ArrayList<>();
    private JsonNode nextProjectId;
    @JsonIgnore
    private boolean hasInvalidProjects;

    /**
     * Constructs stored client data. Legacy files may omit the projects and next project ID fields.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("remark") String remark, @JsonProperty("tags") List<JsonAdaptedTag> tags) {
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
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        remark = source.getRemark().value;
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
        projects.addAll(source.getProjects().stream()
                .map(JsonAdaptedProject::new)
                .collect(Collectors.toList()));
        nextProjectId = LongNode.valueOf(source.getNextProjectId());
        hasInvalidProjects = false;
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
            if (tag == null || tag.getTagName() == null) {
                throw new IllegalValueException(Tag.MESSAGE_CONSTRAINTS);
            }
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        if (remark == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Remark.class.getSimpleName()));
        }
        final Remark modelRemark = new Remark(remark);

        final Set<Tag> modelTags = new HashSet<>(personTags);
        final List<Project> modelProjects = new ArrayList<>();
        for (JsonAdaptedProject project : projects) {
            modelProjects.add(project.toModelType());
        }
        final long modelNextProjectId = readNextProjectId(modelProjects);
        try {
            return new Person(modelName, modelPhone, modelEmail, modelAddress, modelRemark, modelTags,
                    modelProjects, modelNextProjectId);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage(), e);
        }
    }

    private long readNextProjectId(List<Project> modelProjects) throws IllegalValueException {
        if (nextProjectId == null) {
            return modelProjects.stream().mapToLong(Project::getId).max().orElse(0) + 1;
        }
        if (!nextProjectId.isIntegralNumber() || !nextProjectId.canConvertToLong()) {
            throw new IllegalValueException(Person.MESSAGE_PROJECT_ID_COUNTER_CONSTRAINTS);
        }
        return nextProjectId.longValue();
    }
}

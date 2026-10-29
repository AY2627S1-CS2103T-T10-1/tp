package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.project.Project;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    public static final long MAX_NEXT_PROJECT_ID = (long) Integer.MAX_VALUE + 1;
    public static final String MESSAGE_PROJECT_ID_COUNTER_CONSTRAINTS =
            "Next project ID must be greater than every existing project ID and at most 2147483648.";
    public static final String MESSAGE_DUPLICATE_PROJECT_IDS = "A client cannot have duplicate project IDs.";
    public static final String MESSAGE_DUPLICATE_PROJECTS =
            "A client cannot have projects with the same normalized title and deadline.";

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address;
    private final Remark remark;
    private final Set<Tag> tags = new HashSet<>();
    private final List<Project> projects;
    private final long nextProjectId;

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags) {
        this(name, phone, email, address, remark, tags, List.of(), 1);
    }

    /**
     * Constructs a client with immutable projects and the next unused project ID.
     * A counter of {@link #MAX_NEXT_PROJECT_ID} means all positive integer IDs have been used.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            List<Project> projects, long nextProjectId) {
        requireAllNonNull(name, phone, email, address, remark, tags, projects);
        requireAllNonNull(projects);
        checkProjects(projects, nextProjectId);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.remark = remark;
        this.tags.addAll(tags);
        this.projects = List.copyOf(projects);
        this.nextProjectId = nextProjectId;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    public Remark getRemark() {
        return remark;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public List<Project> getProjects() {
        return projects;
    }

    public long getNextProjectId() {
        return nextProjectId;
    }

    /**
     * Returns a copy with updated projects. The counter cannot decrease even when projects are removed.
     */
    public Person withProjects(List<Project> updatedProjects, long updatedNextProjectId) {
        checkArgument(updatedNextProjectId >= nextProjectId, MESSAGE_PROJECT_ID_COUNTER_CONSTRAINTS);
        return new Person(name, phone, email, address, remark, tags, updatedProjects, updatedNextProjectId);
    }

    private static void checkProjects(List<Project> projects, long nextProjectId) {
        checkArgument(nextProjectId >= 1 && nextProjectId <= MAX_NEXT_PROJECT_ID,
                MESSAGE_PROJECT_ID_COUNTER_CONSTRAINTS);
        Set<Integer> ids = new HashSet<>();
        Set<String> titlesAndDeadlines = new HashSet<>();
        for (Project project : projects) {
            checkArgument(project.getId() < nextProjectId, MESSAGE_PROJECT_ID_COUNTER_CONSTRAINTS);
            checkArgument(ids.add(project.getId()), MESSAGE_DUPLICATE_PROJECT_IDS);
            // ISO dates have a fixed length, so this key cannot collide across different deadlines.
            String titleAndDeadline = project.getDeadline() + ":" + project.getTitle().normalizedValue();
            checkArgument(titlesAndDeadlines.add(titleAndDeadline), MESSAGE_DUPLICATE_PROJECTS);
        }
    }

    /**
     * Returns the v1.4 stored-identity comparison, preserving existing data and staged copies.
     * Commands also use {@link #hasSameClientIdentity(Person)} to reject Unicode case variants.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null && (email.value.equalsIgnoreCase(otherPerson.email.value)
                || (normalizedName().equals(otherPerson.normalizedName()) && phone.equals(otherPerson.phone)));
    }

    /**
     * Returns the name used for duplicate comparison without changing its display value.
     */
    private String normalizedName() {
        return name.fullName.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Returns whether client input would duplicate an email or a case-insensitive name and phone.
     * Retains the stored comparison and adds Unicode simple case matching without changing displayed names.
     */
    public boolean hasSameClientIdentity(Person otherPerson) {
        return isSamePerson(otherPerson) || (otherPerson != null && phone.equals(otherPerson.phone)
                && name.fullName.trim().replaceAll("\\s+", " ")
                        .equalsIgnoreCase(otherPerson.name.fullName.trim().replaceAll("\\s+", " ")));
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && remark.equals(otherPerson.remark)
                && tags.equals(otherPerson.tags)
                && projects.equals(otherPerson.projects)
                && nextProjectId == otherPerson.nextProjectId;
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, remark, tags, projects, nextProjectId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("remark", remark)
                .add("tags", tags)
                .add("projects", projects)
                .add("nextProjectId", nextProjectId)
                .toString();
    }

}

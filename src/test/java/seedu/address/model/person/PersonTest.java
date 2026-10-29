package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;
import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void hasSameClientIdentity_unicodeCaseVariants_preservesContactsAndDisplayValues() {
        Locale originalLocale = Locale.getDefault();
        try {
            for (Locale locale : List.of(Locale.ROOT, Locale.forLanguageTag("tr-TR"))) {
                Locale.setDefault(locale);
                for (List<String> names : List.of(List.of("Işık", "IŞIK"), List.of("İ", "i"),
                        List.of("ΟΣ", "ος"), List.of("Σ", "ς"), List.of("\uD801\uDC00", "\uD801\uDC28"),
                        List.of("Straße", "STRAẞE"), List.of("Acme Studio", " acme   STUDIO "))) {
                    Person first = new PersonBuilder().withName(names.get(0)).withEmail("first@example.com").build();
                    Person second = new PersonBuilder(first).withName(names.get(1))
                            .withEmail("second@example.com").build();
                    assertTrue(first.hasSameClientIdentity(second));
                    assertTrue(second.hasSameClientIdentity(first));
                    assertFalse(first.hasSameClientIdentity(new PersonBuilder(second)
                            .withPhone("81234567").build()));
                    assertEquals(names.get(0), first.getName().fullName);
                    assertEquals(names.get(1), second.getName().fullName);
                }
            }
        } finally {
            Locale.setDefault(originalLocale);
        }
        Person first = new PersonBuilder().withName("Straße").withEmail("first@example.com").build();
        assertFalse(first.hasSameClientIdentity(new PersonBuilder(first).withName("STRASSE")
                .withEmail("second@example.com").build()));
        assertFalse(first.hasSameClientIdentity(null));
        assertTrue(first.hasSameClientIdentity(first));
        assertTrue(first.hasSameClientIdentity(new PersonBuilder(first).withName("Other")
                .withPhone("81234567").withEmail("FIRST@EXAMPLE.COM").build()));
    }

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name, distinct contact details -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // same email, even with a different name -> returns true
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // name differs in case, contact details match -> returns true
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name has trailing spaces, contact details match -> returns true
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertTrue(BOB.isSamePerson(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different remark -> returns false
        editedAlice = new PersonBuilder(ALICE).withRemark("Likes baseball").build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", address=" + ALICE.getAddress()
                + ", remark=" + ALICE.getRemark() + ", tags=" + ALICE.getTags()
                + ", projects=" + ALICE.getProjects() + ", nextProjectId=" + ALICE.getNextProjectId() + "}";
        assertEquals(expected, ALICE.toString());
    }

    @Test
    public void constructor_legacyPerson_hasNoProjectsAndStartsAtOne() {
        assertEquals(List.of(), ALICE.getProjects());
        assertEquals(1, ALICE.getNextProjectId());
    }

    @Test
    public void withProjects_defensiveCopy_preservesContactFields() {
        Project project = project(1, "Website redesign", "2026-10-15");
        List<Project> projects = new ArrayList<>(List.of(project));
        Person updated = ALICE.withProjects(projects, 2);
        projects.clear();
        assertEquals(List.of(project), updated.getProjects());
        assertThrows(UnsupportedOperationException.class, () -> updated.getProjects().clear());
        assertEquals(ALICE.getName(), updated.getName());
        assertEquals(ALICE.getPhone(), updated.getPhone());
        assertEquals(ALICE.getEmail(), updated.getEmail());
        assertEquals(ALICE.getAddress(), updated.getAddress());
        assertEquals(ALICE.getRemark(), updated.getRemark());
        assertEquals(ALICE.getTags(), updated.getTags());
        assertEquals(2, updated.getNextProjectId());
        assertEquals(List.of(), ALICE.getProjects());
        assertEquals(1, ALICE.getNextProjectId());
        assertTrue(ALICE.isSamePerson(updated));
    }

    @Test
    public void withProjects_duplicateIdsOrProjects_throwsIllegalArgumentException() {
        Project project = project(1, "Website redesign", "2026-10-15");
        Project repeatedId = project(1, "Another project", "2026-10-16");
        Project duplicate = new Project(2, new ProjectTitle(" WEBSITE\u00a0 REDESIGN "),
                new ProjectDeadline("2026-10-15"), ProjectStatus.IN_PROGRESS, ProjectPriority.HIGH);
        assertThrows(IllegalArgumentException.class, Person.MESSAGE_DUPLICATE_PROJECT_IDS, () ->
                ALICE.withProjects(List.of(project, repeatedId), 3));
        assertThrows(IllegalArgumentException.class, Person.MESSAGE_DUPLICATE_PROJECTS, () ->
                ALICE.withProjects(List.of(project, duplicate), 3));
        assertThrows(NullPointerException.class, () -> ALICE.withProjects(null, 2));
        assertThrows(NullPointerException.class, () -> ALICE.withProjects(Arrays.asList(project, null), 2));
    }

    @Test
    public void withProjects_differentTitleOrDeadline_allowsProjects() {
        List<Project> projects = List.of(project(1, "Website redesign", "2026-10-15"),
                project(2, "Website redesign", "2026-10-16"), project(3, "Landing page", "2026-10-15"));
        assertEquals(projects, ALICE.withProjects(projects, 4).getProjects());
    }

    @Test
    public void withProjects_legacyUnicodeCaseVariants_preservesPreviouslyAcceptedData() {
        List<Project> projects = List.of(project(1, "IşıkWebsite", "2026-10-15"),
                project(2, "IŞIKWEBSITE", "2026-10-15"));
        assertTrue(projects.getFirst().isSameProject(projects.get(1)));
        assertEquals(projects, ALICE.withProjects(projects, 3).getProjects());
    }

    @Test
    public void withProjects_invalidCounter_throwsIllegalArgumentException() {
        Project project = project(2, "Website redesign", "2026-10-15");
        assertThrows(IllegalArgumentException.class, () -> ALICE.withProjects(List.of(project), 1));
        assertThrows(IllegalArgumentException.class, () -> ALICE.withProjects(List.of(project), 2));
        assertThrows(IllegalArgumentException.class, () -> ALICE.withProjects(List.of(), 0));
        assertThrows(IllegalArgumentException.class, () -> ALICE.withProjects(List.of(), -1));
        assertThrows(IllegalArgumentException.class, () ->
                ALICE.withProjects(List.of(), Person.MAX_NEXT_PROJECT_ID + 1));
        assertThrows(IllegalArgumentException.class, () ->
                ALICE.withProjects(List.of(), Long.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> new Person(ALICE.getName(), ALICE.getPhone(),
                ALICE.getEmail(), ALICE.getAddress(), ALICE.getRemark(), ALICE.getTags(), List.of(), 0));
    }

    @Test
    public void withProjects_removedProjects_doesNotReuseIds() {
        Person withProject = ALICE.withProjects(List.of(project(9, "Website redesign", "2026-10-15")), 10);
        Person afterRemoval = withProject.withProjects(List.of(), 10);
        assertEquals(10, afterRemoval.getNextProjectId());
        assertThrows(IllegalArgumentException.class, () -> afterRemoval.withProjects(List.of(), 1));
        Project lastProject = project(Integer.MAX_VALUE, "Last project", "2026-10-15");
        Person exhausted = ALICE.withProjects(List.of(lastProject), Person.MAX_NEXT_PROJECT_ID);
        assertEquals(Person.MAX_NEXT_PROJECT_ID, exhausted.getNextProjectId());
        assertEquals(Person.MAX_NEXT_PROJECT_ID, exhausted.withProjects(List.of(), Person.MAX_NEXT_PROJECT_ID)
                .getNextProjectId());
    }

    @Test
    public void equalsAndHashCode_includeProjectsAndCounter() {
        Project project = project(1, "Website redesign", "2026-10-15");
        Person withProject = ALICE.withProjects(List.of(project), 2);
        Person copy = ALICE.withProjects(List.of(project), 2);
        assertEquals(withProject, copy);
        assertEquals(withProject.hashCode(), copy.hashCode());
        assertFalse(ALICE.equals(withProject));
        assertFalse(withProject.equals(ALICE.withProjects(List.of(project), 3)));
        assertFalse(ALICE.equals(ALICE.withProjects(List.of(), 2)));
    }

    private static Project project(int id, String title, String deadline) {
        return new Project(id, new ProjectTitle(title), new ProjectDeadline(deadline),
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM);
    }
}

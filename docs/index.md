---
layout: page
title: ClientDesk
---

[![CI Status](https://github.com/AY2627S1-CS2103T-T10-1/tp/workflows/Java%20CI/badge.svg)](https://github.com/AY2627S1-CS2103T-T10-1/tp/actions)

ClientDesk is a keyboard-first desktop CRM for freelancers and independent professionals who type fast and prefer typing to mouse interactions. It is designed to keep client details, projects, deadlines and follow-ups in one place.

ClientDesk is under active development. The current version supports client management and recording or deleting
projects with deadlines, status and priority. Project editing, notes, follow-ups, agenda and undo are planned.

Use `add-project 1 title/Website redesign deadline/2026-10-15 priority/high` to record work for the first displayed
client. Use `view 1` to find its project ID, then `delete-project 1 1` to remove a project.

![Interface preview](images/Ui.png)

* Read the [**User Guide**](UserGuide.html) for the current application's commands.
* Read the [**Developer Guide**](DeveloperGuide.html) and [**setup instructions**](SettingUp.html) to get started with development.
* Track our work in the [**issue tracker**](https://github.com/AY2627S1-CS2103T-T10-1/tp/issues).

**Acknowledgements**

* Based on [AddressBook Level 3](https://se-education.org/addressbook-level3/) by [SE-EDU](https://se-education.org/).
* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit5](https://github.com/junit-team/junit5).

# ClientDesk

[![CI Status](https://github.com/AY2627S1-CS2103T-T10-1/tp/workflows/Java%20CI/badge.svg)](https://github.com/AY2627S1-CS2103T-T10-1/tp/actions)
[![Code coverage](https://codecov.io/gh/AY2627S1-CS2103T-T10-1/tp/graph/badge.svg)](https://codecov.io/gh/AY2627S1-CS2103T-T10-1/tp)

ClientDesk is a keyboard-first desktop CRM for freelancers and independent professionals who type fast and prefer typing to mouse interactions. It is designed to keep client details, projects, deadlines and follow-ups in one place.

ClientDesk v1.4 is the alpha release. It supports client management and recording, viewing, editing and deleting
projects with deadlines, status and priority. This release improves data-file safety, command help and project selection
in the details panel. Notes, follow-ups, agenda and undo are planned for later releases.

To record work for a client, use `add-project 1 title/Website redesign deadline/2026-10-15 priority/high`.
The index refers to the currently displayed client list. Use `view 1` to find a project ID, then
`delete-project 1 1` to remove it.

![Interface preview](docs/images/Ui.png)

- [Project website](https://ay2627s1-cs2103t-t10-1.github.io/tp/)
- [User Guide](docs/UserGuide.md)
- [Developer Guide](docs/DeveloperGuide.md)
- [Development setup](docs/SettingUp.md)
- [Issue tracker](https://github.com/AY2627S1-CS2103T-T10-1/tp/issues)

## Team

- [iians0n](https://github.com/iians0n)
- [ClarenceChoo](https://github.com/ClarenceChoo)
- [ting436](https://github.com/ting436)
- [pang16334](https://github.com/pang16334)

## Acknowledgements

ClientDesk is based on [AddressBook Level 3](https://se-education.org/addressbook-level3/) by [SE-EDU](https://se-education.org/), using the [course starter repository](https://github.com/NUS-CS2103-AY2627-S1/tp).

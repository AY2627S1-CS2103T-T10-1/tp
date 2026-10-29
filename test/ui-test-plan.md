# ClientDesk UI test plan

## View client details

Run ClientDesk with Java 25 and its sample data.

1. Enter `list`, then `view 1`. Expect labeled name, phone, email, address,
   remark and tags matching the first client. The list stays unchanged.
2. Enter `find Bernice`, then `view 1`. Expect Bernice's details; the filtered
   list stays unchanged.
3. Enter `view 999`. Expect `The client index provided is invalid.`
4. Enter `view`, `view 0`, `view -1`, `view abc` and `view 1 extra`.
   Each should show invalid command format and the view usage message.
5. Enter `list`, then `edit 1 p/91234567`, then `view 1`.
   Expect the new phone number and all other fields preserved.
6. Restart the app, then `view 1`. Expect the edited phone number to persist.

The Trackie text UI runner is not available in this JavaFX tP repository.
These GUI checks require manual execution; automated command/parser tests cover
view output, filtered indexes, invalid indexes and input validation.

## Unicode search separators (#117)

In disposable data, add Acme Studio and Benson with different emails.
Run `find Acme Studio`, then paste `find Acme Studio` (U+00A0).
Both must report `Found 1 matching clients.` and show only Acme Studio.
Repeat with U+2003 and U+202F. A whitespace-only search reports usage and preserves the list.
Automated coverage: FindCommandParserTest.

## Edit client email (first increment)

Run the JavaFX app with Java 25. These are manual checks; the iP Trackie
text UI runner is not available in this tP repository.

1. Enter `list`, note the first client's fields, then
   `edit-client 1 e/updated@example.com`. Expect `Updated client: <name>.`
   Only the email changes. Restart and confirm the new email persists.
2. Repeat that command. Expect `No changes needed for <name>.`
3. Find one client, then edit its email using displayed index 1.
   The filtered list remains and other fields stay unchanged.
4. Try another client's email in different letter case. Expect
   `Update would duplicate client: <other name>.` and no changes.
5. Try `edit-client 999 e/valid@example.com`. Expect `No client at index 999.`
6. Try `edit-client 0 e/valid@example.com` and `edit-client -1 e/valid@example.com`.
   Expect `Client index must be a displayed positive number.`
7. Try `edit-client 1`. Expect `Specify at least one field to update.`
8. Try `edit-client`, invalid emails, and repeated e/ prefixes.
   Expect an error and unchanged data.
9. Save failure is covered by an automated storage-failure test; expect the
   prior record and filter to remain, with `Client could not be saved. No changes were made.`

## Typographic apostrophes (#118)

In disposable data, enter `add-client n/O’Connor p/91234567 e/oconnor@example.com`.
Expect success; `view` and restart must preserve the curly apostrophe.
A name containing only apostrophes or more than 80 characters must still fail.
Automated coverage: NameTest.

## Client details panel

1. Start the app: expect ClientDesk title, client list, and a right-hand details
   panel with instructions to enter view INDEX or select a client.
2. Enter `view 1`: expect first displayed client selected and contact details
   in the right panel. A client without projects shows `Projects: No projects yet.`
3. View a client with projects: expect stable IDs, titles, deadlines, statuses
   and priorities; projects are ordered by ID. Scroll to inspect long content.
4. Enter `find NAME`, then `view 1`: expect the first filtered client, not the
   first client in the full list.
5. Enter `view 999` or `view abc`: expect an error and unchanged panel content.
6. Select a different client using the list: expect that client's details.
7. Clear the selection: expect the initial instructions, with no stale record.
8. Resize the window and drag the divider: both panes remain accessible.

These manual GUI cases require JavaFX. The Trackie text UI runner is incompatible
with this repository's GUI test plan; automated JavaFX tests exercise panel behavior.

## Project changes retain client selection (Week 10 Y1)

Use disposable client data with two clients. Repeat with `find` restricting the list
before viewing the target client. Use displayed indexes, not the full-list index.

1. View the client and add a project: the client stays selected and the project
   immediately appears in the details panel without another view command.
2. Add a second project, delete the first: only the remaining project appears.
3. Delete the final project: keep the client selected and show No projects yet.
4. Invalid indexes, invalid dates and missing project IDs leave selection/details unchanged.
5. Simulate failed add/delete saves: retain the selected client and previous projects.
6. Delete the selected client itself: clear selection and show the panel instructions.

Automated coverage: ClientDetailsUiTest runs real command-box actions for both
full and filtered client lists, using temporary JSON storage and simulated IO failures.

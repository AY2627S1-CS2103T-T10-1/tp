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

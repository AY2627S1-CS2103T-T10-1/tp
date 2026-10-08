# ClientDesk UI test plan

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

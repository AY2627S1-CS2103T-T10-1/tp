# Client details UI checks

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

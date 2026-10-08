---
layout: page
title: User Guide
---

AddressBook Level 3 (AB3) is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, AB3 can help you manage contacts faster than traditional GUI applications.

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list-clients` : Lists all clients.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a contact named `John Doe` to the Address Book.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `…`​ can appear zero or more times.<br>
  For example, `[t/TAG]…​` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `list`, `exit`, and `clear`, are ignored.<br>
  For example, `list 123` is interpreted as `list`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Viewing help: `help`

Lists the MVP commands and short descriptions in a scrollable help window. Supply one
command name to view its purpose, format, parameter rules and examples. Command names
are case-insensitive. Help does not change client data.

Format: `help [COMMAND]`

Examples:
* `help`
* `help add-client`
* `help agenda`

An unknown command reports `Unknown command '<value>'. Type help to see all commands.`
More than one command name reports `Usage: help [COMMAND]`.

The Help menu and `F1` show the command overview. **Copy URL** copies the ClientDesk
User Guide link so you can paste it into your browser.


### Adding a client: `add-client`

Creates and saves a client, refreshes the client list and selects the new entry.

Format: `add-client n/NAME p/PHONE e/EMAIL [t/TAG]…`

Examples:
* `add-client n/Acme Studio p/+65 8123 4567 e/hello@acme.sg`
* `add-client n/Northstar Labs p/91234567 e/alex@northstar.io t/startup t/web-design`

The name, phone and email are required; tags are optional and repeatable. Parameters
can be supplied in any order. Command names and prefixes are case-insensitive.
Repeated name, phone or email prefixes and unknown prefixes are rejected.

Field | Rules
------|------
Name | 1–80 characters with at least one letter or number. Letters, numbers, spaces, apostrophes, hyphens, periods, ampersands and parentheses are allowed.
Phone | 7–15 digits, with an optional leading `+`, spaces or hyphens. Formatting is removed before storage.
Email | 3–254 characters, exactly one `@`, non-empty local and domain parts, no spaces and a dot in the domain. Domain labels cannot start or end with a hyphen.
Tag | 1–30 lowercase letters, numbers or internal hyphens; no leading or trailing hyphen. Repeated tags are stored once.

A duplicate has the same email (ignoring case), or the same name (ignoring case,
outer spaces and repeated internal spaces) **and** normalized phone. The same name
alone is allowed. Duplicates report `This client already exists: <name>.`
Missing required fields report `Usage: add-client n/NAME p/PHONE e/EMAIL [t/TAG]...`.
If saving fails, ClientDesk reports `Client could not be saved. No changes were made.`
and keeps the existing data and displayed list.

The inherited address field displays `Not provided` for a new client because
`add-client` does not require an address.

### Adding a person: `add`

Adds a person to the address book.

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]…​`

<div markdown="span" class="alert alert-primary">:bulb: **Tip:**
A person can have any number of tags, including zero.
</div>

Examples:
* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 t/criminal`

### Listing all persons: `list`

Shows a list of all persons in the address book.

Format: `list`

### Listing all clients: `list-clients`

Displays all clients and resets the current search filter. Clients appear in case-insensitive name order; clients with the same name retain their creation order. The existing `list` command remains available.

Format: `list-clients`

Example: `find acme` followed by `list-clients` restores the full client list.

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, …​
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* To remove all of a person's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.

### Finding clients: `find`

Finds clients using partial names, phone numbers, email addresses, or tags.

Format: `find KEYWORD [KEYWORD]...`

* Supply at least one keyword, each containing 1 to 50 characters. Separate keywords with whitespace.
* Matching is case-insensitive and uses substrings; for example, `cme` matches `Acme Studio`.
* **Every keyword must match the same client** (an `AND` search). Keywords may match different fields.
  For example, `find acme startup` matches `Acme Studio` with the tag `startup`. Each client appears once.
* Results appear in case-insensitive name order, with ties in creation order. Indexed commands use the displayed order.
* Matches produce `Found <number> matching clients.`. No matches produce `No clients matched: <keywords>.`
  and restore the full client list in name order. Feedback preserves keyword case and uses single spaces.
* Missing keywords or a keyword exceeding 50 characters produce `Usage: find KEYWORD [KEYWORD]...`.
  The previously displayed list stays visible.
* Searching does not change client records or write the data file.

Examples:

* `find acme` finds clients with `acme` in any searchable field.
* `find startup web` finds clients matching both keywords, such as clients with the tags `startup` and `webdesign`.
* `find 8123` finds clients whose phone number contains `8123`, such as `98123456`.

### Deleting a person: `delete`

Deletes the specified person from the address book.

Format: `delete INDEX`

* Deletes the person at the specified `INDEX`.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, …​

Examples:
* `list` followed by `delete 2` deletes the 2nd person in the address book.
* `find Betsy` followed by `delete 1` deletes the 1st person in the results of the `find` command.

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

AddressBook automatically saves data after commands other than `find` and `help`. You do not need to save manually.
The `find` command only changes the displayed list, and `help` only displays instructions; neither writes the data file.

### Editing the data file

AddressBook data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
If your changes make the data file invalid, AddressBook starts with an empty address book at the next run. The invalid file remains on disk until you run a command that saves data. Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</div>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous AddressBook home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add Client** | `add-client n/NAME p/PHONE e/EMAIL [t/TAG]…`<br> e.g., `add-client n/Acme Studio p/+65 8123 4567 e/hello@acme.sg`
**Add** | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]…​` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 t/friend t/colleague`
**Clear** | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit** | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`<br> e.g., `edit 2 n/James Lee e/jameslee@example.com`
**Find** | `find KEYWORD [KEYWORD]...`<br> e.g., `find acme startup`
**List** | `list`
**List Clients** | `list-clients`
**Help** | `help [COMMAND]`

### Viewing client details: `view`

Displays the existing details of a client without modifying the client or the displayed list.

Format: `view INDEX`

The index is a positive integer from the currently displayed list. After a `find` command,
use the index in the search results. Details include name, phone, email, address, remark and tags.
An index outside the displayed list produces an error.

Example: `view 1` displays the first client in the current list.

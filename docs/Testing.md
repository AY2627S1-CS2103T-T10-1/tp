---
layout: page
title: Testing guide
---

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

Before submitting a Java change, use Java 25 and run `./gradlew check coverage` to include tests and Checkstyle.
On Linux, the JavaFX tests require a display: use `xvfb-run --auto-servernum ./gradlew check coverage`, as CI does.
On macOS, use the prescribed Zulu JDK+FX runtime and a desktop session.
Run `./.github/run-checks.sh` for repository text checks and `git diff --check` for whitespace errors.

<div markdown="span" class="alert alert-secondary">:link: **Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</div>

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has four types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`
1. *JavaFX UI tests* enter commands through the application's real command box and inspect feedback and selection.<br>
   For example: `seedu.address.ui.AddProjectUiTest`

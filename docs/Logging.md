---
layout: page
title: Logging guide
---

* The project uses the `java.util.logging` package for logging.
* `LogsCenter` manages logging levels and destinations.
* A class obtains a `Logger` with `LogsCenter.getLogger(Class)`, which logs messages at the configured level.
* Log messages are written to the console and a `.log` file.
* Command handling logs record recognized command types and outcomes. Command arguments, result text and client records are omitted from command and model-initialization logs, including at detailed logging levels.
* Existing log files from older versions are not rewritten. Review them before sharing, as they may contain client details or remarks that have since been changed or deleted in the app.
* The output logging level can be controlled by changing the `LOG_LEVEL` constant in the `LogsCenter` class.
* **When choosing a level for a log message**, follow the conventions given in [_[se-edu/guides] Java: Logging conventions_](https://se-education.org/guides/conventions/java/logging.html).

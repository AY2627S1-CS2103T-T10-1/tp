---
layout: page
title: Logging guide
---

* The project uses the `java.util.logging` package for logging.
* `LogsCenter` manages logging levels and destinations.
* A class obtains a `Logger` with `LogsCenter.getLogger(Class)`, which logs messages at the configured level.
* Log messages are written to the console and to a rotating file set in the ClientDesk home folder. The file-handler
  base pattern is `<home>/addressbook.log`; Java's rotating handler writes generated files `<home>/addressbook.log.0`
  through `<home>/addressbook.log.4` (the active file is normally `.0`). For a packaged JAR, the home folder is the
  folder containing the JAR; an IDE or class-directory launch uses the process working directory. Each file has a
  5 MiB rotation threshold, and the handler appends to an existing active file instead of rewriting it.
  Concurrent file handlers may add an instance suffix and a `.lck` file; their generated paths are checked as well.
* Command handling logs record recognized command types and outcomes. Command arguments, result text and client records are omitted from command and model-initialization logs, including at detailed logging levels.
* Existing log files from older versions are not rewritten. Review them before sharing, as they may contain client details or remarks that have since been changed or deleted in the app.
* Before opening the file handler, ClientDesk checks existing log, rotation, instance and lock paths to keep them inside the home folder. If a path is unsafe, or the file handler cannot be opened, file logging is disabled and console logging remains available.
* The output logging level can be controlled by changing the `LOG_LEVEL` constant in the `LogsCenter` class.
* **When choosing a level for a log message**, follow the conventions given in [_[se-edu/guides] Java: Logging conventions_](https://se-education.org/guides/conventions/java/logging.html).

---
layout: page
title: Documentation guide
---

**Setting up and maintaining the project website:**

* We use [**Jekyll**](https://jekyllrb.com/) to manage documentation.
* The `docs/` folder is used for documentation.
* To learn how to set up and maintain the project website, follow the guide [_[se-edu/guides] **Using Jekyll for project documentation**_](https://se-education.org/guides/tutorials/jekyll.html).
* Note these points when adapting the documentation to a different project/product:
  * The 'Site-wide settings' section of the page linked above has information on how to update site-wide elements such as the top navigation bar.
  * :bulb: Site title, navigation and repository links are configured in `docs/_config.yml`. The print header in
    `docs/_sass/minima/_base.scss` also contains the product name, currently `ClientDesk`.
* If you are using IntelliJ for editing documentation files, you can consider enabling 'soft wrapping' for `*.md` files, as explained in [_[se-edu/guides] **Intellij IDEA: Useful settings**_](https://se-education.org/guides/tutorials/intellijUsefulSettings.html#enabling-soft-wrapping)


**Style guidance:**

* Follow the [**_Google developer documentation style guide_**](https://developers.google.com/style).

* Also relevant is the [_[se-edu/guides] **Markdown coding standard**_](https://se-education.org/guides/conventions/markdown.html)

**Diagrams:**

* See the [_[se-edu/guides] **Using PlantUML**_](https://se-education.org/guides/tutorials/plantUml.html)
* Keep editable `.puml` sources in `docs/diagrams` and their rendered images in `docs/images`. Regenerate the linked
  image whenever its source changes, and check that class/method names and save-before-publish ordering match the
  implementation. Mark future designs explicitly so they cannot be mistaken for implemented behavior.

**Checking a documentation change:**

1. Check commands against the current parser and domain validation. Use disposable data to verify examples and
   manual-test sequences in their documented order, including the stated starting state and expected results.
1. Check relative links, section anchors and image paths. Keep user instructions in the UG and implementation
   rationale in the DG; link to an existing explanation instead of maintaining conflicting copies.
1. With Ruby and the Bundler version in `docs/Gemfile.lock` installed, run `bundle install` from `docs`, then
   `bundle exec jekyll build` or `bundle exec jekyll serve`. Inspect the rendered pages, tables, navigation and diagrams.
1. Run `./.github/run-checks.sh` and `git diff --check` from the repository root. Before a release, ensure the deployed
   guides describe the same tested version as the JAR.

**Converting a document to the PDF format:**

* See the guide [_[se-edu/guides] **Saving web documents as PDF files**_](https://se-education.org/guides/tutorials/savingPdf.html)

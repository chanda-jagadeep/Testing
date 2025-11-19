Build & Automate an Intelligent Registration Form

A polished, automated test suite for a dynamic registration form (HTML / CSS / JavaScript) with Selenium-based end-to-end tests in Java. This repository contains the frontend form (index.html, styles.css, script.js) and three automated test flows (Positive, Negative, Flow C) that verify functionality, validation, and edge cases. The tests capture screenshots and page sources for debugging and are designed to run against a locally hosted version of the form.

Project snapshot / assignment brief

<img width="1920" height="847" alt="error-state" src="https://github.com/user-attachments/assets/6f9482e8-e3f7-46e0-afb0-528cb9e07553" />
Fig:Error State 
<img width="1920" height="847" alt="flow-c-success" src="https://github.com/user-attachments/assets/ffb323e3-3e25-42ec-9941-bd412da51f15" />
Fig : flow-c-success
<img width="1920" height="847" alt="success-state" src="https://github.com/user-attachments/assets/ee469568-a604-440c-98a6-a0182606583c" />
Fig : success-state

Features:
Dynamic Country → State → City selection populated from a data object.
Client-side validation for: required fields, email (including disposable-domain blocking), phone formats (with country code), password strength & confirmation, gender and terms acceptance.
Password strength meter and user-friendly error messages.
Three automation test flows
PositiveRegistrationTest — Valid end-to-end registration scenario (success message).
NegativeRegistrationTest — Missing required field(s) and expected error state.
FlowCRegistrationTest — Advanced negative cases: invalid email, invalid phone, password mismatch, and unchecked terms.


Prerequisites:
Java 17+
Maven 3.8+
Google Chrome (latest recommended)
IDE (Eclipse / VS Code) — optional
Local server to serve static files (Live Server in VS Code or python -m http.server)
Maven dependencies (managed in pom.xml) include Selenium, WebDriverManager, JUnit 5, and slf4j-simple (optional, to silence SLF4J messages).

Setup & Run (frontend):
Place frontend files (index.html, styles.css, script.js) in a folder, e.g. frontend/.
VS Code Live Server:
Right-click index.html → Open with Live Server, copy the provided URL.
Confirm the form opens in your browser at:
http://127.0.0.1:5500/index.html  (or)  http://localhost:8000/index.html

Setup & Run (tests)
Open the Maven project in your IDE (or run from terminal).
Edit the PAGE constant in the three test files to match your local form URL, e.g.:
private final String PAGE = "http://127.0.0.1:5500/index.html";


In Eclipse : Right-click project → Maven → Update Project.

Run tests:

Run all tests via Maven:
mvn test

Run a single test:
mvn -Dtest=PositiveRegistrationTest test
mvn -Dtest=NegativeRegistrationTest test
mvn -Dtest=FlowCRegistrationTest test

Tests create artifacts in the project root: success-state.png, error-state.png, flow-c-success.png, and failure HTML snapshots like flow-c-page-failure.html.

What each test does (summary):
PositiveRegistrationTest — Fills all fields with valid data, selects country/state/city by index, accepts terms, clicks Submit, waits for .success-msg, and saves a screenshot on success.
NegativeRegistrationTest — Leaves a mandatory field empty (e.g., lastName), triggers validation (via blur & validateField()), waits for the appropriate error element (by computed style & innerText), and saves diagnostics.
FlowCRegistrationTest — Intentionally inputs invalid email/phone, mismatched passwords, leaves terms unchecked; triggers validation and asserts specific error messages and that Submit remains disabled.

Debugging tips (common issues):
Directory listing in browser → use the full path to index.html (e.g., /index.html) or rename index.html as index.html so server defaults to it.
Country/State not populated → ensure script.js loads (check DevTools Network tab).
Timeouts in tests → increase explicit wait time in tests or ensure the PAGE constant matches the exact server URL.
FileAlreadyExistsException when saving screenshots → tests use REPLACE_EXISTING in provided versions; make sure you have write permissions in the project folder.
SLF4J console messages → harmless; add slf4j-simple to pom.xml to silence them (already included in the recommended pom).

Recommended pom.xml (high level):
Make sure your pom includes:
selenium-java
junit-jupiter (scope test)
webdrivermanager

slf4j-simple (optional)

(You can find the tested pom used during development in the project.)

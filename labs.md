# The Quest for the Holy Grails: Grails 7 Labs

These labs build a complete Apache Grails 7 application, one stage at a time, around a Monty Python quest domain: knights, castles, quests, tasks, and the enemies that stand in the way. Each lab ends with a git tag, so you can skip ahead, catch up, or compare your work against the finished stage at any point.

The labs accompany the talk *The Quest for the Holy Grails: A Grails 7 Tutorial* given at Community Over Code, Glasgow, 12 October 2026. The talk walks through a subset of these labs live; the rest are here for you to do on your own.

## Prerequisites

- **JDK 21** (any JDK from 17 to 24 works; see the note below about Java 25)
- **Grails 7.2.4** (the current release; Grails 8 is in pre-release at the time of writing)
- **Gradle 8.14.5** (provided by the wrapper, nothing to install)
- An IDE with Groovy support. IntelliJ IDEA Ultimate has a Grails plugin; Community Edition works as a plain Gradle project.
- **Optional:** a container runtime such as Docker Desktop, OrbStack, or Colima, for the Testcontainers browser test in the final lab

The easiest way to get matching tool versions is [SDKMAN](https://sdkman.io):

```bash
sdk install java 21.0.8-tem
sdk install grails 7.2.4
```

The project contains an `.sdkmanrc` file that pins both versions. Once you have them installed, running `sdk env` inside the project directory selects them for your current shell.

> **Note:** Why not Java 25? Grails 7.2.4 builds with Gradle 8.14.5, and Gradle 8.14 officially supports Java 17 through 24. Java 25 needs Gradle 9.1 or later, and the Grails 7 Gradle plugins do not work on Gradle 9; that combination arrives with Grails 8. The build does run on JDK 25 from the command line, but IntelliJ IDEA enforces Gradle's compatibility table and refuses to sync, so stay on 21.

> [!IMPORTANT]
> SDKMAN adds newly installed tools to your `PATH` when a shell starts. If you install Grails and then get `grails: command not found` in a terminal you already had open, either open a new terminal or run `source ~/.sdkman/bin/sdkman-init.sh`.

### Using the git tags

The repository is tagged at the end of every lab. To see where a lab finishes, or to start a lab from a known-good state, check out its tag:

```bash
git checkout step1-quest
```

To return to the latest state, check out `main`. Tag names appear at the end of each lab under **Checkpoint**.

## Table of Contents

0. [Creating the Project](#lab-0-creating-the-project)
1. [The First Domain Class](#lab-1-the-first-domain-class)
2. [Adding a Related Domain Class](#lab-2-adding-a-related-domain-class)
3. [Unit Testing Domain Classes](#lab-3-unit-testing-domain-classes)
4. [Dynamic Finders, Criteria, and Where Queries](#lab-4-dynamic-finders-criteria-and-where-queries)
5. [Completing the Domain Model](#lab-5-completing-the-domain-model)
6. [Scaffolding with @Scaffold](#lab-6-scaffolding-with-scaffold)
7. [A Geocoder Service](#lab-7-a-geocoder-service)
8. [Mapping the Castles](#lab-8-mapping-the-castles)
9. [What's New in Grails 7](#lab-9-whats-new-in-grails-7)
10. [Enemies: Inheritance in GORM](#lab-10-enemies-inheritance-in-gorm)
11. [Optional: A JSON API for the Quest](#lab-11-optional-a-json-api-for-the-quest)

## Lab 0: Creating the Project

Grails applications start life in **Grails Forge**, the web-based project generator at https://grails.apache.org/start. Forge replaces the older `grails create-app` wizard as the recommended starting point, and it also has a plain HTTP API, which is what we use here so that the result is reproducible.

### Step 1: Generate the application

1. Create a directory for the project and change into it:

   ```bash
   mkdir holygrails7
   cd holygrails7
   ```

2. Download the generated application from the Forge API. The URL names the application type (`web`), the fully qualified application name, and the options we want:

   ```bash
   curl -L -o app.zip "https://latest.grails.org/create/web/com.kousenit.holygrails?jdkVersion=JDK_21&gorm=HIBERNATE&servlet=TOMCAT&test=SPOCK&features=testcontainers"
   ```

   The pieces of that URL:

   | Part | Meaning |
   |---|---|
   | `web` | A full web application with GSP views, as opposed to `rest-api` |
   | `com.kousenit.holygrails` | Package `com.kousenit`, application name `holygrails` |
   | `gorm=HIBERNATE` | GORM over Hibernate with an H2 in-memory database |
   | `test=SPOCK` | Spock for unit and integration tests |
   | `features=testcontainers` | Adds Testcontainers for the containerized Geb browser tests in Lab 9 |

   If you prefer the web page, choose the same options there, click **Generate Project**, and download the zip.

3. Unzip the archive into the current directory. The zip contains a single top-level folder named after the application, so flatten it:

   ```bash
   unzip -q app.zip
   mv holygrails/* holygrails/.[!.]* .
   rmdir holygrails
   rm app.zip
   ```

4. Look at what you have:

   ```
   build.gradle           Gradle build using the Grails BOM and Gradle plugins
   gradle.properties      grailsVersion=7.2.4
   grails-app/            The Grails application: conf, controllers, domain, services, views, ...
   src/main/groovy        Plain Groovy sources
   src/test/groovy        Unit tests
   src/integration-test   Integration and functional tests
   grailsw, gradlew       Wrapper scripts; no global install needed
   ```

> **Note:** Every Grails 7 artifact lives under the `org.apache.grails` group. If you have an older Grails project, you will see `org.grails` coordinates instead. The `build.gradle` here uses `platform("org.apache.grails:grails-bom:$grailsVersion")` so that individual artifact versions are never spelled out.

### Step 2: Pin the tool versions

1. Create a file called `.sdkmanrc` in the project root:

   ```
   java=21.0.8-tem
   grails=7.2.4
   ```

2. Select those versions for your shell:

   ```bash
   sdk env
   ```

   SDKMAN prints the versions it switched to. From now on, run `sdk env` whenever you open a new terminal in this project.

### Step 3: Build and test

1. Run the (currently empty) test suite to download dependencies and prove the build works:

   ```bash
   ./gradlew test
   ```

   The first run downloads the Gradle distribution and the Grails dependencies, so give it a minute. It should finish with `BUILD SUCCESSFUL`.

> [!IMPORTANT]
> If the build fails with *Configuration cache problems found in this build* and mentions the `buildProperties` task, you have `org.gradle.configuration-cache=true` in your `~/.gradle/gradle.properties`. Gradle lets that file override the project's own `gradle.properties`, so the fix has to go in `build.gradle`. Add this at the bottom:
>
> ```groovy
> // Grails 7.2.4's buildProperties task captures the Project object, which the
> // Gradle configuration cache cannot serialize. Opt the task out so the build
> // works even when ~/.gradle/gradle.properties enables the cache.
> tasks.matching { it.name == 'buildProperties' }.configureEach {
>     notCompatibleWithConfigurationCache('Grails 7.2.4 buildProperties references Project')
> }
> ```
>
> The `matching` form is needed because the Grails Gradle plugin registers the task after the build script has been evaluated, so `tasks.named('buildProperties')` fails with *Task not found*.

### Step 4: Run the application

1. Start the app:

   ```bash
   ./grailsw run-app
   ```

   or, equivalently, `./gradlew bootRun`. Both start an embedded Tomcat on port 8080.

2. Watch the console. Grails 7 prints a banner with the versions it is running on:

   ```
          holygrails7: 0.1 | JVM: Eclipse Adoptium 21.0.8 | Grails: 7.2.4
               Groovy: 4.0.33 | Spring Boot: 3.5.16 | Spring: 6.2.19
   Grails application running at http://localhost:8080 in environment: development
   ```

   That banner is new in Grails 7, and the dependency versions in it were added in 7.1. It is the quickest way to answer "what is this app actually running on?"

3. Open http://localhost:8080 in a browser. The welcome page lists the installed plugins, the controllers (none yet), and the artefacts in the application.

4. Stop the application with Ctrl-C.

### Step 5: Import into IntelliJ IDEA

1. From the project root, run `idea .` if you have the JetBrains Toolbox command-line launcher, or use **File → Open...** and choose the project directory.
2. IntelliJ recognizes `build.gradle` and imports the project. Accept the defaults.
3. Check **Settings → Build, Execution, Deployment → Build Tools → Gradle** and make sure **Gradle JVM** is the JDK you selected with SDKMAN. IntelliJ does not read `.sdkmanrc`.
4. With IntelliJ Ultimate, the Grails plugin recognizes the `grails-app` layout and adds a **Grails** tool window. Community Edition shows a normal Gradle project, which is all these labs require.

### Step 6: Put it under version control

1. Forge generates a `.gitignore` that covers `build/`, `.gradle/`, and IDE files. Initialize the repository and commit the untouched starter:

   ```bash
   git init -b main
   git add -A
   git commit -m "Grails 7.2.4 starter app from Forge"
   git tag step0-starter
   ```

   Committing the generated project before touching it means every later diff shows exactly what *you* added.

### Key Learning Points

- Grails Forge at https://grails.apache.org/start generates projects, and its HTTP API makes the generation reproducible from a single `curl` command.
- Grails 7 artifacts use the `org.apache.grails` group and are versioned through a BOM, so `build.gradle` names no individual versions.
- `./grailsw run-app` and `./gradlew bootRun` are the same thing: an embedded Tomcat running a Spring Boot application.
- The startup banner reports the JVM, Grails, Groovy, Spring Boot, and Spring versions.
- SDKMAN's `.sdkmanrc` and `sdk env` keep tool versions consistent across machines.

### Checkpoint

```bash
git checkout step0-starter
```

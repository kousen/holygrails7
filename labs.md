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

## Lab 1: The First Domain Class

Everything in a Grails application radiates out from its **domain classes**. A domain class is a Groovy class in `grails-app/domain` that GORM, the Grails object-relational mapper, turns into a database table, a set of persistence methods, and a validation contract. In this lab you create the `Quest` class, give it a constraint, generate a web interface for it, and fix the tests that Grails generates alongside.

### Step 1: Create the domain class

1. From the project root, create the class with the Grails CLI:

   ```bash
   ./grailsw create-domain-class Quest
   ```

   The command creates two files and reports them:

   ```
   | Created grails-app/domain/com/kousenit/Quest.groovy
   | Created src/test/groovy/com/kousenit/QuestSpec.groovy
   ```

   The package comes from `grails.codegen.defaultPackage` in `grails-app/conf/application.yml`, which Forge set to `com.kousenit`.

2. Open `grails-app/domain/com/kousenit/Quest.groovy`. The generated class is nearly empty:

   ```groovy
   package com.kousenit

   class Quest {

       static constraints = {
       }
   }
   ```

### Step 2: Add a property and a constraint

1. Give the quest a name, a `toString` so it reads well in the scaffolded pages, and a constraint that forbids blank names:

   ```groovy
   package com.kousenit

   class Quest {
       String name

       String toString() { name }

       static constraints = {
           name blank: false
       }
   }
   ```

   The `constraints` block is a Groovy DSL. Each line names a property and applies one or more constraints to it. `blank: false` rejects the empty string and whitespace-only strings. Every property is also **non-nullable by default** in GORM, so you never have to say `nullable: false`.

> **Note:** GORM supplies `id` and `version` properties automatically. You will see both as columns in the `QUEST` table and the `version` column drives optimistic locking on updates.

### Step 3: Generate the scaffolding

Grails can generate a complete create, read, update, delete interface for a domain class. Grails 7 offers two styles: `generate-all` writes real source files you can read and edit, and the `@Scaffold` annotation (Lab 6) generates everything at runtime. We start with the files so you can see what a Grails controller looks like.

1. Run the generator with the fully qualified class name:

   ```bash
   ./grailsw generate-all com.kousenit.Quest
   ```

2. Look at what appeared:

   | File | Purpose |
   |---|---|
   | `grails-app/controllers/com/kousenit/QuestController.groovy` | Seven actions: index, show, create, save, edit, update, delete |
   | `grails-app/services/com/kousenit/QuestService.groovy` | A GORM **data service**: an interface GORM implements for you |
   | `grails-app/views/quest/{index,show,create,edit}.gsp` | Groovy Server Pages for each screen |
   | `src/test/groovy/com/kousenit/QuestControllerSpec.groovy` | Unit test for the controller |
   | `src/integration-test/groovy/com/kousenit/QuestServiceSpec.groovy` | Integration test for the service |

3. Read `QuestService.groovy`. It is only an interface:

   ```groovy
   @Service(Quest)
   interface QuestService {
       Quest get(Serializable id)
       List<Quest> list(Map args)
       Long count()
       void delete(Serializable id)
       Quest save(Quest quest)
   }
   ```

   GORM reads the method names and generates the implementation at compile time. Each method is transactional. The controller injects it by property name, `QuestService questService`, and calls it rather than touching the domain class directly.

4. Skim `QuestController.groovy`. Notice the pattern in `save`: bind the request to a `Quest`, call the service, and on a `ValidationException` re-render the `create` view with the errors. The `request.withFormat` block means the same action answers an HTML form with a redirect and a JSON client with a `201 Created`.

### Step 4: Run it and break it

1. Start the application:

   ```bash
   ./grailsw run-app
   ```

2. Open http://localhost:8080. The welcome page now lists `QuestController` under **Available Controllers**. Click it.

3. Click **New Quest**, enter `Seek the grail`, and save. You get a show page for quest 1, and the list page shows your quest with its `toString` value.

4. Now click **New Quest** again and type a few spaces into the name. The browser's HTML5 `required` attribute may stop you first, since the scaffolding adds it for non-nullable properties. If so, submit a quest with a single space instead of nothing. The error that comes back is:

   ```
   Property [name] of class [class com.kousenit.Quest] cannot be null
   ```

   That is the *nullable* error, not the *blank* one. Grails data binding trims strings and, by default, converts empty strings to `null` before validation runs, so `blank: false` never gets a look.

5. Stop the app with Ctrl-C.

### Step 5: Make the blank constraint fire, with a better message

1. Turn off the empty-string conversion in `grails-app/conf/application.yml`, inside the existing `grails:` block:

   ```yaml
   grails:
       codegen:
           defaultPackage: com.kousenit
       databinding:
           convertEmptyStringsToNull: false
   ```

   Trimming still happens, so a name of three spaces becomes the empty string, which `blank: false` rejects.

2. Replace the default error text. Grails looks up validation messages by the key `className.propertyName.constraint`. Add this line to the end of `grails-app/i18n/messages.properties`:

   ```properties
   quest.name.blank=Quests must have a name
   ```

3. Run the app again and submit a quest with a blank name. Now you see **Quests must have a name**.

### Step 6: Fix the generated tests

`generate-all` writes test skeletons that fail on purpose, each with a TODO and an `assert false`, so you cannot forget them. Run the unit tests and watch three fail:

```bash
./gradlew test
```

1. Replace the body of `src/test/groovy/com/kousenit/QuestSpec.groovy` with two real tests:

   ```groovy
   package com.kousenit

   import grails.testing.gorm.DomainUnitTest
   import spock.lang.Specification

   class QuestSpec extends Specification implements DomainUnitTest<Quest> {

       void "a quest with a name is valid"() {
           expect:
           new Quest(name: 'Seek the grail').validate()
       }

       void "a quest needs a name"() {
           when:
           Quest quest = new Quest(name: ' ')

           then:
           !quest.validate()
           quest.errors['name'].code == 'blank'
       }
   }
   ```

   `DomainUnitTest` gives the class its GORM methods without a database. The second test depends on the `convertEmptyStringsToNull` setting from Step 5; without it, the error code would be `nullable`.

2. In `src/test/groovy/com/kousenit/QuestControllerSpec.groovy`, replace the `populateValidParams` method:

   ```groovy
   def populateValidParams(params) {
       assert params != null
       params["name"] = 'Seek the grail'
   }
   ```

   Everything else in that spec is already correct. It mocks `QuestService` with Spock's `Mock()` and checks redirects and flash messages for every action.

3. In `src/integration-test/groovy/com/kousenit/QuestServiceSpec.groovy`, replace `setupData` and the `save` test. The other tests expect five quests and need the id of one of them:

   ```groovy
   private Long setupData() {
       new Quest(name: 'Seek the grail').save(flush: true, failOnError: true)
       new Quest(name: 'Find a shrubbery').save(flush: true, failOnError: true)
       Quest quest = new Quest(name: 'Cross the Bridge of Death').save(flush: true, failOnError: true)
       new Quest(name: 'Storm the Castle Anthrax').save(flush: true, failOnError: true)
       new Quest(name: 'Consult Tim the Enchanter').save(flush: true, failOnError: true)
       quest.id
   }
   ```

   ```groovy
   void "test save"() {
       when:
       Quest quest = new Quest(name: 'Seek the grail')
       questService.save(quest)

       then:
       quest.id != null
   }
   ```

   Also change the `get` test to use the returned id rather than a hard-coded `1`, and delete the two remaining `assert false` lines. The spec is annotated `@Integration`, which boots the whole application, and `@Rollback`, which undoes each test's database changes.

4. Run both test phases:

   ```bash
   ./gradlew test
   ./gradlew integrationTest
   ```

> [!IMPORTANT]
> The integration phase includes `HolygrailsSpec`, a functional test Forge generated that extends `ContainerGebSpec`. It drives a real browser running in a Docker container, so **Docker Desktop (or OrbStack, Colima, Podman, Rancher) must be running**, and the first run downloads a browser image. If you want to skip it for now, run only the service spec:
>
> ```bash
> ./gradlew integrationTest --tests 'com.kousenit.QuestServiceSpec'
> ```

> [!WARNING]
> **On Apple silicon the Geb test fails out of the box.** The default image, `selenium/standalone-chrome`, is published for amd64 only. Docker runs it under emulation and Chrome dies the moment the test connects, with `NoSuchSessionException: session deleted as the browser has closed the connection`. Firefox's image is multi-arch, so switch to it. Create `src/integration-test/resources/GebConfig.groovy`:
>
> ```groovy
> import org.openqa.selenium.firefox.FirefoxOptions
> import org.openqa.selenium.remote.RemoteWebDriver
>
> driver = { new RemoteWebDriver(new FirefoxOptions()) }
> containerBrowser = 'firefox'
> ```
>
> and add the Firefox driver next to the Geb line in `build.gradle`:
>
> ```groovy
> integrationTestImplementation testFixtures("org.apache.grails:grails-geb")
> integrationTestImplementation "org.seleniumhq.selenium:selenium-firefox-driver"
> ```
>
> The plugin builds the image name as `selenium/standalone-<containerBrowser>`. Chromium also has an arm64 image, but Testcontainers rejects it as an unknown substitute for the Chrome image, so Firefox is the one-line answer.

### Key Learning Points

- A domain class is a plain Groovy class in `grails-app/domain`. GORM adds `id`, `version`, persistence methods, and validation.
- Constraints are a DSL. Properties are non-nullable unless you say otherwise.
- `generate-all` produces a controller, a GORM data service interface, four GSP views, and two test skeletons. The skeletons fail until you fill them in.
- Data binding trims strings and converts empty strings to `null` by default. Set `grails.databinding.convertEmptyStringsToNull: false` when you want `blank: false` to do its job.
- Validation messages follow the key pattern `className.propertyName.constraint` in `messages.properties`.
- `@Integration` tests boot the application; `@Rollback` keeps them from leaving data behind.
- `ContainerGebSpec` runs functional tests in a containerized browser. On Apple silicon, point `GebConfig.groovy` at Firefox.

### Checkpoint

```bash
git checkout step1-quest
```

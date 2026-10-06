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

## Lab 2: Adding a Related Domain Class

A quest is a list of tasks. In this lab you add a `Task` class, make it belong to a `Quest`, and use three kinds of constraint: a range, a cross-property validator, and the defaults GORM applies on its own. Along the way you meet `java.time` in domain classes, derived properties, and automatic timestamps.

### Step 1: Create the Task class

1. Generate the class and its test skeleton:

   ```bash
   ./grailsw create-domain-class Task
   ```

2. Replace the contents of `grails-app/domain/com/kousenit/Task.groovy`:

   ```groovy
   package com.kousenit

   import groovy.transform.ToString

   import java.time.LocalDate

   @ToString(includeNames = true, includes = ['name', 'priority', 'completed'])
   class Task {
       String name
       int priority = 3
       LocalDate startDate = LocalDate.now()
       LocalDate endDate = LocalDate.now()
       boolean completed

       static belongsTo = [quest: Quest]

       int getDuration() { (endDate - startDate) + 1 }

       static constraints = {
           name blank: false
           priority range: 1..5
           endDate validator: { LocalDate value, Task task ->
               value >= task.startDate
           }
       }
   }
   ```

   Points to notice:

   - **`belongsTo`** makes `Quest` the owning side. Deleting a quest cascades to its tasks, and `quest` becomes a required property on `Task`.
   - **`range: 1..5`** uses a Groovy range as a constraint. Values outside it fail with the codes `range.toosmall` or `range.toobig`.
   - **`validator:`** takes a closure. With two parameters it receives the property value and the whole instance, so it can compare `endDate` to `startDate`. Returning `false` produces the error code `validator.invalid`.
   - **`getDuration()`** is a getter with no backing field. GORM treats getter-only properties as non-persistent, so there is no `duration` column and no `static transients` list is needed, unlike older Grails versions.
   - **`LocalDate`** maps to a `DATE` column. The subtraction `endDate - startDate` is Groovy's operator overloading for dates, which returns the number of days between them.

> [!IMPORTANT]
> That subtraction will not compile as the project stands. Groovy 4 is modular, and the date operators live in the `groovy-datetime` module. Grails includes `groovy-json`, `groovy-sql`, `groovy-templates`, and `groovy-xml`, but not `groovy-datetime`. Add it to the `dependencies` block of `build.gradle`, right after `grails-core`. The Groovy BOM supplies the version:
>
> ```groovy
> implementation "org.apache.groovy:groovy-datetime"
> ```
>
> You get `date - date`, `date + 1`, `date.format('MMM d')`, and ranges over dates in return.

   - **`@ToString`** from Groovy generates `toString()` so you do not have to write one.

### Step 2: Complete the other side of the relationship

1. Edit `grails-app/domain/com/kousenit/Quest.groovy` to declare the collection of tasks and two timestamp properties:

   ```groovy
   package com.kousenit

   import java.time.LocalDateTime

   class Quest {
       String name
       LocalDateTime dateCreated
       LocalDateTime lastUpdated

       static hasMany = [tasks: Task]

       String toString() { name }

       static constraints = {
           name blank: false
       }
   }
   ```

   `hasMany` gives `Quest` a `Set<Task> tasks` plus an `addToTasks` method. The property names `dateCreated` and `lastUpdated` are special: GORM fills them in automatically on insert and update. They work with `java.time` types as well as `java.util.Date`.

### Step 3: Generate the scaffolding

```bash
./grailsw generate-all com.kousenit.Task
```

This produces the same set of files as for `Quest`: controller, data service, four views, and two test skeletons. Open `grails-app/views/task/create.gsp` and notice that the whole form is one tag, `<f:all bean="task" .../>` from the Fields plugin, which renders a widget for each property: a select for `quest` populated from the database, day, month, and year selects for the dates, and a checkbox for `completed`.

### Step 4: Try it

1. Start the app with `./grailsw run-app` and create a quest, then go to **TaskController → New Task**. The **Quest** dropdown lists your quest by its `toString` value.

2. Create a task with an end date earlier than its start date. The default error reads:

   ```
   Property [endDate] of class [class com.kousenit.Task] with value [...] does not pass custom validation
   ```

3. Replace that with a message. The key for a custom validator is `className.propertyName.validator.invalid`. Add to `grails-app/i18n/messages.properties`:

   ```properties
   task.endDate.validator.invalid=A task cannot end before it starts
   ```

   Restart and try again. Then try a priority of 9 and read the range error, which already includes the bounds.

4. Open the quest's show page. The scaffolding renders the `tasks` collection as links. The `dateCreated` and `lastUpdated` values are stored but not shown; the scaffolding templates leave them out on purpose.

### Step 5: Fill in the tests

1. Replace `src/test/groovy/com/kousenit/TaskSpec.groovy`:

   ```groovy
   package com.kousenit

   import grails.testing.gorm.DomainUnitTest
   import spock.lang.Specification

   class TaskSpec extends Specification implements DomainUnitTest<Task> {

       Quest quest = new Quest(name: 'Seek the grail')
       Task task = new Task(name: 'Defeat the Black Knight', quest: quest)

       void "a task with defaults is valid"() {
           expect:
           task.validate()
           task.priority == 3
           !task.completed
       }

       void "a task that starts and ends today lasts one day"() {
           expect:
           task.duration == 1
       }

       void "the end date may not precede the start date"() {
           when:
           task.endDate = task.startDate.minusDays(1)

           then:
           !task.validate()
           task.errors['endDate'].code == 'validator.invalid'
       }
   }
   ```

   Spock creates a fresh `TaskSpec` instance for every feature method, so the `task` field is a new object in each test.

2. In `TaskControllerSpec.groovy`, provide the valid parameters:

   ```groovy
   def populateValidParams(params) {
       assert params != null
       params["name"] = 'Defeat the Black Knight'
       params["priority"] = 2
   }
   ```

3. In `src/integration-test/groovy/com/kousenit/TaskServiceSpec.groovy`, every task needs a quest, so `setupData` creates one first:

   ```groovy
   private Long setupData() {
       Quest quest = new Quest(name: 'Seek the grail').save(flush: true, failOnError: true)
       new Task(name: 'Run away from killer rabbit', quest: quest).save(flush: true, failOnError: true)
       new Task(name: 'Answer the Bridgekeeper', priority: 4, quest: quest).save(flush: true, failOnError: true)
       Task task = new Task(name: 'Defeat the Black Knight', completed: true, quest: quest).save(flush: true, failOnError: true)
       new Task(name: 'Bring out your dead', quest: quest).save(flush: true, failOnError: true)
       new Task(name: 'Find a shrubbery', priority: 2, quest: quest).save(flush: true, failOnError: true)
       task.id
   }
   ```

   and the `save` test does the same:

   ```groovy
   void "test save"() {
       when:
       Quest quest = new Quest(name: 'Seek the grail').save(flush: true, failOnError: true)
       Task task = new Task(name: 'Defeat the Black Knight', quest: quest)
       taskService.save(task)

       then:
       task.id != null
   }
   ```

   As in Lab 1, change the `get` test to use the returned id and remove the leftover `assert false` lines.

4. Run everything:

   ```bash
   ./gradlew test
   ./gradlew integrationTest
   ```

### Key Learning Points

- `belongsTo` on the child and `hasMany` on the parent make a one-to-many with cascading deletes and an `addTo...` method.
- Constraints include ranges and custom validator closures; the validator's second parameter is the whole instance.
- Getter-only properties are derived, not persisted. Grails 7 needs no `transients` declaration for them.
- `dateCreated` and `lastUpdated` are filled in by GORM and work with `java.time`.
- Groovy 4 is modular. Grails 7 ships `groovy-json`, `groovy-sql`, `groovy-templates`, and `groovy-xml`; add `groovy-datetime` yourself for date operators.
- Custom validator messages use the key `className.propertyName.validator.invalid`.

### Checkpoint

```bash
git checkout step2-task
```

## Lab 3: Unit Testing Domain Classes

Grails tests are written in [Spock](https://spockframework.org), and the `grails-testing-support` libraries supply traits that stand up just enough of Grails for the class under test. In this lab you test every constraint on `Task` without a database, and you learn the error codes each constraint produces, which is what your `messages.properties` keys are built from.

### Step 1: Know the traits

Open `src/test/groovy/com/kousenit/TaskSpec.groovy`. It implements `DomainUnitTest<Task>`. That trait gives `Task` its GORM methods (`validate()`, `save()`, `count()`, dynamic finders) backed by an in-memory simple datastore, with no Hibernate and no H2. It also registers constraints, so `validate()` and `errors` behave as they do in the running application.

Other traits you will meet: `ControllerUnitTest<T>` (used by the generated controller specs), `ServiceUnitTest<T>` (Lab 7), and `DataTest`, which lets a unit test mock several domain classes at once with `mockDomains(Quest, Task)`.

### Step 2: Test every constraint by its error code

Replace `TaskSpec.groovy` with the version below. Each test changes one property on the shared `task` and checks both that validation fails and *which* constraint failed:

```groovy
package com.kousenit

import grails.testing.gorm.DomainUnitTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class TaskSpec extends Specification implements DomainUnitTest<Task> {

    @Shared Quest quest = new Quest(name: 'Seek the grail')
    Task task = new Task(name: 'Defeat the Black Knight', quest: quest)

    void "a task with defaults is valid"() {
        expect:
        task.validate()
        task.priority == 3
        !task.completed
    }

    void "a task that starts and ends today lasts one day"() {
        expect:
        task.duration == 1
    }

    void "duration counts both end points"() {
        when: 'the task ends two days after it starts'
        task.endDate = task.startDate.plusDays(2)

        then: 'it lasts three days'
        task.duration == 3
    }

    void "a blank name is not valid"() {
        when:
        task.name = ' '

        then:
        !task.validate()
        task.errors['name'].code == 'blank'
    }

    void "a task needs a quest"() {
        when:
        task.quest = null

        then:
        !task.validate()
        task.errors['quest'].code == 'nullable'
    }

    void "priorities below 1 are not valid"() {
        when:
        task.priority = 0

        then:
        !task.validate()
        task.errors['priority'].code == 'range.toosmall'
    }

    void "priorities above 5 are not valid"() {
        when:
        task.priority = 6

        then:
        !task.validate()
        task.errors['priority'].code == 'range.toobig'
    }

    @Unroll
    void "a task with priority #priority is valid"() {
        when:
        task.priority = priority

        then:
        task.validate()

        where:
        priority << (1..5)
    }

    void "the end date may not precede the start date"() {
        when:
        task.endDate = task.startDate.minusDays(1)

        then:
        !task.validate()
        task.errors['endDate'].code == 'validator.invalid'
    }
}
```

Things to notice:

- **`@Shared`** keeps one `Quest` for all tests, since none of them change it. The `task` field has no annotation, so Spock builds a fresh one for every feature method; changing its priority in one test cannot leak into the next.
- **`task.errors['name'].code`** reads the Spring `Errors` object GORM attaches after validation. The code is the last segment of the message key: `quest.name.blank` in `messages.properties` corresponds to code `blank` on property `name` of class `Quest`.
- **`@Unroll`** with a `where:` block turns one feature method into five, one per priority. The `#priority` in the method name is replaced for each, so the test report lists *a task with priority 1 is valid* through *a task with priority 5 is valid*.
- The `when:` and `then:` labels take optional descriptions. Use them when the test name alone does not say what the setup means.

### Step 3: Run the tests

```bash
./gradlew test
```

Open `build/reports/tests/test/index.html` to see the unrolled names. Try breaking something, such as changing the range to `1..4`, to watch which tests catch it.

### Key Learning Points

- `DomainUnitTest<T>` tests a domain class with no database. Validation and GORM methods work; Hibernate is not involved.
- Test constraints by their error code: `blank`, `nullable`, `range.toosmall`, `range.toobig`, `validator.invalid`. Those codes are what you customize in `messages.properties`.
- `@Shared` for fixtures that no test mutates; plain fields for per-test state.
- `@Unroll` plus a `where:` block replaces copy-and-paste tests.

### Checkpoint

```bash
git checkout step3-testing
```

## Lab 4: Dynamic Finders, Criteria, and Where Queries

GORM gives you four ways to ask a question of the database, from the terse to the composable: dynamic finders, criteria, where queries, and HQL. Earlier versions of this course explored them in the interactive Grails console (`./grailsw console`, still available in Grails 7 through the `console` dependency in `build.gradle`). An integration test is a better vehicle for a tutorial, because each query sits next to the answer you expect from it, and it keeps running after the lab is over. First, though, the application needs some data.

### Step 1: See the SQL

GORM hides SQL from you, which is pleasant until you want to know what a finder actually does. Turn on SQL logging for the development environment only, in `grails-app/conf/application.yml` under `environments: development: dataSource:`:

```yaml
environments:
  development:
    dataSource:
      dbCreate: create-drop
      logSql: true
      formatSql: true
      url: jdbc:h2:mem:devDb;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE
```

Keep it out of `test`, where it turns the build log into wallpaper.

### Step 2: Seed data on startup

Grails runs `grails-app/init/com/kousenit/BootStrap.groovy` once at startup. We want the same data in the running app and in the query tests, so put it in a plain class that both can call.

1. Create `src/main/groovy/com/kousenit/SeedData.groovy`:

   ```groovy
   package com.kousenit

   import java.time.LocalDate

   class SeedData {

       static Quest seekTheGrail() {
           LocalDate today = LocalDate.now()
           new Quest(name: 'Seek the grail')
                   .addToTasks(name: 'Run away from killer rabbit', priority: 1)
                   .addToTasks(name: 'Answer the Bridgekeeper', priority: 4, startDate: today + 1, endDate: today + 1)
                   .addToTasks(name: 'Defeat the Black Knight', completed: true, startDate: today - 3, endDate: today - 2)
                   .addToTasks(name: 'Bring out your dead', completed: true, priority: 5, startDate: today - 1, endDate: today - 1)
                   .addToTasks(name: 'Find a shrubbery for the Knights Who Say Ni', priority: 2, endDate: today + 7)
                   .addToTasks(name: 'Get taunted by the French', completed: true, priority: 4)
                   .addToTasks(name: 'Weigh a witch against a duck', priority: 3)
                   .addToTasks(name: 'Build a giant wooden rabbit', priority: 2, endDate: today + 14)
                   .addToTasks(name: 'Lobbeth the Holy Hand Grenade of Antioch', priority: 5, startDate: today + 2, endDate: today + 2)
                   .save(failOnError: true)
       }
   }
   ```

   `addToTasks` accepts a map, builds the `Task`, sets its `quest`, and returns the quest, so the calls chain. Saving the quest cascades to the nine tasks. The dates use the Groovy operators from Lab 2.

   `failOnError: true` matters. By default `save()` returns `null` on a validation failure and carries on; in startup code you want it to throw.

2. Replace `BootStrap.groovy`:

   ```groovy
   package com.kousenit

   import grails.util.Environment

   class BootStrap {

       def init = { servletContext ->
           if (Environment.current != Environment.TEST && Quest.count() == 0) {
               SeedData.seekTheGrail()
           }
       }

       def destroy = {
       }
   }
   ```

   The guard keeps seed data out of the test environment, where tests set up their own, and out of a database that already has quests in it.

3. Start the app and open http://localhost:8080/quest/show/1. The quest lists its nine tasks, and the console shows the formatted `insert` statements that put them there.

4. The task links read like `com.kousenit.Task(name:Bring out your dead, priority:5, completed:false)`. That is the `@ToString` output from Lab 2, which suited the test reports but not a web page. Replace the annotation with a plain method:

   ```groovy
   String toString() { name }
   ```

   and delete the `import groovy.transform.ToString`. Restart and the links show task names.

### Step 3: Create the integration test

```bash
./grailsw create-integration-test QuestQueries
```

The skeleton lands in `src/integration-test/groovy/com/kousenit/QuestQueriesSpec.groovy`, annotated `@Integration` and `@Rollback`.

> [!IMPORTANT]
> The obvious move is to call `SeedData.seekTheGrail()` in Spock's `setup()` method. Do not. With `@Rollback`, the transaction begins *after* `setup()` runs, so anything `setup()` saves is committed and stays in the database for every later test, in this spec and in others. That is why the generated service specs call a `setupData()` method at the top of each feature instead. Follow the same pattern.

Replace the skeleton with this spec. Every feature method begins with `seed()`:

```groovy
package com.kousenit

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

import java.time.LocalDate

@Integration
@Rollback
class QuestQueriesSpec extends Specification {

    LocalDate today = LocalDate.now()

    // Not setup(): with @Rollback, setup() runs before the transaction begins,
    // so anything it saves is committed and leaks into the other tests.
    private void seed() {
        SeedData.seekTheGrail()
    }

    void "dynamic finders are built from property names"() {
        seed()

        expect:
        Quest.findByName('Seek the grail').tasks.size() == 9
        Task.findAllByCompleted(true).size() == 3
        Task.findAllByPriorityGreaterThan(3).size() == 4
        Task.countByCompletedAndPriorityGreaterThan(false, 3) == 2
    }

    void "finders can combine comparisons on several properties"() {
        seed()

        when: 'tasks of priority below 4 that start between yesterday and tomorrow'
        List<Task> tasks = Task.findAllByPriorityLessThanAndStartDateBetween(4, today - 1, today + 1)

        then: 'Bring out your dead qualifies on dates but not on priority'
        tasks*.name.sort() == ['Build a giant wooden rabbit',
                               'Find a shrubbery for the Knights Who Say Ni',
                               'Run away from killer rabbit',
                               'Weigh a witch against a duck']
    }

    void "list and count variants"() {
        seed()

        expect:
        Task.count() == 9
        Task.listOrderByPriority()*.priority == [1, 2, 2, 3, 3, 4, 4, 5, 5]
        Task.list(max: 2, sort: 'name').name == ['Answer the Bridgekeeper', 'Bring out your dead']
    }

    void "criteria queries compose restrictions"() {
        seed()

        when:
        List<Task> tasks = Task.withCriteria {
            ilike 'name', '%rabbit%'
            lt 'priority', 3
            order 'name', 'asc'
        }

        then:
        tasks*.name == ['Build a giant wooden rabbit', 'Run away from killer rabbit']
    }

    void "criteria can reach across associations"() {
        seed()

        when: 'quests with an incomplete task of priority 5'
        List<Quest> quests = Quest.withCriteria {
            tasks {
                eq 'completed', false
                eq 'priority', 5
            }
        }

        then:
        quests*.name == ['Seek the grail']
    }

    void "where queries are type-checked criteria in Groovy syntax"() {
        seed()

        when:
        def urgent = Task.where { priority >= 4 && completed == false }

        then:
        urgent.count() == 2
        urgent.list(sort: 'name')*.name == ['Answer the Bridgekeeper', 'Lobbeth the Holy Hand Grenade of Antioch']
    }

    void "where queries can be composed before they run"() {
        seed()

        given:
        def open = Task.where { completed == false }

        when: 'narrow the open tasks to the ones already overdue'
        def overdue = open.where { endDate < today }

        then:
        open.count() == 6
        overdue.count() == 0
    }

    void "findAll with a closure is a where query in disguise"() {
        seed()

        when:
        List<Task> tasks = Task.findAll { priority in [1, 5] }

        then:
        tasks*.priority.sort() == [1, 5, 5]
    }
}
```

What each group shows:

- **Dynamic finders** are methods GORM derives from the name: `findBy`, `findAllBy`, `countBy`, followed by property names, comparators such as `GreaterThan`, `LessThan`, `Between`, `Like`, and `And` or `Or` between them. `listOrderBy<Property>` sorts. They do not exist until you call them.
- **Criteria** queries use a builder closure. Restrictions compose, `order` sorts, and nesting a closure named after an association, `tasks { ... }`, becomes a join. Criteria are the tool for queries built up at runtime from optional filters.
- **Where queries** use Groovy expressions on the properties themselves and are checked at compile time, so a misspelled property is a compiler error. `Task.where { }` returns a `DetachedCriteria`: nothing runs until you call `list()`, `count()`, or `get()` on it, and you can refine it with another `where` first.
- **`findAll { }`** on a domain class is a where query that runs immediately.

> **Note:** The last test assigns the `findAll` result in a `when:` block before asserting on it. Written inline in an `expect:` block, the same call returned every task, because Spock rewrites the expressions in its assertion blocks and GORM's where-query transformation did not see the closure. Treat where-query closures like any other side-effecting call in Spock: run them in `given:` or `when:`, assert in `then:`.

### Step 4: Run it

```bash
./gradlew integrationTest --tests 'com.kousenit.QuestQueriesSpec'
```

Nine tasks, eight features, and a transaction rolled back after each one. Add a test for a query of your own, for example the tasks due in the next week, using whichever style you like least, to see how the three compare.

### Key Learning Points

- `BootStrap.init` runs at startup. Guard seed data by environment and by whether the data already exists; use `failOnError: true` so bad seed data fails loudly.
- `addTo<Collection>` builds the child, wires both sides of the association, and chains.
- With `@Rollback`, `setup()` runs before the transaction. Seed inside each feature method.
- Dynamic finders for one-liners, criteria for queries assembled at runtime, where queries for compile-time checked, composable queries.
- `logSql` and `formatSql` show you what GORM is really sending to the database.

### Checkpoint

```bash
git checkout step4-queries
```

## Lab 5: Completing the Domain Model

Quests need knights, and knights need somewhere to live. This lab adds `Knight` and `Castle`, completes the associations, and writes one more custom validator, this time one whose rule depends on *which* knight you are. By the end the model looks like this:

```
Castle 1 ──< Knight >── 1 Quest 1 ──< Task
```

### Step 1: Castle

1. Generate the class:

   ```bash
   ./grailsw create-domain-class Castle
   ```

2. Replace `grails-app/domain/com/kousenit/Castle.groovy`:

   ```groovy
   package com.kousenit

   class Castle {
       String name
       String city
       String country
       Double latitude
       Double longitude

       static hasMany = [knights: Knight]

       String toString() { name }

       static constraints = {
           name blank: false
           city blank: false
           country blank: false
           latitude nullable: true, range: -90d..90d
           longitude nullable: true, range: -180d..180d
       }
   }
   ```

   The coordinates are `Double`, not `double`, and `nullable: true`, because a castle created through the web form has no coordinates until the geocoder in Lab 7 supplies them. A primitive `double` would default to zero and put every new castle in the Gulf of Guinea.

### Step 2: Knight, and the Bridge of Death

1. Generate the class:

   ```bash
   ./grailsw create-domain-class Knight
   ```

2. Replace `grails-app/domain/com/kousenit/Knight.groovy`:

   ```groovy
   package com.kousenit

   class Knight {
       String title = 'Sir'
       String name
       String favouriteColour
       Quest quest
       Castle castle

       String toString() { "$title $name" }

       static constraints = {
           title inList: ['Sir', 'Lord', 'Lady', 'King', 'Queen']
           name blank: false
           favouriteColour nullable: true, validator: { String colour, Knight knight ->
               // The Bridgekeeper asks every knight. Only Galahad is allowed not to know.
               if (!colour && !knight.name.contains('Galahad')) {
                   return 'bridgeOfDeath'
               }
           }
           quest nullable: true
           castle nullable: true
       }
   }
   ```

   Three things are new here:

   - **`inList`** restricts a property to a fixed set of values. The scaffolding turns it into a dropdown.
   - **Plain references, not `belongsTo`.** A knight refers to a quest and a castle, but neither owns the knight. Knights between quests and knights without a castle are both legal, hence `nullable: true`, and deleting a quest does not delete its knights.
   - **A validator that returns an error code.** Lab 2's validator returned `true` or `false`. This one returns a `String`, which becomes the error code, so the message key is `knight.favouriteColour.bridgeOfDeath` instead of the generic `validator.invalid`. Returning nothing means the value is fine. Note that the closure runs even when the value is `null`; `nullable: true` turns off the nullable check but not your own.

   The rule itself: at the Bridge of Death, every knight is asked their favourite colour. Lancelot says "Blue" and crosses. Galahad says "Blue. No, yellow!" and is cast into the Gorge of Eternal Peril. So every knight must record a favourite colour, except Galahad, who is excused for never having managed a straight answer.

3. Add the message to `grails-app/i18n/messages.properties`:

   ```properties
   knight.favouriteColour.bridgeOfDeath=What... is your favourite colour? Every knight must answer the Bridgekeeper (Galahad excepted)
   ```

4. Give `Quest` its knights. In `Quest.groovy`:

   ```groovy
   static hasMany = [tasks: Task, knights: Knight]
   ```

### Step 3: Scaffolding for both

```bash
./grailsw generate-all com.kousenit.Castle
./grailsw generate-all com.kousenit.Knight
```

Fill in `populateValidParams` in the two new controller specs, with `name`, `city`, and `country` for a castle, and `name` plus `favouriteColour` for a knight, and replace `setupData` and the `save` test in `CastleServiceSpec` and `KnightServiceSpec` as you did in Labs 1 and 2. Five castles and five knights each; the finished versions are in the repository if you would rather read than type.

### Step 4: Test the Bridgekeeper

Replace `src/test/groovy/com/kousenit/KnightSpec.groovy`:

```groovy
package com.kousenit

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification
import spock.lang.Unroll

class KnightSpec extends Specification implements DomainUnitTest<Knight> {

    void "a knight needs a name and a valid title"() {
        expect:
        new Knight(name: 'Lancelot', favouriteColour: 'Blue').validate()
        !new Knight(name: ' ', favouriteColour: 'Blue').validate()
        !new Knight(title: 'Squire', name: 'Patsy', favouriteColour: 'Brown').validate()
    }

    void "quest and castle are optional, because knights wander"() {
        expect:
        new Knight(name: 'Robin', favouriteColour: 'Yellow').validate()
    }

    @Unroll
    void "#name must answer the Bridgekeeper"() {
        when:
        Knight knight = new Knight(name: name)

        then:
        !knight.validate()
        knight.errors['favouriteColour'].code == 'bridgeOfDeath'

        where:
        name << ['Lancelot the Brave', 'Robin the Not-Quite-So-Brave-as-Sir-Lancelot', 'Bedevere the Wise']
    }

    void "Galahad does not have to know his favourite colour"() {
        expect:
        new Knight(name: 'Galahad the Pure').validate()
    }

    void "Galahad may still have one, as long as he does not change his mind"() {
        expect:
        new Knight(name: 'Galahad the Pure', favouriteColour: 'Blue. No, yellow!').validate()
    }
}
```

and `CastleSpec.groovy`:

```groovy
package com.kousenit

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class CastleSpec extends Specification implements DomainUnitTest<Castle> {

    Castle doune = new Castle(name: 'Camelot', city: 'Doune', country: 'Scotland')

    void "coordinates are optional until the geocoder fills them in"() {
        expect:
        doune.validate()
        doune.latitude == null
    }

    void "coordinates must be on the planet"() {
        when:
        doune.latitude = 91
        doune.longitude = -181

        then:
        !doune.validate()
        doune.errors['latitude'].code == 'range.toobig'
        doune.errors['longitude'].code == 'range.toosmall'
    }
}
```

Run `./gradlew test`.

### Step 5: Seed the court

The castles are the places the film was shot. Doune Castle, north of Stirling, played Camelot as well as the interiors of Swamp Castle, Castle Anthrax, and the French castle. Castle Stalker, on its island near Port Appin, was Castle Aaargh. Bodiam Castle in East Sussex was Swamp Castle from the outside.

1. Add a second method to `SeedData.groovy`. The coordinates are hard-coded so that the application never needs the network to start:

   ```groovy
   static List<Castle> theCourt(Quest quest) {
       Castle camelot = new Castle(name: 'Camelot', city: 'Doune', country: 'Scotland',
               latitude: 56.1853d, longitude: -4.0509d)
               .addToKnights(title: 'King', name: 'Arthur', favouriteColour: 'Blue', quest: quest)
               .addToKnights(name: 'Lancelot the Brave', favouriteColour: 'Blue', quest: quest)
               .addToKnights(name: 'Galahad the Pure', quest: quest)
               .addToKnights(name: 'Robin the Not-Quite-So-Brave-as-Sir-Lancelot', favouriteColour: 'Yellow', quest: quest)
               .addToKnights(name: 'Bedevere the Wise', favouriteColour: 'Green', quest: quest)
               .save(failOnError: true)
       Castle aaargh = new Castle(name: 'Castle Aaargh', city: 'Port Appin', country: 'Scotland',
               latitude: 56.5695d, longitude: -5.3870d)
               .save(failOnError: true)
       Castle swamp = new Castle(name: 'Swamp Castle', city: 'Robertsbridge', country: 'England',
               latitude: 51.0023d, longitude: 0.5436d)
               .addToKnights(title: 'Lord', name: 'of Swamp Castle', favouriteColour: 'Huge tracts of land')
               .save(failOnError: true)
       [camelot, aaargh, swamp]
   }
   ```

   Saving a castle cascades to its knights, because `Castle hasMany knights`. Each knight also points at the quest, so after this runs, `quest.knights` has five members without anyone calling `addToKnights` on the quest. GORM maintains both sides.

2. Call it from `BootStrap.groovy`:

   ```groovy
   Quest quest = SeedData.seekTheGrail()
   SeedData.theCourt(quest)
   ```

3. Run the app. Camelot's show page lists five knights, the quest's show page lists the same five, and **New Knight** has a title dropdown. Try to save a knight with no favourite colour, then try again with a name containing Galahad.

### Key Learning Points

- `inList` for enumerated values; the scaffolding renders a select.
- Associations without `belongsTo` are plain references: optional, and not cascaded on delete.
- A validator closure can return an error code of your own, which gives it its own message key.
- Validators run for `null` values; `nullable: true` only disables the built-in nullable check.
- Use wrapper types (`Double`) for optional numeric properties so that "unknown" is `null`, not zero.
- When both sides of an association are set during seeding, GORM keeps them consistent.

### Checkpoint

```bash
git checkout step5-model
```

## Lab 6: Scaffolding with @Scaffold

Every `generate-all` so far produced a 99-line controller, a 17-line service interface, four GSP files, and two test skeletons. That is **static scaffolding**: code you own, can read, and must maintain. Grails 7 adds a second option, the `@Scaffold` annotation, which does the same work at compile time and runtime and leaves nothing in your source tree but an empty class. This lab converts `Castle` to it, so you can compare the two side by side.

### Step 1: Measure what you are about to delete

```bash
wc -l grails-app/controllers/com/kousenit/CastleController.groovy \
      grails-app/services/com/kousenit/CastleService.groovy \
      grails-app/views/castle/*.gsp \
      src/test/groovy/com/kousenit/CastleControllerSpec.groovy
```

Five hundred and thirty-five lines, give or take, none of which you wrote.

### Step 2: Delete it

```bash
git rm grails-app/controllers/com/kousenit/CastleController.groovy \
       grails-app/services/com/kousenit/CastleService.groovy \
       src/test/groovy/com/kousenit/CastleControllerSpec.groovy
git rm -r grails-app/views/castle
```

Keep `CastleServiceSpec` in `src/integration-test`; it will need one small change.

### Step 3: Generate the annotated versions

```bash
./grailsw generate-scaffold-all com.kousenit.Castle
```

The command writes two files. The controller:

```groovy
package com.kousenit

import grails.plugin.scaffolding.annotation.Scaffold
import grails.plugin.scaffolding.RestfulServiceController

@Scaffold(RestfulServiceController<Castle>)
class CastleController {}
```

and the service:

```groovy
package com.kousenit

import grails.plugin.scaffolding.annotation.Scaffold

@Scaffold(Castle)
class CastleService {
}
```

That is the whole Castle web layer now. How it works:

- `@Scaffold` is processed by a Grails **AST transformation** at compile time. `@Scaffold(Castle)` on the service makes the class extend `GormService<Castle>`, which implements `get`, `list`, `count`, `save`, and `delete` against GORM. `@Scaffold(RestfulServiceController<Castle>)` on the controller makes it extend `RestfulController<Castle>` with the seven CRUD actions, and routes every data operation through the `CastleService` bean.
- The simpler `@Scaffold(Castle)` on a controller also works; it extends `RestfulController` and talks to the domain class directly, with no service in between.
- **Views** are generated at runtime, from the same templates `generate-all` uses, whenever no GSP exists for the action. If you later want to customize one page, run `./grailsw generate-views com.kousenit.Castle` and edit only the file you need. You will do exactly that for the map in Lab 8.
- `RestfulController` responds to content negotiation. Try `curl -H "Accept: application/json" http://localhost:8080/castle/show/1` and you get the castle as JSON, knights included, without writing a JSON view.

### Step 4: Fix the tests

1. The old controller spec mocked the service interface method by method, which no longer applies. Replace `src/test/groovy/com/kousenit/CastleControllerSpec.groovy` with a test that proves the transformation happened:

   ```groovy
   package com.kousenit

   import grails.rest.RestfulController
   import grails.testing.web.controllers.ControllerUnitTest
   import spock.lang.Specification

   class CastleControllerSpec extends Specification implements ControllerUnitTest<CastleController> {

       void "the annotation turns an empty class into a RestfulController for Castle"() {
           expect:
           controller instanceof RestfulController
           controller.resource == Castle
           ['index', 'show', 'create', 'save', 'edit', 'update', 'delete'].every { controller.respondsTo(it) }
       }
   }
   ```

2. `GormService.count` takes a `Map` of query arguments, where the data service interface's `count()` took none. In `CastleServiceSpec`, change both `castleService.count()` calls to `castleService.count([:])`.

3. Run `./gradlew test` and `./gradlew integrationTest --tests 'com.kousenit.CastleServiceSpec'`.

### Step 5: Run it

Start the application and visit http://localhost:8080/castle. The list, the show page with its knights, the create form with the blank-name validation, and the edit page all behave as before, with no GSP files on disk. Add a castle, then check the JSON endpoint.

### When to use which

Static scaffolding is a starting point you edit: the generated controller is readable Groovy, and most real Grails applications begin that way and then diverge. `@Scaffold` is for the parts of an application that stay standard: admin screens, reference data, anything where the generated behavior is the behavior you want. You can mix them freely, as this project now does, and you can add or override individual actions on an annotated controller when one page needs something special.

### Key Learning Points

- `@Scaffold` is a compile-time AST transformation. The empty class really does become a `RestfulController` or `GormService` subclass, which is why a unit test can check `instanceof`.
- `generate-scaffold-all` writes the two annotated classes; `generate-all` writes the full static version. Both come from the scaffolding plugin.
- Runtime views fall back to the same templates, and `generate-views` lets you take over one page at a time.
- `RestfulController` gives JSON and XML for free through content negotiation.

### Checkpoint

```bash
git checkout step6-scaffold
```

## Lab 7: A Geocoder Service

Services are where Grails puts business logic: Spring beans in `grails-app/services`, injected into controllers and other services by name, transactional by default. This lab writes one that calls an external web API to find a castle's coordinates, tests it without the network, tests it with the network, and hooks it into the scaffolded `CastleService` so that any castle created through the web form is geocoded on save.

The API is [Open-Meteo's geocoding endpoint](https://open-meteo.com/en/docs/geocoding-api). It is free, needs no key, and answers a town name with JSON:

```
https://geocoding-api.open-meteo.com/v1/search?name=Doune&count=1
```

```json
{"results":[{"name":"Doune","latitude":56.18995,"longitude":-4.05288,"country":"United Kingdom","admin1":"Scotland", ...}]}
```

When nothing matches, the response has no `results` key at all.

### Step 1: Create the service

```bash
./grailsw create-service Geocoder
```

That makes `grails-app/services/com/kousenit/GeocoderService.groovy` and a unit test skeleton. The generated class is annotated `@Transactional`. Remove that: this service touches no database, and a transaction around an HTTP call is a waste.

### Step 2: Exercise: implement the lookup

Give the service two methods with these signatures:

```groovy
package com.kousenit

import groovy.json.JsonSlurper

class GeocoderService {

    static final String BASE = 'https://geocoding-api.open-meteo.com/v1/search'

    /** Fill in latitude and longitude from the castle's city, if the API knows it. Returns the castle. */
    Castle fillInLatLng(Castle castle) {
        // TODO
    }

    /** The first matching place for a town name, as a Map, or null if there is none. */
    Map lookup(String city) {
        // TODO
    }
}
```

Two methods rather than one, because the split is what makes the service testable: `lookup` is the only method that touches the network, so a test can replace just that one. `JsonSlurper` is in `groovy-json`, which Grails includes. `String.toURL()` and `URL.text` or `JsonSlurper.parse(URL)` come from the Groovy JDK. Encode the town name; "Port Appin" has a space in it.

When you have it, compare with this version:

```groovy
    Castle fillInLatLng(Castle castle) {
        Map place = lookup(castle.city)
        if (place) {
            castle.latitude = place.latitude as Double
            castle.longitude = place.longitude as Double
        }
        castle
    }

    Map lookup(String city) {
        String url = "$BASE?name=${URLEncoder.encode(city, 'UTF-8')}&count=1"
        Map response = new JsonSlurper().parse(url.toURL()) as Map
        (response.results as List<Map>)?.find()
    }
```

`find()` with no argument returns the first element, or `null` for an empty or missing list, which is exactly the contract we want.

### Step 3: Unit test with a Spy

Replace `src/test/groovy/com/kousenit/GeocoderServiceSpec.groovy`:

```groovy
package com.kousenit

import grails.testing.gorm.DomainUnitTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class GeocoderServiceSpec extends Specification
        implements ServiceUnitTest<GeocoderService>, DomainUnitTest<Castle> {

    Castle camelot = new Castle(name: 'Camelot', city: 'Doune', country: 'Scotland')

    void "coordinates are copied from the first result"() {
        given: 'a service whose lookup never touches the network'
        GeocoderService geocoder = Spy(GeocoderService) {
            lookup('Doune') >> [name: 'Doune', latitude: 56.18995, longitude: -4.05288]
        }

        when:
        geocoder.fillInLatLng(camelot)

        then:
        camelot.latitude == 56.18995d
        camelot.longitude == -4.05288d
        camelot.validate()
    }

    void "an unknown town leaves the coordinates alone"() {
        given:
        GeocoderService geocoder = Spy(GeocoderService) {
            lookup(_) >> null
        }
        Castle anthrax = new Castle(name: 'Castle Anthrax', city: 'Nowheresville', country: 'Scotland')

        when:
        geocoder.fillInLatLng(anthrax)

        then:
        anthrax.latitude == null
        anthrax.longitude == null
    }

    void "the service returns the castle so calls can chain"() {
        given:
        GeocoderService geocoder = Spy(GeocoderService) { lookup(_) >> null }

        expect:
        geocoder.fillInLatLng(camelot).is(camelot)
    }
}
```

A Spock **Spy** wraps a real object. Methods you stub, here `lookup`, return what you say; everything else runs the real code. So `fillInLatLng` is tested for real, with the network call replaced. The spec implements both `ServiceUnitTest` for the service and `DomainUnitTest<Castle>` so that `new Castle(...)` and `validate()` work.

### Step 4: Integration test against the real API

Create `src/integration-test/groovy/com/kousenit/GeocoderServiceLiveSpec.groovy`:

```groovy
package com.kousenit

import grails.testing.mixin.integration.Integration
import spock.lang.IgnoreIf
import spock.lang.Specification

@Integration
@IgnoreIf({ env.OFFLINE })
class GeocoderServiceLiveSpec extends Specification {

    GeocoderService geocoderService

    void "Doune is where we left it"() {
        when:
        Map place = geocoderService.lookup('Doune')

        then:
        place.country == 'United Kingdom'
        (place.latitude - 56.19).abs() < 0.05
        (place.longitude - -4.05).abs() < 0.05
    }

    void "a town the Bridgekeeper has never heard of returns nothing"() {
        expect:
        geocoderService.lookup('Nowheresville') == null
    }
}
```

The `@Integration` test gets the real `GeocoderService` bean injected by name. `@IgnoreIf({ env.OFFLINE })` skips both tests when an `OFFLINE` environment variable is set, so the build still passes on conference wifi:

```bash
OFFLINE=1 ./gradlew integrationTest
```

### Step 5: Geocode on save

The scaffolded `CastleService` from Lab 6 extends `GormService<Castle>`, so its `save` can be overridden like any method. Edit `grails-app/services/com/kousenit/CastleService.groovy`:

```groovy
package com.kousenit

import grails.plugin.scaffolding.annotation.Scaffold

@Scaffold(Castle)
class CastleService {

    GeocoderService geocoderService

    @Override
    Castle save(Castle castle) {
        if (castle.latitude == null || castle.longitude == null) {
            geocoderService.fillInLatLng(castle)
        }
        super.save(castle)
    }
}
```

`GeocoderService geocoderService` is injected by Spring because the property name matches the bean name; no annotation needed. The `null` check means the seed castles, which arrive with coordinates, never trigger a network call, and startup stays offline.

Run everything:

```bash
./gradlew test
./gradlew integrationTest
```

### Step 6: Try it

Start the app, go to **Castles → New Castle**, and create *Castle Anthrax* in *Doune*, *Scotland*, leaving the coordinates blank. The show page comes back with latitude 56.18995 and longitude -4.05288. The JSON view agrees:

```bash
curl -H "Accept: application/json" http://localhost:8080/castle/show/4
```

### Key Learning Points

- Services are Spring beans injected by name. Drop `@Transactional` when there is no database work.
- Split the network call into its own method so a Spock `Spy` can replace it and test the rest for real.
- `@IgnoreIf` and `@Requires` keep tests that need the outside world from breaking the build when it is not there.
- A `@Scaffold` service is a real subclass of `GormService`; override `save` to add behavior.
- Design seed data so startup never depends on the network.

### Checkpoint

```bash
git checkout step7-geocoder
```

## Lab 8: Mapping the Castles

The castles have coordinates; let us see them. This lab puts an [OpenStreetMap](https://www.openstreetmap.org) map on the castle list page using [Leaflet](https://leafletjs.com), the standard open-source JavaScript mapping library. Along the way you take over one scaffolded view, add an action to a `@Scaffold` controller, and serve a JavaScript library from a **webjar** so the page has no CDN dependency.

### Step 1: Leaflet as a webjar

[WebJars](https://www.webjars.org) package npm libraries as Maven artifacts. Spring Boot serves anything under `META-INF/resources/webjars` on the classpath at `/webjars/**`, and Grails inherits that. Add to the `dependencies` block of `build.gradle`:

```groovy
runtimeOnly "org.webjars.npm:leaflet:1.9.4"
```

After the next restart, `http://localhost:8080/webjars/leaflet/1.9.4/dist/leaflet.js` serves the library from the jar. The map tiles themselves still come from OpenStreetMap's servers, so the page needs the network to show a map, but not to load.

### Step 2: Take over the list view

Lab 6 left `Castle` with no GSP files; the scaffolding renders them from templates at runtime. To customize the list page, generate the static versions and keep only the one you want:

```bash
./grailsw generate-views com.kousenit.Castle
rm grails-app/views/castle/create.gsp grails-app/views/castle/edit.gsp grails-app/views/castle/show.gsp
```

Now `index.gsp` is yours and the other three pages stay dynamic. Open `grails-app/views/castle/index.gsp`.

1. At the very top, before `<!DOCTYPE html>`, import the JSON converter for use later:

   ```jsp
   <%@ page import="grails.converters.JSON" %>
   ```

2. In `<head>`, after the `<title>`, load Leaflet from the webjar and give the map a height:

   ```html
   <link rel="stylesheet" href="${createLink(uri: '/webjars/leaflet/1.9.4/dist/leaflet.css')}"/>
   <script src="${createLink(uri: '/webjars/leaflet/1.9.4/dist/leaflet.js')}"></script>
   <style>#map { height: 420px; }</style>
   ```

   `createLink(uri:)` prefixes the context path, so the page keeps working if the app is ever deployed under `/holygrails`.

3. Above the `<f:table ...>` tag, add the map container:

   ```html
   <div id="map" class="mb-3 border rounded"></div>
   ```

4. Just before `</body>`, add the script that draws the markers. The marker data comes from the model as a Groovy list, rendered into the page as JSON:

   ```html
   <script>
       const markers = ${raw((markers as JSON).toString())};
       // Wait for the stylesheets: Leaflet needs the container's final size to fit the bounds.
       window.addEventListener('load', () => {
           const map = L.map('map');
           L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
               maxZoom: 18,
               attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
           }).addTo(map);
           const group = L.featureGroup(markers.map(m =>
               L.marker([m.lat, m.lng]).bindPopup(
                   `<strong><a href="${'$'}{m.url}">${'$'}{m.name}</a></strong><br>${'$'}{m.city}<br>${'$'}{m.knights} knight(s)`)
           )).addTo(map);
           if (markers.length) {
               map.fitBounds(group.getBounds().pad(0.2));
           } else {
               map.setView([56.19, -4.05], 6);
           }
       });
   </script>
   ```

   The `load` listener matters: the page's stylesheets, including Leaflet's, arrive after this script runs, and `fitBounds` computed against a container with no height yet produces a map zoomed out to the whole world. Waiting for `load` gives Leaflet the real size.

   Two GSP details here. `raw()` stops GSP from HTML-encoding the JSON, which would turn every quote into `&quot;`. And a JavaScript template literal's `${...}` looks exactly like a GSP expression, so it is written as `${'$'}{m.name}`: GSP evaluates `${'$'}` to a literal dollar sign and leaves the braces alone.

### Step 3: Supply the markers from the controller

The list page now expects a `markers` entry in the model. The `@Scaffold` controller can override the scaffolded `index` action to add it. Edit `grails-app/controllers/com/kousenit/CastleController.groovy`:

```groovy
package com.kousenit

import grails.plugin.scaffolding.annotation.Scaffold
import grails.plugin.scaffolding.RestfulServiceController

@Scaffold(RestfulServiceController<Castle>)
class CastleController {

    /** The scaffolded index, plus one extra model entry: every castle that knows where it is. */
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond listAllResources(params), model: [castleCount: countResources(), markers: markers()]
    }

    private List<Map> markers() {
        Castle.findAllByLatitudeIsNotNullAndLongitudeIsNotNull().collect { Castle castle ->
            [name: castle.name, city: castle.city, lat: castle.latitude, lng: castle.longitude,
             knights: castle.knights.size(), url: createLink(action: 'show', id: castle.id)]
        }
    }
}
```

The first two lines of `index` are what `RestfulController` does by itself: `listAllResources` and `countResources` are its protected methods, and because `@Scaffold` made this class a real subclass, you can call them. The third model entry is ours. `respond` renders the `castleList` for the table as before, and the whole model is available to the GSP.

The finder `findAllByLatitudeIsNotNullAndLongitudeIsNotNull` is a mouthful, but it reads as what it does, and it keeps castles that failed geocoding off the map rather than dropping them in the Atlantic at 0, 0.

### Step 4: Look

Restart and open http://localhost:8080/castle. Three markers in Britain: Camelot and Castle Aaargh in Scotland, Swamp Castle on the south coast of England. Click a marker for the castle name, town, and knight count, with a link to its show page. If you created Castle Anthrax in Lab 7, it sits on top of Camelot, since both are Doune Castle.

### Key Learning Points

- Webjars put front-end libraries on the classpath; Spring Boot serves them at `/webjars/**` with no configuration.
- `generate-views` lets you take over a single scaffolded page while the rest stay dynamic.
- A `@Scaffold` controller can override or add actions, and call `RestfulController`'s protected helpers.
- `raw()` disables GSP's HTML encoding when you mean to emit markup or JSON. Use it only for data you trust.
- `${'$'}` is how a GSP emits a literal dollar sign for JavaScript template literals.

### Checkpoint

```bash
git checkout step8-map
```

## Lab 9: What's New in Grails 7

Grails 7.0.0 shipped on 28 October 2025, the first release after Grails graduated to an Apache Software Foundation top-level project, and 7.2.4 is the current release as these labs are written. This lab has one piece of code, a functional test that uses the most visible new testing feature, and then a tour of what changed, aimed at anyone bringing a Grails 5 or 6 application forward. Everything here is taken from the 7.2.4 user guide's *What's new* and *Upgrading* chapters, or was verified while building these labs.

### Step 1: A functional test in a containerized browser

Lab 1 mentioned `ContainerGebSpec`, the Grails 7 way to run [Geb](https://groovy.apache.org/geb/) browser tests: Testcontainers starts a browser in Docker, points it at the running application, and your test drives it. The starter app came with one such test for the welcome page. Here is one for the map from Lab 8. Create `src/integration-test/groovy/com/kousenit/CastleMapSpec.groovy`:

```groovy
package com.kousenit

import grails.plugin.geb.ContainerGebSpec
import grails.testing.mixin.integration.Integration
import org.apache.grails.testing.cleanup.core.DatabaseCleanup

@Integration
@DatabaseCleanup
class CastleMapSpec extends ContainerGebSpec {

    def setup() {
        Castle.withNewTransaction {
            SeedData.theCourt(SeedData.seekTheGrail())
        }
    }

    void 'the castle list shows every castle with coordinates on the map'() {
        when: 'visiting the castle list'
        go '/castle'

        then: 'the page is the scaffolded list'
        title == 'Castle List'

        and: 'Leaflet has drawn one marker per castle'
        waitFor { $('#map .leaflet-marker-icon').size() == 3 }

        when: 'clicking the first marker'
        $('#map .leaflet-marker-icon', 0).click()

        then: 'its popup names the castle and links to it'
        waitFor { $('.leaflet-popup-content').text().contains('knight(s)') }
        $('.leaflet-popup-content a').text() in ['Camelot', 'Castle Aaargh', 'Swamp Castle']
    }
}
```

Three things to notice:

- **`BootStrap` does not run in the test environment**, so the spec seeds its own castles in `setup()`, inside a transaction, because a browser request is not going to run inside the test's transaction.
- **`@DatabaseCleanup`** truncates every table after each test. It is the Grails 7 answer to tests that commit data and therefore cannot use `@Rollback`. It needs the cleanup module for your database in `build.gradle`:

  ```groovy
  integrationTestImplementation "org.apache.grails:grails-testing-support-dbcleanup-h2"
  ```

- **`waitFor`** is Geb's way of waiting for JavaScript. The markers appear only after Leaflet runs on the page's `load` event.

Run it with Docker running:

```bash
./gradlew integrationTest --tests 'com.kousenit.CastleMapSpec'
```

Fifteen seconds or so: container start, application start, one real browser session. The browser recording and reporting options in the Geb plugin README let you keep a video of a failing test.

### Step 2: What changed in Grails 7

#### The foundation

| | Grails 6 | Grails 7.2.4 |
|---|---|---|
| Java | 11+ | **17+** (Gradle 8.14 limits it to 24 in practice) |
| Groovy | 3.0 | **4.0.33** |
| Spring Boot | 2.7 | **3.5.16** |
| Spring Framework | 5.3 | **6.2.19** |
| Jakarta EE | `javax.*` | **`jakarta.*`** |
| Hibernate | 5.6 | 5.6.15 (the `jakarta` build) |
| Gradle | 7.6 | **8.14.5** (8.14.4 minimum; Gradle 9 is not supported) |
| Spock | 2.x | 2.3-groovy-4.0 |

The `javax` to `jakarta` move is the one that touches application code: every `javax.servlet` and `javax.persistence` import changes. The guide suggests the Nebula `jakartaee-migration` Gradle plugin for projects with a lot of them.

#### The ASF move and the Maven coordinates

Every artifact the Grails team publishes now lives in the `org.apache.grails` group, and a single `grails-bom` manages all their versions, so dependency lines lose their version numbers. A few examples:

| Grails 6 | Grails 7 |
|---|---|
| `org.grails:grails-core` | `org.apache.grails:grails-core` |
| `org.grails.plugins:hibernate5` | `org.apache.grails:grails-data-hibernate5` |
| `org.grails.plugins:spring-security-core:6.1.1` | `org.apache.grails:grails-spring-security` |
| `org.grails.plugins:quartz:2.0.13` | `org.apache.grails:grails-quartz` |
| `com.bertramlabs.plugins:asset-pipeline-grails` | `cloud.wondrify:asset-pipeline-grails` |

The full mapping is in `RENAME.md` in the grails-core repository, and `etc/bin/rename_gradle_artifacts.sh` there rewrites a project's Gradle files for you. The Spring Security and Quartz plugins moved into the core repository, are versioned with Grails, and their documentation is now part of the user guide.

In `gradle.properties`, `grailsVersion` is the only version you set. Remove `gormVersion` and `grailsGradlePluginVersion`.

#### Build and tooling

- **Micronaut is gone from the default stack.** Grails 4 through 6 ran Micronaut as the parent application context. Grails 7 removes it, which shrinks builds; `grails-micronaut` is an opt-in for projects that used it.
- **The Gradle build is parallel, lazy, and cacheable.** Most Grails tasks support the build cache. The `buildProperties` task still fights the configuration cache, as Lab 0 found.
- **Reproducible builds.** The ASF requires them for Grails itself, and applications can opt in by setting `SOURCE_DATE_EPOCH` to a fixed timestamp.
- **Both CLIs are included.** `grails` (and `./grailsw`) runs the classic profile-based commands, which now delegate to Gradle; Forge at https://grails.apache.org/start generates projects and has the HTTP API from Lab 0. `grails console` and `schema-export` survive.
- **`stop-app` uses a PID file** written by `run-app` and `bootRun`, instead of JMX.
- **Test dependencies are off the production classpath.**
- **Groovy's invokedynamic is disabled by default** in Grails compiles because Groovy 4 switched it on and it regressed performance; `grails { indy = true }` re-enables it.

#### Features

- **`@Scaffold`** on controllers and services (Lab 6), with `create-scaffold-controller`, `create-scaffold-service`, and `generate-scaffold-all` (7.1).
- **`ContainerGebSpec`** for browser tests in Docker (this lab), with context path support in 7.1.
- **`HttpClientSupport`** trait for HTTP tests with fluent assertions (7.1; Lab 11).
- **`@DatabaseCleanup`** as the alternative to `@Rollback` (this lab).
- **Custom test phases** (7.1): `testPhases { functionalTest { } }` in `build.gradle` gives you a source set, configurations, and a task.
- **Audit annotations** (7.1): `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy` from `grails.gorm.annotation`, as a declared alternative to the `dateCreated` and `lastUpdated` naming convention from Lab 2. `@AutoTimestamp` is deprecated for removal in 8.
- **The startup banner** (7.0), showing dependency versions from 7.1, and customizable.
- **External configuration** is built in: the former external-config plugin, so `application.yml` files outside the jar work without a plugin.
- **GSP**: `formActionSubmit` replaces `actionSubmit`, `g:form` emits a CSRF token when Spring Security's CSRF is on, `g:flashMessages` renders flash as Bootstrap alerts, and the scaffolding and Fields tags support Bootstrap 5.3.
- **URL mappings** (7.1): `group` with shared namespace and controller defaults; a `+` suffix on a path variable for greedy matching, so `/$id+(.$format)?` keeps dots in the id.
- **JSON rendering of dates** is ISO-8601 everywhere, including `java.util.Date`, in both converters and JSON views. If a client parsed epoch milliseconds, it needs updating.
- **SiteMesh 3** (7.2): layout decoration moved from a servlet filter to a Spring MVC view resolver, so async controller results are decorated correctly. No changes needed for most applications.

### Step 3: An upgrade checklist

For a Grails 5 or 6 application, in the order that minimizes surprises:

1. Generate a fresh 7.2.4 app from Forge with the same features and diff its `build.gradle`, `gradle.properties`, and `application.yml` against yours. Several formerly required settings are now plugin defaults, and Forge stopped generating redundant `application.yml` entries in 7.0.11.
2. Move to Java 17 or 21 and Gradle 8.14.x.
3. Run the rename script, or apply `RENAME.md` by hand, and delete version numbers that the BOM now manages.
4. Replace `javax` imports with `jakarta`.
5. Check third-party plugins: every pre-7 plugin needs a 7 release.
6. If you used Micronaut-only features, add `grails-micronaut`; otherwise enjoy the smaller build.
7. Watch for the Groovy 4 behavior changes the guide lists: primitive `boolean` properties no longer generate `getX()`, `DELEGATE_FIRST` closure resolution order changed, and public fields now appear in `MetaClass` properties.
8. Run the tests. Then run them in a container with `ContainerGebSpec`.

### Step 4: Grails 8 is next

Grails 8.0.0 was tagged on 4 October 2026 and was still marked pre-release when these labs were written. What it brings, from the release candidates: Groovy 5, Spring Boot 4, Spock 2.4, GORM for Hibernate 7, and Gradle 9.8, which is what makes Java 25 a supported build JDK. The upgrade path from 7 is designed to be incremental, which is one more reason to get to 7 first.

### Key Learning Points

- Grails 7 is Java 17, Groovy 4, Spring Boot 3.5, and `jakarta.*`, published under `org.apache.grails` with one BOM.
- Micronaut left the default stack; the classic CLI and `grailsw` came back.
- The headline features are `@Scaffold`, containerized Geb testing, `HttpClientSupport`, `@DatabaseCleanup`, and the external configuration integration.
- Upgrade by diffing against a fresh Forge app, running the rename script, and fixing `jakarta` imports.

### Checkpoint

```bash
git checkout step9-grails7
```

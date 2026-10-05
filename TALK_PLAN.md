# The Quest for the Holy Grails: A Grails 7 Tutorial

**Conference:** Community Over Code (Apache), Glasgow
**Date/Time:** 12 October 2026, 14:20–15:00 GMT
**Track:** Groovy — Room: Tweed
**Speaker:** Ken Kousen

> Note: "The Grooviest AI Client" runs in the same room at 12:00 the same day.
> A brief callback (e.g. the methodMissing AI client showing up as a Grails
> service) would make the two talks feel designed as a pair.

## Abstract (as submitted)

A hands-on tour of Apache Grails 7 — the first release as an ASF Top-Level
Project — through a Monty Python quest domain. Knights, castles, quests, and
tasks guide us from domain modeling through constraints, services, testing,
and what's new in Grails 7.

## Background

Also an "aspirational" talk. The abstract as written is a full-day workshop;
in 40 minutes it needs to be cut to three beats. Skip the controller/view layer
entirely, or wave at scaffolding once and move on.

## Grails 7 facts (verified 5 Oct 2026)

- Grails 7.0.0 shipped 28 October 2025 — the first stable release after
  graduating to an ASF Top-Level Project.
- **Current release: 7.2.4** (about two weeks old). Also maintained:
  7.1.7 and 7.0.17. This project targets 7.2.4.
- **Grails 8.0.0 was tagged 4 Oct 2026** but is still marked pre-release on
  GitHub (ASF vote presumably in progress); 8.0.0-RC2 is what Forge serves as
  NEXT. It may be announced the week of the conference — have a one-slide
  "and here comes 8" ready. The Grails site already advertises a JavaMUG talk
  "An Introduction to Apache Grails 8" on 11 Nov 2026.
- 7.2.4 stack (from Forge BOM): Java 17/21/25, Groovy 4.0.33, Spring Boot
  3.5.16, Hibernate 5.6.15 (GORM default), H2 2.4, Geb 8.0.1, Spock 2.3,
  Gradle 8.14.5.
- 8.0.0-RC2 stack: Groovy 5.1.3, Spring Boot 4.1.1, Spock 2.4-groovy-5.0,
  Hibernate 7 support in GORM, jQuery 4, Undertow option.
- Grails 7 features: containerized browser testing with Testcontainers (Geb),
  optional Micronaut integration, SBOM generation, reproducible builds,
  external configuration integration.
- ASF transition changes: mono repo, reworked CLIs, modernized Gradle plugins
  and tasks, and **new Maven coordinates for all artifacts** (e.g.
  `org.apache.grails:grails-core`, `org.apache.grails.profiles:web`). This
  matters to anyone upgrading from Grails 6 — likely half the room.
- Companion plugin major releases: Spring Security 7.0.0, Quartz 4.0.0,
  Redis 5.0.0.
- Forge moved: start.grails.org now redirects to grails.apache.org/start.
  API hosts are latest.grails.org (RELEASE) and next.grails.org (NEXT).
- Gotcha hit while setting up: `org.gradle.configuration-cache=true` in
  `~/.gradle/gradle.properties` breaks the 7.2.4 `buildProperties` task.
  A project-level gradle.properties cannot override it (user-home wins), so
  build.gradle marks that task `notCompatibleWithConfigurationCache`.
- Fun callback for the AI talk: the Grails 8 release notes mention a
  "Grails 8 Upgrade skill" and list @claude among contributors.

## Project setup (done 5 Oct 2026)

- Generated with Forge: web profile, `com.kousenit.holygrails`, JDK 21,
  Hibernate, Spock, Testcontainers feature. Committed untouched.
- `.sdkmanrc` pins java 21.0.8-tem and grails 7.2.4.
- `./gradlew test` passes on the empty app.

## Proposed structure (three beats)

1. **Build the domain.** `Knight`, `Quest`, `Castle`, `Task` with `hasMany` /
   `belongsTo` and constraints. Show scaffolding once to prove it runs.
   Let the theme carry the transitions:
   - a `Shrubbery` domain class
   - a constraint requiring `favouriteColour` unless the knight is Galahad
   - dynamic finders like `Quest.findAllByCompletedAndKnight(false, arthur)`

2. **One service, one test.** A single transactional service method (e.g.
   assigning a quest, completing a task) plus a Spock test — unit and/or
   integration, with a nod to Testcontainers if time permits.

3. **What changed in 7.** A tight segment aimed at the upgrade crowd:
   dependency versions, Maven coordinate changes, CLI/Gradle changes, and the
   new testing and build features above.

## Practical notes

- Generate the starter app from Grails Forge with the current 7.x version and
  commit it before adding anything, so the diff tells the story.
- Have the finished app ready as a fallback; live-code only the domain classes.
- Monty Python references should do structural work (names, constraints,
  finders), not add material.

## Open questions

- Unit tests vs. integration tests for the service — or both briefly?
- Show Testcontainers browser testing live, or just describe it?
- How to work the AI client callback in (a `GrailService`? a `Quest` summary?)
- Whether to show any GSP/controller at all.

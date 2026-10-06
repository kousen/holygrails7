# The Quest for the Holy Grails

A complete [Apache Grails 7](https://grails.apache.org) application built in stages around a Monty Python quest domain: knights, castles, quests, tasks, and the enemies in the way. It accompanies the talk *The Quest for the Holy Grails: A Grails 7 Tutorial* at Community Over Code, Glasgow, October 2026.

**Start with [labs.md](labs.md).** It walks through every stage, from generating the project to a JSON API, and each stage ends with a git tag:

| Tag | Lab |
|---|---|
| `step0-starter` | Creating the project |
| `step1-quest` | The first domain class, scaffolding, constraints |
| `step2-task` | A related domain class, `belongsTo`, validators |
| `step3-testing` | Unit testing domain classes with Spock |
| `step4-queries` | Seed data; dynamic finders, criteria, where queries |
| `step5-model` | Knight and Castle; the Bridge of Death validator |
| `step6-scaffold` | `@Scaffold` replaces 535 lines of generated code |
| `step7-geocoder` | A service calling Open-Meteo, tested with a Spy |
| `step8-map` | A Leaflet map of the castles |
| `step9-grails7` | What's new in Grails 7; a containerized Geb test |
| `step10-enemies` | Inheritance in GORM |
| `step11-json` | JSON views and `HttpClientSupport` |

`main` is the finished application. To see a stage, `git checkout <tag>`.

## Running it

Requires JDK 21 and Grails 7.2.4; `sdk env` selects both via [SDKMAN](https://sdkman.io). Docker is needed only for the browser tests.

```bash
./grailsw run-app          # http://localhost:8080
./gradlew test             # unit tests
./gradlew integrationTest  # integration, HTTP and browser tests
OFFLINE=1 ./gradlew integrationTest   # skip the live geocoder test
```

## Slides

The talk deck is `slides.md`, a [Slidev](https://sli.dev) presentation:

```bash
npm install
npm run dev      # present at http://localhost:3030
npm run export   # holygrails7-slides.pdf
```

The title photo of Doune Castle is by Bill Boaden, CC BY-SA 2.0, via geograph.org.uk and Wikimedia Commons.

## Licence

Apache License 2.0.

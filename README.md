# dk-magic-forge

Start a [REPL](#repls) in your editor or terminal of choice.

Start the server with:

```clojure
(go)
```

The default API is available under http://localhost:3000/api

System configuration is available under `resources/system.edn`.

To reload changes:

```clojure
(reset)
```

## REPLs

### Cursive

Configure a [REPL following the Cursive documentation](https://cursive-ide.com/userguide/repl.html). Using the default "Run with IntelliJ project classpath" option will let you select an alias from the ["Clojure deps" aliases selection](https://cursive-ide.com/userguide/deps.html#refreshing-deps-dependencies).

### CIDER

Use the `cider` alias for CIDER nREPL support (run `clj -M:dev:cider`). See the [CIDER docs](https://docs.cider.mx/cider/basics/up_and_running.html) for more help.

Note that this alias runs nREPL during development. To run nREPL in production (typically when the system starts), use the kit-nrepl library through the +nrepl profile as described in [the documentation](https://kit-clj.github.io/docs/profiles.html#profiles).

### Command Line

Run `clj -M:dev:nrepl` or `make repl`.

Note that, just like with [CIDER](#cider), this alias runs nREPL during development. To run nREPL in production (typically when the system starts), use the kit-nrepl library through the +nrepl profile as described in [the documentation](https://kit-clj.github.io/docs/profiles.html#profiles).

## PROJECT GOAL
- Name: DK's Magic Forge
- Something like a fantasy magic forge themed, a little bit of flair and animations
- Everyone can book a project and see what project(s) I am currently working on
- name, description, pay 1 EUR to make sure you are a real user, first days of work are "cancelable", it is cheaper if I am allowed to stream the project / make it open source
- project tech stack can be customized: language (clojure / rust / ruby / python), etc.
- both websites and cross platform apps are available
- no SFD! only general design description (with functional params), the purpose is not to make the specifications work, it is to deliver good product
- pay is per x work hours, you can cancel at any time and get your code / project at that stage
- for each project we set a max. price, meaning you can be certain that was you wrote in the description will be delivered with that price
- you can opt-in for hosting, I will charge a margin for running the infra, incidents (X EUR/month).
- everything else is managed with tickets (bug tracker)
- no LIABILITY for code (or maybe 1m of testing period)
- no heavy contract, or heavy conditions you pay for what you see
- I have a hourly rate and I inform you when a certain treshhold has passed.

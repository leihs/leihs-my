# leihs-my (working title)

## Development

## Run The Application

### Backend

    lein repl

In the REPL

    (-main "run")

Inspect and set parameters

    (-main "run" "-h")

### Frontend

    lein figwheel

## Compiled Stylesheets/JS Bundle

    sh scripts/prepare-shared-ui.sh

## Building and Testing

## Build

    lein do clean, uberjar
    # run:
    java -jar target/leihs-my.jar run

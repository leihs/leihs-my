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

    cp leihs-ui/dist/leihs-ui-server-side.js resources/all/public/server_side/bundle.js

    cp leihs-ui/bootstrap-theme-leihs/build/bootstrap-leihs.css resources/all/public/my/css/site.css
    cp leihs-ui/dist/leihs-ui-client-side.js resources/all/public/my/leihs-shared-bundle.js

## Building and Testing

## Build

    lein do clean, uberjar


leihs-my   (working title)
============================================


Development
-----------

## Run The Application

### Backend

    lein repl

In the REPL

    (-main "run")

Inspect and set parameters

    (-main "run" "-h")


### Frontend

    lein figwheel


## Compile Stylesheets

The Bootstrap sources require a relative recent version of SASS and the one
provided with lein-sassy 1.0.8 (time of writing) does not suffice.

    sass --watch resources/all/public/admin/css/site.sass:resources/all/public/admin/css/site.css

Minified css saves only about 10% of the size. We deemed that not worth to mess
with it. In production mode effective caching is enabled.


Building and Testing
--------------------

## Build

    lein do clean, uberjar



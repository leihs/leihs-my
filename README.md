Leihs My
========

Prerequisite for Building and Development
-----------------------------------------

This service builds with Java OpenJDK 8. We recommend https://www.jenv.be/ to
switch between JAVA Versions. And once set up:

    jenv shell 1.8

There are defaults but you might want to overwrite `LEIHS_MY_HTTP_BASE_URL` or
`LEIHS_DATABASE_URL` before starting any of the following scripts. E.g.

    export LEIHS_MY_HTTP_BASE_URL=http://localhost:3240

    export DATABASE_URL="postgresql://localhost:5432/leihs?max-pool-size=5"
    export LEIHS_DATABASE_URL="jdbc:${DATABASE_URL}"


Building and testing require that `ruby` in the `PATH` resolves to at least
Ruby 2.6. If the `RUBY` environment Variable is set, then `PATH` is
amended with `~/.rubies/$RUBY/bin`.

    export RUBY=ruby-2.6.3


Building
--------

Building uses local and S3 cached artefacts. This can be disabled by setting
`BUILD_CACHE_DISABLED` to `YES`:

    export BUILD_CACHE_DISABLED=YES


### Build and Run

    ./bin/run

### Build only

    ./bin/build


Development
-----------

### Run the Backend


    ./bin/dev-run-backend

alternatively:


    lein do clean, repl

  and once the REPL is running:

    (-main "run")


### Run the Frontend

    ./bin/dev-run-frontend


Testing
-------

1. Build and Run

  see above

1. start the application

  `java -jar target/leihs-admin.jar run -s secret`

3. run specs in a second terminal

  bundle exec rspec spec/



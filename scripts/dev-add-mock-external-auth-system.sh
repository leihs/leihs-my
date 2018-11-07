#!/usr/bin/env bash

cd ./database

bundle exec rails runner '
  require "../spec/config/database"
  database

  require "../spec/config/factories"
  require "../spec/factories/authentication_system"
  FactoryBot.create(:authentication_system)
  puts "OK"
'

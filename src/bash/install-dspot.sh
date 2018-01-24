#!/usr/bin/env bash

git clone http://github.com/STAMP-project/dspot.git
cd dspot
mvn install -DskipTests

cd ..
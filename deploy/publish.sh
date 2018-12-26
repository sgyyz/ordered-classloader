#!/bin/bash

if [[ "$TRAVIS_BRANCH" = "master" ]]; then
    mvn deploy --settings $GPG_DIR/settings.xml -DperformRelease=true -DskipTests=true
    exit $?
fi
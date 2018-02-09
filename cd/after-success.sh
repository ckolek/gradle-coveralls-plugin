#!/usr/bin/env bash

if [ "$TRAVIS_PULL_REQUEST" = "false" ] && ([ "$TRAVIS_BRANCH" = "master" ] || [ "$TRAVIS_TAG" != "" ]); then
    ./gradlew uploadArchives
fi

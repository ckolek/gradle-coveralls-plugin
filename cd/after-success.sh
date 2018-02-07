#!/usr/bin/env bash

if [ "$TRAVIS_PULL_REQUEST" = "false" ] && ([ "$TRAVIS_BRANCH" = "master" ] || ([ "$TRAVIS_BRANCH" = "stable" ] && [ "$TRAVIS_TAG" != "" ])); then
    ./gradlew uploadArchives
fi

#!/usr/bin/env bash

openssl aes-256-cbc -K $encrypted_58211a83ee70_key -iv $encrypted_58211a83ee70_iv -in cd/codesigning.gpg.enc -out cd/codesigning.gpg -d


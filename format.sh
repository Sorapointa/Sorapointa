#!/usr/bin/env bash

#

ktlint -F \
    "**/*.kt" \
    "**/*.kts" \
    "!**/generated/**" \
    "!**/build/**" \
  --reporter=checkstyle,output=build/ktlint-report.xml

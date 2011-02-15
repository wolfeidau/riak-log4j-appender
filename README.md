== Introduction

This is a very basic log4j appender which stores records in riak. The aim of this project is to build a high
performance, low maintenance logger which can be used for real time system monitoring.

This is not a replacement for appending to a log file, this will always have a place in recording historical events
or errors. I am attempting to overcome the sometimes daunting task of using grep/sed/awk to mine information from logs.

== Goals

At the moment this library stores log events in riak encoded as JSON. Each record includes a sequence number to
enable simple analysis of data loss.



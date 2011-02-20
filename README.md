# Introduction

This is a very basic log4j appender which stores records in riak. The aim of this project is to build a high
performance, low maintenance logger which can be used for real time system monitoring.

This is not a replacement for appending to a log file, this will always have a place in recording historical events
or errors. I am attempting to overcome the sometimes daunting task of using grep/sed/awk to mine information from logs.

# Goals

At the moment this library stores log events in riak encoded as JSON. Each record includes a sequence number to
enable simple analysis of data loss.

This library is roughly feature complete for my first test run. That said I am keen to see some data before doing another
round of hacking. The features I will be working on next are:
* HTTPS support
* basic auth support
* proxy support

# Acknowledgments

This library depends on [Netty](http://www.jboss.org/netty) which is a NIO client server framework which enables
quick and easy development of network applications such as protocol servers and clients.

# References

When looking for some baseline values for the http connection re-use I chose the following values
(see [Wikipedia - HTTP persistent connection](http://en.wikipedia.org/wiki/HTTP_persistent_connection)).
* Max Connections per protocol/host/port is 5
* Connection Idle timeout is 115 seconds

Introduction
============

This is a bit of a brain dump of my experiances, ideas and observations about the riak java client.

Review
======

Having reviewed the Java Riak client software for a few days it was clear that:

* This had grown with the riak product, features have been added increasing its overall code base quite considerably.
* It is bolted at the hip to the Apache HTTP client.
* It is bolted to the JSON library included in the client.
* The API has is centered around one god object, the riak client.
* Data serialisation is typically managed by the caller, not the API and is constrained to JSON via the included library.

When looking over the API, and reviewing it for my needs I came to conclusion that as all I needed was the store operation in the API. The current structure required me to include quite a large footprint for one operation.

How I Used Riak
===============

As my case as a simple one, where data in the form of log records is persisted in Riak. Once stored these records are rarely if ever purged, and never muted. Most interaction is focused on time based filtering, combined with string matching of messages, or other attributes.

Some Observations
=================

Once I had examined my own requirements I became interested in how others were using riak. From the mailing list it seems:

* People were building routines which "loaded" data into riak and were asking about performance.
* Others are using it to read data, and interface with the map reduce sub system.

It is my view that these should be separted into two different types of API.

Research Ideas
==============

As the riak client can be divided into: 

* Riak specific operations
* Interactions via REST
* HTTP transport for said REST
* Proto buffers request response protocol

I would recommend doing some research into those areas which the riak client will have in common with the REST clients out in the Java community. These libraries embody quite a lot of experience coping with the same issues faced by the riak client. In my view these are:

* The HTTP library used gets re-factored to oblivion..
* There is a new JSON lib which goes really fast.
* NIO2 comes out...

REST frameworks to look at are:

* Jersey
* Restlet

The HTTP client libraries to examine and test:

* Apache HTTP client 4.x 
* Jetty HTTP client
* Sonatype async http client
* HTTP client built into the Sun Java Runtime
* Netty, which supports HTTP and Protobuffers


Some Ideas
==========

If I was to plan to rebuild this client, I would probably choose to continue the stable tree as is and start a more decoupled library with a focus on developer needs. The first building block would be assembling a write module that utilised a nio based client with a focus on:

* More Store operations per second
* More performance in routines using chunked transfer of larger messages. 
* Less repetition in when building messages.

Design wise I would go with a more fluent builder based model. A base http layer would be built with one or more http libraries adapted to it. From their each operation would be designed for configure once, reuse for many repeated operations.

The next area I would focus on is building a skeleton for the existing API "functions", like mapred and link navigation. Then like the ruby ripple API, I would build an integrated high level client that acted like a primitive ORM. 

I would recommend also looking at providing a couple of project templates similar to the environment provided by ripple. Ripple adds not only a high level interface to riak, it also provides some routines to deploy mapred Java script libraries. This provides a captive introduction to interaction with riak rather than just a black box client.

It would be nice to garner more information on how these tasks are performed, and indeed how developers are structuring systems which interface with riak. 

Disclaimer
==========

This is a pretty hastily put together dump, I would probably be happier with more revisions before I pushed this out to a wide audience. I really didn't go out to write war and peace it just sort of happened.

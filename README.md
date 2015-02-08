message-cowboy
==============
[![Build Status](https://travis-ci.org/krizsan/message-cowboy.svg?branch=master)](https://travis-ci.org/krizsan/message-cowboy)
[![Coverage Status](https://coveralls.io/repos/krizsan/message-cowboy/badge.svg?branch=master)](https://coveralls.io/r/krizsan/message-cowboy?branch=master)

Round 'em messages up and bring them home!

Message Cowboy is a configurable integration program which is able to schedule the fetching and delivery of messages from one endpoint to another endpoint.
This version of Message Cowboy is using the Mule ESB 3.4.0 community edition to facilitate transports between different types of endpoints.

Configuration:

1) The directory "production-configurations/connectors" contains Mule configuration files containing named connectors. These connectors may then be used in the endpoint URIs for tasks configured in the database.

2) The directory "production-configurations/transport-service-configurations" contains additional Mule configuration files to be available to the Mule instance running in Message Cowboy. 
An example of such a configuration file may be a Mule flow that receives a message, places it on a JMS queue and then, in a transaction, tries to deliver messages from the JMS queue to some (final) destination. All this in order to be able to guarantee the delivery of the message to the final destination.

Message Cowboy will periodically scan the directories mentioned in 1 and 2 and, if changes are detected, refresh the configuration of the embedded Mule instance.

3) The file message-cowboy-configuration.properties contains a number of configuration properties, such as database configuration etc.

In the default configuration, Message Cowboy will run an embedded HSQLDB that persists the database to file and that other programs, such as a database client program, can connect to. 
In addition, Message Cowboy will also run an embedded ActiveMQ JMS broker in its default configuration, which can be reached at tcp://localhost:61616.

4) The database contains Message Cowboy scheduled task configurations and the scheduled tasks will be refreshed periodically by Message Cowboy based on the configuration in the database.
In the default configuration, the database URL is: jdbc:hsqldb:hsql://localhost:9001/mc-db. The database user is "sa" and the password is "" (the empty string).

To obtain a stand-alone version of the Message Cowboy, copy the following files and folders after having built the application using Maven:
* message-cowboy-1.0.0-SNAPSHOT.jar
Application JAR-file. The version number may be different.

* message-cowboy-configuration.properties
Message Cowboy configuration file.

* production-configurations
Directory containing Mule configuration files.

* libraries
Directory containing third-party libraries.

The relationships between the different files and folders should be preserved, in order for Message Cowboy to be able to run correctly.
If using the embedded database, Message Cowboy will create a directory named "MessageCowboyDatabase" containing the HSQLDB database files next to the application JAR-file.

Please refer to the following links for detailed information on Message Cowboy:
http://www.ivankrizsan.se/2014/07/22/message-cowboy-an-introduction/
http://www.ivankrizsan.se/2014/08/04/using-message-cowboy/
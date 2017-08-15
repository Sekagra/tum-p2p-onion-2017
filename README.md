## Execute the Onion Module ##

To execute the Onion module, you can either use the pre-built version contained in the *release/* directory or [build from source](#building-from-source).
Use this command to run the Onion module:

    release/# java -jar Onion_1.0.jar --config path

*path* is the path to the configuration file used by the Voidphone application.
Following parameters are required in the *onion* section:


* hostkey = \<path_to_pem_key\>                         | Path to the key used by this peer.
* listen_address = \<public_ip\>:\<port\>               | P2P address of this peer.
* api_address = \<pubic/private_ip\>:\<port\>           | Address used for API connections to the Onion module
* round_interval = \<round_interval_in_seconds\>      | Round interval the Onion module shall use
* intermediate_hops = \<number_of_intermediate_hops\> | Number of intermediate hops in the tunnel between this peer and the receiver.

In addition, *api_address = \<pubic/private_ip\>:\<port\>* is also required in the *rps* and *auth* section.

A sample configuration is contained in the *config/* directory. (Adapted from the bootstrap.conf in the [testing framework](https://gitlab.lrz.de/voidphone/testing))

## Help and Command Line Arguments ##

To display the help text of the Onion module following command:

    release/# java -jar Onion_1.0.jar --help

Subsequent command line arguments are supported:

* help: Display the help text.
* config: Path to the configuration file. (Required)
* loglevel: The log level used by the Onion module (Default: INFO). Currently all log messages will be printed to stdout. (Optional)



## Building from Source

Maven is required to build the Onion module from source.
We tested the build using Maven-3.3.9 (Apache License 2.0).

The Java source code of the project is contained in the *src/* directory in the repository.
As Maven is used for dependency and build management, execute the following command in the *src/* directory to build the project from source:

    src/# mvn clean package

After a successful build, Maven creates *Onion_1.0.jar* in the *src/target/* directory.
This jar file contains all necessary dependencies.

The standard build will execute the our unit tests and a large integration test (Build tunnel, send data, destroy tunnel with mockups).
To skip all tests use following command to build the project:

    src/# mvn clean package -DskipTests


## Dependencies

Our implementation has the following dependencies:

* jUnit-RELEASE (4.12 currently): Eclipse Public License 1.0
* Bouncy Castle-1.57: MIT X11 License
* Netty-4.1.12.Final: Apache License 2.0
* Google Guice-4.1.0: Apache License 2.0
* log4j-2.8.2: Apache License 2.0
* ini4j-0.5.1: Apache License 2.0

As we are using Maven, all dependencies will be downloaded automatically during build.

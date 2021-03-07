# Password Manager

[![BSD-2 license](https://img.shields.io/badge/License-BSD--2-informational.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-15-orange?logo=java)](https://www.oracle.com/java/technologies/javase-downloads.html)
[![JavaFX](https://img.shields.io/badge/JavaFX-15-orange?logo=java)](https://openjfx.io/)

Desktop application to store passwords, credit card numbers and other sensitive information. 
Application uses 256-bit AES encryption.

![Screenshot](docs/main-window.png)

## Security Considerations

Application enforces security via file encryption only. Application makes no effort to counter attacks targeted 
to user account, operating system or hardware including RAM.

## Build

Make sure ```JAVA_HOME``` is set to JDK 15.

Execute the following:
```shell script
$ ./gradlew clean build
```

## Run

```shell script
$ ./gradlew run
```

To open specific file add ```-Pfile=<file>``` to the command line.

## Binary Packages

To build binary installers perform the following steps:
* On Microsoft Windows: install [WiX Toolset](https://wixtoolset.org/releases/), add its binary directory to ```PATH``` 
environment variable
* Execute the following commands:

```shell script
$ ./gradlew clean jpackage -Pversion=&lt;version>
```

Installation packages will be found in ```build/dist``` directory.

## Support

There is no support for this application.

# Basic Keycloak Gatling Load Test

This project is a basic gatling integration for loadtesting keycloak logins.

_Please note:_

The test scenario in this project executes multiple logins for some user "TESTACCOUNT", who is a part of realm "LOAD-TEST", can complete their authentification once their username and password has been put in. In order to run this test, these parameters must be either implemented on your keycloak server or the `KeycloakLoginSimulator.scala` test scenario must be modified to reflect your existing keycloak server.

## Installations

You must have the following installed:

- Keycloak version 4.8 which can be downloaded by following the official [keycloak installation guide](https://www.keycloak.org/docs/4.8/getting_started/index.html#_install-boot)

- Scala verion 2.12 which can be downloaded from the [scala-lang website](https://www.scala-lang.org/download/)

- Scala Build Tool (sbt) which can be downloaded using the [Homebrew](https://docs.brew.sh/Installation) command `brew install sbt` on Linux/Unix systems or by following the steps found on the [scala-sbt website](https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Windows.html) for Windows.

_NOTE: This version of sbt requires JDK8 or JDK11 to be installed in order to execute._

## Running this test scenario

- Boot up keycloak server.
  Steps to this can be found in the [keycloak installation guide](https://www.keycloak.org/docs/4.8/getting_started/index.html#_install-boot).

- Locate this github project from your terminal and run command `sbt`.
  This should configure the classpath and project settings to your workstation.

- Once the sbt interactive build tool has started up run the command `gatling:test`.
  This should run every test case scenarios defined in a scala file located at `/src/test`.

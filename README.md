# nostr-java
[![](https://jitpack.io/v/xyz.tcheeric/nostr-java.svg)](https://jitpack.io/#xyz.tcheeric/nostr-java)

Nostr-java is a library for generating, signing, and publishing nostr events to relays.

## Requirements
- Maven
- Java 21+

## Usage
### To use nostr-java in your project, two options:

#### Option 1 - add release version and jitpack.io repository to your pom.xml file

```xml
<properties>
    <nostr-java.version>v0.007.1-alpha</nostr-java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
#### Option 2 - Check out and build project directly from source  

```bash
$ cd <your_git_home_dir>
$ git clone git@github.com:tcheeric/nostr-java.git
$ cd nostr-java
$ git checkout <your_chosen_branch>
```

<details>
  <summary>unit-tested build (does not require a nostr-relay for testing)</summary>

###### maven
    (unix)
      $ ./mvnw clean test
      $ ./mvnw install -Dmaven.test.skip=true

    (windows)
      $ ./mvnw.cmd clean test
      $ ./mvnw.cmd install -Dmaven.test.skip=true


###### gradle

    (unix)
      $ ./gradlew clean test
      $ ./gradlew publishToMavenLocal

    (windows)
      $ ./gradlew.bat clean test
      $ ./gradlew.bat publishToMavenLocal
</details>

<details>
  <summary>integration-tested build (requires a nostr-relay for testing)</summary>

valid relay(s) must **_first_** be defined in [relays.properties](nostr-java-api/src/main/resources/relays.properties) file, then

###### maven
    (unix)
      $ ./mvnw clean install

    (windows)
      $ ./mvnw.cmd clean install

###### gradle
    (unix)
      $ ./gradlew clean check
      $ ./gradlew publishToMavenLocal

    (windows)
      $ ./gradlew.bat clean check
      $ ./gradlew.bat publishToMavenLocal        
</details>

#### add dependency to your pom.xml

```xml
<dependencies>
     <dependency>
        <groupId>nostr-java</groupId>
        <artifactId>nostr-java-api</artifactId>
        <version>${nostr-java.version}</version>
    </dependency>
</dependencies>
```



## Examples
I recommend having a look at these repositories/module for examples:
  - [nostr-example](https://github.com/tcheeric/nostr-java/tree/main/nostr-java-examples) module
  - [nostr-client](https://github.com/tcheeric/nostr-client) github repository
  - [SuperConductor](https://github.com/avlo/superconductor) nostr relay


## Supported NIPs
The following NIPs are supported by the API out-of-the-box:
- [NIP-1](https://github.com/nostr-protocol/nips/blob/master/01.md)
- [NIP-2](https://github.com/nostr-protocol/nips/blob/master/02.md)
- [NIP-3](https://github.com/nostr-protocol/nips/blob/master/03.md)
- [NIP-4](https://github.com/nostr-protocol/nips/blob/master/04.md)
- [NIP-5](https://github.com/nostr-protocol/nips/blob/master/05.md)
- [NIP-8](https://github.com/nostr-protocol/nips/blob/master/08.md)
- [NIP-9](https://github.com/nostr-protocol/nips/blob/master/09.md)
- [NIP-12](https://github.com/nostr-protocol/nips/blob/master/12.md)
- [NIP-14](https://github.com/nostr-protocol/nips/blob/master/14.md)
- [NIP-15](https://github.com/nostr-protocol/nips/blob/master/15.md)
- [NIP-20](https://github.com/nostr-protocol/nips/blob/master/20.md)
- [NIP-23](https://github.com/nostr-protocol/nips/blob/master/23.md)
- [NIP-25](https://github.com/nostr-protocol/nips/blob/master/25.md)
- [NIP-28](https://github.com/nostr-protocol/nips/blob/master/28.md)
- [NIP-30](https://github.com/nostr-protocol/nips/blob/master/30.md)
- [NIP-32](https://github.com/nostr-protocol/nips/blob/master/32.md)
- [NIP-40](https://github.com/nostr-protocol/nips/blob/master/40.md)
- [NIP-42](https://github.com/nostr-protocol/nips/blob/master/42.md)
- [NIP-44](https://github.com/nostr-protocol/nips/blob/master/44.md)
- [NIP-46](https://github.com/nostr-protocol/nips/blob/master/46.md)
- [NIP-57](https://github.com/nostr-protocol/nips/blob/master/57.md)
- [NIP-60](https://github.com/nostr-protocol/nips/blob/master/60.md)
- [NIP-61](https://github.com/nostr-protocol/nips/blob/master/61.md)
- [NIP-99](https://github.com/nostr-protocol/nips/blob/master/99.md)

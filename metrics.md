###  nostr-java & superconductor:  gradle -vs- maven metrics

----
##### nostr-java clean build

###### maven:   <span style="color:blue">~19 seconds</span> 

```java
$ time mvn clean install -Dmaven.test.skip=true

  [INFO] BUILD SUCCESS
  [INFO] ------------------------------------------------------------------------
  [INFO] Total time:  17.592 s
  [INFO] Finished at: 2025-03-14T22:31:52-07:00
  [INFO] ------------------------------------------------------------------------

  real	0m19.156s  <-----------------------------------------------------------   ~19 sec
  user	0m58.042s
  sys	0m1.616s
```

###### gradle:  <span style="color:blue">~9 seconds</span>
``` java
$ time gradle clean build -x test  

BUILD SUCCESSFUL in 9s
38 actionable tasks: 38 executed
Configuration cache entry reused.

  real    0m9.618s <-------------------------------------------------------------   ~9 sec
  user    0m2.383s
  sys     0m0.116s  
```
----

#### nostr-java, subsequent build 

###### maven:  <span style="color:blue">~21 seconds</span>

```java
$ time mvn install -Dmaven.test.skip=true
  [INFO] BUILD SUCCESS
  [INFO] ------------------------------------------------------------------------
  [INFO] Total time:  19.680 s
  [INFO] Finished at: 2025-03-14T22:39:35-07:00
  [INFO] ------------------------------------------------------------------------

  real	0m21.324s  <-------------------------------------------------------------   ~21sec
  user	1m3.300s
  sys	0m1.771s
```

###### gradle:  <span style="color:blue">~1 second</span>

```java
$ time gradle build -x test
  Reusing configuration cache.

  BUILD SUCCESSFUL in 1s
  27 actionable tasks: 27 up-to-date
  Configuration cache entry reused.

  real  0m1.178s  <-------------------------------------------------------------   ~1sec
  user	0m2.082s
  sys	0m0.176s
```

----
#### superconductor clean build:

###### maven:  <span style="color:blue">~11 seconds</span>

```java
$ time mvn clean install -Dmaven.test.skip=true
  [INFO] BUILD SUCCESS
  [INFO] ------------------------------------------------------------------------
  [INFO] Total time:  10.437 s
  [INFO] Finished at: 2025-03-14T22:46:58-07:00
  [INFO] ------------------------------------------------------------------------

  real	0m11.989s  <-------------------------------------------------------------   ~11sec
  user	0m36.291s
  sys	0m1.351s
```

###### gradle:  <span style="color:blue">~7 seconds</span>

```java
$ time gradle clean build -x test
        
  BUILD SUCCESSFUL in 7s
  7 actionable tasks: 4 executed, 2 from cache, 1 up-to-date

  real    0m7.655s  <------------------------------------------------------------   ~7sec
  user	0m2.800s
  sys	0m0.164s
```

----
#### superconductor clean build integration- test:  

###### maven:  <span style="color:blue">~1 minute, 15 seconds</span>
``` java
$ mvn clean install

  [INFO] Results:
  [INFO]
  [INFO] Tests run: 52, Failures: 0, Errors: 0, Skipped: 0
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD SUCCESS
  [INFO] ------------------------------------------------------------------------
  [INFO] Total time:  01:15 min  <--------------------------------------------------- ~1min 15sec*   
  [INFO] Finished at: 2025-03-14T23:26:59-07:00
  [INFO] ------------------------------------------------------------------------

  real	1m16.800s  <----------------------------------------------------------------- ~1min 16sec*
  user	2m58.945s
  sys	0m4.427s
```
_*one-second time diff re: maven publish to local repo (`~/.m2/xyz/tcheeric/...`)_

###### gradle:  <span style="color:blue">~49 seconds</span>
``` java
$ time gradle build

  SUCCESS: Executed 61 tests in 41.1s

  BUILD SUCCESSFUL in 49s
  10 actionable tasks: 6 executed, 4 from cache

  real    0m49.269s <-------------------------------------------------------------   ~49sec
  user    0m2.854s
  sys	0m0.201s
```

----

#### superconductor subsequent build test  
_historical note:   significant down/wait-time using maven occurs here, thus motivating gradle build/test option for superconductor, initially, with nostr-java subsequently profiting from same feature/option.  **from** <span style="color:blue">**~1 minute**</span> **down to** </span><span style="color:blue">**~4 seconds**</span> significantly improves workflow continuity.    furthermore, multiple iterative test builds occurring as per typical development results in significant cumulative time saved._

###### maven:  <span style="color:blue">~1 minute, 3 seconds</span>

```java
$ time mvn install
        
  [INFO] Results:
  [INFO] 
  [INFO] Tests run: 52, Failures: 0, Errors: 0, Skipped: 0
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD SUCCESS
  [INFO] ------------------------------------------------------------------------
  [INFO] Total time:  01:03 min  <---------------------------------------------------------   ~1min 3sec**
  [INFO] Finished at: 2025-03-14T23:35:18-07:00
  [INFO] ------------------------------------------------------------------------

  real	1m6.132s  <------------------------------------------------------------------------   ~1min 6sec**
  user	2m32.305s
  sys	0m3.797s
```
_** three-second time diff re: maven publish to local repo (`~/.m2/xyz/tcheeric/...`)_

###### gradle:  <span style="color:blue">~4 seconds</span>
```java
$ time gradle build

BUILD SUCCESSFUL in 3s
10 actionable tasks: 10 up-to-date

real    0m4.020s  <------------------------------------------------------------------------   ~4sec
user	0m2.913s
```

----

#### nostr-java integration tests against running SC

###### maven:  <span style="color:blue">~29 seconds</span>

```java
$ time mvn test

  [INFO] Results:
  [INFO] 
  [INFO] Tests run: 159, Failures: 0, Errors: 0, Skipped: 0
  [INFO] 
  [INFO] ------------------------------------------------------------------------
  [INFO] Reactor Summary for nostr-java 0.6.5-SNAPSHOT:
  [INFO] 
  [INFO] nostr-java ......................................... SUCCESS [  0.012 s]
  [INFO] nostr-java-util .................................... SUCCESS [  0.900 s]
  [INFO] nostr-java-crypto .................................. SUCCESS [  0.353 s]
  [INFO] nostr-java-base .................................... SUCCESS [  0.246 s]
  [INFO] nostr-java-event ................................... SUCCESS [  0.142 s]
  [INFO] nostr-java-id ...................................... SUCCESS [  0.164 s]
  [INFO] nostr-java-client .................................. SUCCESS [  0.438 s]
  [INFO] nostr-java-encryption .............................. SUCCESS [  0.050 s]
  [INFO] nostr-java-encryption-nip04 ........................ SUCCESS [  0.107 s]
  [INFO] nostr-java-encryption-nip44 ........................ SUCCESS [  0.114 s]
  [INFO] nostr-java-api ..................................... SUCCESS [  0.190 s]
  [INFO] nostr-java-examples ................................ SUCCESS [  0.212 s]
  [INFO] nostr-java-test .................................... SUCCESS [ 25.933 s]
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD SUCCESS
  [INFO] ------------------------------------------------------------------------
  [INFO] Total time:  29.084 s    <------------------------------------------------------------------------   ~29sec***
  [INFO] Finished at: 2025-03-15T12:41:13-07:00
  [INFO] ------------------------------------------------------------------------

  real	0m30.669s   <--------------------------------------------------------------------------------------   ~30sec***
  user	1m2.185s
  sys	0m1.698s
```
_*** one-second time diff re: maven publish to local repo (`~/.m2/xyz/tcheeric/...`)_

----

##### useful gradle commands (w/ maven equivalents) 

```java 
alias gc='gradle clean'  // 'maven clean' 
alias gb='gradle build'  // 'maven build'
alias gcb='gradle clean build' // 'maven clean build'

alias gbnotest='gradle build -x test'         // 'maven build -Dmaven.test.skip=true'
alias gcbnotest='gradle clean build -x test'  // 'maven clean build -Dmaven.test.skip=true'

alias gpub='gradle publishToMavenLocal'  // 'maven install'
```

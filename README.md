# Discord IPC
Pure Java 8 library for interacting with locally running Discord instance without the use of JNI.  
Currently, only supports retrieving the logged-in user and setting user's activity.  
The library is tested on Windows.

## Gradle
```groovy
repositories {
    maven { url "https://jitpack.io" }
}
```
```groovy
dependencies {
    implementation 'com.github.ccetl:discord-ipc:1.2'
    // not included but required are:
    implementation 'com.google.code.gson:gson:2.8.9' 
    implementation 'com.kohlschutter.junixsocket:junixsocket-core:2.8.3'
    implementation 'org.apache.commons:commons-exec:1.3'
}
```

## Examples
For examples check out `example/src/main/java/test/Main.java`.

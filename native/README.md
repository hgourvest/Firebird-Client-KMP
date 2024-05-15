# Compile JNI Libraries

Compiled JNI libraries are automatically placed in this folder:

    ../library/src/jvmMain/resources/jni

## Linux and Window

The JNI libraries are cross-compiled on a Debian distribution for Windows and Linux. The following architectures are supported:

- Linux (x86, x64, armv7, armv8)
- Windows (x86, x64)

Install the following packages first.

```
apt install git build-essential cmake g++-i686-linux-gnu g++-arm-linux-gnueabi g++-aarch64-linux-gnu g++-mingw-w64
```

Use the following command to compile all libraries.

```
sh build.sh
```

## Mac Os X

It is only possible to compile the Mac Os library on Mac Os. Install Xcode Command Line Tools and run the following command:

```
sh macos-x86-64.sh
sh macos-aarch64.sh
```

## Android

Android libraries are compiled by Android Gradle Plugin, so you don't need to worry about them. 


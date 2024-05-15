

# Firebird SQL Client Library for Kotlin Multiplatform

This is a [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) project for working with a [Firebird SQL](https://firebirdsql.org/) database.

Supported systems are JVM, Android and Kotlin Native.

Firebird 5 is the version targeted in this library, it should work with earlier versions but this has not been tested.

## Organization

The project is organized into three modules,
- native: The JNI library
- library: The main kotlin library
- library-ext: An extension to the main module containing the following dependencies:
  - [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime)
  - [kotlin-multiplatform-bignum](https://github.com/ionspin/kotlin-multiplatform-bignum)

## Installation

### Android

⚠️ Android 14 is not yet supported due to this [issue](https://github.com/FirebirdSQL/firebird/issues/8110).

Download [Firebird 5 Android Embedded](https://firebirdsql.org/en/firebird-5-0/#android-embed) and put it in the "libs" 
folder at the root of your module. Also include the .aar packages from this project.

Then declare these dependencies in your gradle file

``` kotlin
dependencies {
   ...
   implementation(files("libs/Firebird-5.0.0.xxxx-x-android-embedded.aar"))
   implementation(files("libs/FirebirdClient-release.aar"))
   
   // optional dependencies
   implementation(files("libs/FirebirdClient-ext-release.aar"))
   implementation("com.ionspin.kotlin:bignum:0.3.9")
   implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0-RC.2")
}
```

Initialize the Firebird library configuration. This extracts the necessary configuration files into the "firebird" subfolder of your application's storage space and tells Firebird where to find them.

``` kotlin
override fun onCreate(savedInstanceState: Bundle?) {
   super.onCreate(savedInstanceState)
   FirebirdConf.extractAssets(baseContext, false)
   FirebirdConf.setEnv(baseContext)
   ...
```

### JVM

### Native



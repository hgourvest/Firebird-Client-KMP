

# Firebird SQL Client Library for Kotlin Multiplatform

This is a [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) project for working with a [Firebird SQL](https://firebirdsql.org/) database.

Don't know Firebird? I recommend reading [Firebird 5 Quick Start Guide](https://firebirdsql.org/file/documentation/html/en/firebirddocs/qsg5/firebird-5-quickstartguide.html), for a step-by-step introduction.

Supported systems are JVM, Android and Kotlin Native.

Firebird 5 is the version targeted in this library, it should work with earlier versions but this has not been tested.

## Organization

The project is organized into three modules,
- native: The JNI library
- library: The main kotlin library
- library-ext: An extension to the main module containing the following dependencies:
  - [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime)
  - [kotlin-multiplatform-bignum](https://github.com/ionspin/kotlin-multiplatform-bignum)

## Integration

### JVM & Kotlin Native

``` kotlin
dependencies {
   // the main dependency
   implementation("com.progdigy:FirebirdClient:1.0")
   
   // or the extended dependency 
   implementation("com.progdigy:FirebirdClient-ext:1.0")
}
```

### Android

Download [Firebird 5 Android Embedded](https://firebirdsql.org/en/firebird-5-0/#android-embed) and put it in the "libs" 
folder at the root of your module.

⚠️ There is an [issue](https://github.com/FirebirdSQL/firebird/issues/8110) with Android 14 that has been fixed but is not yet officially available,
you must use the latest version of the AAR file from [Github Continuous Integration](https://github.com/FirebirdSQL/firebird/actions/workflows/main.yml?query=branch%3Av5.0-release+). 
you must be logged in to download the file.

Then declare these dependencies in your gradle file

``` kotlin
dependencies {
   implementation(files("libs/Firebird-5.0.0.xxxx-x-android-embedded.aar"))
   implementation("com.progdigy:FirebirdClient:1.0")
    
}
```

Initialize the Firebird library configuration. This extracts the necessary configuration files into the "firebird" subfolder of your application's storage space and tells Firebird where to find them.

``` kotlin
override fun onCreate(savedInstanceState: Bundle?) {
   super.onCreate(savedInstanceState)
   FirebirdConf.extractAssets(baseContext, false)
   FirebirdConf.setEnv(baseContext)
   
```

## Use cases

### Onboarding

This example creates a local database in **embedded** mode, the default encoding is UTF8 and dialect 3 is recommended for a new database.

```kotlin
fun attachment(block: Attachment.() -> Unit) { 
    val database = File("/tmp/database.fdb")
    if (!database.exists()) {
        Attachment.createDatabase(database.absolutePath, makeDPB {
            setDBCharset("UTF8")
            sqlDialect(3)
        }).use {
            it.transaction {
                execute("CREATE TABLE CUSTOMER (id int, name varchar(255))")
                execute("CREATE GENERATOR GEN_CUSTOMER")

                commitRetaining()

                statement("INSERT INTO CUSTOMER (id, name) VALUES (gen_id(GEN_CUSTOMER, 1), ?)") {
                    listOf("Terry Bull", "Jack Pott", "Anna Conda").forEach { name ->
                        params.setString(0, name)
                        execute()
                    }
                }
            }
            it.block()
        }
    } else
        Attachment.attachDatabase(database.absolutePath).use(block)
}

fun main() {
    attachment {
        statement("select id, name from CUSTOMER") {
            forEach {
                val id = getInt(0)
                val name = getString(1)
                println("id: $id, name: $name")
            }
        }
    }
}
```

### Remote host

It is possible to connect to a remote database, please refer to the relevant [documentation](https://firebirdsql.org/file/documentation/html/en/firebirddocs/qsg5/firebird-5-quickstartguide.html#qsg5-databases-connstrings).

```kotlin
Attachment.attachDatabase("localhost:employee", makeDPB {
    userName("SYSDBA")
    password("masterkey")
})
```

### Open

```kotlin
statement("select id, name from CUSTOMER") {
    open {
        while (!eof) {
            val id = getInt(0)
            val name = getString(1)
            println("id: $id, name: $name")
            fetch()
        }
    }
}
```

### Execute

```kotlin
statement("INSERT INTO CUSTOMER (id, name) VALUES (gen_id(GEN_CUSTOMER, 1), ?)") {
    params.setString(0, "Barry Cade")
    execute()
}
```

### Returning

```kotlin
statement("INSERT INTO CUSTOMER (id, name) VALUES (gen_id(GEN_CUSTOMER, 1), ?) RETURNING ID") {
    params.setString(0, "Ella Vader")
    execute()
    val id = result.getInt(0)
    println("id: $id")
}
```
### Transaction

If you need to run several SQL queries that must be executed atomically, group them together in a single transaction.

```kotlin
transaction {

}
```

You may need to validate or cancel certain changes without leaving the transaction.

```kotlin
transaction {
  commitRetaining()
  rollbackRetaining()
}
```

Transactions can be configured in a number of ways.

```kotlin
transaction(makeTPB {
    write()
    readCommitted()
    noWait()
    recVersion()
}) {

}
```

### Select for update

```kotlin
transaction {
    statement("SELECT * FROM CUSTOMER FOR UPDATE", "S") {
        open {
            statement("UPDATE CUSTOMER SET NAME = ? WHERE CURRENT OF S") {
                while (!eof) {
                    params.setString(0, "John Doe")
                    execute()
                    fetch()
                }
            } 
        }
    }
}
```
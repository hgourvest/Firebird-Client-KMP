package com.progdigy.fbclient

/**
 * FirebirdException is a custom exception class that is thrown when a Firebird operation fails.
 *
 * @property message The error message associated with the exception.
 */
class FirebirdException(val status: STATUS, message: String): Exception(message) {
    constructor(message: String) : this(0, message)
}
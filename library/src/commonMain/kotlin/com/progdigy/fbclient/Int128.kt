package com.progdigy.fbclient

/**
 * Represents a 128-bit signed integer.
 *
 * @property a The most significant 64 bits.
 * @property b The least significant 64 bits.
 */

data class Int128(val a: Long, val b: Long) {
    companion object {
        val ZERO = Int128(0, 0)
    }
}
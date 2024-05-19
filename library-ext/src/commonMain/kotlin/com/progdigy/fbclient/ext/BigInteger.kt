package com.progdigy.fbclient.ext

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.BigInteger.Companion.createFromWordArray
import com.ionspin.kotlin.bignum.integer.Sign
import com.progdigy.fbclient.Attachment.Transaction.SQLDA

/** This method retrieves a BigInteger value from the SQLDA object at the specified index.
 *
 * @param index the index of the field in the SQLDA object to retrieve the BigInteger value
 *  from
 * @return the BigInteger value at the specified index, or null if the field is null
 */
fun SQLDA.getBigIntegerOrNull(index: Int): BigInteger? = if (getIsNull(index)) null else getBigInteger(index)

/**
 * Returns a BigInteger value from the SQLDA at the specified index.
 *
 * @param index the index of the value in the SQLDA
 * @return the BigInteger value at the specified index
 */
fun SQLDA.getBigInteger(index: Int): BigInteger = getInt128(index).let { int128ToBigInteger(it[0], it[1]) }

/**
 * Sets the value of a BigInteger at the specified index in the SQLDA.
 *
 * @param index The index at which to set the BigInteger value.
 * @param value The BigInteger value to be set.
 */
fun SQLDA.setBigInteger(index: Int, value: BigInteger) = value.firebirdDecode { a, b ->
    setInt128(index, a, b)
}

/**
 * Decodes the current [BigInteger] using the Firebird encoding scheme and returns the result as a [LongArray].
 *
 * The Firebird encoding scheme represents a [BigInteger] as a pair of [Long] values.
 * The decoded values are calculated based on the sign and the content of the [BigInteger].
 *
 * @return A [LongArray] containing the decoded values. The first element of the array represents the most significant bits (v0) and the second element represents the least significant
 *  bits (v1).
 */
fun BigInteger.firebirdDecode(): LongArray {
    var v0 = 0L
    var v1 = 0L
    firebirdDecode { a, b ->
        v0 = a; v1 = b
    }
    return longArrayOf(v0, v1)
}

/**
 * Decodes a [BigInteger] using Firebird encoding and invokes the provided [block] with the resulting values.
 *
 * The Firebird encoding scheme represents a [BigInteger] as a pair of [Long] values.
 */
@OptIn(ExperimentalUnsignedTypes::class)
inline fun BigInteger.firebirdDecode(block: (a:Long, b:Long) -> Unit) {
    val words = getBackingArrayCopy().toLongArray()
    val v0: Long = words[0]
    val v1: Long = if (words.size > 1) words[1] else 0L
    when (getSign()) {
        Sign.POSITIVE -> {
            if (v1 == 0L) {
                block(v0, 0L)
            } else {
                if (v1 % 2 == 0L) {
                    block(v0, v1 / 2)
                } else {
                    block(Long.MIN_VALUE + v0, (v1 - 1) / 2)
                }
            }
        }
        Sign.NEGATIVE -> {
            if (v1 == 0L) {
                block(-v0, -1L)
            } else {
                if (v0 == 0L) {
                    if (v1 % 2 == 0L)
                        block(0L, -(v1 / 2))
                    else
                        block(Long.MIN_VALUE, -(v1 + 1) / 2)
                } else {
                    if (v1 % 2 == 0L)
                        block(-v0, -(v1 / 2) - 1)
                    else
                        block(Long.MAX_VALUE - v0 + 1, -(v1 + 1) / 2)
                }
            }
        }
        Sign.ZERO -> block(0, 0)
    }
}

/**
 * Converts two long values representing a 128-bit integer to a BigInteger.
 *
 * @param a The most significant 64 bits of the 128-bit integer.
 * @param b The least significant 64 bits of the 128-bit integer.
 * @return The BigInteger representation of the 128-bit integer.
 */
@OptIn(ExperimentalUnsignedTypes::class)
fun int128ToBigInteger(a: Long, b: Long): BigInteger {
    val sign = when {
        b == 0L -> if (a == 0L) Sign.ZERO else Sign.POSITIVE
        b > 0L -> Sign.POSITIVE
        else -> Sign.NEGATIVE
    }

    return when (sign) {
        Sign.POSITIVE ->
            createFromWordArray((if (a < 0)
                longArrayOf(a - Long.MIN_VALUE, b * 2 + 1)
            else if (b == 0L)
                longArrayOf(a)
            else
                longArrayOf(a, b * 2)).toULongArray(), sign)
        Sign.NEGATIVE ->
            createFromWordArray((when {
                a == 0L -> longArrayOf(0L, -b * 2)
                a == Long.MIN_VALUE -> longArrayOf(0L, (-b - 1) * 2 + 1)
                a < 0L -> {
                    val v2 = (-b - 1)
                    if (v2 == 0L)
                        longArrayOf(-a)
                    else
                        longArrayOf(-a, v2 * 2)
                }
                else ->
                    longArrayOf(Long.MAX_VALUE - a + 1, (-b - 1) * 2 + 1)
            }).toULongArray(), sign)
        Sign.ZERO -> BigInteger.ZERO
    }
}
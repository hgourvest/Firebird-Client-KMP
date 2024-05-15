package com.progdigy.fbclient.ext


import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.BigInteger.Companion.createFromWordArray
import com.ionspin.kotlin.bignum.integer.Sign
import com.progdigy.fbclient.Int128
import com.progdigy.fbclient.Attachment.Transaction.*
import com.progdigy.fbclient.Type

fun SQLDA.getBigIntegerOrNull(index: Int): BigInteger? = if (getIsNull(index)) null else getBigInteger(index)
fun SQLDA.getBigInteger(index: Int): BigInteger = getInt128(index).toBigInteger()

fun SQLDA.setBigInteger(index: Int, value: BigInteger) = Int128.fromBigInteger(value) { a, b ->
    setInt128(index, a, b)
}

fun BigInteger.fbCalcType(): Type =
    when {
        this <= Short.MAX_VALUE && this >= Short.MIN_VALUE -> Type.SHORT
        this <= Int.MAX_VALUE && this >= Int.MIN_VALUE -> Type.INT
        this <= Long.MAX_VALUE && this >= Long.MIN_VALUE -> Type.LONG
        else -> Type.INT128
    }

fun Int128.Companion.fromBigInteger(value: BigInteger): Int128 {
    var v0 = 0L
    var v1 = 0L
    fromBigInteger(value) { a, b ->
        v0 = a; v1 = b
    }
    return Int128(v0, v1)
}

fun Int128.toBigInteger(): BigInteger = Int128.toBigInteger(a, b)

@OptIn(ExperimentalUnsignedTypes::class)
inline fun Int128.Companion.fromBigInteger(value: BigInteger, block: (a:Long, b:Long) -> Unit) {
    val words = value.getBackingArrayCopy().toLongArray()
    val v0: Long = words[0]
    val v1: Long = if (words.size > 1) words[1] else 0L
    when (value.getSign()) {
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
        Sign.ZERO -> ZERO
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun Int128.Companion.toBigInteger(a: Long, b: Long): BigInteger {
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
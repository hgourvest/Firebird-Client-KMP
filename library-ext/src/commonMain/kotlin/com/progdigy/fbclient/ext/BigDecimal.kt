package com.progdigy.fbclient.ext

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import com.progdigy.fbclient.FirebirdException
import com.progdigy.fbclient.Int128
import com.progdigy.fbclient.Attachment.Transaction.*
import com.progdigy.fbclient.Type

fun SQLDA.getBigDecimalOrNull(index: Int): BigDecimal? = if (getIsNull(index)) null else getBigDecimal(index)

fun SQLDA.getBigDecimal(index: Int): BigDecimal =
    BigDecimal.fromBigIntegerWithScale(when (getType(index)) {
        Type.SHORT -> getShort(index).toBigInteger()
        Type.INT -> getInt(index).toBigInteger()
        Type.LONG -> getLong(index).toBigInteger()
        Type.INT128 -> getInt128(index).toBigInteger()
        else ->
            throw FirebirdException("Unexpected Data Type")
    }, getScale(index))

fun SQLDA.setBigDecimal(index: Int, value: BigDecimal) {
    val int = value.fbScaleTo(getScale(index))
    when (getType(index)) {
        Type.SHORT -> setShort(index, int.shortValue(true))
        Type.INT -> setInt(index, int.intValue(true))
        Type.LONG -> setLong(index, int.longValue(true))
        Type.INT128 -> Int128.fromBigInteger(int) { a, b ->
            setInt128(index, a, b)
        }
        else ->
            throw FirebirdException("Unexpected Data Type")
    }
}

fun BigDecimal.fbCalcScale(): Long = exponent - significand.numberOfDecimalDigits() + 1


fun BigDecimal.fbScaleTo(value: Long): BigInteger {
    var current = fbCalcScale()
    if (current == value)
        return significand
    else {
        var ret = significand
        if (current > value) {
            while (current > value) {
                ret = ret.multiply(BigInteger.TEN)
                current--
            }
        } else {
            while (current < value) {
                if (ret.mod(BigInteger.TEN) == BigInteger.ZERO)
                    ret = ret.divide(BigInteger.TEN)
                else
                    throw ArithmeticException()
                current++
            }
        }
        return ret
    }
}

fun BigDecimal.Companion.fromBigIntegerWithScale(value: BigInteger, scale: Long): BigDecimal {
    if (scale < 0) {
        val exponent = value.numberOfDecimalDigits() - 1 + scale
        return fromBigIntegerWithExponent(value, exponent)
    } else
        throw FirebirdException("Invalid scale $scale")
}

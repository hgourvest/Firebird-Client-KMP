package com.progdigy.fbclient.ext

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import com.progdigy.fbclient.Attachment.Transaction.SQLDA
import com.progdigy.fbclient.FirebirdException
import com.progdigy.fbclient.DataType

/**
 * Retrieves the value at the given index from the SQLDA and returns it as a [BigDecimal].
 *
 * @param index the index of the value in the SQLDA
 * @return the value at the given index as a [BigDecimal], or null if the value is null
 */
fun SQLDA.getBigDecimalOrNull(index: Int): BigDecimal? =
    if (getIsNull(index)) null else getBigDecimal(index)

/**
 * Retrieves the value of the specified column as a BigDecimal.
 *
 * @param index the index of the column
 * @return the value of the specified column as a BigDecimal
 * @throws FirebirdException if the column type is unexpected
 */
fun SQLDA.getBigDecimal(index: Int): BigDecimal =
    BigDecimal.fromBigIntegerWithScale(when (getType(index)) {
        DataType.SHORT -> getShort(index).toBigInteger()
        DataType.INT -> getInt(index).toBigInteger()
        DataType.LONG -> getLong(index).toBigInteger()
        DataType.INT128 -> getInt128(index).let {
            int128ToBigInteger(it[0], it[1])
        }
        else ->
            throw FirebirdException("Unexpected Data Type")
    }, getScale(index))

/**
 * Sets a BigDecimal value at the specified index in the SQLDA.
 *
 * @param index the index at which to set the value
 * @param value the BigDecimal value to set
 * @throws FirebirdException if the data type at the specified index is unexpected
 */
fun SQLDA.setBigDecimal(index: Int, value: BigDecimal) {
    val int = value.firebirdScale(getScale(index))
    when (getType(index)) {
        DataType.SHORT -> setShort(index, int.shortValue(true))
        DataType.INT -> setInt(index, int.intValue(true))
        DataType.LONG -> setLong(index, int.longValue(true))
        DataType.INT128 -> int.firebirdDecode { a, b ->
            setInt128(index, a, b)
        }
        else ->
            throw FirebirdException("Unexpected Data Type")
    }
}

/**
 * Calculates the scale of a BigDecimal value based on the Firebird database scale rules.
 *
 * The scale of a BigDecimal is defined as the difference between the exponent, the number of digits to the left of the decimal point,
 * and the number of digits to the right of the decimal point, also known as the significand.
 *
 * This method calculates the Firebird scale by subtracting the number of decimal digits in the significand from the exponent and adding 1.
 *
 * @return The Firebird scale of the BigDecimal value.
 */
fun BigDecimal.firebirdScale(): Long = exponent - significand.numberOfDecimalDigits() + 1

/**
 * Adjusts the scale of a BigDecimal to a specified value.
 *
 * @param value The desired scale value.
 * @return A BigInteger representing the adjusted BigDecimal.
 * @throws ArithmeticException if the BigDecimal cannot be scaled to the desired value.
 */
fun BigDecimal.firebirdScale(value: Long): BigInteger {
    var current = firebirdScale()
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

/**
 * Factory method that creates a BigDecimal instance from a BigInteger value with a specified scale.
 *
 * @param value The BigInteger value to create the BigDecimal from.
 * @param scale The scale to apply to the created BigDecimal.
 * @return The created BigDecimal instance.
 * @throws FirebirdException if the scale is less than 0.
 */
fun BigDecimal.Companion.fromBigIntegerWithScale(value: BigInteger, scale: Long): BigDecimal {
    if (scale < 0) {
        val exponent = value.numberOfDecimalDigits() - 1 + scale
        return fromBigIntegerWithExponent(value, exponent)
    } else
        throw FirebirdException("Invalid scale $scale")
}

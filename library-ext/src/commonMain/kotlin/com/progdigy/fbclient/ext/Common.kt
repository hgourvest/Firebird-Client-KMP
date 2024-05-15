package com.progdigy.fbclient.ext

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.progdigy.fbclient.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import com.progdigy.fbclient.Attachment.Transaction.*

inline fun Statement.forEach(vararg params: Any?, block: SQLDA.() -> Unit) {
    open(*params) {
        while (!eof) {
            block()
            fetch()
        }
    }
}

fun Statement.execute(vararg params: Any?) {
    setParams(*params)
    execute()
}

inline fun Statement.open(vararg params: Any?, block: Statement.RecordSet.() -> Unit) {
    setParams(*params)
    open(block)
}

fun Statement.setParams(vararg what: Any?) {
    val p = params
    for (index in what.indices)
       p.setAny(index, what[index])
}

fun SQLDA.setAny(index: Int, value: Any?){
    when (value) {
        null -> setIsNull(index)
        is Boolean -> setBoolean(index, value)
        is Byte -> setShort(index, value.toShort())
        is Short -> setShort(index, value)
        is Int -> setInt(index, value)
        is Long -> setLong(index, value)
        is String -> setString(index, value)
        is ByteArray -> setByteArray(index, value)
        is Float -> setFloat(index, value)
        is Double -> setDouble(index, value)
        is Int128 -> setInt128(index, value.a, value.b)
        is BigInteger -> setBigInteger(index, value)
        is BigDecimal -> setBigDecimal(index, value)
        is LocalDate -> setDate(index, value)
        is LocalTime -> setTime(index, value)
        is LocalDateTime -> setDateTime(index, value)
        else -> throw FirebirdException("unhandled data type ${value}")
    }
}

fun SQLDA.getAnyOrNull(index: Int): Any? = if (getIsNull(index)) null else getAny(index)

fun SQLDA.getAny(index: Int): Any {
    return when (getType(index)) {
        Type.SHORT -> if (getScale(index) > 0) getBigDecimal(index) else getShort(index)
        Type.INT -> if (getScale(index) > 0) getBigDecimal(index) else getInt(index)
        Type.LONG -> if (getScale(index) > 0) getBigDecimal(index) else getLong(index)
        Type.FLOAT -> getFloat(index)
        Type.DOUBLE -> getDouble(index)
        Type.STRING, Type.BLOB_TEXT -> getString(index)
        Type.BYTEARRAY, Type.BLOB_BINARY -> getByteArray(index)
        Type.INT128 -> if (getScale(index) > 0) getBigDecimal(index) else getBigInteger(index)
        Type.BOOLEAN -> getBoolean(index)
        Type.DATE -> getDate(index)
        Type.TIME -> getTime(index)
        Type.DATETIME -> getLocalDateTime(index)
        else -> throw FirebirdException("Unhandled data type ${getType(index)}")
    }
}
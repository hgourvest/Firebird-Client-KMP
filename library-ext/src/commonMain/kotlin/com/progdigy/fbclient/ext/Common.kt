package com.progdigy.fbclient.ext

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.progdigy.fbclient.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import com.progdigy.fbclient.Attachment.Transaction.*

/**
 * Executes the provided block of code within the record set's scope for each record in the result set.
 *
 * @param params The parameters to be used when executing or opening the statement.
 * @param block The code block to execute within the record set's scope.
 */
inline fun Statement.forEach(vararg params: Any?, block: SQLDA.() -> Unit) {
    open(*params) {
        while (!eof) {
            block()
            fetch()
        }
    }
}

/**
 * Executes the SQL statement.
 *
 * @param params the values to set as parameters in the statement
 */
fun Statement.execute(vararg params: Any?) {
    setParams(*params)
    execute()
}

/**
 * Executes the given SQL statement with the provided parameters.
 *
 * @param sql the SQL statement to be executed
 * @param params the parameters to be passed to the SQL statement
 */
fun Attachment.execute(sql: String, vararg params: Any?) {
    transaction {
        statement(sql) {
            setParams(*params)
            execute()
        }
    }
}

/**
 * Opens a statement set and executes the provided block of code within the record set's scope.
 *
 * @param params The values of parameters to set in the statement.
 * @param block The code block to execute within the record set's scope.
 */
inline fun Statement.open(vararg params: Any?, block: Statement.RecordSet.() -> Unit) {
    setParams(*params)
    open(block)
}

/**
 * Opens a database record set using the specified SQL query and parameter values.
 *
 * @param sql The SQL query to execute.
 * @param params The parameter values, if any.
 * @param block The lambda expression to be executed on the opened record set.
 */
inline fun Attachment.open(sql: String, vararg params: Any?, block: Statement.RecordSet.() -> Unit) {
    statement(sql) {
        setParams(*params)
        open(block)
    }
}

/**
 * Sets the values of parameters in the statement.
 *
 * @param what the values to set as parameters in the statement
 */
fun Statement.setParams(vararg what: Any?) {
    val p = params
    for (index in what.indices)
       p.setAny(index, what[index])
}

/**
 * Sets the value of a parameter at the given index with any supported data type.
 *
 * @param index the index of the parameter
 * @param value the value to set
 *
 * @throws FirebirdException if the data type is not supported
 */
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
        is BigInteger -> setBigInteger(index, value)
        is BigDecimal -> setBigDecimal(index, value)
        is LocalDate -> setDate(index, value)
        is LocalTime -> setTime(index, value)
        is LocalDateTime -> setDateTime(index, value)
        else -> throw FirebirdException("unhandled data type ${value}")
    }
}

/**
 * Retrieves the value at the specified index in the SQLDA object.
 *
 * @param index The index of the value to retrieve.
 * @return The value at the specified index, or null if the value is null.
 */
fun SQLDA.getAnyOrNull(index: Int): Any? = if (getIsNull(index)) null else getAny(index)

/**
 * Retrieves the value at the specified index as an instance of Any.
 *
 * @param index the index of the value to retrieve
 * @return the value at the specified index as an instance of Any
 * @throws FirebirdException if the data type at the specified index is unhandled
 */
fun SQLDA.getAny(index: Int): Any {
    return when (getType(index)) {
        DataType.SHORT -> if (getScale(index) > 0) getBigDecimal(index) else getShort(index)
        DataType.INT -> if (getScale(index) > 0) getBigDecimal(index) else getInt(index)
        DataType.LONG -> if (getScale(index) > 0) getBigDecimal(index) else getLong(index)
        DataType.FLOAT -> getFloat(index)
        DataType.DOUBLE -> getDouble(index)
        DataType.STRING, DataType.BLOB_TEXT -> getString(index)
        DataType.BYTEARRAY, DataType.BLOB_BINARY -> getByteArray(index)
        DataType.INT128 -> if (getScale(index) > 0) getBigDecimal(index) else getBigInteger(index)
        DataType.BOOLEAN -> getBoolean(index)
        DataType.DATE -> getDate(index)
        DataType.TIME -> getTime(index)
        DataType.DATETIME -> getLocalDateTime(index)
        else -> throw FirebirdException("Unhandled data type ${getType(index)}")
    }
}
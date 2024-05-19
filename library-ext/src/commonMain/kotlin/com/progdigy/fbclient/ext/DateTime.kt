package com.progdigy.fbclient.ext

import com.progdigy.fbclient.*
import kotlinx.datetime.*
import com.progdigy.fbclient.Attachment.Transaction.*

/**
 * Converts a standard `TimeZone` object to a `TimeZoneId` object.
 *
 * @return The converted `TimeZoneId` object.
 */
fun TimeZone.toFirebirdTimeZoneId(): TimeZoneId = TimeZoneId(fb_tzid_gmt.id - fb_tzids.indexOf(id))

/**
 * Converts the TimeZoneId to a TimeZone object.
 *
 * @return the TimeZone object corresponding to the TimeZoneId, or null if the id is less than or equal to 0
 */
fun TimeZoneId.toTimeZone(): TimeZone? = if (id > 0) TimeZone.of(getName()) else null

/**
 * Retrieves the LocalDateTime value stored in the specified column at the given index.
 *
 * @param index the column index
 * @return the LocalDateTime value at the specified index, or null if the column value is null
 */
fun SQLDA.getLocalDateTimeOrNull(index: Int): LocalDateTime? = if (getIsNull(index)) null else getLocalDateTime(index)

/**
 * Retrieves the value of the specified column as a LocalDate or null if the value is null.
 *
 * @param index the index of the column (1-based) to retrieve the value from
 * @return the value of the specified column as a LocalDate or null if the value is null
 */
fun SQLDA.getDateOrNull(index: Int): LocalDate? = if (getIsNull(index)) null else getDate(index)

/**
 * Retrieves the time value at the specified index from the SQLDA and returns it as a [LocalTime] object.
 * If the value at the given index is null, it returns null.
 *
 * @param index  the index of the value to retrieve from the SQLDA
 * @return the time value at the specified index, or null if the value is null
 */
fun SQLDA.getTimeOrNull(index: Int): LocalTime? = if (getIsNull(index)) null else getTime(index)

/**
 * Retrieves the value of the specified index as a [LocalDateTime] if it is not null,
 * otherwise returns null.
 *
 * @param index The index of the value to retrieve.
 * @return The [LocalDateTime] value at the specified index if it is not null, otherwise null.
 */
fun SQLDA.getDateTimeOrNull(index: Int): LocalDateTime? = if (getIsNull(index)) null else getDateTime(index)

/**
 * Retrieves the LocalDate value from the specified column index.
 *
 * @param index the index of the column from which to retrieve the date value
 * @return the LocalDate value retrieved from the specified column index
 */
fun SQLDA.getDate(index: Int): LocalDate = LocalDate.fromEpochDays(getEpochDays(index))

/**
 * Retrieves the time value from this SQLDA at the specified index.
 *
 * @param index The index of the time value to retrieve.
 * @return The time value at the specified index.
 */
fun SQLDA.getTime(index: Int): LocalTime = LocalTime.fromMillisecondOfDay(getMillisecondOfDay(index))

/**
 * Retrieves a [LocalDateTime] value from the SQLDA at the specified index.
 *
 * @param index the index of the value within the SQLDA.
 * @return the retrieved [LocalDateTime] object.
 */
fun SQLDA.getDateTime(index: Int): LocalDateTime = LocalDateTime(getDate(index), getTime(index))

/**
 * Retrieves the TimeZone value from the specified index.
 *
 * @param index the index of the TimeZone value to retrieve
 * @return the TimeZone value at the specified index, or null if the index is invalid or the TimeZone value is not found
 */
fun SQLDA.getTimeZone(index: Int): TimeZone? {
    val tz = getTimeZoneId(index)
    return if (tz.id > 0L) tz.toTimeZone()
    else null
}

/**
 * Retrieves the LocalDateTime value at the specified index from the SQLDA.
 *
 * @param index the index of the LocalDateTime value to retrieve
 * @return the LocalDateTime value at the specified index
 */
fun SQLDA.getLocalDateTime(index: Int): LocalDateTime {
    val dt = getDateTime(index)
    val tz = getTimeZone(index)
    return if (tz != null) dt.toInstant(TimeZone.UTC).toLocalDateTime(tz)
    else dt
}

/**
 * Returns the system date and time at the specified index.
 * @param index the index of the system date and time value in the SQLDA.
 * @return the system date and time as a LocalDateTime object.
 */
fun SQLDA.getSystemDateTime(index: Int): LocalDateTime {
    val dt = getDateTime(index)
    val tz = getTimeZoneId(index)
    return if (tz.id > 0) {
        dt.toInstant(TimeZone.UTC).toLocalDateTime(TimeZone.currentSystemDefault())
    } else dt
}

/**
 * Set the date value at the specified index in this SQLDA object.
 *
 * @param index The index of the parameter.
 * @param value The LocalDate value to be set.
 */
fun SQLDA.setDate(index: Int, value: LocalDate) = setEpochDays(index, value.toEpochDays())

/**
 * Sets the value of a parameter at the specified index to the given LocalTime value.
 *
 * @param index The index of the parameter to set. The first parameter has an index of 1.
 * @param value The LocalTime value to set.
 */
fun SQLDA.setTime(index: Int, value: LocalTime) = setMillisecondOfDay(index, value.toMillisecondOfDay())

/**
 * Sets the date and time value at the given index in the SQLDA object.
 *
 * @param index the zero-based index of the value to be set.
 * @param value the LocalDateTime value to be set.
 */
fun SQLDA.setDateTime(index: Int, value: LocalDateTime) {
    setDate(index, value.date)
    setTime(index, value.time)
}

/**
 * Sets the time zone for a specific index in this SQLDA object.
 *
 * @param index The index of the SQLDA object to set the time zone for.
 * @param value The time zone to set for the specified index.
 */
fun SQLDA.setTimeZone(index: Int, value: TimeZone) = setTimeZoneId(index, value.toFirebirdTimeZoneId())

/**
 * Sets the value of a parameter at the specified index to the provided Instant value.
 *
 * @param index The index of the parameter.
 * @param value The Instant value to set.
 * @param tz The TimeZone to use for conversion. Defaults to TimeZone.currentSystemDefault().
 *
 * @throws FirebirdException if the data type of the parameter at the given index is incompatible.
 */
fun SQLDA.setInstant(index: Int, value: Instant, tz: TimeZone = TimeZone.currentSystemDefault()) {
    when (getType(index)) {
        Type.DATE -> setDate(index, value.toLocalDateTime(tz).date)
        Type.TIME -> setTime(index, value.toLocalDateTime(tz).time)
        Type.DATETIME -> setDateTime(index, value.toLocalDateTime(tz))
        Type.TIME_TZ -> {
            setTime(index, value.toLocalDateTime(TimeZone.UTC).time)
            setTimeZone(index, tz)
        }
        Type.DATETIME_TZ -> {
            setDateTime(index, value.toLocalDateTime(TimeZone.UTC))
            setTimeZone(index, tz)
        }
        else -> throw FirebirdException("Incompatible Data Type")
    }
}
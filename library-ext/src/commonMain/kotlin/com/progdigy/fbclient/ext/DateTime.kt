package com.progdigy.fbclient.ext

import com.progdigy.fbclient.*
import kotlinx.datetime.*
import com.progdigy.fbclient.Attachment.Transaction.*

fun TimeZone.toFbTimeZoneId(): TimeZoneId = TimeZoneId(fb_tzid_gmt.id - fb_tzids.indexOf(id))

fun TimeZoneId.toTimeZone(): TimeZone? = if (id > 0) TimeZone.of(getName()) else null

fun SQLDA.getLocalDateTimeOrNull(index: Int): LocalDateTime? = if (getIsNull(index)) null else getLocalDateTime(index)
fun SQLDA.getDateOrNull(index: Int): LocalDate? = if (getIsNull(index)) null else getDate(index)
fun SQLDA.getTimeOrNull(index: Int): LocalTime? = if (getIsNull(index)) null else getTime(index)
fun SQLDA.getDateTimeOrNull(index: Int): LocalDateTime? = if (getIsNull(index)) null else getDateTime(index)

fun SQLDA.getDate(index: Int): LocalDate = LocalDate.fromEpochDays(getEpochDays(index))
fun SQLDA.getTime(index: Int): LocalTime = LocalTime.fromMillisecondOfDay(getMillisecondOfDay(index))
fun SQLDA.getDateTime(index: Int): LocalDateTime = LocalDateTime(getDate(index), getTime(index))

fun SQLDA.getTimeZone(index: Int): TimeZone? {
    val tz = getTimeZoneId(index)
    return if (tz.id > 0L) tz.toTimeZone()
    else null
}

fun SQLDA.getLocalDateTime(index: Int): LocalDateTime {
    val dt = getDateTime(index)
    val tz = getTimeZone(index)
    return if (tz != null) dt.toInstant(TimeZone.UTC).toLocalDateTime(tz)
    else dt
}

fun SQLDA.getSystemDateTime(index: Int): LocalDateTime {
    val dt = getDateTime(index)
    val tz = getTimeZoneId(index)
    return if (tz.id > 0) {
        dt.toInstant(TimeZone.UTC).toLocalDateTime(TimeZone.currentSystemDefault())
    } else dt
}

fun SQLDA.setDate(index: Int, value: LocalDate) = setEpochDays(index, value.toEpochDays())

fun SQLDA.setTime(index: Int, value: LocalTime) = setMillisecondOfDay(index, value.toMillisecondOfDay())

fun SQLDA.setDateTime(index: Int, value: LocalDateTime) {
    setDate(index, value.date)
    setTime(index, value.time)
}

fun SQLDA.setTimeZone(index: Int, value: TimeZone) = setTimeZoneId(index, value.toFbTimeZoneId())

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
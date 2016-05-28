package tools

import org.joda.time.LocalDateTime

import org.joda.time.{DateTimeZone, DateTime}
import org.joda.time.format.DateTimeFormat

object Date {
	def isExpired(date:String):Boolean = {
		val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
		val now = new DateTime(DateTimeZone.forOffsetHours(3)).toLocalDateTime
		val eta = formatter.parseLocalDateTime(date)
		return if(now.isBefore(eta.minusMinutes(10))){false} else {true}
	}

	def isValid(date:String):Boolean = {
		val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
		return try{
			val eta = formatter.parseLocalDateTime(date)
			true
		}
		catch{
			case e:IllegalArgumentException => {
				false
			}
		}
	}

	def format(input:String):LocalDateTime = {
		val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		return formatter.parseLocalDateTime(input)
	}
}

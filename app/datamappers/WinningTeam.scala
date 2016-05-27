package datamappers

import java.time.LocalTime

import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import org.joda.time.{DateTimeZone, DateTime}
import play.api.Play

object WinningTeam {
	def getWinningTeamOfUser(user:String):String = {
		val result = Db.session.execute(
			"SELECT winning_team FROM users WHERE user_name = ?",
			user
		)
		import scala.collection.JavaConversions._

		val x = result.one()
		return x.getString("winning_team")
	}

	def getWinningTeamOfAllUsers():List[(String,String)] = {
		val result = Db.session.execute(
			"SELECT user_name, winning_team FROM users"
		)
		import scala.collection.JavaConversions._
		return result.all().toList
				.map(x =>
					(
						x.getString("user_name"),
						x.getString("winning_team")
					)
				).filter(_._1!="admin").sortBy(_._1)
	}


	def changeWinningTeam(user:String, team:String):Boolean = {

		val etaString = Play.current.configuration.getString("euro2016.winningTeamSetEta").getOrElse("2016-06-10 20:00:00")
		val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
		val eta = formatter.parseLocalDateTime(etaString)
		val now = new DateTime(DateTimeZone.forOffsetHours(3)).toLocalDateTime
		return if(now.isBefore(eta)) {
			Db.session.execute(
				"UPDATE users SET winning_team = ? WHERE user_name = ?",
				team, user
			)
			true
		}
		else {
			false
		}
	}



}

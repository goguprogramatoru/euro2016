package models

import java.time.LocalDateTime

import org.joda.time.{DateTimeZone, DateTime}

object Stats {

	var lastRefresh = new DateTime(DateTimeZone.forOffsetHours(3)).toLocalDateTime

	var allUsers = datamappers.Users.getUserList().filter(_ != "admin")
	var allGames = datamappers.Games.getGames()
	var allScores = datamappers.Games.getAllScores()

	def refresh() = {
		val now = new DateTime(DateTimeZone.forOffsetHours(3)).toLocalDateTime
		if(now.minusMinutes(1).isAfter(lastRefresh)) {	//spammers protect
			allUsers = datamappers.Users.getUserList().filter(_ != "admin")
			allGames = datamappers.Games.getGames()
			allScores = datamappers.Games.getAllScores()
		}
	}

	def getNbWinners(game:models.Game):Int = {
		return allUsers.map(usr =>
			{
				val concat = usr + " | "+ game.key
				this.allScores.getOrElse(concat,"-")
			}
		).filter(_ == (game.team1Score + " - "+game.team2Score)).size
	}

	def getUserGamePivot():Map[String,Map[String,Double]] = {

		val nbParticipants = allUsers.size
		val allWinLoose:Map[String,Map[String,Double]] = allGames.map(game =>
		{
			val nbWinners = this.getNbWinners(game)
			val tmp:Map[String,Double] = allUsers.map(usr =>
			{
				val betScore = allScores.getOrElse(usr +" | "+game.key,"-")
				val winScore = game.team1Score + " - " + game.team2Score

				val winLoose = if(betScore == winScore){nbParticipants/nbWinners - 1.0} else {-1.0}
				usr -> winLoose
			}).toMap
			game.key -> tmp
		}).toMap

		return allWinLoose
	}
}

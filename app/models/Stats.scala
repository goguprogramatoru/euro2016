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

				val winLoose = if(betScore == winScore){nbParticipants*1.0/nbWinners} else {0.0}
				usr -> winLoose
			}).toMap
			game.key -> tmp
		}).toMap

		return allWinLoose
	}

	def getUserStats(pivotStats:Map[String,Map[String,Double]]):Map[String,Double] = {
		return pivotStats
			.flatMap(_._2
				.map(tpl => (tpl._1,tpl._2,"whatever"))) //i added whatever to avoid grouping by _._1 . specific to tpl2
					.map(tpl => (tpl._1, tpl._2)
			)
				.groupBy(_._1).mapValues(_.map(_._2).sum)
					.toMap[String,Double]

	}

	def getWinOrReported(pivotStats:Map[String,Map[String,Double]]):(Double,Double) = {
		val total = allUsers.size * allGames.size * 1.0
		val totalWinnings = pivotStats.flatMap(_._2.map(_._2)).foldLeft(0.0)(_+_)
		val totalRemaining = total - totalWinnings
		return (totalWinnings, totalRemaining)
	}

}

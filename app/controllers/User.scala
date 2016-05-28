package controllers

import com.datastax.driver.core.{Row, Cluster}
import controllers.actionbuilders.controllers.ActionTypes.UserAction
import controllers.actionbuilders.controllers.actionbuilders.controllers.ActionTypes.AdminAction
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.DateTimeFormat
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.twirl.api.Html

object User extends Controller {

	case class WinningTeamData(team:String)
	case class GameScoreData(team1Score:Int, team2Score:Int)

	val winningTeamInputForm = Form(
		mapping(
			"in_team" -> nonEmptyText
		)(WinningTeamData.apply)(WinningTeamData.unapply)
	)

	val gameScoreInputForm = Form(
		mapping(
			"in_team1_score" -> number(0,100),
			"in_team2_score" -> number(0,100)
		)(GameScoreData.apply)(GameScoreData.unapply)
	)

	def displayPage(request:Request[AnyContent], pageId:Int, page:Html):Result = {
		val userName = request.session.get("connectedUser").getOrElse("nope")
		val userMenu = views.html.user.userMenu.render(userName,pageId)
		return Ok(views.html.common.main(userMenu,page))
	}

	def index = UserAction { request =>
		val indexPage = views.html.user.index.render()
		displayPage(request,0,indexPage)
	}

	def winningTeamInterface = UserAction { request =>
		val userName = request.session.get("connectedUser").getOrElse("nope")
		val winningTeam = datamappers.WinningTeam.getWinningTeamOfUser(userName)

		val etaString = Play.current.configuration.getString("euro2016.winningTeamSetEta").getOrElse("2016-06-10 20:00:00")
		val expired = tools.Date.isExpired(etaString)

		val winningTeams = datamappers.WinningTeam.getWinningTeamOfAllUsers()

		val page = views.html.user.winningteam.view.render(winningTeam,etaString, expired, winningTeams)
		displayPage(request,1,page)
	}

	def setWinningTeamInterface = UserAction{request =>
		val page = views.html.user.winningteam.set.render(models.Countries.getList())
		displayPage(request,1,page)
	}

	def setWinningTeam = UserAction{
		implicit request =>
			winningTeamInputForm.bindFromRequest().fold(
				formWithErrors => {
					println(request.body.asFormUrlEncoded.get("in_team").toString())
					Common.error("Wrong input",routes.User.winningTeamInterface().absoluteURL())
				},
				winningTeamData => {
					val userName = request.session.get("connectedUser").getOrElse("nope")
					val success = datamappers.WinningTeam.changeWinningTeam(userName,winningTeamData.team)
					if(success == true) {
						Redirect(routes.User.winningTeamInterface())
					}
					else {
						Common.error("Too late!",routes.User.winningTeamInterface().absoluteURL())
					}
				}
			)
	}

	def myScores = UserAction{request =>
		val userName = request.session.get("connectedUser").getOrElse("nope")
		val allGames = datamappers.Games.getGames()
		val myScores = datamappers.Games.getAllScoresOfUser(userName)
		val page = views.html.user.myscores.view(allGames, myScores)
		displayPage(request,2,page)
	}


	def setGameScoreInputPage(gameKey:String) = UserAction {request =>
		val game = datamappers.Games.getGame(gameKey)
		val page = views.html.user.myscores.set(game.team1, game.team2)
		val expired = tools.Date.isExpired(game.date)
		if(expired == true){
			Common.error("Ultima data de setare a scorului = data meciului - 10 minute",routes.User.myScores().url)
		}
		else {
			displayPage(request,2,page)
		}
	}

	def setGameScore(gameKey:String) = UserAction{
		implicit request =>
			gameScoreInputForm.bindFromRequest().fold(
				formWithErrors => {
					Common.error("Wrong input!",routes.User.myScores().absoluteURL())
				},
				gameScoreData => {
					val userName = request.session.get("connectedUser").getOrElse("nope")

					val game = datamappers.Games.getGame(gameKey)
					val expired = tools.Date.isExpired(game.date)
					if(expired == true){
						Common.error("Ultima data de setare a scorului = data meciului - 10 minute",routes.User.myScores().absoluteURL())
					}
					else {
						datamappers.Games.setUserScore(userName, gameKey, gameScoreData.team1Score, gameScoreData.team2Score)
						Redirect(routes.User.myScores())
					}
				}
			)
	}

	def otherPeopleScores(gameKey:String) = UserAction{request =>
		val game = datamappers.Games.getGame(gameKey)
		if(tools.Date.isExpired(game.date))
		{
			val otherPeopleScores = datamappers.Games.getAllScoresOfGame(game.key)
			val winnerScore = game.team1Score + " - "+game.team2Score

			val nbWinners = otherPeopleScores.filter(_._2 == winnerScore).size

			val allUsers = datamappers.Users.getUserList()

			val nbUsers = allUsers.size - 2 //without himself and without admin
			val participants = otherPeopleScores.map(x => x._1).toSet
			val missingUsers = allUsers.filter(usr => !participants.contains(usr)).filter(_!="admin")

			val winSize = nbUsers * 1.0 / nbWinners
			val reportSize = if(nbWinners > 0){0} else {nbUsers+1}
			val page = views.html.user.myscores.other.render(otherPeopleScores, missingUsers, winnerScore, game.team1, game.team2, winSize, reportSize)
			displayPage(request,2,page)

		}
		else {
			NotFound("Nice try")
		}
	}

	def stats() = UserAction{request =>
		models.Stats.refresh()
		val allUsers = datamappers.Users.getUserList().sorted.filter(_!="admin")
		val allGames = datamappers.Games.getGames()
				.filterNot(game => game.team1Score == -1 || game.team2Score == -1)
					.sortBy(_.date).reverse
		val pivot = models.Stats.getUserGamePivot()
		val winningsPerUser = models.Stats.getUserStats(pivot)
		val (totalWin,totalRemaining) = models.Stats.getWinOrReported(pivot)
		val page = views.html.user.stats.pivot(allUsers,allGames,pivot,winningsPerUser,totalWin,totalRemaining)
		displayPage(request,3,page)
	}

}
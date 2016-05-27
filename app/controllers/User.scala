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


	def index = UserAction { request =>
		val userName = request.session.get("connectedUser").getOrElse("nope")
		val userMenu = views.html.user.userMenu.render(userName,0)
		val indexPage = views.html.user.index.render()
		Ok(views.html.common.main(userMenu,indexPage))
	}

	def winningTeamInterface = UserAction { request =>
		val userName = request.session.get("connectedUser").getOrElse("nope")
		val userMenu = views.html.user.userMenu.render(userName,1)
		val winningTeam = datamappers.WinningTeam.getWinningTeamOfUser(userName)
		val etaString = datamappers.Settings.getSetting("winning_team_set_eta")
		val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		val eta = formatter.parseLocalDateTime(etaString)
		val now = new DateTime(DateTimeZone.forOffsetHours(3)).toLocalDateTime
		val expired = if(now.isBefore(eta)){false} else {true}

		val winningTeams = datamappers.WinningTeam.getWinningTeamOfAllUsers()

		val page = views.html.user.winningteam.view.render(winningTeam,etaString, expired, winningTeams)
		Ok(views.html.common.main(userMenu,page))
	}

	def setWinningTeamInterface = UserAction{request =>
		val userName = request.session.get("connectedUser").getOrElse("nope")
		val userMenu = views.html.user.userMenu.render(userName,1)
		val page = views.html.user.winningteam.set.render(models.Countries.getList())
		Ok(views.html.common.main(userMenu,page))
	}

	def setWinningTeam = UserAction{
		implicit request =>
			winningTeamInputForm.bindFromRequest().fold(
				formWithErrors => {
					println(request.body.asFormUrlEncoded.get("in_team").toString())
					Ok("Wrong input!")
				},
				winningTeamData => {
					val userName = request.session.get("connectedUser").getOrElse("nope")
					val success = datamappers.WinningTeam.changeWinningTeam(userName,winningTeamData.team)
					if(success == true) {
						Redirect(routes.User.winningTeamInterface())
					}
					else {
						Ok("Too late!")
					}
				}
			)
	}

	def myScores = UserAction{request =>
		val userName = request.session.get("connectedUser").getOrElse("nope")
		val userMenu = views.html.user.userMenu.render(userName,2)
		val allGames = datamappers.Games.getGames()
		val myScores = datamappers.Games.getAllScoresOfUser(userName)
		val page = views.html.user.myscores.view(allGames, myScores)
		Ok(views.html.common.main(userMenu,page))
	}


	def setGameScoreInputPage(gameKey:String) = UserAction {request =>
		val game = datamappers.Games.getGame(gameKey)
		val userName = request.session.get("connectedUser").getOrElse("nope")
		val menu = views.html.user.userMenu.render(userName,2)
		val page = views.html.user.myscores.set(game.team1, game.team2)


		val expired = tools.Date.isExpired(game.date)
		if(expired == true){
			Ok("Mars!")
		}
		else {
			Ok(views.html.common.main(menu, page))
		}
	}

	def setGameScore(gameKey:String) = UserAction{
		implicit request =>
			gameScoreInputForm.bindFromRequest().fold(
				formWithErrors => {
					Ok("Wrong input!")
				},
				gameScoreData => {
					val userName = request.session.get("connectedUser").getOrElse("nope")

					val game = datamappers.Games.getGame(gameKey)
					val expired = tools.Date.isExpired(game.date)
					if(expired == true){
						Ok("Mars!")
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
			val userName = request.session.get("connectedUser").getOrElse("nope")
			val menu = views.html.user.userMenu.render(userName,2)
			Ok(views.html.common.main(menu,page))

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
		val page = views.html.user.stats.pivot(allUsers,allGames,pivot)
		val userName = request.session.get("connectedUser").getOrElse("nope")
		val menu = views.html.user.userMenu.render(userName,3)
		Ok(views.html.common.main(menu,page))
	}

}
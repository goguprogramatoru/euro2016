//todos
//check input format for date when adding a new game
//user, pass for the db connection

package controllers

import com.datastax.driver.core.{Row, Cluster}
import controllers.User._
import controllers.actionbuilders.controllers.ActionTypes.UserAction
import controllers.actionbuilders.controllers.actionbuilders.controllers.ActionTypes.AdminAction
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.twirl.api.Html

object Admin extends Controller{

	case class UserTokenInputData(userName:String)
	case class GameAddData(key:String, date:String, step:String, team1:String, team2:String)
	case class GameScoreData(team1Score:Int, team2Score:Int)
	case class SettingsData(value:String)

	val userTokenInputForm = Form(
		mapping(
			"in_user" -> nonEmptyText
		)(UserTokenInputData.apply)(UserTokenInputData.unapply)
	)

	val gameAddInputForm = Form(
		mapping(
			"in_key" -> nonEmptyText,
			"in_date" -> nonEmptyText,
			"in_step" -> nonEmptyText,
			"in_team1" -> nonEmptyText,
			"in_team2" -> nonEmptyText
		)(GameAddData.apply)(GameAddData.unapply)
	)

	val gameScoreInputForm = Form(
		mapping(
			"in_team1_score" -> number(-1,100),
			"in_team2_score" -> number(-1,100)
		)(GameScoreData.apply)(GameScoreData.unapply)
	)

	val settingsInputForm = Form(
		mapping(
			"in_val" -> nonEmptyText
		)(SettingsData.apply)(SettingsData.unapply)
	)

	def index = AdminAction {request =>
		val menu = views.html.admin.adminMenu.render("admin",-1)
		val index = views.html.admin.adminIndex.render()
		Ok(views.html.common.main(menu,index))
	}

	def createTokenInputPage = AdminAction{
		val menu = views.html.admin.adminMenu.render("admin",0)
		val page = views.html.admin.usermanagement.createUserTokenInput.render()
		Ok(views.html.common.main(menu,page))
	}

	def createTokenResultPage = AdminAction{
		implicit request =>

			userTokenInputForm.bindFromRequest().fold(
				formWithErrors => {
					Ok("Wrong input!")
				},
				userTokenInputData => {
					val token = scala.util.Random.alphanumeric.take(10).mkString
					datamappers.Users.createToken(userTokenInputData.userName,token)
					val page = views.html.admin.usermanagement.createUserTokenOutput(request.host,userTokenInputData.userName, token)
					val menu = views.html.admin.adminMenu.render("admin",0)
					Ok(views.html.common.main(menu,page))
				}
			)
	}

	def viewUsers = AdminAction{
		val userList = datamappers.Users.getUserList()

		val menu = views.html.admin.adminMenu.render("admin",1)
		val page = views.html.admin.usermanagement.usersView(userList)
		Ok(views.html.common.main(menu,page))
	}

	def deleteUser(userName:String) = AdminAction{
		datamappers.Users.deleteUser(userName)
		Redirect(routes.Admin.viewUsers())
	}

	def viewTokens = AdminAction{
		val userList = datamappers.Users.getUserOfTokens()
		val menu = views.html.admin.adminMenu.render("admin",0)
		val page = views.html.admin.usermanagement.tokenView(userList)
		Ok(views.html.common.main(menu,page))
	}

	def deleteToken(userName:String) = AdminAction{
		datamappers.Users.deleteToken(userName)
		Redirect(routes.Admin.viewTokens())
	}

	def viewGames = AdminAction{
		val menu = views.html.admin.adminMenu.render("admin",2)
		val gamesList = datamappers.Games.getGames()
		val page = views.html.admin.games.gamesView(gamesList)
		Ok(views.html.common.main(menu,page))
	}

	def addGameInputPage = AdminAction{
		val menu = views.html.admin.adminMenu.render("admin",2)
		val page = views.html.admin.games.addGame(models.Countries.getList())
		Ok(views.html.common.main(menu,page))
	}

	def addGame = AdminAction{
		implicit request =>
			gameAddInputForm.bindFromRequest().fold(
				formWithErrors => {
					Ok("Wrong input!")
				},
				gameAddData => {
					val token = scala.util.Random.alphanumeric.take(10).mkString
					val success = datamappers.Games.insertGame(
						gameAddData.key,
						gameAddData.step,
						gameAddData.date,
						gameAddData.team1,
						gameAddData.team2
					)
					if(success == false){
						Ok("Failed. Game with the same key already exists")
					}
					else {
						Redirect(routes.Admin.viewGames())
					}
				}
			)
	}

	def deleteGame(gameKey:String) = AdminAction {
		datamappers.Games.deleteGame(gameKey)
		Redirect(routes.Admin.viewGames())
	}

	def setGameScoreInputPage(gameKey:String) = AdminAction {
		val game = datamappers.Games.getGame(gameKey)

		val menu = views.html.admin.adminMenu.render("admin",2)
		val page = views.html.admin.games.setGameScore(game.team1, game.team2)
		Ok(views.html.common.main(menu,page))
	}

	def setGameScore(gameKey:String) = AdminAction{
		implicit request =>
			gameScoreInputForm.bindFromRequest().fold(
				formWithErrors => {
					Ok("Wrong input!")
				},
				gameScoreData => {
					datamappers.Games.setGameScore(gameKey, gameScoreData.team1Score, gameScoreData.team2Score)
					Redirect(routes.Admin.viewGames())
				}
			)
	}

	def viewSettings() = AdminAction{
		val settings = datamappers.Settings.getSettings()

		val menu = views.html.admin.adminMenu.render("admin",3)
		val page = views.html.admin.settings.viewSettings(settings)
		Ok(views.html.common.main(menu,page))
	}

	def changeSettingInputPage(key:String) = AdminAction{
		val menu = views.html.admin.adminMenu.render("admin",3)
		val page = views.html.admin.settings.setValue(key)
		Ok(views.html.common.main(menu,page))
	}

	def changeSetting(key:String) = AdminAction{
		implicit request =>
			settingsInputForm.bindFromRequest().fold(
				formWithErrors => {
					Ok("Wrong input!")
				},
				settingsData => {
					datamappers.Settings.update(key, settingsData.value)
					Redirect(routes.Admin.viewSettings())
				}
			)
	}
}

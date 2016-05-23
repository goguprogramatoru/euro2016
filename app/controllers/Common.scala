package controllers

import play.api.mvc.Controller
import com.datastax.driver.core.{Row, Cluster}
import controllers.User._
import controllers.actionbuilders.controllers.ActionTypes.UserAction
import controllers.actionbuilders.controllers.actionbuilders.controllers.ActionTypes.AdminAction
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.twirl.api.Html


object Common extends Controller{

	case class LoginData(user:String,var pass:String)
	case class CreateUserData(var pass:String)

	val loginForm = Form(
		mapping(
			"in_user" -> nonEmptyText(),
			"in_pass" -> nonEmptyText()
		)(LoginData.apply)(LoginData.unapply)
	)

	val createUserForm = Form(
		mapping(
			"in_pass" -> nonEmptyText()
		)(CreateUserData.apply)(CreateUserData.unapply)
	)

	def loginPage() = Action{request =>

		var attempts = request.session.get("failedAttempts").getOrElse("0").toInt
		Ok(views.html.common.login(attempts))
	}

	def logout() = Action{
		Redirect(routes.Common.loginPage()).withNewSession
	}

	def loginHandler() = Action{
		implicit request =>

			loginForm.bindFromRequest().fold(
				formWithErrors => {
					Redirect(routes.Common.loginPage())
				},
				loginData => {
					loginData.pass = tools.Security.md5(loginData.pass)

					val nb = datamappers.Users.countUsersWith(loginData.user,loginData.pass)

					if(nb==1){
						if(loginData.user=="admin"){
							Redirect("/admin").withSession("connectedUser" -> loginData.user, "failedAttempts" -> "0")
						}
						else {
							Redirect("/").withSession("connectedUser" -> loginData.user, "failedAttempts" -> "0")
						}
					}
					else {
						var attempts = request.session.get("failedAttempts").getOrElse("0").toInt
						if(attempts>3){
							Thread.sleep(5000)
						}
						attempts += 1
						Redirect(routes.Common.loginPage()).withSession("failedAttempts" -> attempts.toString)
					}
				}
			)
	}

	def createUserInput(userName:String, token:String) = Action {
		if(datamappers.Users.tokenExists(userName, token) == false){
			NotFound("No valid token, ask the admin for a new one")
		}
		else {
			Ok(views.html.common.createUserInput(userName))
		}
	}

	def createUserOutput(userName:String, token:String) = Action{
		implicit request =>
			createUserForm.bindFromRequest().fold(
				formWithErrors => {
					Redirect(routes.Common.createUserInput(userName,token))
				},
				createUserData => {
					createUserData.pass = tools.Security.md5(createUserData.pass)
					val result = datamappers.Users.consumeToken(userName,token,createUserData.pass)
					if(result == false){
						NotFound("Something wrong happened. Talk with the admin")
					}
					else {
						Ok(views.html.common.createUserOutput())
					}
				}
			)


	}
}

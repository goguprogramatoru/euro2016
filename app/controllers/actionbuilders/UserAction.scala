package controllers.actionbuilders

package controllers.ActionTypes

import play.api.mvc.{Result, Request, ActionBuilder}
import play.api.mvc._

import scala.concurrent.Future

object UserAction extends ActionBuilder[Request] {

	override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {

		val user = request.session.get("connectedUser").getOrElse("nope")

		if(user!="nope" && user!="admin"){
			val res =  block(request) //moving on
			return res
		}
		else {
			Future.successful(Results.Redirect("/login"))
		}

	}
}

package datamappers

object Users{

	def countUsersWith(user:String, password:String):Long = {
		val results = Db.session.execute(
			"SELECT COUNT(*) AS nb FROM users WHERE user_name= ? AND password = ? ALLOW FILTERING",
			user,password
		)
		return results.one().getLong("nb")
	}

	def createToken(userName:String, token:String) = {
		Db.session.execute(
			"INSERT INTO user_tokens (user_name, user_token) VALUES (?,?)",
			userName, token
		)
	}

	def tokenExists(userName:String,token:String):Boolean = {
		val results = Db.session.execute(
			"SELECT COUNT(*) AS nb FROM user_tokens WHERE user_name= ? AND user_token = ? ALLOW FILTERING",
			userName,token
		)
		val nb = results.one().getLong("nb")
		return if(nb>0l) {true} else {false}
	}

	def consumeToken(userName:String, token:String, password:String):Boolean = {
		val results = Db.session.execute(
			"SELECT COUNT(*) AS nb FROM user_tokens WHERE user_name= ? AND user_token = ? ALLOW FILTERING",
			userName,token
		)
		val nb = results.one().getLong("nb")
		return if(nb == 0L){
			false
		}
		else {
			Db.session.execute(
				"DELETE FROM user_tokens WHERE user_name = ?",
				userName
			)

			Db.session.execute(
				"INSERT INTO users (user_name, password) VALUES (?,?)",
				userName,password
			)

			true
		}
	}

	def getUserList():List[String] = {
		val res = Db.session.execute(
			"SELECT user_name FROM users"
		)
		import scala.collection.JavaConversions._
		return res.all().toList.map(_.getString("user_name")).toList
	}

	def deleteUser(user:String) = {
		Db.session.execute(
			"DELETE FROM users WHERE user_name = ?",
			user
		)
	}

	def getUserOfTokens():List[(String,String)] = {
		val res = Db.session.execute(
			"SELECT user_name, user_token FROM user_tokens"
		)
		import scala.collection.JavaConversions._
		return res.all().toList.map(x => (x.getString("user_name"), x.getString("user_token"))).toList
	}

	def deleteToken(user:String) = {
		Db.session.execute(
			"DELETE FROM user_tokens WHERE user_name = ?",
			user
		)
	}
}

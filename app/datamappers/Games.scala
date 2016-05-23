package datamappers

object Games {
	def insertGame(key:String, step:String, date:String, team1:String, team2:String):Boolean = {

		val results = Db.session.execute(
			"SELECT COUNT(*) AS nb FROM games WHERE game_key = ? ALLOW FILTERING",
			key
		)
		val nb = results.one().getLong("nb")

		return if(nb > 0){
			false
		}
		else {
			Db.session.execute(
				"INSERT INTO games (game_key, game_step, game_date, team1, team2, team1_score, team2_score) VALUES (?,?,?,?,?,-1,-1);",
				key, step, date, team1, team2
			)
			true
		}
	}

	def getGames():List[models.Game] = {
		val result = Db.session.execute(
			"SELECT game_key, game_date, game_step, team1, team2, team1_score, team2_score FROM games"
		)
		import scala.collection.JavaConversions._
		return result.all().toList
				.map(x =>
					models.Game(
						x.getString("game_key"),
						x.getString("game_date"),
						x.getString("game_step"),
						x.getString("team1"),
						x.getString("team2"),
						x.getInt("team1_score"),
						x.getInt("team2_score")
					)
				).sortBy(_.date)
	}

	def getGame(gameKey:String):models.Game = {
		val result = Db.session.execute(
			"SELECT game_key, game_date, game_step, team1, team2, team1_score, team2_score FROM games WHERE game_key = ?",
			gameKey
		)
		import scala.collection.JavaConversions._

		val x = result.one()

		models.Game(
			x.getString("game_key"),
			x.getString("game_date"),
			x.getString("game_step"),
			x.getString("team1"),
			x.getString("team2"),
			x.getInt("team1_score"),
			x.getInt("team2_score")
		)

	}


	def deleteGame(gameKey:String) = {
		Db.session.execute(
			"DELETE FROM games WHERE game_key = ?",
			gameKey
		)
	}

	def setGameScore(gameKey:String, team1Score:Int, team2Score:Int) = {
		Db.session.execute(
			"UPDATE games SET team1_score = "+team1Score+" , team2_score = "+team2Score+" WHERE game_key = ?",
			gameKey
		)
	}

	def setUserScore(user:String, gameKey:String, team1Score:Int, team2Score:Int) = {
		Db.session.execute(
			"INSERT INTO user_scores (user_name, game_key, team1_score, team2_score) VALUES (?,?,"+team1Score+","+team2Score+")",
			user,gameKey
		)
	}

	def getAllScoresOfUser(user:String):Map[String,String] = {
		val result = Db.session.execute(
			"SELECT game_key, team1_score, team2_score FROM user_scores WHERE user_name = ?",
			user
		)
		import scala.collection.JavaConversions._

		val r:Map[String,String] = result.all().map(x=>
				{
					val scoreConcat:String = x.getInt("team1_score").toString + " - " + x.getInt("team2_score").toString
					x.getString("game_key") -> scoreConcat
				}

		).toMap

		return r
	}

	def getAllScoresOfGame(gameKey:String):List[(String,String)] = {
		val result = Db.session.execute(
			"SELECT user_name, team1_score, team2_score FROM user_scores WHERE game_key = ? ALLOW FILTERING",
			gameKey
		)

		import scala.collection.JavaConversions._
		return result.all().toList
				.map(x =>
					{
						val scoreConcat = x.getInt("team1_score").toString +" - "+x.getInt("team2_score").toString
						(
							x.getString("user_name"),
							scoreConcat
						)
					}
				).sortBy(_._1)
	}

	def getAllScores():Map[String,String] = {
		val result = Db.session.execute(
			"SELECT user_name, game_key, team1_score, team2_score FROM user_scores ALLOW FILTERING"
		)
		import scala.collection.JavaConversions._

		val r:Map[String,String] = result.all().map(x=>
		{
			val userGameConcat:String = x.getString("user_name") + " | " +x.getString("game_key")
			val scoreConcat:String = x.getInt("team1_score").toString + " - " + x.getInt("team2_score").toString
			userGameConcat -> scoreConcat
		}

		).toMap

		return r
	}
}

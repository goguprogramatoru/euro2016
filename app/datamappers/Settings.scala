package datamappers

object Settings {
	def getSettings():List[(String,String)] = {
		val result = Db.session.execute(
			"SELECT key, val FROM settings"
		)
		import scala.collection.JavaConversions._
		return result.all().toList
				.map(x =>
					(
						x.getString("key"),
						x.getString("val")
					)
				).sortBy(_._1)
	}

	def update(key:String, value:String) = {
		Db.session.execute(
			"UPDATE settings SET val = ? WHERE key = ?",
			value, key
		)
	}

	def getSetting(key:String):String = {
		val results = Db.session.execute(
			"SELECT val FROM settings WHERE key = ?",
			key
		)
		return results.one().getString("val")
	}
}

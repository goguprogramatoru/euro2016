package datamappers

/**
  * Created by podgornai on 09.07.2016.
  */
object Champion {
	def getChampion():String = {
		val results = Db.session.execute(
			"SELECT value_data FROM kv WHERE key_data = ? ALLOW FILTERING",
			"champion"
		)
		return results.one().getString("value_data")
	}

	def setChampion(champion:String) = {
		Db.session.execute(
			"INSERT INTO kv (key_data, value_data) VALUES (?,?);",
			"champion", champion
		)
	}
}

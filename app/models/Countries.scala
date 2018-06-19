package models

object Countries {
	def getList():List[String] = {
		return List(
			"egypt",
			"morocco",
			"nigeria",
			"senegal",
			"tunisia",
			"australia",
			"iran",
			"japan",
			"korea republic",
			"saudi arabia",
			"belgium",
			"croatia",
			"denmark",
			"england",
			"france",
			"germany",
			"iceland",
			"poland",
			"portugal",
			"russia",
			"serbia",
			"spain",
			"sweden",
			"switzerland",
			"costa rica",
			"mexico",
			"panama",
			"argentina",
			"brazil",
			"colombia",
			"peru",
			"uruguay"
		).sorted
	}

	def getPrettyName(name:String):String = {
		return name.capitalize
	}

	def getPicturePath(name:String):String = {
		return name.replaceAll(" ","_")+".png"
	}
}

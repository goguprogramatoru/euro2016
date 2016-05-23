package models

object Countries {
	def getList():List[String] = {
		return List(
			"albania",
			"austria",
			"cehia",
			"elvetia",
			"germania",
			"irlanda",
			"italia",
			"portugalia",
			"rusia",
			"spania",
			"turcia",
			"ungaria",
			"anglia",
			"belgia",
			"croatia",
			"franta",
			"irlanda de nord",
			"islanda",
			"polonia",
			"romania",
			"slovacia",
			"suedia",
			"ucraina",
			"wales"
		).sorted
	}

	def getPrettyName(name:String):String = {
		return name.capitalize
	}

	def getPicturePath(name:String):String = {
		return name.replaceAll(" ","_")+".png"
	}
}

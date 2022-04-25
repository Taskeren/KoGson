package cn.taskeren.kogson.test

import cn.taskeren.kogson.*
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Test

class TestJustGson {

	@Test
	fun testGson() {
		val el = JsonObject()

		el["a"] = 1
		el["b"] = "This is the Bravo"

		assert(el["b"].asString == "This is the Bravo")
		assert(el.getSafe("ah") == null)

		println("TestJustGson::JsonObject::done")

		val arr = JsonArray()
		val testContent = listOf<Any?>("AAA", true, 1024, 0b01, 0x12AF, null, 0.02F, 3.26, 9991L, 'H', "Hex")
		arr.addAll(testContent)

		assert(arr.getSafe(1)?.asBoolean == true)
		assert(arr.getSafe(5)?.asJsonNull != null)
		assert(arr.getSafe(9)?.asString == "H")

		println("TestJustGson::JsonArray::done")
	}

}


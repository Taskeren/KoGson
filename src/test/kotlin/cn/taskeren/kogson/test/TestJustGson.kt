package cn.taskeren.kogson.test

import cn.taskeren.kogson.*
import cn.taskeren.kogson.KoGson.elementsUnsafe
import cn.taskeren.kogson.KoGson.membersUnsafe
import cn.taskeren.kogson.KoGson.putAll
import com.google.gson.*
import org.junit.Ignore
import org.junit.Test
import kotlin.test.asserter

class TestJustGson {

	@Test
	@Ignore
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

	@Test
	fun test_fromJson() {
		val gson = Gson()

		data class TheData(val name: String)

		val serializedData = """
			{
				"name": "Toaster"
			}
		""".trimIndent()

		val deserialized = gson.fromJson<TheData>(serializedData)

		assert(deserialized.name == "Toaster") { "::fromGson" }
	}

	@Test
	fun test_elementConvert() {
		val jsonObject: JsonElement = JsonObject()
		val jsonArray: JsonElement = JsonArray()
		val jsonPrimitive: JsonElement = JsonPrimitive("TEST")

		assert(jsonObject.jsonObjectSafe == jsonObject) { "JsonElementConvert::Object_Equals" }
		assert(jsonArray.jsonArraySafe == jsonArray) { "JsonElementConvert::Array_Equals" }
		assert(jsonPrimitive.jsonPrimitiveSafe == jsonPrimitive) { "JsonElementConvert::Primitive_Equals" }

		assert(jsonObject.jsonPrimitiveSafe == null) { "JsonElementConvert::Object_Convert_To_Primitive" }
	}

	@Test
	fun test_JsonObject_set() {
		val jsonObject = JsonObject()

		jsonObject["1"] = JsonPrimitive("arc")
		jsonObject["2"] = '0'
		jsonObject["3"] = Long.MAX_VALUE
		jsonObject["4"] = "The 3rd property is the Longest Long"
		jsonObject["IsTheForthPropertyRight"] = true
	}

	@Test
	fun test_AddALL() {
		val jsonObject = JsonObject()
		val contents = mapOf(
			"0" to "TRUE",
			"1" to true,
			"2" to "YEE",
			"3" to '0',
			"4" to null,
			"5" to 10000L,
			"6" to 3.2F,
			"7" to 12
		)
		jsonObject.putAll(contents)

		val jsonArray = JsonArray()
		val objects = listOf("TRUE", true, "YEE", '0', null, 10000L, 3.2F, 12)
		jsonArray.addAll(objects)

		for(i in 0..7) {
			assert(jsonObject["$i"] == jsonArray.get(i)) {
				"Object(${jsonObject["$i"]}) != Array(${jsonArray.get(i)})"
			}
		}
	}

	@Test
	fun test_Unsafe() {
		val jsonObject = JsonObject()
		jsonObject.membersUnsafe["THE_NULL"] = null
		assert(jsonObject.get("THE_NULL") == null)
	}

}


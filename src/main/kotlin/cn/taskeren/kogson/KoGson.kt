@file:Suppress("unused")

package cn.taskeren.kogson

import cn.taskeren.kogson.KoGson.addAll
import cn.taskeren.kogson.KoGson.createJsonArray
import cn.taskeren.kogson.KoGson.createJsonObject
import cn.taskeren.kogson.KoGson.putAll
import com.google.gson.*
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import java.io.Reader
import java.lang.reflect.Field
import java.lang.reflect.Type

// Fast "deserialize" without Type parameter
inline fun <reified T> Gson.fromJson(json: String): T         = fromJson(json,    (object : TypeToken<T>(){}).type)
inline fun <reified T> Gson.fromJson(reader: Reader): T       = fromJson(reader,  (object : TypeToken<T>(){}).type)
inline fun <reified T> Gson.fromJson(element: JsonElement): T = fromJson(element, (object : TypeToken<T>(){}).type)

// Type-checked converters for JsonElement
val JsonElement.jsonObjectSafe:    JsonObject?    get() = if(isJsonObject)    asJsonObject    else null
val JsonElement.jsonArraySafe:     JsonArray?     get() = if(isJsonArray)     asJsonArray     else null
val JsonElement.jsonPrimitiveSafe: JsonPrimitive? get() = if(isJsonPrimitive) asJsonPrimitive else null

// Fast "set" methods for JsonObject
operator fun JsonObject.set(property: String, value: JsonElement) = add(property, value)
operator fun JsonObject.set(property: String, value: Char)        = addProperty(property, value)
operator fun JsonObject.set(property: String, value: Number)      = addProperty(property, value)
operator fun JsonObject.set(property: String, value: String)      = addProperty(property, value)
operator fun JsonObject.set(property: String, value: Boolean)     = addProperty(property, value)

// "addAll" methods for JsonArray and JsonObject
fun JsonArray.addAll(c: Collection<*>) = addAll(c.toJsonElementCollection())
fun JsonObject.putAll(m: Map<*, *>) = putAll(m.toJsonElementMap())

// serialize the object into JsonElement with [theMagicGson],
// used by following methods.
private fun Any?.toJsonElement() = KoGson.theMagicGson.toJsonTree(this)

// convert Collection<Any?> into Collection<JsonElement>,
// transformed values with Any?::toJsonElement above
private fun Collection<*>.toJsonElementCollection(): List<JsonElement> {
	if(this.isEmpty()) return listOf()
	return map(Any?::toJsonElement)
}

// convert Map<Any?, Any?> to Map<String, JsonElement>,
// transformed keys with Any?::toString and values with Any?::toJsonElement above
private fun Map<*, *>.toJsonElementMap(): Map<String, JsonElement> {
	if(this.isEmpty()) return mapOf()
	return this.map { it.key.toString() to it.value.toJsonElement() }.toMap()
}

// Type-checked "get" methods for JsonObject
fun JsonObject.getSafe(elementName: String): JsonElement? =
	if(has(elementName)) get(elementName) else null
fun JsonObject.getObjectSafe(objectName: String): JsonObject? =
	if(has(objectName) && get(objectName).isJsonObject) getAsJsonObject(objectName) else null
fun JsonObject.getArraySafe(arrayName: String): JsonArray? =
	if(has(arrayName) && get(arrayName).isJsonArray) getAsJsonArray(arrayName) else null
fun JsonObject.getPrimitiveSafe(primitiveName: String): JsonPrimitive? =
	if(has(primitiveName) && get(primitiveName).isJsonPrimitive) getAsJsonPrimitive(primitiveName) else null

// Type-checked "get" methods for JsonArray
fun JsonArray.getSafe(index: Int): JsonElement? =
	if(index in 0 until size()) get(index) else null
fun JsonArray.getObjectSafe(index: Int): JsonObject? = getSafe(index)?.jsonObjectSafe
fun JsonArray.getArraySafe(index: Int): JsonArray? = getSafe(index)?.jsonArraySafe
fun JsonArray.getPrimitiveSafe(index: Int): JsonPrimitive? = getSafe(index)?.jsonPrimitiveSafe

object KoGson {

	var theMagicGson: Gson = Gson()
		private set

	fun updateTheMagicGson(block: GsonBuilder.() -> Gson) { theMagicGson = block(theMagicGson.newBuilder()) }

	// private final List<JsonElement> elements;
	private val refElements = JsonArray::class.java.getDeclaredField("elements").apply(Field::trySetAccessible)
	// private final LinkedTreeMap<String, JsonElement> members;
	private val refMembers = JsonObject::class.java.getDeclaredField("members").apply(Field::trySetAccessible)

	// The "elements" field in JsonArray to store the JsonElements
	val JsonArray.elementsUnsafe @Suppress("UNCHECKED_CAST") get() = refElements.get(this) as ArrayList<JsonElement>
	// The "members" field in JsonObject to store the JsonElements keyed by the Strings(names).
	val JsonObject.membersUnsafe @Suppress("UNCHECKED_CAST") get() = refMembers.get(this) as LinkedTreeMap<String, JsonElement>

	// Add JsonElements directly into the JsonArray by reflection
	fun JsonArray.addAll(c: Collection<JsonElement>) = apply { elementsUnsafe.addAll(c) }
	// Add String-JsonElement entries into the JsonObject by reflection
	fun JsonObject.putAll(m: Map<String, JsonElement>) = apply { membersUnsafe.putAll(m) }

	/**
	 * Generate [JsonArray] with initial elements [c]
	 * @param c Initial elements
	 */
	fun <E> createJsonArray(c: Collection<E>): JsonArray =
		if(c.isEmpty()) {
			JsonArray()
		} else {
			JsonArray().apply { this.addAll(c.toJsonElementCollection()) }
		}

	/**
	 * Generate [JsonObject] with initial named elements [m]
	 * @param m Initial elements
	 */
	fun <K, V> createJsonObject(m: Map<K, V>): JsonObject =
		if(m.isEmpty()) {
			JsonObject()
		} else {
			JsonObject().apply { this.putAll(m.toJsonElementMap()) }
		}
}
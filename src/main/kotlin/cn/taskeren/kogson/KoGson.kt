@file:Suppress("unused")

package cn.taskeren.kogson

import cn.taskeren.kogson.KoGson.elementsUnsafe
import cn.taskeren.kogson.KoGson.membersUnsafe
import com.google.gson.*
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import java.io.Reader

// Fast "deserialize" without Type parameter
inline fun <reified T> Gson.fromJson(json: String): T = fromJson(json, (object : TypeToken<T>() {}).type)
inline fun <reified T> Gson.fromJson(reader: Reader): T = fromJson(reader, (object : TypeToken<T>() {}).type)
inline fun <reified T> Gson.fromJson(element: JsonElement): T = fromJson(element, (object : TypeToken<T>() {}).type)

// Type-checked converters for JsonElement
val JsonElement.jsonObjectSafe: JsonObject? get() = if(isJsonObject) asJsonObject else null
val JsonElement.jsonArraySafe: JsonArray? get() = if(isJsonArray) asJsonArray else null
val JsonElement.jsonPrimitiveSafe: JsonPrimitive? get() = if(isJsonPrimitive) asJsonPrimitive else null

// Fast "set" methods for JsonObject
operator fun JsonObject.set(property: String, value: JsonElement) = add(property, value)
operator fun JsonObject.set(property: String, value: Char) = addProperty(property, value)
operator fun JsonObject.set(property: String, value: Number) = addProperty(property, value)
operator fun JsonObject.set(property: String, value: String) = addProperty(property, value)
operator fun JsonObject.set(property: String, value: Boolean) = addProperty(property, value)

// "addAll" methods for JsonArray and JsonObject
fun JsonArray.addAll(c: Collection<*>) = apply { elementsUnsafe.addAll(c.toJsonElementCollection()) }
fun JsonObject.putAll(m: Map<*, *>) = apply { membersUnsafe.putAll(m.toJsonElementMap()) }

// serialize the object into JsonElement with [theMagicGson],
// used by following methods.
private fun Any?.toJsonElement() = KoGson.theMagicGson.toJsonTree(this)

// convert Collection<Any?> into Collection<JsonElement>,
// transformed values with Any?::toJsonElement above
internal fun Collection<*>.toJsonElementCollection(): List<JsonElement> {
	if(this.isEmpty()) return listOf()
	return map(Any?::toJsonElement)
}

// convert Map<Any?, Any?> to Map<String, JsonElement>,
// transformed keys with Any?::toString and values with Any?::toJsonElement above
internal fun Map<*, *>.toJsonElementMap(): Map<String, JsonElement> {
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

// Add content to JsonArrays and JsonObjects
fun Collection<*>.toJsonArray() =
	if(this.isEmpty()) JsonArray() else JsonArray().also { it.addAll(this.toJsonElementCollection()) }

fun Map<*, *>.toJsonObject() =
	if(this.isEmpty()) JsonObject() else JsonObject().also { it.putAll(this.toJsonElementMap()) }

object KoGson {

	internal var theMagicGson: Gson = Gson()

	fun updateTheMagicGson(block: GsonBuilder.() -> Gson) {
		theMagicGson = block(theMagicGson.newBuilder())
	}

	// private final List<JsonElement> elements;
	internal val refElements = JsonArray::class.java.getDeclaredField("elements").apply { isAccessible = true }

	// private final LinkedTreeMap<String, JsonElement> members;
	internal val refMembers = JsonObject::class.java.getDeclaredField("members").apply { isAccessible = true }

	// The "elements" field in JsonArray to store the JsonElements
	internal val JsonArray.elementsUnsafe @Suppress("UNCHECKED_CAST") get() = refElements.get(this) as ArrayList<JsonElement>

	// The "members" field in JsonObject to store the JsonElements keyed by the Strings(names).
	internal val JsonObject.membersUnsafe @Suppress("UNCHECKED_CAST") get() = refMembers.get(this) as LinkedTreeMap<String, JsonElement>
}
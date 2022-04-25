@file:Suppress("unused")

package cn.taskeren.kogson

import cn.taskeren.kogson.KoGson.createJsonArray
import com.google.gson.*
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import java.io.Reader
import java.lang.reflect.Field
import java.lang.reflect.Type

// 反序列化方法
inline fun <reified T> Gson.fromJson(json: String): T         = fromJson(json,    (object : TypeToken<T>(){}).type)
inline fun <reified T> Gson.fromJson(reader: Reader): T       = fromJson(reader,  (object : TypeToken<T>(){}).type)
inline fun <reified T> Gson.fromJson(element: JsonElement): T = fromJson(element, (object : TypeToken<T>(){}).type)

// 安全转换 JsonElement
val JsonElement.jsonObjectSafe:    JsonObject?    get() = if(isJsonObject)    asJsonObject    else null
val JsonElement.jsonArraySafe:     JsonArray?     get() = if(isJsonArray)     asJsonArray     else null
val JsonElement.jsonPrimitiveSafe: JsonPrimitive? get() = if(isJsonPrimitive) asJsonPrimitive else null

// 快捷赋值 JsonObject
operator fun JsonObject.set(property: String, value: JsonElement) = add(property, value)
operator fun JsonObject.set(property: String, value: Char)        = addProperty(property, value)
operator fun JsonObject.set(property: String, value: Number)      = addProperty(property, value)
operator fun JsonObject.set(property: String, value: String)      = addProperty(property, value)
operator fun JsonObject.set(property: String, value: Boolean)     = addProperty(property, value)

// 快捷添加内容 JsonArray
fun JsonArray.addAll(c: Collection<*>) =
	addAll(createJsonArray(c))

private fun Any?.toJsonElement() = KoGson.theMagicGson.toJsonTree(this)

private fun Collection<*>.toJsonElementCollection(): List<JsonElement> {
	if(this.isEmpty()) return listOf()
	return map(Any?::toJsonElement)
}

private fun Map<*, *>.toJsonElementMap(): Map<String, JsonElement> {
	if(this.isEmpty()) return mapOf()
	return this.mapKeys(Any?::toString).mapValues(Any?::toJsonElement)
}

// 安全获取 JsonObject 内容
fun JsonObject.getSafe(elementName: String): JsonElement? =
	if(has(elementName)) get(elementName) else null
fun JsonObject.getObjectSafe(objectName: String): JsonObject? =
	if(has(objectName) && get(objectName).isJsonObject) getAsJsonObject(objectName) else null
fun JsonObject.getArraySafe(arrayName: String): JsonArray? =
	if(has(arrayName) && get(arrayName).isJsonArray) getAsJsonArray(arrayName) else null
fun JsonObject.getPrimitiveSafe(primitiveName: String): JsonPrimitive? =
	if(has(primitiveName) && get(primitiveName).isJsonPrimitive) getAsJsonPrimitive(primitiveName) else null

// 安全获取 JsonArray 内容
fun JsonArray.getSafe(index: Int): JsonElement? =
	if(index in 0 until size()) get(index) else null
fun JsonArray.getObjectSafe(index: Int): JsonObject? = getSafe(index)?.jsonObjectSafe
fun JsonArray.getArraySafe(index: Int): JsonArray? = getSafe(index)?.jsonArraySafe
fun JsonArray.getPrimitiveSafe(index: Int): JsonPrimitive? = getSafe(index)?.jsonPrimitiveSafe

// Gson 操作
fun Gson.registerTypeAdapter(type: Type, adapter: Any): Gson = newBuilder().registerTypeAdapter(type, adapter).create()
fun Gson.registerTypeAdapterFactory(factory: TypeAdapterFactory): Gson = newBuilder().registerTypeAdapterFactory(factory).create()

object KoGson {

	var theMagicGson: Gson = Gson()
		private set

	fun updateTheMagicGson(block: Gson.() -> Gson) { theMagicGson = block(theMagicGson) }

	// private final List<JsonElement> elements;
	private val refElements = JsonArray::class.java.getDeclaredField("elements").apply(Field::trySetAccessible)
	// private final LinkedTreeMap<String, JsonElement> members;
	private val refMembers = JsonObject::class.java.getDeclaredField("members").apply(Field::trySetAccessible)

	val JsonArray.elementsUnsafe @Suppress("UNCHECKED_CAST") get() = refElements.get(this) as ArrayList<JsonElement>
	val JsonObject.membersUnsafe @Suppress("UNCHECKED_CAST") get() = refMembers.get(this) as LinkedTreeMap<String, JsonElement>

	fun JsonArray.addAll(c: Collection<JsonElement>) = apply { elementsUnsafe.addAll(c) }
	fun JsonObject.putAll(m: Map<String, JsonElement>) = apply { membersUnsafe.putAll(m) }

	fun <E> createJsonArray(c: Collection<E>): JsonArray =
		if(c.isEmpty()) {
			JsonArray()
		} else {
			JsonArray().apply { this.addAll(c.toJsonElementCollection()) }
		}

	fun <K, V> createJsonObject(m: Map<K, V>): JsonObject =
		if(m.isEmpty()) {
			JsonObject()
		} else {
			JsonObject().apply { this.putAll(m.toJsonElementMap()) }
		}
}
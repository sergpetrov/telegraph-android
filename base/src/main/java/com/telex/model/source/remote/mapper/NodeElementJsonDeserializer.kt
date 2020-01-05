package com.telex.model.source.remote.mapper

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.telex.model.source.remote.data.NodeElementData
import java.lang.reflect.Type

/**
 * @author Sergey Petrov
 */
class NodeElementJsonDeserializer : JsonDeserializer<NodeElementData> {

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext): NodeElementData {
        if (json.isJsonObject) {
            val jsonObj = json.asJsonObject
            val children = arrayListOf<NodeElementData>()
            val childrenJson = jsonObj?.get("children")?.asJsonArray
            childrenJson?.forEach { element ->
                if (element.isJsonObject) {
                    children.add(context.deserialize(element, NodeElementData::class.java))
                } else if (!element.asString.isNullOrEmpty()) {
                    children.add(NodeElementData(text = element.asString))
                }
            }
            return NodeElementData(
                    tag = jsonObj["tag"].asString,
                    attrs = context.deserialize(jsonObj["attrs"], HashMap::class.java) ?: mutableMapOf(),
                    children = children
            )
        } else {
            return NodeElementData(text = json.asJsonPrimitive.asString)
        }
    }
}

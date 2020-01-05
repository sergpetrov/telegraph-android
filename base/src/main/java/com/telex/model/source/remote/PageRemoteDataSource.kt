package com.telex.model.source.remote

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.telex.model.source.remote.api.RestApiProvider
import com.telex.model.source.remote.data.NodeElementData
import com.telex.model.source.remote.data.PageData
import com.telex.model.source.remote.data.PageListData
import com.telex.model.source.remote.data.ResponseData
import com.telex.model.system.ServerManager
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import java.lang.reflect.Type
import javax.inject.Inject
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * @author Sergey Petrov
 */
class PageRemoteDataSource @Inject constructor(
    private val apiProvider: RestApiProvider,
    private val serverManager: ServerManager
) {

    fun getPages(offset: Int): Single<PageListData> {
        return apiProvider.getRestApi()
                .getPages(offset)
                .map { response ->
                    val totalCount = response.result.totalCount
                    val pages = response.result.pages

                    var pageNumber = totalCount - offset - 1

                    pages.forEach { page ->
                        page.number = pageNumber
                        pageNumber--
                    }

                    response.result
                }
    }

    fun getPage(path: String) = apiProvider.getRestApi().getPage(path)

    fun editPage(path: String, title: String, authorName: String?, authorUrl: String?, content: List<NodeElementData>): Single<ResponseData<PageData>> {
        /* val body = MultipartBody.Builder("TelegraPhBoundary21")
                 .setType(MediaType.parse("multipart/form-data"))
                 .addFormDataPart("Data", "content.html", requestFile)
                 .addPart( MultipartBody.Part.createFormData("title", title))
                 .addPart( MultipartBody.Part.createFormData("page_id", title))
                 .build()
         return api.savePage("https://edit.telegra.ph/save", body)*/

        val contentJson = nodesToJson(content)

        return apiProvider.getRestApi().editPage(path, title, authorName, authorUrl, contentJson)
    }

    fun createPage(title: String, authorName: String?, authorUrl: String?, content: List<NodeElementData>): Single<ResponseData<PageData>> {
        val contentJson = nodesToJson(content)

        return apiProvider.getRestApi().createPage(title, authorName, authorUrl, nodesToJson(content))
    }

    private fun nodesToJson(content: List<NodeElementData>): String {
        return GsonBuilder()
                .disableHtmlEscaping()
                .registerTypeAdapter(NodeElementData::class.java, JsonSerializer<NodeElementData> { nodeElementData: NodeElementData, type: Type, context: JsonSerializationContext ->
                    if (!nodeElementData.tag.isNullOrBlank() && nodeElementData.text.isNullOrBlank()) {
                        val result = JsonObject()
                        result.addProperty("tag", nodeElementData.tag)
                        result.add("attrs", context.serialize(nodeElementData.attrs))
                        result.add("children", context.serialize(nodeElementData.children))
                        result
                    } else {
                        JsonPrimitive(nodeElementData.text)
                    }
                }).create().toJson(content)
    }

    fun uploadImage(file: File): Observable<String> {
        val requestFile = RequestBody.create(MediaType.parse("image/*"), file)
        val body = MultipartBody.Part.createFormData("file", "blob", requestFile)
        return apiProvider.getRestApi().uploadImage(serverManager.getImageUploadEndPoint(), body)
                .map { data -> data[0] }
                .map { serverManager.getEndPoint() + it.src }
    }
}

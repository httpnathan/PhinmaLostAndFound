package com.example.phinmalostandfound

import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*

abstract class VolleyMultipartRequest(
    method: Int,
    url: String,
    private val listener: Response.Listener<NetworkResponse>,
    errorListener: Response.ErrorListener
) : Request<NetworkResponse>(method, url, errorListener) {

    private val boundary = "volley-boundary-" + System.currentTimeMillis()
    private val lineEnd = "\r\n"
    private val twoHyphens = "--"

    override fun getBodyContentType(): String {
        return "multipart/form-data;boundary=$boundary"
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)

        try {
            // Text params
            val params = params
            if (params != null && params.isNotEmpty()) {
                textParse(dos, params, getParamsEncoding())
            }

            // File params
            val data = getByteData()
            if (data != null && data.isNotEmpty()) {
                dataParse(dos, data)
            }

            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

            return bos.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return ByteArray(0)
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        return Response.success(
            response,
            HttpHeaderParser.parseCacheHeaders(response)
        )
    }

    override fun deliverResponse(response: NetworkResponse) {
        listener.onResponse(response)
    }

    @Throws(AuthFailureError::class)
    override fun getParams(): MutableMap<String, String> {
        return HashMap()
    }

    protected open fun getByteData(): Map<String, DataPart>? {
        return null
    }

    @Throws(IOException::class)
    private fun textParse(
        dos: DataOutputStream,
        params: Map<String, String>,
        encoding: String
    ) {
        for ((key, value) in params) {
            dos.writeBytes(twoHyphens + boundary + lineEnd)
            dos.writeBytes("Content-Disposition: form-data; name=\"$key\"$lineEnd")
            dos.writeBytes("Content-Type: text/plain; charset=$encoding$lineEnd")
            dos.writeBytes(lineEnd)
            dos.writeBytes(value)
            dos.writeBytes(lineEnd)
        }
    }

    @Throws(IOException::class)
    private fun dataParse(
        dos: DataOutputStream,
        data: Map<String, DataPart>
    ) {
        for ((key, dataPart) in data) {
            writeDataPart(dos, dataPart, key)
        }
    }

    @Throws(IOException::class)
    private fun writeDataPart(
        dos: DataOutputStream,
        dataFile: DataPart,
        inputName: String
    ) {
        dos.writeBytes(twoHyphens + boundary + lineEnd)
        dos.writeBytes(
            "Content-Disposition: form-data; name=\"$inputName\"; filename=\"" +
                    dataFile.fileName + "\"" + lineEnd
        )
        dos.writeBytes("Content-Type: ${dataFile.type}$lineEnd")
        dos.writeBytes(lineEnd)

        dos.write(dataFile.content)

        dos.writeBytes(lineEnd)
    }

    class DataPart(
        val fileName: String,
        val content: ByteArray,
        val type: String = "image/jpeg"
    )
}
package com.winsun.fruitmix.newdesign201804.file.upload

import android.util.Log
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.FinishTaskState
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.StartingTaskState
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.IOException

const val TAG = "ProgressUploadRequest"

class ProgressUploadRequestBody(val requestBody: RequestBody,val task: Task):RequestBody() {

    private var bufferedSink: BufferedSink? = null

    private val startingTaskState:StartingTaskState = StartingTaskState(0,task.abstractFile.size,"0KB/s",task)

    init {

        task.setCurrentState(startingTaskState)

    }

    /**
     * Returns the Content-Type header for this body.
     */
    override fun contentType(): MediaType {
        return requestBody.contentType()
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return requestBody.contentLength()
    }

    /**
     * Writes the content of this request to `out`.
     *
     * @param sink
     */
    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {

        if (bufferedSink == null)
            bufferedSink = Okio.buffer(sink(sink))

        requestBody.writeTo(bufferedSink)
        bufferedSink!!.flush()

    }

    private fun sink(sink: BufferedSink): Sink {

        return object : ForwardingSink(sink) {
            internal var bytesWritten = 0L
            internal var contentLength = 0L

            @Throws(IOException::class)
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                if (contentLength == 0L) {
                    contentLength = contentLength()

                    Log.d(TAG, "write: contentLength:$contentLength")
                }
                bytesWritten += byteCount

                if (bytesWritten == contentLength) {

                    Log.d(TAG, "currentUploadSize: $bytesWritten")

                    startingTaskState.setCurrentHandleFileSize(bytesWritten)

                    task.setCurrentState(FinishTaskState(task))

                } else {

                    if (bytesWritten - startingTaskState.currentHandledSize > 1024) {

                        startingTaskState.setCurrentHandleFileSize(bytesWritten)

                        task.setCurrentState(startingTaskState)

                    }

                }

            }
        }
    }

}
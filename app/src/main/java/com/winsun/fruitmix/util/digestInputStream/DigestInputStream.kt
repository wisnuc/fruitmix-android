package com.winsun.fruitmix.util.digestInputStream

import java.io.IOException

interface DigestInputStream {

    @Throws(IOException::class)
    fun read(b: ByteArray): Int

    @Throws(IOException::class)
    fun read(b: ByteArray, off: Int, len: Int): Int

    @Throws(IOException::class)
    fun close()

}

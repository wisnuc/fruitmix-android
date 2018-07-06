package com.winsun.fruitmix.util.digestInputStream

import java.io.FileInputStream

class DigestFileInputStream(private val fileInputStream: FileInputStream):DigestInputStream {

    override fun read(b: ByteArray): Int {
        return fileInputStream.read(b)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return fileInputStream.read(b,off,len)
    }

    override fun close() {
        fileInputStream.close()
    }

}
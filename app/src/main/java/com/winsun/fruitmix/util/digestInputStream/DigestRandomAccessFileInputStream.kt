package com.winsun.fruitmix.util.digestInputStream

import java.io.RandomAccessFile

class DigestRandomAccessFileInputStream(val randomAccessFile: RandomAccessFile):DigestInputStream {

    override fun read(b: ByteArray): Int {
        return randomAccessFile.read(b)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return randomAccessFile.read(b,off,len)
    }

    override fun close() {
        randomAccessFile.close()
    }

}
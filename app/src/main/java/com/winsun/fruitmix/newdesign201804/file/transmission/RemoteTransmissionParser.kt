package com.winsun.fruitmix.newdesign201804.file.transmission

import com.winsun.fruitmix.newdesign201804.file.transmission.model.Transmission
import com.winsun.fruitmix.parser.BaseRemoteDataParser
import com.winsun.fruitmix.parser.RemoteDatasParser
import java.util.*

class RemoteTransmissionParser:BaseRemoteDataParser(),RemoteDatasParser<Transmission> {

    override fun parse(json: String?): MutableList<Transmission> {

        return Collections.emptyList()

    }

}
package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import android.content.Context
import com.winsun.fruitmix.db.DBUtils
import com.winsun.fruitmix.file.data.station.InjectStationFileRepository
import com.winsun.fruitmix.http.InjectHttp
import com.winsun.fruitmix.newdesign201804.util.getCurrentUserUUID
import com.winsun.fruitmix.newdesign201804.file.list.data.InjectFileDataSource
import com.winsun.fruitmix.thread.manage.ThreadManager
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl

public class InjectTransmissionTaskRepository {

    companion object {

        fun provideInstance(context: Context): TransmissionTaskRepository {

            val threadManager = ThreadManagerImpl.getInstance()

            val currentUserUUID = context.getCurrentUserUUID()

            val transmissionTaskDBDataSource = provideTransmissionTaskDBDataSource(context, threadManager, currentUserUUID)

            val taskManager = provideTaskManager(transmissionTaskDBDataSource)

            return TransmissionTaskRepository(taskManager, TransmissionTaskRemoteDataSource(
                    threadManager, currentUserUUID, InjectHttp.provideIHttpUtil(context),
                    InjectHttp.provideHttpRequestFactory(context)), transmissionTaskDBDataSource,
                    threadManager)

        }

        private fun provideTransmissionTaskDBDataSource(context: Context, threadManager: ThreadManager,
                                                        currentUserUUID: String): TransmissionTaskDBDataSource {
            return TransmissionTaskDBDataSource(DBUtils.getInstance(context),
                    InjectFileDataSource.inject(context), threadManager,
                    InjectStationFileRepository.provideStationFileRepository(context),
                    currentUserUUID)
        }

        private fun provideTaskManager(transmissionTaskDBDataSource: TransmissionTaskDBDataSource): TaskManager {
            val taskManager = TaskManager
            taskManager.init(transmissionTaskDBDataSource)

            return taskManager
        }

    }

}
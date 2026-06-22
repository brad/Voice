package voice.core.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters

public class MetroWorkerFactory(private val creators: Map<String, WorkerCreator>) : WorkerFactory() {

  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters,
  ): ListenableWorker? {
    return creators[workerClassName]?.create(appContext, workerParameters)
  }
}

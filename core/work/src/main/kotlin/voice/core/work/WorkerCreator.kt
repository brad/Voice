package voice.core.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

public interface WorkerCreator {
  public fun create(
    context: Context,
    parameters: WorkerParameters,
  ): ListenableWorker
}

public interface WorkerCreatorWithClass {
  public val workerClass: Class<out ListenableWorker>
  public val creator: WorkerCreator
}

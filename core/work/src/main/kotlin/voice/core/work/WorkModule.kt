package voice.core.work

import android.content.Context
import androidx.work.WorkManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
public interface WorkModule {

  @Provides
  @SingleIn(AppScope::class)
  public fun workManager(context: Context): WorkManager = WorkManager.getInstance(context)

  @Provides
  @SingleIn(AppScope::class)
  public fun metroWorkerFactory(creators: Map<String, @JvmSuppressWildcards WorkerCreator> = emptyMap()): MetroWorkerFactory {
    return MetroWorkerFactory(creators)
  }
}

package voice.core.work

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import voice.core.initializer.AppInitializer

@ContributesIntoSet(AppScope::class)
public class WorkManagerInitializer(private val workerFactory: MetroWorkerFactory) : AppInitializer {

  override fun onAppStart(application: Application) {
    val configuration = Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .build()
    WorkManager.initialize(application, configuration)
  }
}

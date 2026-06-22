package voice.core.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MetroWorkerFactoryTest {

  @Test
  fun `factory should return worker when creator is provided`() {
    val dummyWorkerName = DummyWorker::class.java.name
    val creator = object : WorkerCreator {
      override fun create(
        context: Context,
        parameters: WorkerParameters,
      ): ListenableWorker {
        return DummyWorker(context, parameters)
      }
    }
    val factory = MetroWorkerFactory(mapOf(dummyWorkerName to creator))

    val worker = factory.createWorker(
      mockk(relaxed = true),
      dummyWorkerName,
      mockk(relaxed = true),
    )

    assertNotNull(worker)
  }

  @Test
  fun `factory should return null when creator is not provided`() {
    val factory = MetroWorkerFactory(emptyMap())

    val worker = factory.createWorker(
      mockk(relaxed = true),
      "UnknownWorker",
      mockk(relaxed = true),
    )

    assertNull(worker)
  }

  class DummyWorker(
    context: Context,
    params: WorkerParameters,
  ) : Worker(context, params) {
    override fun doWork(): Result = Result.success()
  }
}

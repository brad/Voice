package voice.features.bookOverview.deleteBook

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.documentfile.provider.DocumentFile
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import voice.core.data.BookId
import voice.core.data.repo.BookRepository
import voice.core.data.repo.GenerationRepository
import voice.core.logging.api.Logger
import voice.core.scanner.MediaScanTrigger
import voice.core.work.AudiobookGenerationManager
import voice.features.bookOverview.bottomSheet.BottomSheetItem
import voice.features.bookOverview.bottomSheet.BottomSheetItemViewModel
import voice.features.bookOverview.di.BookOverviewScope

@SingleIn(BookOverviewScope::class)
@ContributesIntoSet(BookOverviewScope::class)
class DeleteBookViewModel(
  private val application: Application,
  private val mediaScanTrigger: MediaScanTrigger,
  private val bookRepository: BookRepository,
  private val generationRepository: GenerationRepository,
  private val audiobookGenerationManager: AudiobookGenerationManager,
) : BottomSheetItemViewModel {

  private val scope = MainScope()

  private val _state = mutableStateOf<DeleteBookViewState?>(null)
  internal val state: State<DeleteBookViewState?> get() = _state

  override suspend fun items(bookId: BookId): List<BottomSheetItem> {
    val book = bookRepository.get(bookId)
    return if (book == null) {
      val generation = generationRepository.progressForBook(bookId)
      if (generation != null) {
        listOf(BottomSheetItem.CancelImport)
      } else {
        emptyList()
      }
    } else {
      listOf(BottomSheetItem.DeleteBook)
    }
  }

  override suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  ) {
    if (item != BottomSheetItem.DeleteBook && item != BottomSheetItem.CancelImport) return

    _state.value = DeleteBookViewState(
      id = bookId,
      deleteCheckBoxChecked = false,
      isImport = item == BottomSheetItem.CancelImport,
      fileToDelete = bookId.toUri().pathSegments
        .let { segments ->
          val result = segments.lastOrNull()?.removePrefix("primary:")
          if (result.isNullOrEmpty()) {
            Logger.w("Could not determine path for $segments")
            segments.joinToString(separator = "\"")
          } else {
            result
          }
        },
    )
  }

  internal fun onDismiss() {
    _state.value = null
  }

  internal fun onDeleteCheckBoxCheck(checked: Boolean) {
    _state.value = _state.value?.copy(deleteCheckBoxChecked = checked)
  }

  internal fun onConfirmDeletion() {
    val state = _state.value
    if (state != null) {
      check(state.confirmButtonEnabled)
      scope.launch {
        if (state.isImport) {
          audiobookGenerationManager.cancelImport(state.id)
        } else {
          val uri = state.id.toUri()
          val documentFile = DocumentFile.fromSingleUri(application, uri)
          documentFile?.delete()
          mediaScanTrigger.scan(restartIfScanning = true)
        }
      }
    }
    _state.value = null
  }
}

data class DeleteBookViewState(
  val id: BookId,
  val deleteCheckBoxChecked: Boolean,
  val fileToDelete: String,
  val isImport: Boolean = false,
) {

  val confirmButtonEnabled = isImport || deleteCheckBoxChecked
}

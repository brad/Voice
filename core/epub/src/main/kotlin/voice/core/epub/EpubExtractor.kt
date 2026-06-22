package voice.core.epub

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import java.io.InputStream

class EpubExtractor {
    fun extract(inputStream: InputStream): EpubBookData {
        val epubBook = EpubReader().readEpub(inputStream)
        val title = epubBook.title
        val chapters = epubBook.spine.spineReferences.map { ref ->
            EpubChapter(
                title = ref.resource.title ?: "Untitled",
                content = ref.resource.inputStream.bufferedReader().readText()
            )
        }
        return EpubBookData(title, chapters)
    }
}

data class EpubBookData(val title: String, val chapters: List<EpubChapter>)
data class EpubChapter(val title: String, val content: String)

package voice.core.epub

import nl.siegmann.epublib.epub.EpubReader
import java.io.InputStream

class EpubExtractor {
  fun extract(inputStream: InputStream): EpubBookData {
    val epubBook = EpubReader().readEpub(inputStream)
    val title = epubBook.title ?: "Unknown Title"
    val author = epubBook.metadata.authors.firstOrNull()?.let { "${it.firstname} ${it.lastname}".trim() }
    val chapters =
      epubBook.spine.spineReferences.map { ref ->
        EpubChapter(
          title = ref.resource.title ?: "Untitled",
          content = stripHtml(ref.resource.inputStream.bufferedReader().readText()),
        )
      }
    return EpubBookData(title, author, chapters)
  }

  internal fun stripHtml(html: String): String {
    return html
      .replace(Regex("<head>.*?</head>", RegexOption.IGNORE_CASE), "")
      .replace(Regex("<script.*?>.*?</script>", RegexOption.IGNORE_CASE), "")
      .replace(Regex("<style.*?>.*?</style>", RegexOption.IGNORE_CASE), "")
      .replace(Regex("<[^>]*>"), " ")
      .replace("&nbsp;", " ")
      .replace("&amp;", "&")
      .replace("&gt;", ">")
      .replace("&lt;", "<")
      .replace("&quot;", "\"")
      .replace("&apos;", "'")
      .replace(Regex("\\s+"), " ")
      .trim()
  }
}

data class EpubBookData(
  val title: String,
  val author: String?,
  val chapters: List<EpubChapter>,
)

data class EpubChapter(
  val title: String,
  val content: String,
)

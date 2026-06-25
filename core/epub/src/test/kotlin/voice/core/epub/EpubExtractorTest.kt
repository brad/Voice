package voice.core.epub

import org.junit.Assert.assertEquals
import org.junit.Test

class EpubExtractorTest {

    private val extractor = EpubExtractor()

    @Test
    fun `test stripHtml removes tags and decodes entities`() {
        val html = """
            <html>
            <head><title>Ignore me</title></head>
            <body>
                <h1>Title</h1>
                <p>Hello &nbsp; world &amp; everyone!</p>
                <script>alert('bad');</script>
                <style>.bad { color: red; }</style>
                <div>Quotes: &quot; &apos;</div>
            </body>
            </html>
        """.trimIndent()

        val expected = "Title Hello world & everyone! Quotes: \" '"
        val actual = extractor.stripHtml(html)

        assertEquals(expected, actual)
    }
}

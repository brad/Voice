package voice.core.work

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import voice.core.data.BookId
import voice.core.data.Character
import voice.core.data.VoiceMapping
import kotlin.uuid.Uuid

class SpeechConfigBuilderTest {

  @Test
  fun `test single mapping uses voiceConfig`() {
    val bookId = BookId("test")
    val char = Character(Uuid.random(), bookId, "Narrator", null, null, null, null)
    val mappings = listOf(
      VoiceMapping(Uuid.random(), char.id, "Charon", 1.0f, 1.0f, 1.0f)
    )
    val characters = listOf(char)

    val config = SpeechConfigBuilder.build(mappings, characters)

    assertNotNull(config.voiceConfig)
    assertNull(config.multiSpeakerVoiceConfig)
    assertEquals("Charon", config.voiceConfig?.prebuiltVoiceConfig?.voiceName)
  }

  @Test
  fun `test two mappings uses multiSpeakerVoiceConfig`() {
    val bookId = BookId("test")
    val char1 = Character(Uuid.random(), bookId, "Narrator", null, null, null, null)
    val char2 = Character(Uuid.random(), bookId, "Joe", null, null, null, null)
    val mappings = listOf(
      VoiceMapping(Uuid.random(), char1.id, "Charon", 1.0f, 1.0f, 1.0f),
      VoiceMapping(Uuid.random(), char2.id, "Puck", 1.0f, 1.0f, 1.0f)
    )
    val characters = listOf(char1, char2)

    val config = SpeechConfigBuilder.build(mappings, characters)

    assertNull(config.voiceConfig)
    assertNotNull(config.multiSpeakerVoiceConfig)
    assertEquals(2, config.multiSpeakerVoiceConfig?.speakerVoiceConfigs?.size)
    assertEquals("Narrator", config.multiSpeakerVoiceConfig?.speakerVoiceConfigs?.get(0)?.speaker)
    assertEquals("Charon", config.multiSpeakerVoiceConfig?.speakerVoiceConfigs?.get(0)?.voiceConfig?.prebuiltVoiceConfig?.voiceName)
    assertEquals("Joe", config.multiSpeakerVoiceConfig?.speakerVoiceConfigs?.get(1)?.speaker)
    assertEquals("Puck", config.multiSpeakerVoiceConfig?.speakerVoiceConfigs?.get(1)?.voiceConfig?.prebuiltVoiceConfig?.voiceName)
  }

  @Test
  fun `test more than two mappings limits to two voices`() {
    val bookId = BookId("test")
    val char1 = Character(Uuid.random(), bookId, "Narrator", null, null, null, null)
    val char2 = Character(Uuid.random(), bookId, "Joe", null, null, null, null)
    val char3 = Character(Uuid.random(), bookId, "Jane", null, null, null, null)
    val mappings = listOf(
      VoiceMapping(Uuid.random(), char1.id, "Charon", 1.0f, 1.0f, 1.0f),
      VoiceMapping(Uuid.random(), char2.id, "Puck", 1.0f, 1.0f, 1.0f),
      VoiceMapping(Uuid.random(), char3.id, "Kore", 1.0f, 1.0f, 1.0f)
    )
    val characters = listOf(char1, char2, char3)

    val config = SpeechConfigBuilder.build(mappings, characters)

    assertNull(config.voiceConfig)
    assertNotNull(config.multiSpeakerVoiceConfig)
    assertEquals(2, config.multiSpeakerVoiceConfig?.speakerVoiceConfigs?.size)
  }
}

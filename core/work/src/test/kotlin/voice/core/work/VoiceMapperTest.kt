package voice.core.work

import org.junit.Assert.assertEquals
import org.junit.Test
import voice.core.data.BookId
import voice.core.data.Character
import kotlin.uuid.Uuid

class VoiceMapperTest {

  @Test
  fun `test mapping female character`() {
    val character = Character(
      id = Uuid.random(),
      bookId = BookId("test"),
      name = "Alice",
      gender = "Female",
      age = "Adult",
      energy = "Medium",
      personality = "Kind",
    )
    val mapping = VoiceMapper.mapToVoice(character)
    assertEquals("Kore", mapping.voiceName)
  }

  @Test
  fun `test mapping low energy female character`() {
    val character = Character(
      id = Uuid.random(),
      bookId = BookId("test"),
      name = "Alice",
      gender = "Female",
      age = "Adult",
      energy = "Low",
      personality = "Calm",
    )
    val mapping = VoiceMapper.mapToVoice(character)
    assertEquals("Aoide", mapping.voiceName)
  }

  @Test
  fun `test mapping male character`() {
    val character = Character(
      id = Uuid.random(),
      bookId = BookId("test"),
      name = "Bob",
      gender = "Male",
      age = "Adult",
      energy = "Medium",
      personality = "Strong",
    )
    val mapping = VoiceMapper.mapToVoice(character)
    assertEquals("Charon", mapping.voiceName)
  }

  @Test
  fun `test mapping high energy male character`() {
    val character = Character(
      id = Uuid.random(),
      bookId = BookId("test"),
      name = "Bob",
      gender = "Male",
      age = "Adult",
      energy = "High",
      personality = "Excited",
    )
    val mapping = VoiceMapper.mapToVoice(character)
    assertEquals("Puck", mapping.voiceName)
  }

  @Test
  fun `test mapping child character`() {
    val character = Character(
      id = Uuid.random(),
      bookId = BookId("test"),
      name = "Charlie",
      gender = "Male",
      age = "Child",
      energy = "Medium",
      personality = "Playful",
    )
    val mapping = VoiceMapper.mapToVoice(character)
    assertEquals("Puck", mapping.voiceName)
  }

  @Test
  fun `test mapping elderly character`() {
    val character = Character(
      id = Uuid.random(),
      bookId = BookId("test"),
      name = "Dave",
      gender = "Male",
      age = "Elderly",
      energy = "Low",
      personality = "Wise",
    )
    val mapping = VoiceMapper.mapToVoice(character)
    assertEquals("Fenrir", mapping.voiceName)
  }
}

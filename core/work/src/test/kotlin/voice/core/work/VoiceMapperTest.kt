package voice.core.work

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import voice.core.data.BookId
import voice.core.data.Character
import voice.core.data.PovType
import voice.core.data.VoiceMapping
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
    assertEquals(1.0f, mapping.pitch)
    assertEquals(1.0f, mapping.speed)
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
    assertEquals(0.95f, mapping.speed)
    assertEquals(0.9f, mapping.energy)
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
    assertEquals(1.05f, mapping.speed)
    assertEquals(1.1f, mapping.energy)
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
    assertEquals(1.05f, mapping.pitch)
    assertEquals(1.02f, mapping.speed)
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
    assertEquals(0.9f, mapping.speed, 0.001f)
    assertEquals(0.95f, mapping.pitch, 0.001f)
  }

  @Test
  fun `test narrator mapping`() {
    val character = Character(
      id = Uuid.random(),
      bookId = BookId("test"),
      name = "Narrator",
      gender = null,
      age = null,
      energy = null,
      personality = null,
    )
    val mapping = VoiceMapper.mapToVoice(character)
    assertEquals("Charon", mapping.voiceName)
    assertEquals(0.95f, mapping.speed)
    assertEquals(1.0f, mapping.pitch)
    assertEquals(0.9f, mapping.energy)
  }

  @Test
  fun `test voice reuse varies pitch`() {
    val bookId = BookId("test")
    val char1 = Character(
      id = Uuid.random(),
      bookId = bookId,
      name = "Alice",
      gender = "Female",
      age = "Adult",
      energy = "Medium",
      personality = "Kind",
    )
    val mapping1 = VoiceMapper.mapToVoice(char1)
    assertEquals("Kore", mapping1.voiceName)
    assertEquals(1.0f, mapping1.pitch)

    val char2 = Character(
      id = Uuid.random(),
      bookId = bookId,
      name = "Eve",
      gender = "Female",
      age = "Adult",
      energy = "Medium",
      personality = "Shy",
    )
    val mapping2 = VoiceMapper.mapToVoice(char2, null, null, listOf(mapping1))
    assertEquals("Kore", mapping2.voiceName)
    assertNotEquals(mapping1.pitch, mapping2.pitch)
    assertEquals(1.05f, mapping2.pitch)
  }

  @Test
  fun `test first person narrator matching pov character voice`() {
    val bookId = BookId("test")
    val povChar = Character(
      id = Uuid.random(),
      bookId = bookId,
      name = "Alice",
      gender = "Female",
      age = "Adult",
      energy = "Medium",
      personality = "Kind",
    )
    val narrator = Character(
      id = Uuid.random(),
      bookId = bookId,
      name = "Narrator",
      gender = null,
      age = null,
      energy = null,
      personality = null,
    )

    val mapping = VoiceMapper.mapToVoice(
      character = narrator,
      povType = PovType.FIRST_PERSON,
      povCharacterName = "Alice",
      allCharacters = listOf(povChar),
    )

    assertEquals("Kore", mapping.voiceName)
    assertEquals(0.95f, mapping.speed)
    assertEquals(0.9f, mapping.energy)
  }

  @Test
  fun `test third person limited narrator matching focal character voice`() {
    val bookId = BookId("test")
    val focalChar = Character(
      id = Uuid.random(),
      bookId = bookId,
      name = "Bob",
      gender = "Male",
      age = "Adult",
      energy = "Medium",
      personality = "Strong",
    )
    val narrator = Character(
      id = Uuid.random(),
      bookId = bookId,
      name = "Narrator",
      gender = null,
      age = null,
      energy = null,
      personality = null,
    )

    val mapping = VoiceMapper.mapToVoice(
      character = narrator,
      povType = PovType.THIRD_PERSON_LIMITED,
      povCharacterName = "Bob",
      allCharacters = listOf(focalChar),
    )

    assertEquals("Charon", mapping.voiceName)
    assertEquals(0.95f, mapping.speed)
    assertEquals(0.9f, mapping.energy)
  }

  @Test
  fun `test omniscient narrator uses default voice`() {
    val bookId = BookId("test")
    val narrator = Character(
      id = Uuid.random(),
      bookId = bookId,
      name = "Narrator",
      gender = null,
      age = null,
      energy = null,
      personality = null,
    )

    val mapping = VoiceMapper.mapToVoice(
      character = narrator,
      povType = PovType.OMNISCIENT,
      povCharacterName = null,
      allCharacters = emptyList(),
    )

    assertEquals("Charon", mapping.voiceName)
    assertEquals(0.95f, mapping.speed)
  }
}

package voice.core.work

import voice.core.data.Character
import voice.core.data.PovType
import voice.core.data.VoiceMapping
import kotlin.uuid.Uuid

internal object VoiceMapper {

  private const val VOICE_KORE = "Kore"
  private const val VOICE_AOIDE = "Aoide"
  private const val VOICE_PUCK = "Puck"
  private const val VOICE_CHARON = "Charon"
  private const val VOICE_FENRIR = "Fenrir"

  fun mapToVoice(
    character: Character,
    povType: PovType? = null,
    povCharacterName: String? = null,
    existingMappings: List<VoiceMapping> = emptyList(),
    allCharacters: List<Character> = emptyList(),
  ): VoiceMapping {
    val name = character.name.lowercase()
    val gender = character.gender?.lowercase() ?: "unknown"
    val age = character.age?.lowercase() ?: "adult"
    val energyTrait = character.energy?.lowercase() ?: "medium"

    val isNarrator = name == "narrator"

    val voiceName = when {
      isNarrator -> {
        if (povType == PovType.FIRST_PERSON && povCharacterName != null) {
          val povChar = allCharacters.find { it.name.lowercase() == povCharacterName.lowercase() }
          if (povChar != null) {
             // Use same voice as POV character
             mapToVoice(povChar, null, null, emptyList(), emptyList()).voiceName
          } else {
             VOICE_CHARON
          }
        } else {
          VOICE_CHARON
        }
      }
      gender.contains("female") || gender.contains("woman") || gender.contains("girl") -> {
        when {
          energyTrait.contains("low") -> VOICE_AOIDE
          else -> VOICE_KORE
        }
      }
      gender.contains("male") || gender.contains("man") || gender.contains("boy") -> {
        when {
          age.contains("child") || age.contains("teen") || energyTrait.contains("high") -> VOICE_PUCK
          age.contains("elder") || age.contains("old") -> VOICE_FENRIR
          else -> VOICE_CHARON
        }
      }
      else -> {
        // Unknown or non-binary
        when {
          energyTrait.contains("high") -> VOICE_PUCK
          else -> VOICE_KORE
        }
      }
    }

    val usageCount = existingMappings.count { it.voiceName == voiceName }

    // Vary pitch based on usage count to differentiate characters sharing the same voice
    val pitchBase = when (usageCount % 5) {
      0 -> 1.0f
      1 -> 1.05f
      2 -> 0.95f
      3 -> 1.02f
      4 -> 0.98f
      else -> 1.0f
    }

    var speed = 1.0f
    var energy = 1.0f
    var pitch = pitchBase

    if (isNarrator) {
      // Narrator specific tuning: "storyteller" tuning
      speed = 0.95f
      energy = 0.9f
    } else {
      // Apply energy traits
      when {
        energyTrait.contains("high") -> {
          speed += 0.05f
          energy += 0.1f
        }
        energyTrait.contains("low") -> {
          speed -= 0.05f
          energy -= 0.1f
        }
      }

      // Apply age traits
      when {
        age.contains("child") || age.contains("teen") -> {
          pitch += 0.05f
          speed += 0.02f
        }
        age.contains("elder") || age.contains("old") -> {
          speed -= 0.05f
          pitch -= 0.05f
        }
      }
    }

    return VoiceMapping(
      id = Uuid.random(),
      characterId = character.id,
      voiceName = voiceName,
      speed = speed.coerceIn(0.5f, 2.0f),
      pitch = pitch.coerceIn(0.5f, 2.0f),
      energy = energy.coerceIn(0.5f, 2.0f),
    )
  }
}

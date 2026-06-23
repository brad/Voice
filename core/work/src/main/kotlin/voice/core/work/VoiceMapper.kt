package voice.core.work

import voice.core.data.Character
import voice.core.data.VoiceMapping
import kotlin.uuid.Uuid

internal object VoiceMapper {

  private const val VOICE_KORE = "Kore"
  private const val VOICE_AOIDE = "Aoide"
  private const val VOICE_PUCK = "Puck"
  private const val VOICE_CHARON = "Charon"
  private const val VOICE_FENRIR = "Fenrir"

  fun mapToVoice(character: Character, existingMappings: List<VoiceMapping> = emptyList()): VoiceMapping {
    val gender = character.gender?.lowercase() ?: "unknown"
    val age = character.age?.lowercase() ?: "adult"
    val energy = character.energy?.lowercase() ?: "medium"

    val voiceName = when {
      gender.contains("female") || gender.contains("woman") || gender.contains("girl") -> {
        when {
          energy.contains("low") -> VOICE_AOIDE
          else -> VOICE_KORE
        }
      }
      gender.contains("male") || gender.contains("man") || gender.contains("boy") -> {
        when {
          age.contains("child") || age.contains("teen") || energy.contains("high") -> VOICE_PUCK
          age.contains("elder") || age.contains("old") -> VOICE_FENRIR
          else -> VOICE_CHARON
        }
      }
      else -> {
        // Unknown or non-binary
        when {
          energy.contains("high") -> VOICE_PUCK
          else -> VOICE_KORE
        }
      }
    }

    val usageCount = existingMappings.count { it.voiceName == voiceName }
    val pitch = when (usageCount % 5) {
      0 -> 1.0f
      1 -> 1.1f
      2 -> 0.9f
      3 -> 1.05f
      4 -> 0.95f
      else -> 1.0f
    }

    return VoiceMapping(
      id = Uuid.random(),
      characterId = character.id,
      voiceName = voiceName,
      speed = 1.0f,
      pitch = pitch,
      energy = 1.0f,
    )
  }
}

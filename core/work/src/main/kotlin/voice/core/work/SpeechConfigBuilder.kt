package voice.core.work

import voice.core.data.Character
import voice.core.data.VoiceMapping
import voice.core.gemini.MultiSpeakerVoiceConfig
import voice.core.gemini.PrebuiltVoiceConfig
import voice.core.gemini.SpeakerVoiceConfig
import voice.core.gemini.SpeechConfig
import voice.core.gemini.VoiceConfig

internal object SpeechConfigBuilder {
  fun build(
    mappings: List<VoiceMapping>,
    characters: List<Character>,
  ): SpeechConfig {
    return if (mappings.size == 1) {
      SpeechConfig(
        voiceConfig = VoiceConfig(
          prebuiltVoiceConfig = PrebuiltVoiceConfig(voiceName = mappings[0].voiceName),
        ),
      )
    } else {
      // Preview model requires exactly 2 voices for multi-speaker
      val limitedMappings = mappings.take(2)
      val speakerVoiceConfigs = limitedMappings.map { mapping ->
        val character = characters.find { it.id == mapping.characterId }
        SpeakerVoiceConfig(
          speaker = character?.name ?: "Narrator",
          voiceConfig = VoiceConfig(
            prebuiltVoiceConfig = PrebuiltVoiceConfig(voiceName = mapping.voiceName),
          ),
        )
      }
      SpeechConfig(
        multiSpeakerVoiceConfig = MultiSpeakerVoiceConfig(speakerVoiceConfigs),
      )
    }
  }
}

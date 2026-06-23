package voice.core.gemini

public object GeminiAnalysisPrompts {
  public const val CHARACTER_EXTRACTION_PROMPT: String = """
    Extract all distinct characters from the following text. For each character, provide their:
    - Name (the name they are most commonly referred to by)
    - Gender (if identifiable, e.g., "Male", "Female", "Non-binary")
    - Age (if identifiable, e.g., "Child", "Teenager", "Adult", "Elderly")
    - Energy (their speaking energy level, e.g., "Low", "Medium", "High")
    - Personality (a few words describing their personality/tone)

    Additionally, identify any unusual words, proper names, or technical terms that might require specific pronunciation guidance and provide their phonetic spelling (e.g., using simple phonetic respelling or IPA if clear).

    Format the output as a JSON object with a "characters" array and a "pronunciations" array.
  """

  public const val INCREMENTAL_CHARACTER_EXTRACTION_PROMPT: String = """
    You are analyzing a book to identify characters and word pronunciations for audiobook generation.

    Known Characters (from previous chunks):
    %s

    New Text Chunk:
    %s

    Task:
    1. Identify all characters in the "New Text Chunk".
    2. Reconcile them with "Known Characters":
       - If a character is already known, update their details if the new chunk provides more info.
       - If a character is new, add them.
       - Resolve different names for the same character to one primary name.
    3. Identify any unusual words, proper names, or technical terms in this chunk that might require specific pronunciation guidance and provide their phonetic spelling.
    4. Return the FULL updated list of all characters identified so far, and any new pronunciations found in THIS chunk.

    Format the output as a JSON object with a "characters" array and a "pronunciations" array.
  """

  public val CHARACTER_EXTRACTION_SCHEMA: ResponseSchema = ResponseSchema(
    type = "object",
    properties = mapOf(
      "characters" to ResponseSchema(
        type = "array",
        description = "List of extracted characters",
        items = ResponseSchema(
          type = "object",
          properties = mapOf(
            "name" to ResponseSchema(type = "string", description = "Character name"),
            "gender" to ResponseSchema(type = "string", description = "Character gender (e.g., Male, Female)"),
            "age" to ResponseSchema(type = "string", description = "Character age group (e.g., Child, Adult)"),
            "energy" to ResponseSchema(type = "string", description = "Energy level (e.g., Low, High)"),
            "personality" to ResponseSchema(type = "string", description = "Personality traits"),
          ),
          required = listOf("name"),
        ),
      ),
      "pronunciations" to ResponseSchema(
        type = "array",
        description = "List of words requiring phonetic guidance",
        items = ResponseSchema(
          type = "object",
          properties = mapOf(
            "word" to ResponseSchema(type = "string", description = "The word or name"),
            "phonetic" to ResponseSchema(type = "string", description = "Phonetic spelling"),
          ),
          required = listOf("word", "phonetic"),
        ),
      ),
    ),
    required = listOf("characters", "pronunciations"),
  )
}

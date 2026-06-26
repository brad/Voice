package voice.core.gemini

public object GeminiAnalysisPrompts {
  public const val CHARACTER_EXTRACTION_PROMPT: String = """
    Extract all distinct characters from the following text. For each character, provide their:
    - Name (the name they are most commonly referred to by)
    - Gender (if identifiable, e.g., "Male", "Female", "Non-binary")
    - Age (if identifiable, e.g., "Child", "Teenager", "Adult", "Elderly")
    - Energy (their speaking energy level, e.g., "Low", "Medium", "High")
    - Personality (a few words describing their personality/tone)

    Important: Provide all information that can be reasonably inferred from the text to ensure high-quality voice assignment. However, **do not invent details** that are not present or strongly implied. If a field truly cannot be identified, omit it or set it to null.

    Additionally, identify any unusual words, proper names, or technical terms that might require specific pronunciation guidance and provide their phonetic spelling (e.g., using simple phonetic respelling or IPA if clear).

    Format the output as a JSON object with a "characters" array and a "pronunciations" array.
  """

  public const val INCREMENTAL_CHARACTER_EXTRACTION_PROMPT: String = """
    You are analyzing a book to identify characters, word pronunciations, and narrative point of view (POV) for audiobook generation.

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
    4. Determine the narrative Point of View (POV) of this chunk:
       - "FIRST_PERSON": Narrated by a character (uses "I", "me", "my"). Identify the character name if possible.
       - "THIRD_PERSON_LIMITED": Narrated objectively but focuses on the thoughts/feelings of one character at a time. Identify the focal character name.
       - "OMNISCIENT": Narrator knows everything about all characters and events, often switching focus freely.
    5. Return the FULL updated list of all characters identified so far, any new pronunciations found in THIS chunk, and the detected POV.
    6. For each character, ensure all fields (gender, age, energy, personality) are included if the information is available or can be reasonably inferred. **Do not invent details**. If a detail is unknown, return null for that field.

    Format the output as a JSON object with:
    - "characters": array of character objects
    - "pronunciations": array of pronunciation objects
    - "povType": "FIRST_PERSON", "THIRD_PERSON_LIMITED", or "OMNISCIENT"
    - "povCharacterName": name of the POV/focal character (optional)
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
      "povType" to ResponseSchema(
        type = "string",
        description = "Narrative point of view",
        enum = listOf("FIRST_PERSON", "THIRD_PERSON_LIMITED", "OMNISCIENT"),
      ),
      "povCharacterName" to ResponseSchema(
        type = "string",
        description = "Name of the POV or focal character",
      ),
    ),
    required = listOf("characters", "pronunciations", "povType"),
  )
}

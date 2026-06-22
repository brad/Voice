package voice.core.gemini

object GeminiAnalysisPrompts {
  const val CHARACTER_EXTRACTION_PROMPT = """
    Extract all distinct characters from the following text. For each character, provide their:
    - Name (the name they are most commonly referred to by)
    - Gender (if identifiable, e.g., "Male", "Female", "Non-binary")
    - Age (if identifiable, e.g., "Child", "Teenager", "Adult", "Elderly")
    - Energy (their speaking energy level, e.g., "Low", "Medium", "High")
    - Personality (a few words describing their personality/tone)

    Format the output as a JSON object with a "characters" array.
  """

  val CHARACTER_EXTRACTION_SCHEMA = ResponseSchema(
    type = "object",
    properties = mapOf(
      "characters" to SchemaProperty(
        type = "array",
        description = "List of extracted characters",
      ),
    ),
    required = listOf("characters"),
    items = ResponseSchema(
      type = "object",
      properties = mapOf(
        "name" to SchemaProperty(type = "string", description = "Character name"),
        "gender" to SchemaProperty(type = "string", description = "Character gender (e.g., Male, Female)"),
        "age" to SchemaProperty(type = "string", description = "Character age group (e.g., Child, Adult)"),
        "energy" to SchemaProperty(type = "string", description = "Energy level (e.g., Low, High)"),
        "personality" to SchemaProperty(type = "string", description = "Personality traits"),
      ),
      required = listOf("name"),
    ),
  )
}

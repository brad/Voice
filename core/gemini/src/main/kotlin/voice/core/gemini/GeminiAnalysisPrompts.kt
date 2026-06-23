package voice.core.gemini

public object GeminiAnalysisPrompts {
  public const val CHARACTER_EXTRACTION_PROMPT: String = """
    Extract all distinct characters from the following text. For each character, provide their:
    - Name (the name they are most commonly referred to by)
    - Gender (if identifiable, e.g., "Male", "Female", "Non-binary")
    - Age (if identifiable, e.g., "Child", "Teenager", "Adult", "Elderly")
    - Energy (their speaking energy level, e.g., "Low", "Medium", "High")
    - Personality (a few words describing their personality/tone)

    Format the output as a JSON object with a "characters" array.
  """

  public const val INCREMENTAL_CHARACTER_EXTRACTION_PROMPT: String = """
    You are analyzing a book to identify characters for audiobook generation.

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
    3. Return the FULL updated list of all characters identified in the book so far.

    Format the output as a JSON object with a "characters" array.
  """

  public val CHARACTER_EXTRACTION_SCHEMA: ResponseSchema = ResponseSchema(
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

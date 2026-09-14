import os

from dotenv import load_dotenv

from mistralai.client import Mistral


# ==========================================
# LOAD ENVIRONMENT VARIABLES
# ==========================================

load_dotenv()


# ==========================================
# MISTRAL CONFIGURATION
# ==========================================

MISTRAL_API_KEY = os.getenv(
    "MISTRAL_API_KEY"
)


MODEL_NAME = os.getenv(
    "MODEL_NAME",
    "mistral-small-latest"
)


# ==========================================
# LANGUAGE HELPER
# ==========================================

def get_language_from_path(
    file_path
):

    lower = str(
        file_path
    ).lower()


    extensions = {

        ".py": "Python",

        ".java": "Java",

        ".js": "JavaScript",

        ".jsx": "JavaScript React",

        ".ts": "TypeScript",

        ".tsx": "TypeScript React",

        ".cpp": "C++",

        ".c": "C",

        ".h": "C/C++ Header",

        ".hpp": "C++ Header",

        ".cs": "C#",

        ".go": "Go",

        ".rs": "Rust",

        ".php": "PHP",

        ".html": "HTML",

        ".css": "CSS",

        ".sql": "SQL"
    }


    for extension, language in (
        extensions.items()
    ):

        if lower.endswith(
            extension
        ):

            return language


    return "Unknown"


# ==========================================
# NORMALIZE CHUNK
# ==========================================

def normalize_chunk(
    chunk,
    index=0
):

    # ======================================
    # STRING CHUNK
    # ======================================

    if isinstance(
        chunk,
        str
    ):

        return {

            "content":
                chunk,

            "file_path":
                "unknown",

            "language":
                "Unknown",

            "chunk_index":
                index
        }


    # ======================================
    # INVALID CHUNK
    # ======================================

    if not isinstance(
        chunk,
        dict
    ):

        return {

            "content":
                str(chunk),

            "file_path":
                "unknown",

            "language":
                "Unknown",

            "chunk_index":
                index
        }


    # ======================================
    # GET METADATA SAFELY
    # ======================================

    metadata = chunk.get(
        "metadata",
        {}
    )


    if not isinstance(
        metadata,
        dict
    ):

        metadata = {}


    # ======================================
    # GET FILE PATH
    # ======================================

    file_path = (

        chunk.get(
            "file_path"
        )

        or

        metadata.get(
            "file_path"
        )

        or

        metadata.get(
            "path"
        )

        or

        "unknown"
    )


    # ======================================
    # GET LANGUAGE
    # ======================================

    language = (

        chunk.get(
            "language"
        )

        or

        metadata.get(
            "language"
        )

        or

        get_language_from_path(
            file_path
        )
    )


    # ======================================
    # GET CONTENT
    # ======================================

    content = chunk.get(
        "content",
        ""
    )


    return {

        "content":
            str(content),

        "file_path":
            str(file_path),

        "language":
            str(language),

        "chunk_index":

            chunk.get(
                "chunk_index",

                metadata.get(
                    "chunk_index",
                    index
                )
            )
    }


# ==========================================
# RULE-BASED CODE ANALYSIS
# ==========================================

def analyze_code_chunk(
    content,
    file_path
):

    issues = []

    suggestions = []


    content_lower = (
        content.lower()
    )


    # ======================================
    # SECURITY
    # ======================================

    if "eval(" in content:

        issues.append(
            "🔴 HIGH — Unsafe use of `eval()` "
            "was detected. Untrusted input could "
            "execute arbitrary code."
        )

        suggestions.append(
            "Replace `eval()` with safe parsing "
            "or explicit logic."
        )


    if "exec(" in content:

        issues.append(
            "🔴 HIGH — Unsafe use of `exec()` "
            "was detected. Dynamic code execution "
            "can create serious security risks."
        )

        suggestions.append(
            "Avoid dynamic code execution unless "
            "it is strictly required."
        )


    if "os.system(" in content:

        issues.append(
            "🔴 HIGH — `os.system()` was detected. "
            "Unvalidated input may lead to command "
            "injection."
        )

        suggestions.append(
            "Use `subprocess.run()` with validated "
            "arguments instead."
        )


    # ======================================
    # ERROR HANDLING
    # ======================================

    if "except exception" in content_lower:

        issues.append(
            "🟠 MEDIUM — Broad exception handling "
            "using `except Exception` was detected."
        )

        suggestions.append(
            "Catch specific exception types where "
            "possible."
        )


    if "except:" in content_lower:

        issues.append(
            "🟠 MEDIUM — Bare `except:` was detected. "
            "It may hide unexpected errors."
        )

        suggestions.append(
            "Catch specific exception types instead "
            "of using a bare `except:`."
        )


    # ======================================
    # DEBUGGING
    # ======================================

    if "print(" in content:

        issues.append(
            "🟡 LOW — `print()` statements were "
            "detected."
        )

        suggestions.append(
            "Use structured logging for production "
            "applications."
        )


    if "console.log(" in content_lower:

        issues.append(
            "🟡 LOW — `console.log()` statements "
            "were detected."
        )

        suggestions.append(
            "Remove unnecessary debug logs before "
            "production deployment."
        )


    # ======================================
    # EMPTY FILE
    # ======================================

    if not content.strip():

        issues.append(
            "⚪ INFO — This code chunk is empty."
        )


    # ======================================
    # NO ISSUES
    # ======================================

    if not issues:

        issues.append(
            "🟢 No major rule-based issues were "
            "detected in the analyzed code."
        )


    # ======================================
    # DEFAULT SUGGESTIONS
    # ======================================

    if not suggestions:

        suggestions.append(
            "Add automated tests for important "
            "application logic."
        )

        suggestions.append(
            "Add clear documentation for complex "
            "functions and modules."
        )


    return issues, suggestions


# ==========================================
# INTELLIGENT FALLBACK REVIEW
# ==========================================

def fallback_review(
    relevant_chunks,
    reason="AI service unavailable"
):

    # ======================================
    # NORMALIZE INPUT FIRST
    # ======================================

    normalized_chunks = []


    for index, chunk in enumerate(
        relevant_chunks
    ):

        normalized_chunks.append(

            normalize_chunk(
                chunk,
                index
            )

        )


    review = (

        "# ReviewForge Repository Review\n\n"

        "## Summary\n\n"

        "The repository was successfully processed "
        "using ReviewForge's local code analysis engine. "

        "External AI review was unavailable because: "

        f"**{reason}**.\n\n"

        "A rule-based analysis was performed on "
        "the retrieved source code.\n\n"
    )


    # ======================================
    # GROUP CHUNKS BY FILE
    # ======================================

    files = {}


    for chunk in normalized_chunks:

        file_path = chunk.get(
            "file_path",
            "unknown"
        )


        content = chunk.get(
            "content",
            ""
        )


        language = chunk.get(
            "language",
            "Unknown"
        )


        if file_path not in files:

            files[file_path] = {

                "content":
                    "",

                "language":
                    language,

                "chunks":
                    0
            }


        files[file_path]["content"] += (
            "\n" + content
        )


        files[file_path]["chunks"] += 1


    # ======================================
    # FILE REVIEWS
    # ======================================

    all_issues = []


    review += (
        "## File Reviews\n\n"
    )


    # ======================================
    # NO FILES
    # ======================================

    if not files:

        review += (
            "No valid code files were available "
            "for local analysis.\n\n"
        )


    # ======================================
    # ANALYZE FILES
    # ======================================

    for file_path, file_data in (
        files.items()
    ):

        content = file_data[
            "content"
        ]


        language = file_data[
            "language"
        ]


        chunk_count = file_data[
            "chunks"
        ]


        issues, suggestions = (
            analyze_code_chunk(
                content,
                file_path
            )
        )


        review += (
            f"### {file_path}\n\n"
        )


        review += (
            f"**Language:** "
            f"{language}\n\n"
        )


        review += (
            f"**Code chunks analyzed:** "
            f"{chunk_count}\n\n"
        )


        review += (
            "#### Issues Found\n\n"
        )


        for issue in issues:

            review += (
                f"- {issue}\n"
            )


            if (

                "No major" not in issue

                and

                "INFO" not in issue

            ):

                all_issues.append(
                    issue
                )


        review += (
            "\n#### Suggested Improvements\n\n"
        )


        for suggestion in suggestions:

            review += (
                f"- {suggestion}\n"
            )


        review += (
            "\n---\n\n"
        )


    # ======================================
    # OVERALL ANALYSIS
    # ======================================

    review += (
        "## Overall Analysis\n\n"
    )


    if all_issues:

        review += (

            f"ReviewForge detected "
            f"**{len(all_issues)} potential issue(s)** "
            f"in the analyzed code.\n\n"
        )

    else:

        review += (

            "No major rule-based issues were "
            "detected in the analyzed code chunks.\n\n"
        )


    # ======================================
    # RECOMMENDATIONS
    # ======================================

    review += (

        "## Recommended Next Steps\n\n"

        "1. Add unit tests for important functions.\n"

        "2. Improve error handling for external "
        "services and user input.\n"

        "3. Replace debug statements with "
        "structured logging.\n"

        "4. Add documentation for important "
        "modules and complex logic.\n"

        "5. Run the review again when AI access "
        "is available for deeper analysis.\n\n"
    )


    # ======================================
    # ANALYSIS INFORMATION
    # ======================================

    review += (

        "## Analysis Information\n\n"

        f"- Relevant code chunks analyzed: "
        f"{len(normalized_chunks)}\n"

        f"- Files involved: "
        f"{len(files)}\n"

        "- Repository cloning: Successful\n"

        "- Code extraction: Successful\n"

        "- RAG retrieval: Successful\n"

        "- Local code analysis: Successful\n"

        "- AI-generated review: Not used\n"
    )


    return review


# ==========================================
# GENERATE AI CODE REVIEW
# ==========================================

def review_with_mistral(
    relevant_chunks
):

    # ======================================
    # CHECK CODE CHUNKS
    # ======================================

    if not relevant_chunks:

        return {

            "review": (
                "# ReviewForge Repository Review\n\n"
                "No relevant code chunks were "
                "available for analysis."
            ),

            "ai_used": False,

            "review_engine":
                "No Analysis",

            "analysis_type":
                "No relevant code found"
        }


    # ======================================
    # NORMALIZE ALL CHUNKS
    # ======================================

    normalized_chunks = []


    for index, chunk in enumerate(
        relevant_chunks
    ):

        normalized_chunks.append(

            normalize_chunk(
                chunk,
                index
            )

        )


    # ======================================
    # CHECK API KEY
    # ======================================

    if not MISTRAL_API_KEY:

        print(
            "MISTRAL_API_KEY not found."
        )

        print(
            "Using local fallback review."
        )


        return {

            "review":

                fallback_review(
                    normalized_chunks,
                    "Mistral API key is not configured"
                ),

            "ai_used":
                False,

            "review_engine":
                "Rule-Based Analyzer",

            "analysis_type":
                "Local Rule-Based Analysis"
        }


    # ======================================
    # PREPARE CODE CONTEXT
    # ======================================

    code_context = ""


    for index, chunk in enumerate(
        normalized_chunks,
        1
    ):

        file_path = chunk.get(
            "file_path",
            "unknown"
        )


        language = chunk.get(
            "language",
            "Unknown"
        )


        content = chunk.get(
            "content",
            ""
        )


        code_context += f"""

==================================================

FILE: {file_path}

LANGUAGE: {language}

CODE CHUNK {index}:

{content}

"""


    # ======================================
    # CREATE PROMPT
    # ======================================

    prompt = f"""

You are a senior software engineer
and expert code reviewer.

Analyze the repository code below.

Generate an accurate and practical
code review.

Focus on:

1. Bugs and logical errors
2. Security vulnerabilities
3. Performance problems
4. Code quality
5. Maintainability
6. Error handling
7. Bad coding practices
8. Improvements

IMPORTANT:

- Only report issues visible in the code.
- Mention specific file names.
- Explain why each issue matters.
- Suggest practical fixes.
- Do not invent problems.
- Mention positive aspects.

Use Markdown.

Use this structure:

# ReviewForge Repository Review

## Summary

## Critical Issues

## Code Quality Issues

## Security Issues

## Performance Issues

## Suggested Improvements

## Strengths

Repository Code:

{code_context}

"""


    # ======================================
    # CALL MISTRAL
    # ======================================

    try:

        print(
            "Generating AI review with Mistral..."
        )


        client = Mistral(
            api_key=MISTRAL_API_KEY
        )


        response = client.chat.complete(

            model=MODEL_NAME,

            messages=[

                {

                    "role": "system",

                    "content": (
                        "You are a senior software "
                        "engineer and expert code reviewer."
                    )
                },

                {

                    "role": "user",

                    "content":
                        prompt
                }

            ],

            temperature=0.2,

            max_tokens=3000
        )


        # ==================================
        # EXTRACT REVIEW
        # ==================================

        review = (
            response
            .choices[0]
            .message
            .content
        )


        if not review:

            raise ValueError(
                "Mistral returned an empty response."
            )


        print(
            "AI review generated successfully."
        )


        return {

            "review":
                review,

            "ai_used":
                True,

            "review_engine":
                "Mistral AI",

            "analysis_type":
                "AI-Powered Code Review"
        }


    # ======================================
    # ERROR HANDLING
    # ======================================

    except Exception as error:

        error_message = str(
            error
        )


        print(
            f"AI review failed: "
            f"{error_message}"
        )


        # ==================================
        # RATE LIMIT
        # ==================================

        if (

            "429" in error_message

            or

            "rate limit"
            in error_message.lower()

            or

            "rate_limited"
            in error_message.lower()

        ):

            print(
                "Mistral API rate limit detected."
            )

            print(
                "Using local intelligent "
                "fallback review."
            )


            return {

                "review":

                    fallback_review(
                        normalized_chunks,
                        "Mistral API rate limit reached"
                    ),

                "ai_used":
                    False,

                "review_engine":
                    "Rule-Based Analyzer",

                "analysis_type":
                    "Fallback Rule-Based Analysis"
            }


        # ==================================
        # OTHER AI ERRORS
        # ==================================

        print(
            "Using local fallback review."
        )


        return {

            "review":

                fallback_review(
                    normalized_chunks,
                    f"AI service error: "
                    f"{error_message}"
                ),

            "ai_used":
                False,

            "review_engine":
                "Rule-Based Analyzer",

            "analysis_type":
                "Fallback Rule-Based Analysis"
        }
from fastapi import FastAPI, HTTPException

from services.github_service import (
    clone_repository,
    get_repository_name,
    delete_repository
)

from services.code_service import (
    extract_code_files,
    chunk_code_files
)

from services.vector_service import (
    store_chunks,
    retrieve_relevant_chunks
)

from services.review_service import (
    review_with_mistral
)


# ==========================================
# FASTAPI APP
# ==========================================

app = FastAPI(
    title="ReviewForge AI Service",
    version="1.0.0"
)


# ==========================================
# HOME ENDPOINT
# ==========================================

@app.get("/")
def home():

    return {
        "message": "ReviewForge AI Service is running",
        "status": "healthy"
    }


# ==========================================
# REVIEW GITHUB REPOSITORY
# ==========================================

@app.post("/review/github")
def review_github_repository(repo_url: str):

    repository_path = None

    try:

        # ==================================
        # STEP 1: CLONE REPOSITORY
        # ==================================

        print("\nSTEP 1: Cloning repository...")

        repository_path = clone_repository(
            repo_url
        )


        # ==================================
        # GET REPOSITORY NAME
        # ==================================

        repo_name = get_repository_name(
            repo_url
        )


        # ==================================
        # STEP 2: EXTRACT CODE FILES
        # ==================================

        print(
            "STEP 2: Extracting code files..."
        )

        code_files = extract_code_files(
            repository_path
        )


        # ==================================
        # NO CODE FILES
        # ==================================

        if not code_files:

            return {
                "status": "completed",
                "repo_name": repo_name,
                "files": [],
                "files_analyzed": 0,
                "chunk_count": 0,
                "retrieved_chunks": 0,

                "review": (
                    "# ReviewForge Repository Review\n\n"
                    "## Summary\n\n"
                    "No supported source code files "
                    "were found in this repository."
                ),

                "ai_used": False,

                "review_engine": "No Analysis",

                "analysis_type":
                    "No supported code found"
            }


        # ==================================
        # GET FILE NAMES SAFELY
        # ==================================

        if isinstance(code_files, dict):

            file_names = list(
                code_files.keys()
            )

        elif isinstance(code_files, list):

            file_names = []

            for file_data in code_files:

                if isinstance(file_data, dict):

                    file_path = (
                        file_data.get("file_path")
                        or
                        file_data.get("path")
                        or
                        "unknown"
                    )

                    file_names.append(
                        str(file_path)
                    )

        else:

            file_names = []


        # ==================================
        # STEP 3: CHUNK CODE
        # ==================================

        print(
            "STEP 3: Chunking code..."
        )

        chunks = chunk_code_files(
            code_files
        )


        # ==================================
        # NO CHUNKS
        # ==================================

        if not chunks:

            return {
                "status": "completed",

                "repo_name": repo_name,

                "files": file_names,

                "files_analyzed":
                    len(file_names),

                "chunk_count": 0,

                "retrieved_chunks": 0,

                "review": (
                    "# ReviewForge Repository Review\n\n"
                    "## Summary\n\n"
                    "Source files were found but "
                    "no code chunks could be generated."
                ),

                "ai_used": False,

                "review_engine":
                    "No Analysis",

                "analysis_type":
                    "No code chunks generated"
            }


        # ==================================
        # STEP 4: STORE CHUNKS
        # ==================================

        print(
            "STEP 4: Storing chunks in ChromaDB..."
        )

        stored_count = store_chunks(
            repo_name,
            chunks
        )


        # ==================================
        # STEP 5: RETRIEVE RELEVANT CHUNKS
        # ==================================

        print(
            "STEP 5: Retrieving relevant chunks..."
        )

        review_query = (
            "Review the repository for bugs, "
            "security vulnerabilities, code quality, "
            "performance problems, error handling "
            "and possible improvements."
        )

        relevant_chunks = (
            retrieve_relevant_chunks(
                repo_name,
                review_query
            )
        )


        # ==================================
        # STEP 6: GENERATE REVIEW
        # ==================================

        print(
            "STEP 6: Generating code review..."
        )

        review_result = review_with_mistral(
            relevant_chunks
        )


        # ==================================
        # SUCCESS RESPONSE
        # ==================================

        return {

            "status": "completed",

            "repo_name": repo_name,

            "files": file_names,

            "files_analyzed":
                len(file_names),

            "chunk_count":
                len(chunks),

            "stored_chunks":
                stored_count,

            "retrieved_chunks":
                len(relevant_chunks),

            "review":
                review_result.get(
                    "review",
                    "No review generated."
                ),

            "ai_used":
                review_result.get(
                    "ai_used",
                    False
                ),

            "review_engine":
                review_result.get(
                    "review_engine",
                    "Unknown"
                ),

            "analysis_type":
                review_result.get(
                    "analysis_type",
                    "Unknown"
                )
        }


    # ======================================
    # ERROR
    # ======================================

    except Exception as error:

        print(
            f"Repository review failed: {error}"
        )

        raise HTTPException(
            status_code=500,
            detail=str(error)
        )


    # ======================================
    # CLEANUP
    # ======================================

    finally:

        if repository_path:

            try:

                delete_repository(
                    repository_path
                )

                print(
                    "Repository cleanup completed."
                )

            except Exception as cleanup_error:

                print(
                    "Repository cleanup failed: "
                    f"{cleanup_error}"
                )
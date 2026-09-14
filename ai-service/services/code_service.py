from pathlib import Path


# ==========================================
# SUPPORTED CODE FILE EXTENSIONS
# ==========================================

SUPPORTED_EXTENSIONS = {

    ".py",

    ".java",

    ".js",

    ".jsx",

    ".ts",

    ".tsx",

    ".cpp",

    ".c",

    ".h",

    ".hpp",

    ".cs",

    ".go",

    ".rs",

    ".php",

    ".html",

    ".css",

    ".sql"

}


# ==========================================
# FOLDERS TO IGNORE
# ==========================================

IGNORED_DIRECTORIES = {

    ".git",

    "node_modules",

    "venv",

    ".venv",

    "__pycache__",

    "dist",

    "build",

    "target",

    ".idea",

    ".vscode",

    "coverage"

}


# ==========================================
# LIMITS
# ==========================================

MAX_FILE_SIZE = 500000

MAX_FILES = 100

MAX_CHUNKS_PER_FILE = 50

CHUNK_SIZE = 1500

CHUNK_OVERLAP = 200


# ==========================================
# CHECK IF DIRECTORY SHOULD BE IGNORED
# ==========================================

def should_ignore_path(file_path: Path):

    for part in file_path.parts:

        if part in IGNORED_DIRECTORIES:

            return True

    return False


# ==========================================
# EXTRACT CODE FILES
# ==========================================

def extract_code_files(repo_path):

    repo_path = Path(repo_path)

    code_files = []

    print(
        "Scanning repository for source files..."
    )


    for file_path in repo_path.rglob("*"):

        # ----------------------------------
        # LIMIT NUMBER OF FILES
        # ----------------------------------

        if len(code_files) >= MAX_FILES:

            print(
                f"Maximum file limit reached: {MAX_FILES}"
            )

            break


        # ----------------------------------
        # SKIP DIRECTORIES
        # ----------------------------------

        if file_path.is_dir():

            continue


        # ----------------------------------
        # SKIP IGNORED FOLDERS
        # ----------------------------------

        if should_ignore_path(file_path):

            continue


        # ----------------------------------
        # CHECK FILE EXTENSION
        # ----------------------------------

        if file_path.suffix.lower() not in SUPPORTED_EXTENSIONS:

            continue


        # ----------------------------------
        # CHECK FILE SIZE
        # ----------------------------------

        try:

            file_size = file_path.stat().st_size

            if file_size > MAX_FILE_SIZE:

                print(
                    f"Skipping large file: {file_path.name}"
                )

                continue

        except Exception:

            continue


        # ----------------------------------
        # READ FILE
        # ----------------------------------

        try:

            content = file_path.read_text(
                encoding="utf-8",
                errors="ignore"
            )


            if not content.strip():

                continue


            relative_path = str(
                file_path.relative_to(
                    repo_path
                )
            )


            code_files.append({

                "file_path": relative_path,

                "content": content

            })


        except Exception as error:

            print(
                f"Unable to read file "
                f"{file_path.name}: {error}"
            )


    print(
        f"Code files extracted: {len(code_files)}"
    )


    return code_files


# ==========================================
# CHUNK SINGLE TEXT
# ==========================================

def chunk_text(

        text,

        chunk_size=CHUNK_SIZE,

        overlap=CHUNK_OVERLAP

):

    if not text:

        return []


    chunks = []

    start = 0

    text_length = len(text)


    while start < text_length:

        end = min(

            start + chunk_size,

            text_length

        )


        chunk = text[start:end].strip()


        if chunk:

            chunks.append(
                chunk
            )


        if end >= text_length:

            break


        start = end - overlap


    return chunks


# ==========================================
# CHUNK CODE FILES
# ==========================================

def chunk_code_files(code_files):

    all_chunks = []


    print(
        "Creating code chunks..."
    )


    for file_data in code_files:

        file_path = file_data.get(
            "file_path",
            "unknown"
        )


        content = file_data.get(
            "content",
            ""
        )


        if not content:

            continue


        file_chunks = chunk_text(
            content
        )


        # ----------------------------------
        # LIMIT CHUNKS PER FILE
        # ----------------------------------

        file_chunks = file_chunks[
            :MAX_CHUNKS_PER_FILE
        ]


        for index, chunk in enumerate(
                file_chunks
        ):

            all_chunks.append({

                "content": chunk,

                "metadata": {

                    "file_path": file_path,

                    "chunk_index": index

                }

            })


    print(
        f"Total chunks created: {len(all_chunks)}"
    )


    return all_chunks
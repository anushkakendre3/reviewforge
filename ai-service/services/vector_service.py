from pathlib import Path

import chromadb


# CHROMA DATABASE PATH

BASE_DIR = (
    Path(__file__)
    .resolve()
    .parent
    .parent
)

CHROMA_PATH = (
    BASE_DIR / "chroma_db"
)

CHROMA_PATH.mkdir(
    exist_ok=True



client = chromadb.PersistentClient(
    path=str(CHROMA_PATH)
)


# CLEAN COLLECTION NAME

def clean_collection_name(repo_name):

    name = str(repo_name).lower()

    name = name.replace(
        " ",
        "_"
    )

    name = name.replace(
        "-",
        "_"
    )

    cleaned = []

    for character in name:

        if (
            character.isalnum()
            or character == "_"
        ):

            cleaned.append(
                character
            )


    name = "".join(
        cleaned
    )


    if not name:

        name = "reviewforge_repo"


    # Chroma collection names need
    # a reasonable minimum length

    if len(name) < 3:

        name = (
            "repo_" + name
        )


    return name


# GET OR CREATE COLLECTION

def get_collection(repo_name):

    collection_name = (
        clean_collection_name(
            repo_name
        )
    )


    try:

        return (
            client.get_or_create_collection(
                name=collection_name
            )
        )


    except Exception as error:

        raise RuntimeError(
            "Unable to create vector collection: "
            f"{error}"
        )


# NORMALIZE CHUNK

def normalize_chunk(chunk, index=0):

    if not isinstance(chunk, dict):

        return {
            "content": str(chunk),
            "file_path": "unknown",
            "language": "unknown",
            "chunk_index": index
        }


    # CONTENT

    content = chunk.get(
        "content",
        ""
    )


    # METADATA

    metadata = chunk.get(
        "metadata",
        {}
    )


    if not isinstance(metadata, dict):

        metadata = {}


    # FILE PATH

    file_path = (

        chunk.get("file_path")

        or

        metadata.get("file_path")

        or

        metadata.get("path")

        or

        "unknown"
    )


    # LANGUAGE

    language = (

        chunk.get("language")

        or

        metadata.get("language")

        or

        get_language_from_path(
            str(file_path)
        )
    )


    # CHUNK INDEX

    chunk_index = (

        chunk.get("chunk_index")

        or

        metadata.get("chunk_index")

        or

        index
    )


    return {

        "content":
            str(content),

        "file_path":
            str(file_path),

        "language":
            str(language),

        "chunk_index":
            int(chunk_index)
    }


# GET LANGUAGE FROM FILE PATH

def get_language_from_path(file_path):

    lower = (
        file_path.lower()
    )


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

        if lower.endswith(extension):

            return language


    return "Unknown"


# STORE CODE CHUNKS

def store_chunks(
    repo_name,
    chunks
):

    if not chunks:

        print(
            "No chunks available to store."
        )

        return 0


    collection = get_collection(
        repo_name
    )


    # CLEAR OLD DATA

    try:

        existing = collection.get()

        existing_ids = (
            existing.get(
                "ids",
                []
            )
        )


        if existing_ids:

            collection.delete(
                ids=existing_ids
            )

            print(
                f"Deleted old chunks: "
                f"{len(existing_ids)}"
            )


    except Exception as error:

        print(
            "Old data cleanup warning: "
            f"{error}"
        )


    # PREPARE DATA

    documents = []

    metadatas = []

    ids = []


    for index, raw_chunk in enumerate(
        chunks
    ):

        chunk = normalize_chunk(
            raw_chunk,
            index
        )


        content = chunk.get(
            "content",
            ""
        )


        if not content.strip():

            continue


        documents.append(
            content
        )


        metadatas.append({

            "file_path":
                chunk.get(
                    "file_path",
                    "unknown"
                ),

            "language":
                chunk.get(
                    "language",
                    "Unknown"
                ),

            "chunk_index":
                chunk.get(
                    "chunk_index",
                    index
                )
        })


        ids.append(
            f"{repo_name}_{index}"
        )


    # NO DOCUMENTS

    if not documents:

        print(
            "No valid documents available."
        )

        return 0



    # STORE IN CHROMADB


    try:

        collection.add(

            documents=documents,

            metadatas=metadatas,

            ids=ids

        )


        print(
            f"Chunks stored successfully: "
            f"{len(documents)}"
        )


        return len(documents)


    except Exception as error:

        raise RuntimeError(
            "Unable to store chunks: "
            f"{error}"
        )


# RETRIEVE RELEVANT CHUNKS

def retrieve_relevant_chunks(
    repo_name,
    query,
    top_k=10
):

    collection = get_collection(
        repo_name
    )


    try:

        total_chunks = (
            collection.count()
        )


        if total_chunks == 0:

            print(
                "Vector database is empty."
            )

            return []


        results = collection.query(

            query_texts=[
                query
            ],

            n_results=min(
                top_k,
                total_chunks
            )
        )


        documents = (
            results.get(
                "documents",
                [[]]
            )[0]
        )


        metadatas = (
            results.get(
                "metadatas",
                [[]]
            )[0]
        )


        retrieved_chunks = []


        # NORMALIZE RETRIEVED DATA

        for index, document in enumerate(
            documents
        ):

            metadata = {}


            if index < len(metadatas):

                metadata = (
                    metadatas[index]
                    or {}
                )


            file_path = (
                metadata.get(
                    "file_path",
                    "unknown"
                )
            )


            language = (
                metadata.get(
                    "language"
                )
                or
                get_language_from_path(
                    file_path
                )
            )


            chunk_index = (
                metadata.get(
                    "chunk_index",
                    index
                )
            )


            # Return the exact format
            # expected by review_service.py

            retrieved_chunks.append({

                "content":
                    document,

                "file_path":
                    file_path,

                "language":
                    language,

                "chunk_index":
                    chunk_index
            })


        print(
            f"Relevant chunks retrieved: "
            f"{len(retrieved_chunks)}"
        )


        return retrieved_chunks


    except Exception as error:

        raise RuntimeError(
            "Unable to retrieve chunks: "
            f"{error}"
        )
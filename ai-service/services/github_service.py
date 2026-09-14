import shutil
import uuid
from pathlib import Path

from git import Repo


# ==========================================
# REPOSITORIES FOLDER
# ==========================================

BASE_DIR = Path(__file__).resolve().parent.parent

REPOSITORIES_DIR = BASE_DIR / "repositories"

REPOSITORIES_DIR.mkdir(
    exist_ok=True
)


# ==========================================
# VALIDATE GITHUB URL
# ==========================================

def validate_github_url(repo_url: str):

    if not repo_url:
        raise ValueError(
            "Repository URL is required."
        )

    repo_url = repo_url.strip()

    if not repo_url.startswith(
        "https://github.com/"
    ):
        raise ValueError(
            "Please provide a valid GitHub repository URL."
        )

    return repo_url


# ==========================================
# GET REPOSITORY NAME
# ==========================================

def get_repository_name(repo_url: str):

    repo_url = repo_url.strip()

    repo_url = repo_url.rstrip("/")

    repo_name = repo_url.split("/")[-1]

    if repo_name.endswith(".git"):

        repo_name = repo_name[:-4]

    return repo_name


# ==========================================
# CLONE REPOSITORY
# ==========================================

def clone_repository(repo_url: str):

    repo_url = validate_github_url(
        repo_url
    )

    repo_name = get_repository_name(
        repo_url
    )

    unique_id = str(
        uuid.uuid4()
    )[:8]

    folder_name = (
        repo_url
        .replace(
            "https://github.com/",
            ""
        )
        .replace(
            "/",
            "_"
        )
        .replace(
            ".git",
            ""
        )
        + "_"
        + unique_id
    )

    repo_path = (
        REPOSITORIES_DIR
        / folder_name
    )

    try:

        print(
            f"Cloning repository: {repo_url}"
        )

        Repo.clone_from(
            repo_url,
            repo_path
        )

        print(
            f"Repository cloned successfully: {repo_path}"
        )

        return repo_path

    except Exception as error:

        delete_repository(
            repo_path
        )

        raise RuntimeError(
            f"Unable to clone repository: {str(error)}"
        )


# ==========================================
# DELETE REPOSITORY
# ==========================================

def delete_repository(repo_path):

    if not repo_path:
        return

    try:

        repo_path = Path(
            repo_path
        )

        if repo_path.exists():

            print(
                f"Deleting repository: {repo_path}"
            )

            shutil.rmtree(
                repo_path,
                ignore_errors=True
            )

    except Exception as error:

        print(
            f"Repository cleanup warning: {error}"
        )
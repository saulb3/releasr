# Releasr

Plugin Gradle qui automatise la publication de tes artefacts sur **Maven/Nexus** et **GitHub Packages**, avec une gestion du versioning automatique basée sur les tags Git.

---

## Installation

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { 
            url = uri("https://repo.lylaw.fr/repository/maven-public/") 
        }
    }
}
```

```kotlin
// build.gradle.kts
plugins {
    id("fr.ladder.releasr") version "<latest version>"
}
```

---

## Configuration

```kotlin
releasr {
    url = "https://repo.lylaw.fr/repository/maven-releases/"
    username = "mon-utilisateur"
    password = "mon-mot-de-passe"
}
```

---

## Versioning automatique

Releasr gère automatiquement la version de l'artefact publié selon le contexte :

| Contexte | Format de version                                 | Exemple                       |
|---|---------------------------------------------------|-------------------------------|
| Push sur une branche | `<nextVersion>-<timestamp>-<branch>-<commitHash>` | `1.0.1-1a2b3c4d-main-e5f6g7h` |
| Tag Git (`v1.0.0`) | Version du tag sans le préfixe `v`                | `1.0.0`                       |

La `nextVersion` est calculée automatiquement en incrémentant le patch du dernier tag Git trouvé.

Le `timestamp` est en base `16 [0-9a-f]`.

---

## Publication

```bash
# Publication standard
./gradlew publish

# Forcer une version spécifique
./gradlew publish -Pversion=1.2.0
```

---

## Exemple GitHub Actions

```yaml
name: Publish

on:
  push:
    branches: [main]
    tags: ["v*"]

jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0  # nécessaire pour récupérer les tags Git

      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Publish
        run: ./gradlew publish
        env:
          refType: ${{ startsWith(github.ref, 'refs/tags/') && 'tag' || 'branch' }}
          refName: ${{ github.ref_name }}
          commitHash: ${{ github.sha }}
          repoUser: ${{ secrets.REPO_USER }}
          repoPassword: ${{ secrets.REPO_PASSWORD }}
          githubUser: ${{ github.actor }}
          githubPassword: ${{ secrets.GITHUB_TOKEN }}
          githubRepository: ${{ github.repository }}
```

---

## Variables d'environnement

| Variable | Description |
|---|---|
| `refType` | `branch` ou `tag` — détermine le format de version |
| `refName` | Nom du tag Git (ex: `v1.0.0`) |
| `commitHash` | Hash du commit courant |
| `repoUser` | Identifiant Nexus |
| `repoPassword` | Mot de passe Nexus |
| `githubUser` | Identifiant GitHub |
| `githubPassword` | Token GitHub (`GITHUB_TOKEN`) |
| `githubRepository` | Dépôt GitHub (ex: `org/repo`) |
# Alcoholic — structure du projet

Mod Minecraft Forge **1.19.2** (Gradle multi-module). Racine : `C:\\Users\\djden\\source\\repos\\Alcoholic`.

## Modules (settings.gradle)

- `alcoholic-api`, `domain`, `platform-api` : contrats / domaine / abstraction plateforme
- `application` : logique applicative (compatibilité, processing, grain, industriel, menus, fluides)
- `minecraft-common` : code partagé Minecraft + ressources générées
- `platform-forge-1.19.2` : bootstrap Forge ; classe d’entrée `AlcoholicForgeMod` (`com.djden.alcoholic.forge`)
- Intégrations : `integration-vinery`, `integration-brewery`, `integration-create` (+ `integration-create-forge-1.19.2`), `integration-crossroads` (+ `integration-crossroads-1.19.2`), `integration-test-addon`

## Point d’entrée Forge

`AlcoholicForgeMod` assemble : compatibility, content, processing, grain, industrial, menus, fluids. Méthode `registerIndustrial` pour les machines industrielles.

## Outils locaux

- Serena : projet `Alcoholic`, LSP Java
- GrepAI : `.grepai/` à la racine, embeddings Ollama `nomic-embed-text`

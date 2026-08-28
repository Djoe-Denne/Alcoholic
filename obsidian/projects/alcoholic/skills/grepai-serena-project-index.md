---
title: GrepAI and Serena Project Index
category: skills
tags: [software-architecture, type/procedure, project/alcoholic]
aliases: [grepai MCP, serena activate]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/cf84333f-a53c-46c7-a80e-f7af5a46cdb3/cf84333f-a53c-46c7-a80e-f7af5a46cdb3.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/28/rollout-2026-08-28T09-50-29-01a04759-4c09-7432-8f38-5610849e1278.jsonl"
summary: >-
  GrepAI can index many repos. Cursor's MCP path is what pins a single project. Serena must be switched to Alcoholic when several workspaces run.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-28T19:15:00+02:00
updated: 2026-08-28T19:15:00+02:00
---

# GrepAI and Serena Project Index

GrepAI and Serena were installed for this repo. They do not write features; they answer “where is X?” and rename symbols.

## GrepAI is not one-repo-only

`grepai init` works in every repository. What pinned Alcoholic was the **Cursor MCP command**: Cursor starts GrepAI at IDE boot without the open workspace folder, so a missing path crashed the server. Putting Alcoholic’s path in the **user** `mcp.json` makes tools always search Alcoholic, even in another repo.

Prefer either:

1. One GrepAI per repo via that repo’s `.cursor/mcp.json`, and remove the hardcoded path from the user file.
2. One GrepAI workspace that adds Alcoholic, re-ff8, and others; MCP becomes `grepai mcp-serve --workspace <name>`.

Embeddings: Ollama + `nomic-embed-text`. Exclude generated vine JSON and loot tables or the index fills with datagen. `.grepai/` stays gitignored.

## Serena

Register and `activate_project` on Alcoholic. When several Codex/Cursor chats run, **switch the Serena project** before symbolic search. LSP Java on this repo indexed on the order of 450 files.

## Related

- [[context-mode-project-mcp]]
- [[blockbench-java-block-workflow]]
- [[alcoholic]]

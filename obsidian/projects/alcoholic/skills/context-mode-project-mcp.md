---
title: Context-Mode Project MCP
category: skills
tags: [software-architecture, type/procedure, project/alcoholic]
aliases: [context-mode, CONTEXT_MODE_PROJECT_DIR]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/71c75251-42a8-478c-91d9-e4cace99b0d6/71c75251-42a8-478c-91d9-e4cace99b0d6.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a3c55add-caa0-4ee2-b1fa-7130ea510bd5/a3c55add-caa0-4ee2-b1fa-7130ea510bd5.jsonl"
summary: >-
  Context-mode is one MCP per project. Put CONTEXT_MODE_PROJECT_DIR in this repo's .cursor/mcp.json, never in the user mcp.json.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-27T16:20:00+02:00
updated: 2026-08-28T19:15:00+02:00
---

# Context-Mode Project MCP

`context-mode` is not a user-global server. One `CONTEXT_MODE_PROJECT_DIR` in `~/.cursor/mcp.json` would overwrite every workspace.

## Rule

Create `.cursor/mcp.json` at the **Alcoholic repo root**. Point `CONTEXT_MODE_PROJECT_DIR` at that absolute root. Leave other MCP servers (browser, Blockbench, QMD is CLI-only) in the user file.

Do not reuse another repo path (re-ff8, Reimaginated, and so on). After the first install, restart the AI client so it sees PATH and the project server.

A timeout in one chat does not always mean the server is down: a previous chat can still hold the same project MCP. Check `.cursor/mcp.json` first, then `ctx doctor`. Keep **one** heavy agent chat open when modeling; a loaded thread that already did MCP/wiki work should be closed before a Blockbench machine pass. ^[inferred]

GrepAI and Serena are separate indexes. See [[grepai-serena-project-index]]. `.cursor/mcp.json` stays gitignored (absolute Node and `CONTEXT_MODE_PROJECT_DIR` paths). Skills under `.cursor/skills/` stay versioned.

## Related to modeling

The same chats copied Codex Blockbench skills and the `blockbench` MCP into this Cursor instance. Runtime authoring still follows [[blockbench-java-block-workflow]] and [[blockbench]].

## Related

- [[alcoholic]]
- [[blockbench-java-block-workflow]]
- [[grepai-serena-project-index]]
- [[codex-ajouter-modeles-3d-minecraft]]

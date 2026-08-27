---
title: Blockbench
category: entities
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [BlockbenchMCP, blockbench-mcp]
sources:
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-28-31-01a03f54-b465-7cc3-9e92-2bcfc0c8e291.jsonl"
summary: Desktop Blockbench 5.1.x plus the sosadly MCP plugin on 127.0.0.1:8787, used to author Alcoholic Java block models.
provenance:
  extracted: 0.9
  inferred: 0.08
  ambiguous: 0.02
created: 2026-08-27T13:30:00+02:00
updated: 2026-08-27T13:30:00+02:00
---

# Blockbench

Alcoholic machine models are authored in the desktop Blockbench app (verified 5.1.6), not by hand-editing cube JSON as the first step.

## MCP install (Codex thread)

The Codex conversation **Installer le MCP Blockbench** compiled [sosadly/blockbench-mcp](https://github.com/sosadly/blockbench-mcp) to `C:\Users\djden\.codex\mcp-servers\blockbench-mcp` and registered the `blockbench` server in Codex `config.toml`. The plugin file is `blockbench_mcp.js` under the Blockbench plugins folder. The HTTP bridge is `127.0.0.1:8787`.

The same thread installed `$img2blockbench` (`img2blockbench 0.1.0` plus `trimesh` / `numpy` / `scipy`) as an optional reconstruction skill. Final Alcoholic deliverables stay Java Block/Item.

Cursor later mirrored the same MCP command and copied `blockbench-mcp-modeling` and `img2blockbench` into `~\.cursor\skills\`.

## Related

- [[blockbench-java-block-workflow]]
- [[artisanal-machine-voxel-models]]
- [[codex-ajouter-modeles-3d-minecraft]]

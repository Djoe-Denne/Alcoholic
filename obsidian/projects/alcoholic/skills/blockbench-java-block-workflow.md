---
title: Blockbench Java Block Workflow
category: skills
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [blockbench-mcp-modeling in Alcoholic]
sources:
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-28-31-01a03f54-b465-7cc3-9e92-2bcfc0c8e291.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-48-08-01a03f66-acf1-7931-95db-a9b0b9d8961f.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/fa499a53-567a-462e-aa1e-cc2c5df0a700/fa499a53-567a-462e-aa1e-cc2c5df0a700.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/71c75251-42a8-478c-91d9-e4cace99b0d6/71c75251-42a8-478c-91d9-e4cace99b0d6.jsonl"
summary: How Alcoholic authors one Java machine in Blockbench: one target, grey silhouette, locked oak, 512 paint, user stop, then downsample.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-27T13:30:00+02:00
updated: 2026-08-27T16:20:00+02:00
---

# Blockbench Java Block Workflow

Use this when adding or revising a shipped Alcoholic block model. One machine per task. See [[artisanal-machine-voxel-models]] and [[codex-ajouter-modeles-3d-minecraft]].

## Bridge

1. Open desktop Blockbench. **Tools → Start MCP Server** (port 8787).
2. If the plugin is missing: **File → Plugins → Load Plugin from File** → `%AppData%\Blockbench\plugins\blockbench_mcp.js`, then **Always allow**.
3. Codex/Cursor MCP `blockbench` points at `~/.codex/mcp-servers/blockbench-mcp/dist/index.js` with `BLOCKBENCH_MCP_HOST/PORT`.
4. Skills: `$blockbench-mcp-modeling` is required. `$img2blockbench` is optional for the first silhouette only.

You do not need to quit Blockbench to use the bridge. Restart the AI client after first install so it sees the MCP and PATH.

## Passes

1. Name the single target (`malting_floor`, `primitive_combustion_engine`, or `malt_mill`).
2. Identify the matching region of the reference board.
3. Build a grey silhouette in a **new** Java Block/Item project. Do not edit the other machines' files.
4. Fix proportions, facing (`-Z` unless specified), and coplanar faces.
5. Shelf-pack box UVs, then paint a 512×512 atlas in the [[resource-pack-resolution-chain]] language.
6. **Locked oak** — copy the primitive-engine master tile (warm medium oak, oval knots). Do not invent a darker wood or sample the malting-floor honey posts. Staves/posts rotate the same tile 90°.
7. Screenshot several angles. Stop and wait for approval.
8. Only then downsample from the master and export into `assets/alcoholic/` plus `resourcepacks/`.

Canonical finished examples are the engine, malting floor, and mash tun. The mill is painted but not a finished export example until it follows the same pack + 64 default chain. See the repo skill `alcoholic-java-machine-model`.

Do not ship GeckoLib or Bedrock JSON as the game deliverable.

## Related

- [[blockbench]]
- [[malting-floor-visual]]
- [[primitive-combustion-engine-visual]]
- [[malt-mill-visual]]
- [[artisanal-processing]]

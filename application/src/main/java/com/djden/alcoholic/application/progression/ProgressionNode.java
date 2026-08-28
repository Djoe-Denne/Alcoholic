package com.djden.alcoholic.application.progression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ProgressionNode {
    private final String id;
    private final ProgressionChapter chapter;
    private final ProgressionLine line;
    private final List<String> parents;
    private final String icon;
    private final ProgressionTriggerKind trigger;
    private final List<ProgressionCriterion> criteria;
    private final String questHex;
    private final String taskHex;
    private final ProgressionFrame frame;
    private final boolean hidden;
    private final boolean toast;
    private final boolean announceToChat;
    private final double canvasX;
    private final double canvasY;
    private final int minRequiredDependencies;

    private ProgressionNode(Builder builder) {
        this.id = builder.id;
        this.chapter = builder.chapter;
        this.line = builder.line;
        this.parents = List.copyOf(builder.parents);
        this.icon = builder.icon;
        this.trigger = builder.trigger;
        this.criteria = List.copyOf(builder.criteria);
        this.questHex = builder.questHex;
        this.taskHex = builder.taskHex;
        this.frame = builder.frame;
        this.hidden = builder.hidden;
        this.toast = builder.toast;
        this.announceToChat = builder.announceToChat;
        this.canvasX = builder.canvasX;
        this.canvasY = builder.canvasY;
        this.minRequiredDependencies = builder.minRequiredDependencies;
    }

    public static Builder builder(String id, ProgressionChapter chapter, ProgressionLine line) {
        return new Builder(id, chapter, line);
    }

    public String id() {
        return id;
    }

    public String advancementId() {
        return "alcoholic:" + id;
    }

    public ProgressionChapter chapter() {
        return chapter;
    }

    public ProgressionLine line() {
        return line;
    }

    public List<String> parents() {
        return parents;
    }

    public String icon() {
        return icon;
    }

    public ProgressionTriggerKind trigger() {
        return trigger;
    }

    public List<ProgressionCriterion> criteria() {
        return criteria;
    }

    public String questHex() {
        return questHex;
    }

    public String taskHex() {
        return taskHex;
    }

    public ProgressionFrame frame() {
        return frame;
    }

    public boolean hidden() {
        return hidden;
    }

    public boolean toast() {
        return toast;
    }

    public boolean announceToChat() {
        return announceToChat;
    }

    public double canvasX() {
        return canvasX;
    }

    public double canvasY() {
        return canvasY;
    }

    public int minRequiredDependencies() {
        return minRequiredDependencies;
    }

    public boolean junctionOr() {
        return minRequiredDependencies > 0 && minRequiredDependencies < parents.size();
    }

    public Optional<String> vanillaParentId() {
        if (parents.isEmpty()) {
            return Optional.empty();
        }
        if (line == ProgressionLine.SHARED && parents.size() >= 2) {
            return Optional.of(chapter == ProgressionChapter.ARTISANAL ? "root" : "industrial_root");
        }
        return Optional.of(parents.get(0));
    }

    public static final class Builder {
        private final String id;
        private final ProgressionChapter chapter;
        private final ProgressionLine line;
        private final List<String> parents = new ArrayList<>();
        private final List<ProgressionCriterion> criteria = new ArrayList<>();
        private String icon;
        private ProgressionTriggerKind trigger;
        private String questHex;
        private String taskHex;
        private ProgressionFrame frame = ProgressionFrame.TASK;
        private boolean hidden;
        private boolean toast = true;
        private boolean announceToChat = true;
        private Double canvasX;
        private Double canvasY;
        private int minRequiredDependencies;

        private Builder(String id, ProgressionChapter chapter, ProgressionLine line) {
            this.id = Objects.requireNonNull(id, "id");
            this.chapter = Objects.requireNonNull(chapter, "chapter");
            this.line = Objects.requireNonNull(line, "line");
        }

        public Builder parents(String... parentIds) {
            parents.addAll(List.of(parentIds));
            return this;
        }

        public Builder icon(String itemPath) {
            this.icon = itemPath;
            return this;
        }

        public Builder inventory(ProgressionCriterion... values) {
            return criteria(ProgressionTriggerKind.INVENTORY, values);
        }

        public Builder harvest(ProgressionCriterion... values) {
            return criteria(ProgressionTriggerKind.HARVEST, values);
        }

        public Builder process(ProgressionCriterion... values) {
            return criteria(ProgressionTriggerKind.PROCESS, values);
        }

        public Builder formed(ProgressionCriterion... values) {
            return criteria(ProgressionTriggerKind.FORMED, values);
        }

        public Builder hex(String quest, String task) {
            this.questHex = quest;
            this.taskHex = task;
            return this;
        }

        public Builder frame(ProgressionFrame value) {
            this.frame = value;
            return this;
        }

        public Builder hidden() {
            this.hidden = true;
            return this;
        }

        public Builder noAnnounce() {
            this.announceToChat = false;
            return this;
        }

        public Builder canvas(double x, double y) {
            this.canvasX = x;
            this.canvasY = y;
            return this;
        }

        public Builder minRequiredDependencies(int value) {
            this.minRequiredDependencies = value;
            return this;
        }

        public ProgressionNode build() {
            Objects.requireNonNull(icon, "icon");
            Objects.requireNonNull(trigger, "trigger");
            Objects.requireNonNull(questHex, "questHex");
            Objects.requireNonNull(taskHex, "taskHex");
            Objects.requireNonNull(canvasX, "canvasX");
            Objects.requireNonNull(canvasY, "canvasY");
            if (criteria.isEmpty()) {
                throw new IllegalStateException("Node " + id + " has no criteria");
            }
            if (line == ProgressionLine.WINE && canvasX >= 0) {
                throw new IllegalStateException("Wine node " + id + " must have x < 0");
            }
            if (line == ProgressionLine.SHARED && canvasX != 0) {
                throw new IllegalStateException("Shared node " + id + " must have x = 0");
            }
            if (line == ProgressionLine.BEER && canvasX <= 0) {
                throw new IllegalStateException("Beer node " + id + " must have x > 0");
            }
            return new ProgressionNode(this);
        }

        private Builder criteria(ProgressionTriggerKind kind, ProgressionCriterion... values) {
            this.trigger = kind;
            criteria.addAll(List.of(values));
            return this;
        }
    }
}

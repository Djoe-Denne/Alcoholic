package com.djden.alcoholic.domain.process;

/**
 * Non-linear oxygen exposure during aging. Too little is reductive; the
 * mid band is micro-oxygenation; too much is oxidative damage.
 */
public final class OxygenCurve {
    public static final double REDUCTIVE_MAX = 0.06;
    public static final double SWEET_MAX = 0.35;

    private OxygenCurve() {
    }

    public record Evaluation(double defects, double complexityBonus) {
        public Evaluation {
            defects = clamp01(defects);
            complexityBonus = clamp01(complexityBonus);
        }
    }

    public static Evaluation evaluate(double oxidation, double agingTime) {
        if (!Double.isFinite(agingTime) || agingTime <= 0.0) {
            return new Evaluation(0.0, 0.0);
        }
        double exposure = finiteNonNegative(oxidation);
        if (exposure < REDUCTIVE_MAX) {
            return new Evaluation((REDUCTIVE_MAX - exposure) / REDUCTIVE_MAX * 0.25, 0.0);
        }
        if (exposure <= SWEET_MAX) {
            double span = SWEET_MAX - REDUCTIVE_MAX;
            double t = (exposure - REDUCTIVE_MAX) / span;
            double bonus = 0.18 * (1.0 - Math.abs(t - 0.45));
            return new Evaluation(0.0, bonus);
        }
        return new Evaluation(Math.min(0.45, (exposure - SWEET_MAX) / 0.65 * 0.45), 0.0);
    }

    public static double woodSweetSpot(double wood) {
        double value = finiteNonNegative(wood);
        if (value <= 0.0) {
            return 0.0;
        }
        if (value <= 0.55) {
            return clamp01(value);
        }
        return clamp01(Math.max(0.0, 0.55 - (value - 0.55)));
    }

    private static double finiteNonNegative(double value) {
        return Double.isFinite(value) && value > 0.0 ? value : 0.0;
    }

    private static double clamp01(double value) {
        if (!Double.isFinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}

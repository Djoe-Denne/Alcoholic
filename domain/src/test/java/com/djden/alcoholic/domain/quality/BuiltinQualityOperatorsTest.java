package com.djden.alcoholic.domain.quality;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.liquid.LiquidBatchView;
import com.djden.alcoholic.api.process.ExecutorModifiers;
import com.djden.alcoholic.api.quality.QualityEvaluationContext;
import com.djden.alcoholic.api.quality.QualityOperator;
import com.djden.alcoholic.api.quality.QualitySignal;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.domain.process.QualityProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinQualityOperatorsTest {
    @Test
    void readReturnsPropertyOrZero() {
        @SuppressWarnings("unchecked")
        QualityOperator<BuiltinQualityOperators.ReadConfig> read =
                (QualityOperator<BuiltinQualityOperators.ReadConfig>) BuiltinQualityOperators.map()
                        .get(BuiltinQualityOperators.READ);
        LiquidBatch batch = LiquidBatch.of(
                ResourceId.parse("alcoholic:red_grape_must"),
                1000,
                PropertyBag.empty().with(QualityProfile.SUGAR, 0.42)
        );
        QualityEvaluationContext context = new Context(batch);
        assertEquals(
                0.42,
                read.evaluate(context, new BuiltinQualityOperators.ReadConfig(QualityProfile.SUGAR)).get("value", 0.0),
                1e-9
        );
        assertEquals(
                0.0,
                read.evaluate(context, new BuiltinQualityOperators.ReadConfig(QualityProfile.COLOR)).get("value", 1.0),
                1e-9
        );
    }

    private record Context(LiquidBatchView batch) implements QualityEvaluationContext {
        @Override
        public ExecutorModifiers modifiers() {
            return ExecutorModifiers.identity();
        }

        @Override
        public QualitySignal inputSignal(String port) {
            return QualitySignal.empty();
        }
    }
}

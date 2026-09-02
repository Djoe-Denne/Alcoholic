package com.djden.alcoholic.domain.vessel;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BarrelHistoryTest {
    private static final ResourceId FIRST = ResourceId.parse("test:first");
    private static final ResourceId SECOND = ResourceId.parse("test:second");
    private static final ResourceId ETHANOL = ResourceId.parse("alcoholic:ethanol");

    @Test
    void snapshotKeepsOnlyTransferableAxes() {
        PropertyBag snapshot = CaskImprint.snapshot(
                PropertyBag.empty()
                        .with(CaskImprint.ACIDITY, 0.40)
                        .with(ETHANOL, 0.43)
                        .with(CaskImprint.SUGAR, 0.0),
                CaskImprint.defaultProperties()
        );
        assertEquals(0.40, snapshot.get(CaskImprint.ACIDITY).map(Number.class::cast).orElse(0.0).doubleValue(), 1e-9);
        assertTrue(snapshot.get(ETHANOL).isEmpty());
        assertTrue(snapshot.get(CaskImprint.SUGAR).isEmpty());
    }

    @Test
    void secondEmptyingFadesPreviousImprint() {
        BarrelHistory first = BarrelHistory.empty().recordEmptying(
                FIRST,
                PropertyBag.empty().with(CaskImprint.ACIDITY, 0.40)
        );
        assertEquals(0.40, first.caskImprint().get(CaskImprint.ACIDITY), 1e-9);
        BarrelHistory second = first.recordEmptying(
                SECOND,
                PropertyBag.empty().with(CaskImprint.ACIDITY, 0.20)
        );
        assertEquals(2, second.usageCount());
        assertEquals((0.40 * CaskImprint.FADE + 0.20) * 0.5, second.caskImprint().get(CaskImprint.ACIDITY), 1e-9);
    }

    @Test
    void newAxisOnLaterEmptyingIsNotHalved() {
        BarrelHistory first = BarrelHistory.empty().recordEmptying(
                FIRST,
                PropertyBag.empty().with(CaskImprint.ACIDITY, 0.40)
        );
        BarrelHistory second = first.recordEmptying(
                SECOND,
                PropertyBag.empty().with(CaskImprint.TANNIN, 0.30)
        );
        assertEquals(0.40 * CaskImprint.FADE, second.caskImprint().get(CaskImprint.ACIDITY), 1e-9);
        assertEquals(0.30, second.caskImprint().get(CaskImprint.TANNIN), 1e-9);
    }

    @Test
    void emptyIncomingKeepsFadedImprint() {
        BarrelHistory first = BarrelHistory.empty().recordEmptying(
                FIRST,
                PropertyBag.empty().with(CaskImprint.ACIDITY, 0.40)
        );
        BarrelHistory second = first.recordEmptying(SECOND, PropertyBag.empty());
        assertEquals(0.40 * CaskImprint.FADE, second.caskImprint().get(CaskImprint.ACIDITY), 1e-9);
    }

    @Test
    void volumeWeightScalesASip() {
        assertEquals(0.0, CaskImprint.volumeWeight(0, 4000), 1e-9);
        assertEquals(0.025, CaskImprint.volumeWeight(100, 4000), 1e-9);
        assertEquals(1.0, CaskImprint.volumeWeight(4000, 4000), 1e-9);
        assertEquals(1.0, CaskImprint.volumeWeight(8000, 4000), 1e-9);
    }

    @Test
    void previousContentsStayCappedAtFour() {
        BarrelHistory history = BarrelHistory.empty();
        for (int index = 0; index < 6; index++) {
            history = history.recordEmptying(ResourceId.parse("test:fill_" + index), PropertyBag.empty());
        }
        assertEquals(6, history.usageCount());
        assertEquals(BarrelHistory.MAX_PREVIOUS, history.previousContents().size());
        assertEquals(ResourceId.parse("test:fill_2"), history.previousContents().get(0));
    }
}

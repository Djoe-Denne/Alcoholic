package com.djden.alcoholic.application.quality;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.beverage.BeverageIdentity;
import com.djden.alcoholic.api.process.ExecutorModifiers;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.application.beverage.FixtureCatalogs;
import com.djden.alcoholic.application.beverage.LoadBeverageCatalogUseCase;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.domain.liquid.BatchProvenance;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.domain.process.QualityProfile;
import com.djden.alcoholic.domain.quality.QualityEvaluator;
import com.djden.alcoholic.domain.quality.QualityGraphIds;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualityProfilesTest {
    private static final ResourceId MUST = ResourceId.parse("alcoholic:red_grape_must");

    @Test
    void ethanolDoesNotRaiseTheSummary() {
        AlcoholicApi api = api();
        BeverageCatalog catalog = catalog(api);
        QualityProfile a = QualityProfiles.derive(batch(0.70, 0.10, 0.0, 0.0), catalog, api);
        QualityProfile b = QualityProfiles.derive(batch(0.70, 0.10, 0.55, 0.0), catalog, api);
        assertEquals(a.summary(), b.summary(), 1e-9);
        assertEquals(a.complexity(), b.complexity(), 1e-9);
    }

    @Test
    void industrialCapClampsComplexityAndSummary() {
        AlcoholicApi api = api();
        BeverageCatalog catalog = catalog(api);
        LiquidBatch excellent = batch(0.90, 0.20, 0.12, 0.0);
        QualityProfile artisanal = QualityProfiles.derive(excellent, catalog, api, ExecutorModifiers.artisanal());
        QualityProfile industrial = QualityProfiles.derive(excellent, catalog, api, ExecutorModifiers.industrialVat());
        assertTrue(artisanal.complexity() > 0.70);
        assertEquals(0.55, industrial.complexity(), 1e-9);
        assertTrue(industrial.summary() <= 0.55 + 1e-9);
        assertTrue(industrial.defects() >= ExecutorModifiers.industrialVat().purityFloor() - 1e-9);
    }

    @Test
    void stampedCapSurvivesIdentityDerivation() {
        AlcoholicApi api = api();
        BeverageCatalog catalog = catalog(api);
        LiquidBatch stamped = QualityProfile.stampCap(
                batch(0.90, 0.20, 0.12, 0.0),
                ExecutorModifiers.industrialPress()
        );
        QualityProfile profile = QualityProfiles.derive(stamped, catalog, api);
        assertTrue(profile.complexity() <= 0.55 + 1e-9);
        assertTrue(profile.summary() <= 0.55 + 1e-9);
        assertTrue(profile.defects() >= ExecutorModifiers.industrialPress().purityFloor() - 1e-9);
    }

    @Test
    void oxygenCurveShapesDefectsAndComplexity() {
        AlcoholicApi api = api();
        BeverageCatalog catalog = catalog(api);
        QualityProfile low = QualityProfiles.derive(aged(0.45, 0.01, 8_000), catalog, api);
        QualityProfile mid = QualityProfiles.derive(aged(0.45, 0.18, 8_000), catalog, api);
        QualityProfile high = QualityProfiles.derive(aged(0.45, 0.80, 8_000), catalog, api);
        assertTrue(low.defects() > 0.0);
        assertTrue(mid.complexity() > low.complexity());
        assertTrue(high.defects() > mid.defects());
    }

    @Test
    void fermentationStressRaisesDefects() {
        AlcoholicApi api = api();
        BeverageCatalog catalog = catalog(api);
        LiquidBatch clean = batch(0.60, 0.10, 0.10, 0.0);
        LiquidBatch stressed = new LiquidBatch(
                clean.identity(),
                clean.baseLiquid(),
                clean.volume(),
                clean.properties(),
                BatchProvenance.empty().withSummaries(0.40, 0.0, 0.0, 0.0)
        );
        assertTrue(QualityProfiles.derive(stressed, catalog, api).defects()
                > QualityProfiles.derive(clean, catalog, api).defects());
        assertTrue(QualityProfiles.derive(stressed, catalog, api).purity()
                < QualityProfiles.derive(clean, catalog, api).purity());
    }

    @Test
    void fermentationStressPropertyIsReadWithoutProvenance() {
        AlcoholicApi api = api();
        BeverageCatalog catalog = catalog(api);
        LiquidBatch clean = batch(0.60, 0.10, 0.10, 0.0);
        LiquidBatch stressed = clean.withProperty(QualityProfile.FERMENTATION_STRESS, 0.40);
        assertTrue(QualityProfiles.derive(stressed, catalog, api).defects()
                > QualityProfiles.derive(clean, catalog, api).defects());
    }

    @Test
    void tanninRaisesComplexityOnGenericAndWine() {
        AlcoholicApi api = api();
        BeverageCatalog catalog = catalog(api);
        LiquidBatch plain = batch(0.70, 0.45, 0.12, 0.20);
        LiquidBatch tannic = plain.withProperty(QualityProfile.TANNIN, 0.50);
        assertTrue(QualityProfiles.derive(tannic, catalog, api).complexity()
                > QualityProfiles.derive(plain, catalog, api).complexity());
        assertTrue(
                evaluateGraph(api, QualityGraphIds.WINE, tannic).complexity()
                        > evaluateGraph(api, QualityGraphIds.WINE, plain).complexity()
        );
    }

    @Test
    void wineBalanceDoesNotDiluteMissingHopAxes() {
        AlcoholicApi api = api();
        BeverageCatalog catalog = catalog(api);
        LiquidBatch wine = batch(0.70, 0.45, 0.12, 0.20);
        QualityProfile generic = QualityProfiles.derive(wine, catalog, api);
        QualityProfile wineGraph = evaluateGraph(api, QualityGraphIds.WINE, wine);
        double sugarAcid = 1.0 - (Math.abs(0.50 - 0.35) + Math.abs(0.45 - 0.45)) / 2.0;
        assertEquals(sugarAcid, generic.balance(), 1e-9);
        assertEquals(generic.summary(), wineGraph.summary(), 1e-9);
        assertEquals(sugarAcid, wineGraph.balance(), 1e-9);
    }

    @Test
    void beerGraphDoesNotDiluteWineBalance() {
        AlcoholicApi api = api();
        LiquidBatch wine = batch(0.70, 0.45, 0.12, 0.20);
        QualityProfile beerGraph = evaluateGraph(api, QualityGraphIds.BEER, wine);
        QualityProfile wineGraph = evaluateGraph(api, QualityGraphIds.WINE, wine);
        assertEquals(0.5, beerGraph.balance(), 1e-9);
        assertTrue(wineGraph.balance() > beerGraph.balance());
    }

    @Test
    void beerGraphIgnoresTanninComplexity() {
        AlcoholicApi api = api();
        LiquidBatch plain = batch(0.70, 0.45, 0.12, 0.20);
        LiquidBatch tannic = plain.withProperty(QualityProfile.TANNIN, 0.50);
        assertEquals(
                evaluateGraph(api, QualityGraphIds.BEER, plain).complexity(),
                evaluateGraph(api, QualityGraphIds.BEER, tannic).complexity(),
                1e-9
        );
        assertTrue(
                evaluateGraph(api, QualityGraphIds.WINE, tannic).complexity()
                        > evaluateGraph(api, QualityGraphIds.WINE, plain).complexity()
        );
    }

    @Test
    void craftCapClampsBelowArtisanal() {
        AlcoholicApi api = api();
        BeverageCatalog catalog = catalog(api);
        LiquidBatch excellent = batch(0.90, 0.20, 0.12, 0.0);
        QualityProfile craft = QualityProfiles.derive(excellent, catalog, api, ExecutorModifiers.craftVat());
        QualityProfile artisanal = QualityProfiles.derive(excellent, catalog, api, ExecutorModifiers.artisanal());
        assertTrue(craft.complexity() <= 0.82 + 1e-9);
        assertTrue(artisanal.complexity() > craft.complexity());
    }

    @Test
    void spiritGraphEvaluates() {
        AlcoholicApi api = api();
        QualityProfile profile = evaluateGraph(api, QualityGraphIds.SPIRIT, batch(0.70, 0.20, 0.40, 0.30));
        assertTrue(profile.summary() > 0.0);
        assertEquals(0.5, profile.balance(), 1e-9);
    }

    @Test
    void deriveUsesWineBeerAndSpiritFromCatalog() {
        AlcoholicApi api = api();
        BeverageCatalog catalog = catalog(api);
        LiquidBatch wine = batch(0.70, 0.45, 0.12, 0.20)
                .withIdentity(new BeverageIdentity(ResourceId.parse("testpack:red_wine")));
        LiquidBatch beer = batch(0.70, 0.45, 0.12, 0.20)
                .withIdentity(new BeverageIdentity(ResourceId.parse("testpack:beer")));
        LiquidBatch whisky = batch(0.70, 0.20, 0.40, 0.30)
                .withIdentity(new BeverageIdentity(ResourceId.parse("testpack:whisky")));
        assertEquals(QualityGraphIds.WINE, QualityResolver.resolve(wine, catalog).id());
        assertEquals(QualityGraphIds.BEER, QualityResolver.resolve(beer, catalog).id());
        assertEquals(QualityGraphIds.SPIRIT, QualityResolver.resolve(whisky, catalog).id());
        assertTrue(QualityProfiles.derive(wine, catalog, api).summary() >= 0.0);
        assertTrue(QualityProfiles.derive(beer, catalog, api).summary() >= 0.0);
        assertTrue(QualityProfiles.derive(whisky, catalog, api).summary() >= 0.0);
    }

    @Test
    void deriveRejectsEmptyOperatorRegistry() {
        AlcoholicApi api = AlcoholicApi.create();
        BeverageCatalog catalog = BeverageCatalog.empty();
        assertThrows(
                IllegalStateException.class,
                () -> QualityProfiles.derive(batch(0.70, 0.10, 0.0, 0.0), catalog, api)
        );
    }

    @Test
    void unknownIdentityFallsBackToGeneric() {
        AlcoholicApi api = api();
        BeverageCatalog catalog = catalog(api);
        LiquidBatch unknown = LiquidBatch.of(ResourceId.parse("testpack:unknown"), 1000, PropertyBag.empty());
        assertEquals(QualityGraphIds.GENERIC, QualityResolver.resolve(unknown, catalog).id());
        assertTrue(QualityProfiles.derive(unknown, catalog, api).summary() >= 0.0);
    }

    private static QualityProfile evaluateGraph(AlcoholicApi api, ResourceId graphId, LiquidBatch batch) {
        return QualityEvaluator.evaluate(
                ShippedQualityGraphs.graph(api, graphId),
                api.qualityOperators(),
                batch,
                ExecutorModifiers.identity()
        );
    }

    private static BeverageCatalog catalog(AlcoholicApi api) {
        return new LoadBeverageCatalogUseCase().load(
                FixtureCatalogs.ingredients(),
                FixtureCatalogs.processes(),
                Map.of(
                        ResourceId.parse("testpack:beverages/red_wine"),
                        FixtureCatalogs.read("data/testpack/alcoholic/beverages/red_wine.json"),
                        ResourceId.parse("testpack:beverages/beer"),
                        FixtureCatalogs.read("data/testpack/alcoholic/beverages/beer.json"),
                        ResourceId.parse("testpack:beverages/whisky"),
                        FixtureCatalogs.read("data/testpack/alcoholic/beverages/whisky.json")
                ),
                FixtureCatalogs.liquids(),
                FixtureCatalogs.quality(),
                api
        );
    }

    private static AlcoholicApi api() {
        AlcoholicApi api = AlcoholicApi.create();
        BuiltinRegistrations.install(api);
        return api;
    }

    private static LiquidBatch aged(double harvest, double oxidation, double agingTicks) {
        LiquidBatch clean = batch(harvest, 0.20, 0.12, 0.40);
        return new LiquidBatch(
                clean.identity(),
                clean.baseLiquid(),
                clean.volume(),
                clean.properties().with(QualityProfile.OXIDATION, oxidation),
                BatchProvenance.empty().withSummaries(0.0, agingTicks, 0.35, oxidation)
        );
    }

    private static LiquidBatch batch(double harvest, double acid, double ethanol, double maturity) {
        return LiquidBatch.of(
                MUST,
                1000,
                PropertyBag.empty()
                        .with(QualityProfile.HARVEST_QUALITY, harvest)
                        .with(QualityProfile.ACIDITY, acid)
                        .with(QualityProfile.SUGAR, 0.50)
                        .with(QualityProfile.ETHANOL, ethanol)
                        .with(QualityProfile.MATURITY, maturity)
        );
    }
}

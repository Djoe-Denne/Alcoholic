package com.djden.alcoholic.application.quality;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.beverage.BeverageIdentity;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.application.beverage.FixtureCatalogs;
import com.djden.alcoholic.application.beverage.LoadBeverageCatalogUseCase;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.domain.quality.QualityGraphIds;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QualityResolverTest {
    @Test
    void usesBeverageQualityThenGeneric() {
        BeverageCatalog catalog = catalog();
        LiquidBatch wine = LiquidBatch.of(ResourceId.parse("testpack:red_wine"), 1000, PropertyBag.empty())
                .withIdentity(new BeverageIdentity(ResourceId.parse("testpack:red_wine")));
        assertEquals(QualityGraphIds.WINE, QualityResolver.resolve(wine, catalog).id());

        LiquidBatch beer = LiquidBatch.of(ResourceId.parse("testpack:beer"), 1000, PropertyBag.empty())
                .withIdentity(new BeverageIdentity(ResourceId.parse("testpack:beer")));
        assertEquals(QualityGraphIds.BEER, QualityResolver.resolve(beer, catalog).id());

        LiquidBatch unknown = LiquidBatch.of(ResourceId.parse("testpack:unknown"), 1000, PropertyBag.empty());
        assertEquals(QualityGraphIds.GENERIC, QualityResolver.resolve(unknown, catalog).id());
    }

    @Test
    void omittedQualityUsesGeneric() {
        BeverageCatalog catalog = catalog();
        LiquidBatch cider = LiquidBatch.of(ResourceId.parse("testpack:apple_must"), 1000, PropertyBag.empty())
                .withIdentity(new BeverageIdentity(ResourceId.parse("testpack:cider")));
        assertEquals(QualityGraphIds.GENERIC, QualityResolver.resolve(cider, catalog).id());
    }

    @Test
    void droppedIdentityKeepsBaseLiquidGraph() {
        BeverageCatalog catalog = catalog();
        LiquidBatch blended = LiquidBatch.of(ResourceId.parse("testpack:red_wine"), 1000, PropertyBag.empty());
        assertEquals(QualityGraphIds.WINE, QualityResolver.resolve(blended, catalog).id());
    }

    @Test
    void baseLiquidGraphIdSelectsGraph() {
        BeverageCatalog catalog = catalog();
        LiquidBatch named = LiquidBatch.of(QualityGraphIds.BEER, 1000, PropertyBag.empty());
        assertEquals(QualityGraphIds.BEER, QualityResolver.resolve(named, catalog).id());
    }

    @Test
    void missingGenericThrows() {
        LiquidBatch unknown = LiquidBatch.of(ResourceId.parse("testpack:unknown"), 1000, PropertyBag.empty());
        assertThrows(IllegalStateException.class, () -> QualityResolver.resolve(unknown, BeverageCatalog.empty()));
    }

    private static BeverageCatalog catalog() {
        AlcoholicApi api = AlcoholicApi.create();
        BuiltinRegistrations.install(api);
        return new LoadBeverageCatalogUseCase().load(
                FixtureCatalogs.ingredients(),
                FixtureCatalogs.processes(),
                Map.of(
                        ResourceId.parse("testpack:beverages/red_wine"),
                        FixtureCatalogs.read("data/testpack/alcoholic/beverages/red_wine.json"),
                        ResourceId.parse("testpack:beverages/beer"),
                        FixtureCatalogs.read("data/testpack/alcoholic/beverages/beer.json"),
                        ResourceId.parse("testpack:beverages/cider"),
                        FixtureCatalogs.read("data/testpack/alcoholic/beverages/cider.json")
                ),
                FixtureCatalogs.liquids(),
                FixtureCatalogs.quality(),
                api
        );
    }
}

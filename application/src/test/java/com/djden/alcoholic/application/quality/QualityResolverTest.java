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
import com.djden.alcoholic.domain.quality.BuiltinQualityGraphs;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QualityResolverTest {
    @Test
    void usesBeverageQualityThenGeneric() {
        AlcoholicApi api = AlcoholicApi.create();
        BuiltinRegistrations.install(api);
        BeverageCatalog catalog = new LoadBeverageCatalogUseCase().load(
                FixtureCatalogs.ingredients(),
                FixtureCatalogs.processes(),
                Map.of(
                        ResourceId.parse("testpack:beverages/red_wine"),
                        FixtureCatalogs.read("data/testpack/alcoholic/beverages/red_wine.json"),
                        ResourceId.parse("testpack:beverages/beer"),
                        FixtureCatalogs.read("data/testpack/alcoholic/beverages/beer.json")
                ),
                FixtureCatalogs.liquids(),
                api
        );
        LiquidBatch wine = LiquidBatch.of(ResourceId.parse("testpack:red_wine"), 1000, PropertyBag.empty())
                .withIdentity(new BeverageIdentity(ResourceId.parse("testpack:red_wine")));
        assertEquals(BuiltinQualityGraphs.WINE, QualityResolver.resolve(wine, catalog).id());

        LiquidBatch beer = LiquidBatch.of(ResourceId.parse("testpack:beer"), 1000, PropertyBag.empty())
                .withIdentity(new BeverageIdentity(ResourceId.parse("testpack:beer")));
        assertEquals(BuiltinQualityGraphs.BEER, QualityResolver.resolve(beer, catalog).id());

        LiquidBatch unknown = LiquidBatch.of(ResourceId.parse("testpack:unknown"), 1000, PropertyBag.empty());
        assertEquals(BuiltinQualityGraphs.GENERIC, QualityResolver.resolve(unknown, catalog).id());
    }
}

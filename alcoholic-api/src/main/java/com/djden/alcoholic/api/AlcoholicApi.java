package com.djden.alcoholic.api;

import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.event.CatalogReloadListener;
import com.djden.alcoholic.api.process.ProcessHandler;
import com.djden.alcoholic.api.process.ProcessType;
import com.djden.alcoholic.api.property.LiquidProperty;
import com.djden.alcoholic.api.quality.QualityOperator;
import com.djden.alcoholic.api.registry.MutableRegistry;
import com.djden.alcoholic.api.registry.ProcessRegistrar;
import com.djden.alcoholic.api.registry.PropertyRegistrar;
import com.djden.alcoholic.api.registry.QualityOperatorRegistrar;
import com.djden.alcoholic.api.registry.RegistrationException;
import com.djden.alcoholic.api.registry.RegistryView;
import com.djden.alcoholic.api.registry.VesselRegistrar;
import com.djden.alcoholic.api.vessel.VesselProfileView;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Versioned public façade. Java addons register process types, properties,
 * and quality operators here during bootstrap. The instance is frozen before
 * the first datapack reload.
 */
@PublicApi
public final class AlcoholicApi {
    private static final AtomicReference<AlcoholicApi> SHARED = new AtomicReference<>(new AlcoholicApi());

    private final MutableRegistry<ProcessType<?>> processes = new MutableRegistry<>(ProcessType::id);
    private final MutableRegistry<LiquidProperty<?>> properties = new MutableRegistry<>(LiquidProperty::id);
    private final MutableRegistry<VesselProfileView> vessels = new MutableRegistry<>(VesselProfileView::id);
    private final MutableRegistry<QualityOperator<?>> qualityOperators = new MutableRegistry<>(QualityOperator::id);
    private final ProcessRegistrar processRegistrar = new ProcessRegistrarView();
    private final PropertyRegistrar propertyRegistrar = new PropertyRegistrarView();
    private final VesselRegistrar vesselRegistrar = new VesselRegistrarView();
    private final QualityOperatorRegistrar qualityRegistrar = new QualityOperatorRegistrarView();
    private final List<CatalogReloadListener> reloadListeners = new CopyOnWriteArrayList<>();

    public static AlcoholicApi shared() {
        return SHARED.get();
    }

    public static AlcoholicApi create() {
        return new AlcoholicApi();
    }

    /**
     * Test-only replacement of the process-wide shared instance.
     */
    public static void resetSharedForTests() {
        SHARED.set(new AlcoholicApi());
    }

    public ProcessRegistrar processes() {
        return processRegistrar;
    }

    public PropertyRegistrar properties() {
        return propertyRegistrar;
    }

    public VesselRegistrar vessels() {
        return vesselRegistrar;
    }

    public QualityOperatorRegistrar qualityOperators() {
        return qualityRegistrar;
    }

    public RegistryView<ProcessType<?>> processView() {
        return processes;
    }

    public RegistryView<LiquidProperty<?>> propertyView() {
        return properties;
    }

    public RegistryView<VesselProfileView> vesselView() {
        return vessels;
    }

    public RegistryView<QualityOperator<?>> qualityView() {
        return qualityOperators;
    }

    public void addReloadListener(CatalogReloadListener listener) {
        reloadListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void notifyCatalogReloaded() {
        for (CatalogReloadListener listener : reloadListeners) {
            listener.onCatalogReloaded();
        }
    }

    public void freeze() {
        processes.freeze();
        properties.freeze();
        vessels.freeze();
        qualityOperators.freeze();
    }

    public boolean isFrozen() {
        return processes.isFrozen()
                && properties.isFrozen()
                && vessels.isFrozen()
                && qualityOperators.isFrozen();
    }

    private final class ProcessRegistrarView implements ProcessRegistrar {
        @Override
        public <C> ProcessType<C> register(
                ResourceId id,
                DataCodec<C> configCodec,
                ProcessHandler<C> handler
        ) {
            return register(ProcessType.of(id, configCodec, handler));
        }

        @Override
        public <C> ProcessType<C> register(ProcessType<C> type) {
            processes.register(type);
            return type;
        }

        @Override
        public Optional<ProcessType<?>> get(ResourceId id) {
            return processes.get(id);
        }

        @Override
        public Set<ResourceId> ids() {
            return processes.ids();
        }

        @Override
        public Collection<ProcessType<?>> values() {
            return processes.values();
        }
    }

    private final class PropertyRegistrarView implements PropertyRegistrar {
        @Override
        public <T> LiquidProperty<T> register(ResourceId id, Class<T> valueType, DataCodec<T> codec) {
            return register(LiquidProperty.of(id, valueType, codec));
        }

        @Override
        public <T> LiquidProperty<T> register(LiquidProperty<T> property) {
            properties.register(property);
            return property;
        }

        @Override
        public Optional<LiquidProperty<?>> get(ResourceId id) {
            return properties.get(id);
        }

        @Override
        public Set<ResourceId> ids() {
            return properties.ids();
        }

        @Override
        public Collection<LiquidProperty<?>> values() {
            return properties.values();
        }
    }

    private final class QualityOperatorRegistrarView implements QualityOperatorRegistrar {
        @Override
        public <C> QualityOperator<C> register(QualityOperator<C> operator) {
            qualityOperators.register(operator);
            return operator;
        }

        @Override
        public Optional<QualityOperator<?>> get(ResourceId id) {
            return qualityOperators.get(id);
        }

        @Override
        public Set<ResourceId> ids() {
            return qualityOperators.ids();
        }

        @Override
        public Collection<QualityOperator<?>> values() {
            return qualityOperators.values();
        }
    }

    private final class VesselRegistrarView implements VesselRegistrar {
        @Override
        public VesselProfileView register(VesselProfileView profile) {
            return vessels.register(profile);
        }

        @Override
        public Optional<VesselProfileView> get(ResourceId id) {
            return vessels.get(id);
        }

        @Override
        public Set<ResourceId> ids() {
            return vessels.ids();
        }

        @Override
        public Collection<VesselProfileView> values() {
            return vessels.values();
        }
    }

    public static RegistrationException alreadyFrozen() {
        return new RegistrationException("Alcoholic API is frozen");
    }
}

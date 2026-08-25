package com.djden.alcoholic.forge.registry;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.platform.api.registry.RegistryPort;
import com.djden.alcoholic.platform.api.registry.RegistryRef;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;
import java.util.function.Supplier;

public final class ForgeRegistryPort<T> implements RegistryPort<T> {
    private final String namespace;
    private final DeferredRegister<T> deferredRegister;

    public ForgeRegistryPort(IForgeRegistry<T> registry, String namespace) {
        this.namespace = namespace;
        this.deferredRegister = DeferredRegister.create(registry, namespace);
    }

    public void attach(IEventBus modEventBus) {
        deferredRegister.register(modEventBus);
    }

    @Override
    public RegistryRef<T> register(ResourceId id, Supplier<? extends T> factory) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(factory, "factory");
        if (!namespace.equals(id.namespace())) {
            throw new IllegalArgumentException(
                    "Registry port for " + namespace + " cannot register " + id
            );
        }

        RegistryObject<T> registered = deferredRegister.register(id.path(), factory::get);
        return new ForgeRegistryRef<>(id, registered);
    }

    private record ForgeRegistryRef<T>(
            ResourceId id,
            RegistryObject<T> delegate
    ) implements RegistryRef<T> {
        @Override
        public T get() {
            return delegate.get();
        }
    }
}

package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.vessel.EnvironmentProfileView;
import com.djden.alcoholic.api.vessel.VesselProfileView;

import java.util.Optional;

@PublicApi
public interface ProcessContext {
    default double temperatureCelsius() {
        return environment().map(EnvironmentProfileView::temperature).orElse(20.0);
    }

    default double deltaTicks() {
        return 1.0;
    }

    default boolean yeastPresent() {
        return false;
    }

    default Optional<VesselProfileView> vessel() {
        return Optional.empty();
    }

    default Optional<EnvironmentProfileView> environment() {
        return Optional.empty();
    }

    default long gameTime() {
        return 0L;
    }

    default ExecutorModifiers executorModifiers() {
        return ExecutorModifiers.identity();
    }

    static ProcessContext empty() {
        return EmptyProcessContext.INSTANCE;
    }

    static ProcessContext of(double temperatureCelsius, double deltaTicks, boolean yeastPresent) {
        return of(
                temperatureCelsius,
                deltaTicks,
                yeastPresent,
                Optional.empty(),
                Optional.empty(),
                0L,
                ExecutorModifiers.identity()
        );
    }

    static ProcessContext of(
            double temperatureCelsius,
            double deltaTicks,
            boolean yeastPresent,
            Optional<VesselProfileView> vessel,
            Optional<EnvironmentProfileView> environment,
            long gameTime
    ) {
        return of(
                temperatureCelsius,
                deltaTicks,
                yeastPresent,
                vessel,
                environment,
                gameTime,
                ExecutorModifiers.identity()
        );
    }

    static ProcessContext of(
            double temperatureCelsius,
            double deltaTicks,
            boolean yeastPresent,
            Optional<VesselProfileView> vessel,
            Optional<EnvironmentProfileView> environment,
            long gameTime,
            ExecutorModifiers executorModifiers
    ) {
        return new SimpleProcessContext(
                temperatureCelsius,
                deltaTicks,
                yeastPresent,
                vessel,
                environment,
                gameTime,
                executorModifiers == null ? ExecutorModifiers.identity() : executorModifiers
        );
    }

    enum EmptyProcessContext implements ProcessContext {
        INSTANCE
    }

    record SimpleProcessContext(
            double temperatureCelsius,
            double deltaTicks,
            boolean yeastPresent,
            Optional<VesselProfileView> vessel,
            Optional<EnvironmentProfileView> environment,
            long gameTime,
            ExecutorModifiers executorModifiers
    ) implements ProcessContext {
        public SimpleProcessContext {
            vessel = vessel == null ? Optional.empty() : vessel;
            environment = environment == null ? Optional.empty() : environment;
            executorModifiers = executorModifiers == null
                    ? ExecutorModifiers.identity()
                    : executorModifiers;
        }

        @Override
        public double temperatureCelsius() {
            return temperatureCelsius;
        }

        @Override
        public ExecutorModifiers executorModifiers() {
            return executorModifiers;
        }
    }
}

package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.application.process.AgingConfig;
import com.djden.alcoholic.application.process.BoilConfig;
import com.djden.alcoholic.application.process.ConditionConfig;
import com.djden.alcoholic.application.process.FermentConfig;
import com.djden.alcoholic.application.process.MaltConfig;
import com.djden.alcoholic.application.process.MashConfig;
import com.djden.alcoholic.application.process.ProcessRecipeResolver;
import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;
import com.djden.alcoholic.domain.multiblock.MachineKind;
import com.djden.alcoholic.domain.multiblock.MultiblockDefinition;
import com.djden.alcoholic.domain.process.ProcessDefinition;
import com.djden.alcoholic.domain.process.TemperatureProfile;
import com.djden.alcoholic.domain.vessel.EnvironmentProfile;
import com.djden.alcoholic.minecraft.menu.MachineAccess;
import com.djden.alcoholic.minecraft.menu.MachineContainerData;
import com.djden.alcoholic.minecraft.menu.MachineProcessStage;
import com.djden.alcoholic.minecraft.process.ItemLots;
import com.djden.alcoholic.minecraft.process.MinecraftSelectorMatcher;
import com.djden.alcoholic.minecraft.process.ProcessRuntime;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

record ControllerTelemetry(
        int ambientTemperatureDeci,
        int heatTemperatureDeci,
        int humidityPermille,
        int preferredTemperatureMinDeci,
        int preferredTemperatureMaxDeci,
        int operatingTemperatureMinDeci,
        int operatingTemperatureMaxDeci,
        int requiredHumidityPermille,
        int requiredHeatTemperatureDeci,
        int driveSpeedDeci,
        int driveMinimumDeci,
        int driveMaximumDeci,
        int driveCapacityDeci,
        int requiredDriveCapacityDeci,
        int processStageCode,
        int processDefinitionIndex,
        int flags
) {
    private static final ResourceId DEFAULT_MALT = ResourceId.parse("alcoholic:malt_pale");

    static ControllerTelemetry capture(
            MultiblockControllerBlockEntity machine,
            MultiblockDefinition definition
    ) {
        EnvironmentProfile environment = machine.environment();
        int unavailable = MachineAccess.DATA_UNAVAILABLE;
        int preferredMin = unavailable;
        int preferredMax = unavailable;
        int operatingMin = unavailable;
        int operatingMax = unavailable;
        int requiredHumidity = unavailable;
        int requiredHeat = unavailable;
        int driveSpeed = unavailable;
        int driveMin = unavailable;
        int driveMax = unavailable;
        int driveCapacity = unavailable;
        int requiredDriveCapacity = unavailable;
        int flags = MachineContainerData.FLAG_CONTROLLER_TELEMETRY;

        ProcessRuntime runtime = ProcessRuntime.shared();
        Optional<ProcessDefinition> active = activeProcess(machine, definition, runtime);
        Optional<TemperatureProfile> temperature = active.flatMap(process -> temperature(definition.kind(), process));
        if (temperature.isPresent()) {
            TemperatureProfile profile = temperature.orElseThrow();
            preferredMin = deci(profile.preferred().min());
            preferredMax = deci(profile.preferred().max());
            operatingMin = deci(profile.operating().min());
            operatingMax = deci(profile.operating().max());
            if (definition.kind() == MachineKind.MASH || definition.kind() == MachineKind.BOIL) {
                flags |= MachineContainerData.FLAG_PROCESS_USES_HEAT;
            } else {
                flags |= MachineContainerData.FLAG_PROCESS_USES_AMBIENT;
            }
        }

        if (definition.kind() == MachineKind.MALT) {
            requiredHumidity = active.map(process -> MaltConfig.CODEC.decode(process.config(), "telemetry").moistureRequirement())
                    .map(ControllerTelemetry::permille)
                    .orElse(unavailable);
            requiredHeat = deci(IndustrialProcessTicks.MALT_KILN_MIN_CELSIUS);
            flags |= MachineContainerData.FLAG_HUMIDITY_REQUIRED;
            flags |= MachineContainerData.FLAG_KILN_HEAT_REQUIRED;
        }

        if (definition.kinetic().required()) {
            MechanicalDriveState drive = machine.drive(definition);
            driveSpeed = deci(drive.speed());
            driveMin = deci(definition.kinetic().minRpm());
            driveMax = deci(definition.kinetic().maxRpm());
            driveCapacity = deci(drive.availableCapacity());
            requiredDriveCapacity = deci(definition.kinetic().requiredCapacity());
            flags |= MachineContainerData.FLAG_DRIVE_REQUIRED;
        }

        String stage = machine.processStage();
        if ((stage == null || stage.isBlank()) && active.isPresent()) {
            if (definition.kind() == MachineKind.PRESS) {
                stage = "pressing";
            } else if (definition.kind() == MachineKind.FERMENT) {
                stage = "fermenting";
            }
        }

        return new ControllerTelemetry(
                deci(environment.temperature()),
                deci(machine.heatCelsius()),
                permille(environment.humidity()),
                preferredMin,
                preferredMax,
                operatingMin,
                operatingMax,
                requiredHumidity,
                requiredHeat,
                driveSpeed,
                driveMin,
                driveMax,
                driveCapacity,
                requiredDriveCapacity,
                MachineProcessStage.encode(stage),
                processIndex(definition, active, runtime),
                flags
        );
    }

    private static Optional<ProcessDefinition> activeProcess(
            MultiblockControllerBlockEntity machine,
            MultiblockDefinition definition,
            ProcessRuntime runtime
    ) {
        Optional<ResourceId> fromJob = processFromJob(machine.processJob());
        if (fromJob.isPresent()) {
            Optional<ProcessDefinition> process = runtime.beverages().catalog().process(fromJob.orElseThrow());
            if (process.isPresent()) {
                return process;
            }
        }
        if (definition.processType().isEmpty()) {
            return Optional.empty();
        }

        Optional<ResourceId> selected = Optional.ofNullable(machine.boundDefinition());
        if (definition.kind() == MachineKind.MALT && selected.isEmpty()) {
            selected = Optional.of(DEFAULT_MALT);
        }
        ItemStack input = MaltBatchInventory.firstInput(
                machine.inventory(),
                machine.layout().inputSlotCount()
        );
        Optional<ResourceId> solid = input.isEmpty() ? Optional.empty() : Optional.of(ItemLots.id(input));
        Optional<ResourceId> liquid = machine.tank().contents().flatMap(batch -> batch.baseLiquid());
        try {
            Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                    runtime.beverages().catalog(),
                    runtime.beverages().api(),
                    definition.processType().orElseThrow(),
                    MinecraftSelectorMatcher.create(runtime.beverages()),
                    solid,
                    liquid,
                    selected
            );
            Optional<ProcessDefinition> resolved = invocation.flatMap(value -> {
                try {
                    return runtime.beverages().catalog().process(ResourceId.parse(value.nodeId()));
                } catch (RuntimeException ignored) {
                    return Optional.empty();
                }
            });
            return resolved.isPresent()
                    ? resolved
                    : selected.flatMap(id -> runtime.beverages().catalog().process(id));
        } catch (RuntimeException ignored) {
            return selected.flatMap(id -> runtime.beverages().catalog().process(id));
        }
    }

    private static Optional<ResourceId> processFromJob(String job) {
        if (job == null || job.isBlank()) {
            return Optional.empty();
        }
        int separator = job.indexOf('|');
        String id = separator < 0 ? job : job.substring(0, separator);
        try {
            return Optional.of(ResourceId.parse(id));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static Optional<TemperatureProfile> temperature(MachineKind kind, ProcessDefinition process) {
        try {
            return switch (kind) {
                case FERMENT -> Optional.of(FermentConfig.CODEC.decode(process.config(), "telemetry").temperature());
                case MALT -> Optional.of(MaltConfig.CODEC.decode(process.config(), "telemetry").temperature());
                case MASH -> Optional.of(MashConfig.CODEC.decode(process.config(), "telemetry").temperature());
                case BOIL -> Optional.of(BoilConfig.CODEC.decode(process.config(), "telemetry").temperature());
                case CONDITION -> Optional.of(ConditionConfig.CODEC.decode(process.config(), "telemetry").temperature());
                case AGE -> Optional.of(AgingConfig.CODEC.decode(process.config(), "telemetry").temperature());
                default -> Optional.empty();
            };
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static int processIndex(
            MultiblockDefinition definition,
            Optional<ProcessDefinition> active,
            ProcessRuntime runtime
    ) {
        if (definition.processType().isEmpty() || active.isEmpty()) {
            return MachineAccess.DATA_UNAVAILABLE;
        }
        List<ResourceId> ids = runtime.beverages().catalog().processes().values().stream()
                .filter(process -> definition.processType().orElseThrow().equals(process.processType()))
                .map(ProcessDefinition::id)
                .sorted(Comparator.comparing(ResourceId::toString))
                .toList();
        return ids.indexOf(active.orElseThrow().id());
    }

    private static int deci(double value) {
        return Double.isFinite(value) ? (int) Math.round(value * 10.0) : MachineAccess.DATA_UNAVAILABLE;
    }

    private static int permille(double value) {
        return Double.isFinite(value) ? (int) Math.round(value * 1000.0) : MachineAccess.DATA_UNAVAILABLE;
    }
}

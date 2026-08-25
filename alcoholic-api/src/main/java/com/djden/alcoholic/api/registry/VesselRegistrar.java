package com.djden.alcoholic.api.registry;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.vessel.VesselProfileView;

@PublicApi
public interface VesselRegistrar extends RegistryView<VesselProfileView> {
    VesselProfileView register(VesselProfileView profile);
}

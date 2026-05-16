package dk.magnusjensen.tender;

import dk.magnusjensen.tender.api.EconomyProvider;
import dk.magnusjensen.tender.api.Tender;
import dk.magnusjensen.tender.api.TenderAPI;

import java.util.List;
import java.util.Optional;

public class TenderAPIImpl implements TenderAPI {

    private final ProviderRegistry registry;

    public TenderAPIImpl() {
        this.registry = new ProviderRegistry();

        this.registry.discoverProviders();
    }

    @Override
    public int apiVersion() {
        return Tender.API_VERSION;
    }

    @Override
    public Optional<EconomyProvider> activeProvider() {
        return Optional.ofNullable(registry.getActiveProvider());
    }

    @Override
    public List<EconomyProvider> availableProviders() {
        return registry.getProviders();
    }
}

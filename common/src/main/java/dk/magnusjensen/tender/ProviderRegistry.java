package dk.magnusjensen.tender;

import dk.magnusjensen.tender.api.EconomyProvider;
import dk.magnusjensen.tender.api.EconomyProviderFactory;
import dk.magnusjensen.tender.config.ServerConfig;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ProviderRegistry {

    private final List<EconomyProvider> discoveredProviders;
    private EconomyProvider activeProvider;

    public ProviderRegistry() {
        this.discoveredProviders = new ArrayList<>();
    }

    private void selectActiveProvider() {
        if (activeProvider != null) {
            Constants.LOG.debug("Active provider already selected: {}", activeProvider.id());
            // No-op
            return;
        }

        if (!Objects.equals(ServerConfig.OVERRIDE_PROVIDER_PRIORITY, "")) {
            Optional<EconomyProvider> overrideProvider = discoveredProviders.stream()
                .filter(provider -> provider.id().equals(ServerConfig.OVERRIDE_PROVIDER_PRIORITY))
                .findFirst();

            if (overrideProvider.isPresent()) {
                activeProvider = overrideProvider.get();
                Constants.LOG.info("Selected active economy provider via config override: {}", activeProvider.id());
                return;
            } else {
                Constants.LOG.warn("Config override for provider priority set to '{}', but no matching provider was found. Falling back to default selection.", ServerConfig.OVERRIDE_PROVIDER_PRIORITY);
            }
        }

        this.activeProvider = discoveredProviders.stream()
            .max(Comparator.comparingInt(EconomyProvider::priority))
            .orElseThrow();

        Constants.LOG.info("Selected active economy provider: {}", activeProvider.id());
    }

    private void register(EconomyProvider provider) {
        this.discoveredProviders.add(provider);
    }

    public void discoverProviders() {
        ServiceLoader.load(EconomyProvider.class, getClass().getClassLoader())
            .stream()
            .forEach(provider -> {
                try {
                    EconomyProvider instance = provider.get();
                    register(instance);
                    Constants.LOG.info("Discovered economy provider: {}", instance.id());
                } catch (ServiceConfigurationError e) {
                    Constants.LOG.error("Failed to load provider {}", provider.type(), e);
                }
            });

        // Load factories and check if available
        ServiceLoader.load(EconomyProviderFactory.class, getClass().getClassLoader())
            .stream()
            .forEach(factory -> {
                try {
                    var instance = factory.get();
                    if (instance.isAvailable()) {
                        EconomyProvider provider = instance.create();
                        if (!Objects.equals(provider.id(), instance.id())) {
                            Constants.LOG.error("Provider ID mismatch for factory {}: expected {}, got {}. Skipping provider.", instance.id(), instance.id(), provider.id());
                            return;
                        }
                        register(provider);
                        Constants.LOG.info("Discovered economy provider via factory: {}", provider.id());
                    } else {
                        Constants.LOG.info("Economy provider factory {} is not available in the current environment, skipping.", instance.id());
                    }
                } catch (ServiceConfigurationError e) {
                    Constants.LOG.error("Failed to load provider factory {}", factory.type(), e);
                }
            });

        // After discovering all providers, select the active one.
        selectActiveProvider();
    }

    public List<EconomyProvider> getProviders() {
        return Collections.unmodifiableList(discoveredProviders);
    }

    public @Nullable EconomyProvider getActiveProvider() {
        return activeProvider;
    }
}

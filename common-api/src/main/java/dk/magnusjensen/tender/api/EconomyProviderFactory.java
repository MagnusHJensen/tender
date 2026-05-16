package dk.magnusjensen.tender.api;

/**
 * Factory for creating instances of {@link EconomyProvider}.
 * You should implement this interface, when the EconomyProvider you are building is not part of the actual mod.
 * If the provider is part of the mod, you should implement {@link EconomyProvider} directly, and make it discoverable via service loader.
 * If not, then implement this, and make this factory discoverable via service loader. This allows the provider to be loaded lazily, and only if it is actually available in the current environment.
 */
public interface EconomyProviderFactory {
    /**
     * The unique ID of this provider.
     * @return the unique ID of this provider
     */
    String id();

    /**
     * Whether this provider is available in the current environment.
     * @return true if this provider is available, false otherwise
     */
    boolean isAvailable();

    /**
     * Creates a new instance of this provider. This method will only be called if {@link #isAvailable()} returns true.
     * @return a new instance of this provider
     */
    EconomyProvider create();
}

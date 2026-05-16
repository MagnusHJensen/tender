package dk.magnusjensen.tender.api;

import java.util.List;
import java.util.Optional;

public interface TenderAPI {
    int apiVersion();
    Optional<EconomyProvider> activeProvider();
    List<EconomyProvider> availableProviders();
}

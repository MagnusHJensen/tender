package dk.magnusjensen.tender.api;

public class Tender {
    public static final int API_VERSION = 1;
    private static TenderAPI API;

    public static TenderAPI api() {
        if (API == null) {
            throw new IllegalStateException("Tender API not initialized");
        }
        return API;
    }

    public static void _init(TenderAPI api) {
        if (Tender.API != null) {
            throw new IllegalStateException("Tender API already initialized");
        }
        Tender.API = api;
    }
}

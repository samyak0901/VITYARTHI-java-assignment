package model;

/**
 * Enumeration representing the stock and expiry status of a medicine item.
 */
public enum StockStatus {
    IN_STOCK("In Stock", "#10B981", "#ECFDF5"),         // Green
    LOW_STOCK("Low Stock", "#F59E0B", "#FFFBEB"),       // Amber/Yellow
    EXPIRING_SOON("Expiring Soon", "#F97316", "#FFF7ED"), // Orange
    EXPIRED("Expired", "#EF4444", "#FEF2F2");           // Red

    private final String label;
    private final String hexColor;
    private final String bgHexColor;

    StockStatus(String label, String hexColor, String bgHexColor) {
        this.label = label;
        this.hexColor = hexColor;
        this.bgHexColor = bgHexColor;
    }

    public String getLabel() {
        return label;
    }

    public String getHexColor() {
        return hexColor;
    }

    public String getBgHexColor() {
        return bgHexColor;
    }

    @Override
    public String toString() {
        return label;
    }
}

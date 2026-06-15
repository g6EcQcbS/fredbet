package de.fred4jupiter.fredbet.excel;

public record PointCountResult(String username, String displayName, Integer points, Long numberOfPointsCount) {

    /**
     * Convenience constructor used by the JPQL constructor expression, which only
     * selects the username (no display name). The display name can be enriched
     * afterwards via {@link #withDisplayName(String)}.
     */
    public PointCountResult(String username, Integer points, Long numberOfPointsCount) {
        this(username, null, points, numberOfPointsCount);
    }

    /**
     * Returns a copy of this result with the given display name set.
     */
    public PointCountResult withDisplayName(String displayName) {
        return new PointCountResult(username, displayName, points, numberOfPointsCount);
    }

    /**
     * Returns the display name when present, otherwise falls back to the username.
     */
    public String displayNameOrUsername() {
        return displayName != null && !displayName.isBlank() ? displayName : username;
    }
}

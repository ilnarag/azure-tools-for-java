package com.microsoft.azure.toolkit.intellij.java.sdk.models;

import java.time.OffsetDateTime;

public final class MavenArtifactDetails {
    private final String groupId;
    private final String artifactId;
    private String version;
    private OffsetDateTime lastUpdated;

    public MavenArtifactDetails(final String groupId, final String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(final OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}


// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.toolkit.intellij.java.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * A model class to represent a deprecated dependency and a list of suggested replacements.
 */
public class DeprecatedDependency {
    @JsonProperty
    private final String deprecatedDependency;
    @JsonProperty
    private final List<String> suggestedReplacements;

    /**
     * Creates an instance of {@link DeprecatedDependency}.
     * @param deprecatedDependency The group, artifact and version string.
     * @param suggestedReplacements The suggested replacement for the outdated dependency.
     */
    public DeprecatedDependency(final String deprecatedDependency, final List<String> suggestedReplacements) {
        this.deprecatedDependency = deprecatedDependency;
        this.suggestedReplacements = suggestedReplacements;
    }

    /**
     * Returns the group, artifact and version string for the outdated dependency.
     * @return The group, artifact and version string.
     */
    public String getDeprecatedDependency() {
        return deprecatedDependency;
    }

    /**
     * Returns the list of suggested replacements for the outdated dependency.
     * @return The list of suggested replacements for the outdated dependency.
     */
    public List<String> getSuggestedReplacements() {
        return suggestedReplacements;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DeprecatedDependency that = (DeprecatedDependency) o;
        return deprecatedDependency.equals(that.deprecatedDependency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deprecatedDependency);
    }
}

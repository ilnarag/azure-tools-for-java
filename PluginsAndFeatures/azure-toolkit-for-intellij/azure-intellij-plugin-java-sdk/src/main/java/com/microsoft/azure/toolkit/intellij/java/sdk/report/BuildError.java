// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.toolkit.intellij.java.sdk.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Class to represent the build errors.
 */
public class BuildError {
    @JsonIgnore
    private String message;
    @JsonProperty
    private BuildErrorCode code;
    @JsonProperty
    private BuildErrorLevel level;
    @JsonProperty
    private List<String> additionalDetails;

    /**
     * Creates an instance of {@link BuildError}.
     * @param message The error message.
     * @param code The {@link BuildErrorCode error code}.
     * @param level The {@link BuildErrorLevel error level}.
     */
    public BuildError(String message, BuildErrorCode code, BuildErrorLevel level) {
        this.message = message;
        this.code = code;
        this.level = level;
    }

    /**
     * Creates an instance of {@link BuildError}.
     * @param message The error message.
     * @param code The {@link BuildErrorCode error code}.
     * @param level The {@link BuildErrorLevel error level}.
     * @param additionalDetails Additional details about the error.
     */
    public BuildError(String message, BuildErrorCode code, BuildErrorLevel level, List<String> additionalDetails) {
        this.message = message;
        this.code = code;
        this.level = level;
        this.additionalDetails = additionalDetails;
    }

    /**
     * Returns the error message.
     * @return the error message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the error code.
     * @return the error code.
     */
    public BuildErrorCode getCode() {
        return code;
    }

    /**
     * Returns the error severity level.
     * @return the error severity level.
     */
    public BuildErrorLevel getLevel() {
        return level;
    }

    /**
     * Returns additional details about this error.
     * @return additional details about this error.
     */
    public List<String> getAdditionalDetails() {
        return additionalDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final BuildError that = (BuildError) o;
        return Objects.equals(message, that.message)
                && Objects.equals(code, that.code)
                && level == that.level
                && Objects.equals(additionalDetails, that.additionalDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, code, level, additionalDetails);
    }
}

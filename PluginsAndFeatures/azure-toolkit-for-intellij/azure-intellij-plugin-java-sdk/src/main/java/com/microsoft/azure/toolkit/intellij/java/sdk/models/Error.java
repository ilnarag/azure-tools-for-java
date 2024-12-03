// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.toolkit.intellij.java.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Class to represent the project errors.
 */
public class Error {
    @JsonIgnore
    private String message;
    @JsonProperty
    private ErrorCode code;
    @JsonProperty
    private ErrorLevel level;
    @JsonProperty
    private List<String> additionalDetails;

    /**
     * Creates an instance of {@link Error}.
     * @param message The error message.
     * @param code The {@link ErrorCode error code}.
     * @param level The {@link ErrorLevel error level}.
     */
    public Error(String message, ErrorCode code, ErrorLevel level) {
        this.message = message;
        this.code = code;
        this.level = level;
    }

    /**
     * Creates an instance of {@link Error}.
     * @param message The error message.
     * @param code The {@link ErrorCode error code}.
     * @param level The {@link ErrorLevel error level}.
     * @param additionalDetails Additional details about the error.
     */
    public Error(String message, ErrorCode code, ErrorLevel level, List<String> additionalDetails) {
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
    public ErrorCode getCode() {
        return code;
    }

    /**
     * Returns the error severity level.
     * @return the error severity level.
     */
    public ErrorLevel getLevel() {
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
        final Error that = (Error) o;
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

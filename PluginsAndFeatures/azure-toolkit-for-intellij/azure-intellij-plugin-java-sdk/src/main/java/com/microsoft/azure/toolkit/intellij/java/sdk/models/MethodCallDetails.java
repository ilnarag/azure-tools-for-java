// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.toolkit.intellij.java.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * A model class that represents the details of a method call.
 */
public class MethodCallDetails {
    @JsonProperty
    private String methodName;

    @JsonProperty
    private int callFrequency;

    /**
     * Returns the name of the method.
     *
     * @return The name of the method.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the name of the method.
     *
     * @param methodName The name of the method.
     * @return The updated {@link MethodCallDetails} object.
     */
    public MethodCallDetails setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    /**
     * Returns the number of times the method was called.
     *
     * @return The number of times the method was called.
     */
    public int getCallFrequency() {
        return callFrequency;
    }

    /**
     * Sets the number of times the method was called.
     *
     * @param callFrequency The number of times the method was called.
     * @return The updated {@link MethodCallDetails} object.
     */
    public MethodCallDetails setCallFrequency(int callFrequency) {
        this.callFrequency = callFrequency;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MethodCallDetails that = (MethodCallDetails) o;
        return callFrequency == that.callFrequency && Objects.equals(methodName, that.methodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodName, callFrequency);
    }
}

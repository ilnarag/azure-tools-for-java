// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.toolkit.intellij.java.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The project report that contains detailed information about the project including error messages, recommended
 * changes and Azure SDK usage.
 */
public class MavenProjectReport {
    @JsonProperty
    private String groupId;
    @JsonProperty
    private String artifactId;
    @JsonProperty
    private String version;
    @JsonProperty
    private String bomVersion;
    @JsonProperty
    private List<String> azureDependencies;
    @JsonProperty
    private List<DeprecatedDependency> deprecatedDependencies;
    @JsonProperty
    private List<DeprecatedDependency> deprecatedTransitiveDependencies;
    @JsonProperty
    private List<MethodCallDetails> serviceMethodCalls;
    @JsonProperty
    private List<MethodCallDetails> betaMethodCalls;
    @JsonProperty
    private List<Error> errors;

    /**
     * Returns the list of build errors.
     *
     * @return The list of build errors.
     */
    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public void addErrors(List<Error> errors) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.addAll(errors);
    }

    /**
     * Adds a build error to the report.
     *
     * @param error The build error to add.
     */
    public void addError(Error error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }

    /**
     * Returns the version of the BOM used to build the project.
     *
     * @return The version of the BOM used to build the project.
     */
    public String getBomVersion() {
        return this.bomVersion;
    }

    /**
     * The list of Azure dependencies used by the project.
     *
     * @return The list of Azure dependencies used by the project.
     */
    public List<String> getAzureDependencies() {
        return this.azureDependencies;
    }

    /**
     * Sets the list of service method calls.
     *
     * @param serviceMethodCalls the serviceMethodCalls to set
     */
    public void setServiceMethodCalls(List<MethodCallDetails> serviceMethodCalls) {
        this.serviceMethodCalls = serviceMethodCalls;
    }

    public void addServiceMethodCall(MethodCallDetails serviceMethodCall) {
        if (this.serviceMethodCalls == null) {
            this.serviceMethodCalls = new ArrayList<>();
        }
        this.serviceMethodCalls.add(serviceMethodCall);
    }

    public void addAllServiceMethodCalls(List<MethodCallDetails> serviceMethodCalls) {
        if (this.serviceMethodCalls == null) {
            this.serviceMethodCalls = new ArrayList<>();
        }
        this.serviceMethodCalls.addAll(serviceMethodCalls);
    }

    /**
     * Sets the list of beta method calls.
     *
     * @param betaMethodCalls the betaMethodCalls to set.
     */
    public void setBetaMethodCalls(List<MethodCallDetails> betaMethodCalls) {
        this.betaMethodCalls = betaMethodCalls;
    }

    public void addBetaMethodCall(MethodCallDetails betaMethodCall) {
        if (this.betaMethodCalls == null) {
            this.betaMethodCalls = new ArrayList<>();
        }
        this.betaMethodCalls.add(betaMethodCall);
    }

    public void addAllBetaMethodCalls(List<MethodCallDetails> betaMethodCalls) {
        if (this.betaMethodCalls == null) {
            this.betaMethodCalls = new ArrayList<>();
        }
        this.betaMethodCalls.addAll(betaMethodCalls);
    }

    /**
     * Sets the list of outdated direct dependencies.
     *
     * @param deprecatedDependencies the outdatedDirectDependencies to set
     */
    public void setDeprecatedDependencies(List<DeprecatedDependency> deprecatedDependencies) {
        this.deprecatedDependencies = deprecatedDependencies;
    }

    public void addDeprecatedDependency(DeprecatedDependency deprecatedDependency) {
        if (this.deprecatedDependencies == null) {
            this.deprecatedDependencies = new ArrayList<>();
        }
        this.deprecatedDependencies.add(deprecatedDependency);
    }

    public void addAllDeprecatedDependencies(List<DeprecatedDependency> deprecatedDependencies) {
        if (this.deprecatedDependencies == null) {
            this.deprecatedDependencies = new ArrayList<>();
        }
        this.deprecatedDependencies.addAll(deprecatedDependencies);
    }

    /**
     * Returns the outdated direct dependencies.
     *
     * @return the outdated direct dependencies.
     */
    public List<DeprecatedDependency> getDeprecatedDependencies() {
        return deprecatedDependencies;
    }

    /**
     * Sets the list of outdated transitive dependencies.
     *
     * @param deprecatedTransitiveDependencies the outdated transitive dependencies to set
     */
    public void setDeprecatedTransitiveDependencies(List<DeprecatedDependency> deprecatedTransitiveDependencies) {
        this.deprecatedTransitiveDependencies = deprecatedTransitiveDependencies;
    }

    /**
     * Returns the outdated transitive dependencies.
     *
     * @return the outdated transitive dependencies.
     */
    public List<DeprecatedDependency> getDeprecatedTransitiveDependencies() {
        return deprecatedTransitiveDependencies;
    }

    /**
     * Sets the version of the BOM used to build the project.
     *
     * @param bomVersion the bomVersion to set
     */
    public void setBomVersion(String bomVersion) {
        this.bomVersion = bomVersion;
    }

    /**
     * Sets the list of Azure dependencies used by the project.
     *
     * @param azureDependencies the azureDependencies to set
     */
    public void setAzureDependencies(List<String> azureDependencies) {
        this.azureDependencies = azureDependencies;
    }

    public void addAzureDependency(String azureDependency) {
        if (this.azureDependencies == null) {
            this.azureDependencies = new ArrayList<>();
        }
        this.azureDependencies.add(azureDependency);
    }

    public void addAllAzureDependencies(List<String> azureDependencies) {
        if (this.azureDependencies == null) {
            this.azureDependencies = new ArrayList<>();
        }
        this.azureDependencies.addAll(azureDependencies);
    }

    /**
     * Returns the list of service method calls.
     *
     * @return the serviceMethodCalls
     */
    public List<MethodCallDetails> getServiceMethodCalls() {
        return this.serviceMethodCalls;
    }

    /**
     * Returns the list of beta method calls.
     *
     * @return the betaMethodCalls
     */
    public List<MethodCallDetails> getBetaMethodCalls() {
        return this.betaMethodCalls;
    }

    /**
     * Sets the groupId of the project.
     *
     * @param groupId the groupId to set
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Returns the groupId of the project.
     *
     * @return the groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the artifactId of the project.
     *
     * @param artifactId the artifactId to set
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Returns the artifactId of the project.
     *
     * @return the artifactId
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Sets the version of the project.
     *
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the version of the project.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final MavenProjectReport that = (MavenProjectReport) o;
        return Objects.equals(groupId, that.groupId)
                && Objects.equals(artifactId, that.artifactId)
                && Objects.equals(version, that.version)
                && Objects.equals(bomVersion, that.bomVersion)
                && Objects.equals(azureDependencies, that.azureDependencies)
                && Objects.equals(deprecatedDependencies, that.deprecatedDependencies)
                && Objects.equals(deprecatedTransitiveDependencies, that.deprecatedTransitiveDependencies)
                && Objects.equals(serviceMethodCalls, that.serviceMethodCalls)
                && Objects.equals(betaMethodCalls, that.betaMethodCalls)
                && Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version, bomVersion, azureDependencies, deprecatedDependencies,
                deprecatedTransitiveDependencies, serviceMethodCalls, betaMethodCalls, errors);
    }

}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.toolkit.intellij.java.sdk.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Enumeration of all project error codes.
 */
public class ErrorCode extends ExpandableStringEnum<ErrorCode> {

    public static final ErrorCode BOM_NOT_USED = fromString("BomNotUsed");
    public static final ErrorCode BOM_VERSION_OVERRIDDEN = fromString("BomVersionOverridden");
    public static final ErrorCode BETA_API_USED = fromString("BetaApiUsed");
    public static final ErrorCode OUTDATED_DEPENDENCY = fromString("OutdatedDependency");
    public static final ErrorCode BETA_DEPENDENCY_USED = fromString("BetaDependencyUsed");
    public static final ErrorCode DEPRECATED_DEPENDENCY_USED = fromString("DeprecatedDependencyUsed");
    public static final ErrorCode DEPRECATED_TRANSITIVE_DEPENDENCY = fromString("DeprecatedTransitiveDependency");

    /**
     * Creates or finds a {@link ErrorCode} from its string representation.
     *
     * @param name the name of the error code.
     * @return the {@link ErrorCode} associated with the name.
     */
    public static ErrorCode fromString(String name) {
        return fromString(name, ErrorCode.class);
    }

    /**
     * Returns all the known list of {@link ErrorCode}s.
     * @return known build error codes.
     */
    public static Collection<ErrorCode> values() {
        return values(ErrorCode.class);
    }
}

package de.unistuttgart.isw.dtservices.services.auth;

import java.util.List;

public interface IScopeBuilder {
    
    /**
     * Get all available scopes
     * @return
     */
    List<Scope> getAvailableScopes(List<ServicePermission> permissions);

    /**
     * Build a scope from a service value and a list of operations
     * @param permissions the permissions to build the scope from
     * @return
     */
    List<Scope> buildScope(List<ServicePermission> permissions);

    /**
     * Build a string representation of a scope
     * @param scopes
     * @return
     */
    String buildStringScope(List<Scope> scopes);

    /**
     * Parse a scope string
     * @param scope
     * @return
     */
    Scope parseScope(String scope);
}

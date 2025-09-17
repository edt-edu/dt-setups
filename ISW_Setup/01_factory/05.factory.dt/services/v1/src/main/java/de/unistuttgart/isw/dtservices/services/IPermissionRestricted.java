package de.unistuttgart.isw.dtservices.services;

import java.util.List;

import de.unistuttgart.isw.dtservices.services.auth.ServicePermission;

/**
 *
 * Interface for services that are restricted by permissions
 * 
 * 
 * 
 */
public interface IPermissionRestricted {

    /**
     * list all available permissions for this service. This list is used by the authorisation service to issue tokens
     */
    public List<ServicePermission> getAllPermissions();
    
}

package services.dtservices.services;

import services.dtservices.services.auth.ServicePermission;

import java.util.List;

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

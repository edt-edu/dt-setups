package services.dtservices.services.auth;


import services.dtservices.services.GatewayService;

import java.util.List;
import java.util.Map;

public abstract class AuthService {

    protected GatewayService gatewayService;

    public AuthService(GatewayService gatewayService){
        this.gatewayService = gatewayService;
    }

    /**
     * authenticates a user and returns a token
     */
    abstract public String authenticate(Map<String, String> credentials);

    /**
     * validates a token
     */
    abstract public boolean validateToken(String token);

    /**
     * create the scope builder for this auth service. The scope defines the permissions of a token
     */
    abstract public IScopeBuilder getScopeBuilder(); 

    /**
     * get the scopes for a token
     */
    abstract public String getScopes(final String token);

    /**
     * get all available scopes for this auth service
     */
    public List<Scope> getAvailableScopes(){
        return this.getScopeBuilder().getAvailableScopes(getAllPermissions());
    } 

    /**
     * get all permissions handled by this auth service
     */
    abstract List<ServicePermission> getAllPermissions();


    /**
     * add a user to the auth service
     * @param credentials
     */
    abstract public void addUser(Map<String, String> credentials, List<ServicePermission> allowedScopes);

    /**
     * remove a user from the auth service
     * @param userID i.e. the username
     */
    abstract public void removeUser(String userID);
}

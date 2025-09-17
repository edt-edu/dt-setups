package services.dtservices.services.auth;



import services.dtservices.services.GatewayService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InMemoryAuthService extends AuthService {

    final private List<InMemoryUser> users = new LinkedList<>();
    final private Map<String, InMemoryTokens> tokens = new HashMap<>();
    final private IScopeBuilder scopeBuilder;

    public InMemoryAuthService(GatewayService gs) {
        super(gs);
        this.scopeBuilder = this.getScopeBuilder();       
    }

    /**
     * Authenticate a user with the given credentials
     * @param credentials
     * @return the token if the user exists and the password is correct, otherwise null
     */
    @Override
    public String authenticate(Map<String, String> credentials) {        
        for (InMemoryUser user : users) {
            if (user.getUsername().equals(credentials.get("username")) && user.getPassword().equals(credentials.get("password"))) {
                // user exists and password is correct
                List<Scope> scopes = this.scopeBuilder.buildScope(user.getPermissions());
                String scopeString = this.scopeBuilder.buildStringScope(scopes);
                String tokenValue = java.util.UUID.randomUUID().toString().replace("-", "");
                InMemoryTokens token = new InMemoryTokens(tokenValue, scopeString);
                tokens.put(tokenValue, token);
                return tokenValue;
            }
        }
        return null;
    }

    @Override
    public boolean validateToken(String token) {
        return tokens.containsKey(token);
    }

    @Override
    public IScopeBuilder getScopeBuilder() {
        return new DefaultScopeBuilder();   
    }

    @Override
    public String getScopes(String token) {
        InMemoryTokens tokenObj = tokens.get(token);
        if(tokenObj == null){
            return null;
        }
        return tokenObj.getScopes();
    }

   

    @Override
    public void addUser(Map<String, String> credentials, List<ServicePermission> servicePermissions) {
        InMemoryUser newUser = new InMemoryUser(credentials.get("username"), credentials.get("password"), servicePermissions);
        users.add(newUser);
    }

    @Override
    public void removeUser(String userID) {
        this.users.removeIf(user -> user.getUsername().equals(userID));
    }

    
    @Override
    List<ServicePermission> getAllPermissions() {
        return this.gatewayService.getAllPermissions();
    }



    private static class InMemoryUser {
        private final String username;
        private final String password;
        private final List<ServicePermission> permissions;

        public InMemoryUser(String username, String password, List<ServicePermission> permissions) {
            this.username = username;
            this.password = password;
            this.permissions = permissions;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public List<ServicePermission> getPermissions() {
            return permissions;
        }
    }

    private static class InMemoryTokens {
        private final String token;
        private final String scopes;

        public InMemoryTokens(String token, String scopes) {
            this.token = token;
            this.scopes = scopes;
        }

        public String getToken() {
            return token;
        }

        public String getScopes() {
            return scopes;
        }
    }

 
    
}

package services.dtservices.services.auth;

import java.util.LinkedList;
import java.util.List;


public class DefaultScopeBuilder implements IScopeBuilder {

    @Override
    public List<Scope> getAvailableScopes(List<ServicePermission> permissions) {
        return this.buildScope(permissions);
    }

    @Override
    public List<Scope> buildScope(List<ServicePermission> permissions) {

        List<Scope> scopes = new LinkedList<>();
        
        // service.<service_value_type>.<service_value>.<operation>
        permissions.forEach(permission -> {
            final ServiceValue service = permission.getService();
            final List<ServiceOperation> operations = permission.getOperations();            
            operations.forEach(operation -> {
                final String value = String.format("service.%s.%s.%s", service.getType().getValue(), service.getValue(), operation.getValue());
                final Scope scope = new Scope(service.getValue() + ": " + operation.getValue(),"", value);
                scopes.add(scope);
            });
        });
        return scopes;
    }

    public String buildStringScope(List<Scope> scopes) {
        final StringBuilder sb = new StringBuilder();
        scopes.forEach(scope -> {            
            sb.append(scope.getValue());
            sb.append(" ");
        });        
        return sb
            .toString()
            .strip();

    }

    @Override
    public Scope parseScope(String scope) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'parseScope'");
    }
   

   
}

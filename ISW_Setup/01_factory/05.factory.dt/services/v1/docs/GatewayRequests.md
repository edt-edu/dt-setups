# DSL Query Request

Request: XML request, check for gateway_request.xsd for the schema.

The body attribute of the request must contain the "dsl", "dsl_query", or "query" and should contain the corresponding query language version.


# Manifest Request

get all information about the gateway and its registered services and authorisation policies.

Response: 
    - Gateway: the gateway information
    - Services: List of services
    - Policies: List of policies

## Policies
    - name: string
    - description: string
    - paramters: {
        - name : string
        - type: string
        - description: string
    }[]

# Authorisation Request

runs a authorisation check on the gateway.

Request: 
 - Policy: the policy to use for the authorisation
 - Parameters: Map<string, string> of parameters to use for the authorisation check


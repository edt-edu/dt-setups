# Service Types

## Overview

<!-- 
create a table of service types (name, description, example) 

Types: Auth Service
       READONLY_BATCH Service     > Infoservice about services of DT
       READONLY_STREAM Service    > LOG Service that produces a strean of data
       UI Service > UI Service that provides a UI
       WRITE_STREAM Service  > real-time action service, autonomous driving, require constant changes in actions of the car's behavior
       WRITE_BATCH Service > update a model with once value

-->

| Name | Description | Example |
|------|-------------|---------|
| Auth Service | Service that provides authentication and authorization, issue OAuth2 based tokens, maybe even OIDC as extension | LDAP Implementation, In-Memory|
| READONLY_BATCH Service | Service that provides return data as RestAPI without modifing models | Service that provides information about services of DT |
| READONLY_STREAM Service | Service that produces a stream of data, i.e. as Event on a Queue or Websocket, but cannot change the model | LOG Service |
| UI Service | Service that provides a UI with CRUD Operator for models | UI Service that provides a UI, MontiGem Based? |
| WRITE_STREAM Service | Service that provides real-time action service | autonomous driving, require constant changes in actions of the car's behavior and car's model; LOG Writter |
| WRITE_BATCH Service | Service that updates a model with once value | Trigger an single time event like set a crane's position   |


### Auth Service
The auth service is a service that provides authentication and authorization. It issues OAuth2 based tokens, maybe even OIDC as an extension. 
The service can be implemented with LDAP, In-Memory or other custom backends.

Auth Service are required in a production environment to ensure a trusted space for DT Services.

### Write Services

Services can either write data once as a batch usally with a REST API or provide a stream of data that is written in real-time.
A batch write service will immidiatly update the model with the new value, and returns a status code. It is best for simple CRUD operations. 
Consitency is depended on the service implementation and underlying database or storage system.

A stream write service will accept a stream of data, which can be infinite. The service will update the model in real-time, will Consitency can not be guaranteed.


### Dependency Management
Service that a deployed as independent units, for example as microservices, are prune to dependencies. 
Especially if new services can be build on top of others. This will be problematic once a service requires an update and it cannot 
notify services that depend on it. While services that are composed during build time can throw an error before deployment.

This would require some sort of orchastration system that will gradually update services and restart all dependent services without causing downtime.
It is important to note that the dependency path can be very complex and nested. And even circle dependencies can occur during runtime.

On the other side services that are composed as the build time an deployed as single unit can 
not be updated without a complete redeployment the whole system and the reusage of individual services are sort of limited within an organisation, 
which may cause an increased cost of development and maintenance, because of the need to addional hardware and 
redundant service components.

### Plugin System

- Weiter ausbreiten, was die Services machen, Update hinzufügen als Plugin
- Details zu den WRITE
- Plugin System
- Trusted Space (Auth Service)
- Laufzeitprobleme beim Dependencies
       - Libraries and frameworks
       - Databases
       - External APIs or services


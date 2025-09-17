# Operators

## Describe \<Service>

This operator will return the schema return by a service and additonal metadata usablable by a frontend.

Return Data: 
- Schema of the service data
- Metadata
- Data Source (Service Name, Model Name)

Metadata for all services:
- Name(s)
- Description(s)
- Service Type(s)
- Service ID(s)
- Service Version(s)
- Data Refreshment interval(s) (if applicable, it tells how often the data is expected to be updated)


## Select / Select Where

SQL-like

DT - Data Base
Service = Table
Model = Column

Composed Service = Sub Query

Use for visualisation purposes. It is used to query a visualisation service or return values from a computation service.

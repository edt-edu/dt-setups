package services.dtservices.services.query;


public interface Insertable {
    public InsertResponse onInsert(InsertableOperation operation);
}

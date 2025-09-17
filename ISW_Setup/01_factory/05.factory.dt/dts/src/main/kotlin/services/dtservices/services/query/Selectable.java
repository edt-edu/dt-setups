package services.dtservices.services.query;

public interface Selectable {
    public SelectResponse onSelect(SelectionOperation operation);
}

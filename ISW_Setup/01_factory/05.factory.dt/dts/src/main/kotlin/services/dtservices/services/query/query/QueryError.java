package services.dtservices.services.query.query;

public class QueryError extends RuntimeException {

    public QueryError(String message) {
        super(message);
    }

    public QueryError(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryError(Throwable cause) {
        super(cause);
    }

    public QueryError() {
        super();
    }
    
}

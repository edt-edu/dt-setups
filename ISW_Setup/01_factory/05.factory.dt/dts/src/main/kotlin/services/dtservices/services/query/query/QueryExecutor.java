package services.dtservices.services.query.query;


import services.dtservices.services.GatewayService;

public interface QueryExecutor<Querytype, ResponseType> {
    public ExecutorResponse<ResponseType> executeQuery(GatewayService gateway, Querytype queryvalue);

    public QueryContext getContext();
}

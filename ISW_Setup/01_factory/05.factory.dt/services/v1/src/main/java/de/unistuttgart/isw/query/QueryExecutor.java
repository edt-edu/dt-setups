package de.unistuttgart.isw.query;

import de.unistuttgart.isw.dtservices.services.GatewayService;

public interface QueryExecutor<Querytype, ResponseType> {
    public ExecutorResponse<ResponseType> executeQuery(GatewayService gateway, Querytype queryvalue);

    public QueryContext getContext();
}

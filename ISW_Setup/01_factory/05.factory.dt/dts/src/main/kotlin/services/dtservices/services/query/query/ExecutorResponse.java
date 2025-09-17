package services.dtservices.services.query.query;


import services.dtservices.services.query.query.responses.*;

import java.util.function.Function;

/**
 * builds the json response for a query
 */
public class ExecutorResponse<ResponseType> {

    private ResponseType response;


    private ExecutorResponse() {
        
    }

    /**
     * adds a new list response
     */
    public static ExecutorResponse<ListResponse> listResponse(Function<ListResponse.ListResponseBuilder, ListResponse.ListResponseBuilder> listResponse) {
        if(listResponse != null ){
            ListResponse lr = listResponse.apply(ListResponse.builder()).build();
            return simpleResponse(lr);
        }
        return null;
    }

    public static ExecutorResponse<SelectResponse> selectResponse(Function<SelectResponse.SelectResponseBuilder, SelectResponse.SelectResponseBuilder> selectResponse) {
        if(selectResponse != null ){
            SelectResponse sr = selectResponse.apply(SelectResponse.builder()).build();
            return simpleResponse(sr);
        }
        return null;
    }

    public static ExecutorResponse<DescribeResponse> describeResponse(Function<DescribeResponse.Builder, DescribeResponse> describeResponse) {
        if(describeResponse != null ){
            DescribeResponse dr = describeResponse.apply(DescribeResponse.builder());
            return simpleResponse(dr);
        }
        return null;
    }

    public static ExecutorResponse<SwitchResponse> switchResponse(String oldContext, String newContext) {
        SwitchResponse sr = new SwitchResponse(oldContext, newContext);
        return simpleResponse(sr);
    }

    public static <ResponseType> ExecutorResponse<ResponseType> simpleResponse(ResponseType response) {
        ExecutorResponse<ResponseType> er = new ExecutorResponse<>();
        er.response = response;
        return er;
    }

    public static ExecutorResponse<SigilResponse> sigilResponse(SigilResponse sigilResponse) {
        return simpleResponse(sigilResponse);
    }

    /**
     * gets the response
     */
    public ResponseType getResponse() {
        return response;
    }

    
}


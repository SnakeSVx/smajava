package be.svx.smajava;

import be.svx.smajava.commands.StartRequest;
import be.svx.smajava.commands.StartResponse;

/**
 * Created by Stijn on 5/02/14.
 */
public class RequestResponseFactory {

    public static Request makeRequest(RequestType requestType){
        Request request = null;
        if(requestType == RequestType.START){
            request = new StartRequest();
        }
        return request;
    }


    public static  Response makeResponse(ResponseType responseType){
        Response response = null;
        if(responseType == ResponseType.START)
        {
            response = new StartResponse();
        }
        return  response;
    }
}

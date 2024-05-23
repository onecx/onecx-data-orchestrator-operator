package org.tkit.onecx.data.orchestrator.operator.client;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public interface DataClientApi {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    Response updateDate(Object data);
}

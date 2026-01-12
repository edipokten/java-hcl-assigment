package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.fulfilment.FulfilmentAssignmentService.FulfilmentAssignmentResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("fulfilment")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfilmentResource {

    @Inject FulfilmentAssignmentService service;

    @POST
    @Path("stores/{storeId}/products/{productId}/warehouses/{warehouseBuCode}")
    @Transactional
    public Response assign(
            @PathParam("storeId") Long storeId,
            @PathParam("productId") Long productId,
            @PathParam("warehouseBuCode") String warehouseBuCode) {

        FulfilmentAssignmentResponse resp = service.assign(storeId, productId, warehouseBuCode);
        return Response.status(201).entity(resp).build();
    }
}

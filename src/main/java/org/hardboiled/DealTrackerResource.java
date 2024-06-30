package org.hardboiled;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@Path("/dealtracker")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DealTrackerResource {

    @Inject
    DealTrackerManager dealTrackerManager;

    @GET
    @Path("/getAll")
    public List<DealTracker> getAll() {
        return dealTrackerManager.getAll();
    }

    @GET
    @Path("/get/{transactionRef}")
    public Optional<DealTracker> getByTransactionRef(@PathParam("transactionRef") String transactionRef) {
        return dealTrackerManager.getByTransactionRef(transactionRef);
    }

    @GET
    @Path("/getDealsWaiting")
    public List<DealTracker> getDealsWaiting() {
        return dealTrackerManager.getDealsWaiting();
    }

    @GET
    @Path("/getDealsInProcessing")
    public List<DealTracker> getDealsInProcessing() {
        return dealTrackerManager.getDealsInProcessing();
    }

    @POST
    @Path("/process")
    public void processDealTrackerMessage(DealTracker dealTracker) {
        log.info("Process called for deal -> {}", dealTracker);
        dealTrackerManager.processDealTracker(dealTracker);
    }

    @DELETE
    @Path("/delete/{transactionRef}")
    public void deleteByTransactionRef(@PathParam("transactionRef") String transactionRef) {
        dealTrackerManager.deleteByTransactionRef(transactionRef);
    }

}

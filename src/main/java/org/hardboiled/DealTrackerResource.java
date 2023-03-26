package org.hardboiled;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

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
        dealTrackerManager.processDealTracker(dealTracker);
    }

    @DELETE
    @Path("/delete/{transactionRef}")
    public void deleteByTransactionRef(@PathParam("transactionRef") String transactionRef) {
        dealTrackerManager.deleteByTransactionRef(transactionRef);
    }

}

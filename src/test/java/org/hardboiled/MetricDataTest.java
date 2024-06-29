package org.hardboiled;

import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@QuarkusTest
public class MetricDataTest {

    @Inject
    DealTrackerManager dealTrackerManager;

    /**
     * Ensure that the basic create works in the manager
     */
    @Test
    public void testMetricDataCreate() {
        val dt1 = dealTrackerManager.create("abc123::1",
                "chainABC",
                1,
                null,
                null,
                null,
                null);
        assert (dealTrackerManager.getByTransactionRef(dt1.getDealReference()).isPresent());
        dealTrackerManager.getByTransactionRef("abc123::1").ifPresent(toDelete -> dealTrackerManager.delete(toDelete));
    }

    /**
     * Ensure that we can call to update a metric
     */
    @Test
    public void testMetricDataCreateAndUpdate() {
        val arrivalTime = LocalDateTime.of(2024, Month.SEPTEMBER, 4, 5, 6, 7);
        val sentTime = LocalDateTime.of(2024, Month.SEPTEMBER, 4, 5, 7, 8);
        val dt2 = dealTrackerManager.create("abc123::2",
                "chainABC",
                1,
                arrivalTime,
                null,
                null,
                null);
        assert (dealTrackerManager.getAll().stream()).filter(toUpdate ->
                    toUpdate.id.equals(dt2.id)
                ).toList().size() == 1;
        dealTrackerManager.update(dt2);
        dt2.setSentTime(sentTime);
        dealTrackerManager.update(dt2);
        assert (dealTrackerManager.getAll().stream().filter(updated ->
                    updated.id.equals(dt2.id)
                        && updated.arrivalTime.equals(arrivalTime)
                        && updated.sentTime.equals(sentTime)
                ).toList().size() == 1);
        dealTrackerManager.getByTransactionRef(dt2.getDealReference()).ifPresent(toDelete ->
                dealTrackerManager.delete(toDelete));
    }

    /**
     * Ensure that we can create and then delete items via the dealManager
     */
    @Test
    public void testMetricDataCreateAndDelete() {
        val dt3 = dealTrackerManager.create("abc123::3",
                "chainABC",
                1,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());
        assert (dealTrackerManager.getAll().stream()).filter(toDelete -> toDelete.id.equals(dt3.id))
                .toList().size() == 1;
        dealTrackerManager.delete(dt3);
        assert (dealTrackerManager.getAll().stream().filter(deleted -> deleted.id.equals(dt3.id))
                .toList().isEmpty());
    }

    @Test
    public void testMetricDataInVariousStates() {
        // fully processed - should be deleted - COMPLETE
        val dt1 = new DealTracker("chain123::1", "chainABC", 1, LocalDateTime.now(), null, null, null);
        dealTrackerManager.processDealTracker(dt1);
        dt1.setSentTime(LocalDateTime.now());
        dealTrackerManager.processDealTracker(dt1);
        dt1.setVestCompleteTime(LocalDateTime.now());
        dealTrackerManager.processDealTracker(dt1);
        dt1.setAppCompleteTime(LocalDateTime.now());
        dealTrackerManager.processDealTracker(dt1);

        // After a full cycle of all 4 messages the deal should be completed/removed
        assertEquals(dealTrackerManager.getDealsInProcessing().size(), 0);
        assertEquals(dealTrackerManager.getDealsWaiting().size(), 0);

        // waiting for app to complete - PROCESSING
        val dt2 = new DealTracker("chain123::2", "chainBCD", 2, LocalDateTime.now(), null, null, null);
        dealTrackerManager.processDealTracker(dt2);
        dt2.setSentTime(LocalDateTime.now());
        dealTrackerManager.processDealTracker(dt2);
        dt2.setVestCompleteTime(LocalDateTime.now());
        dealTrackerManager.processDealTracker(dt2);

        // After 2 messages have been processed then the deal should be in processing - waiting for application
        // to say it's completed the processing of the deal
        assertEquals(1, dealTrackerManager.getDealsInProcessing().size());
        assertEquals(0, dealTrackerManager.getDealsWaiting().size());

        // waiting for app to ack - PROCESSING
        val dt3 = new DealTracker("chain123::3", "chainBCD", 1, LocalDateTime.now(), null, null, null);
        dealTrackerManager.processDealTracker(dt3);
        dt3.setSentTime(LocalDateTime.now());
        dealTrackerManager.processDealTracker(dt3);

        assertEquals(2, dealTrackerManager.getDealsInProcessing().size());
        assertEquals(0, dealTrackerManager.getDealsWaiting().size(), 0);

        // waiting for previous version - WAITING
        val dt4 = dealTrackerManager.create("chain123::4", "chainCDE", 1, LocalDateTime.now(), null, null, null);
        dealTrackerManager.processDealTracker(dt4);

        assertEquals(2, dealTrackerManager.getDealsInProcessing().size());
        assertEquals(1, dealTrackerManager.getDealsWaiting().size());

        // we get a couple of app updates
        dt3.setVestCompleteTime(LocalDateTime.now());
        dealTrackerManager.processDealTracker(dt3);

        assertEquals(2, dealTrackerManager.getDealsInProcessing().size());
        assertEquals(1, dealTrackerManager.getDealsWaiting().size());

        // and some more
        dt2.setAppCompleteTime(LocalDateTime.now());
        dealTrackerManager.processDealTracker(dt2);
        dt3.setAppCompleteTime(LocalDateTime.now());
        dealTrackerManager.processDealTracker(dt3);
        dt4.setVestCompleteTime(LocalDateTime.now());
        dealTrackerManager.processDealTracker(dt4);

        assertEquals(1, dealTrackerManager.getDealsInProcessing().size());
        assertEquals(0, dealTrackerManager.getDealsWaiting().size());

        // tidy up
        Stream.of(dt1, dt2, dt3, dt4)
                .forEach(dealTracker -> dealTrackerManager.getByTransactionRef(dealTracker.getDealReference())
                        .ifPresent(toDelete -> dealTrackerManager.delete(toDelete)));

        assertEquals(0, dealTrackerManager.getDealsInProcessing().size());
        assertEquals(0, dealTrackerManager.getDealsWaiting().size());
    }

}

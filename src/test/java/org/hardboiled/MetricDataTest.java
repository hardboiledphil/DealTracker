package org.hardboiled;

import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.stream.Stream;

@Slf4j
@QuarkusTest
public class MetricDataTest {

    @Inject
    DealTrackerManager dealTrackerManager;

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

    @Test
    public void testMetricDataCreateAndUpdate() {
        val arrivalTime = LocalDateTime.now();
        val dt2 = dealTrackerManager.create("abc123::2",
                "chainABC",
                1,
                arrivalTime,
                null,
                null,
                null);
        assert (dealTrackerManager.getAll().stream()).filter(dealTracker -> dealTracker.id.equals(dt2.id)).toList().size() == 1;
        dealTrackerManager.update(dt2);
        assert (dealTrackerManager.getAll().stream().filter(dealTracker -> dealTracker.id.equals(dt2.id)
                && dealTracker.arrivalTime.equals(arrivalTime)).toList().size() == 1);
        dealTrackerManager.getByTransactionRef(dt2.getDealReference()).ifPresent(toDelete -> dealTrackerManager.delete(toDelete));
    }

    @Test
    public void testMetricDataCreateAndDelete() {
        val dt3 = dealTrackerManager.create("abc123::3",
                "chainABC",
                1,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());
        assert (dealTrackerManager.getAll().stream()).filter(dealTracker -> dealTracker.id.equals(dt3.id))
                .toList().size() == 1;
        dealTrackerManager.delete(dt3);
        assert (dealTrackerManager.getAll().stream().filter(dealTracker -> dealTracker.id.equals(dt3.id))
                .toList().size() == 0);
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

        // waiting for app to complete - PROCESSING
        val dt2 = new DealTracker("chain123::2", "chainBCD", 2, LocalDateTime.now(), null, null, null);
        dt2.setSentTime(LocalDateTime.now());
        dealTrackerManager.processDealTracker(dt2);
        dt2.setVestCompleteTime(LocalDateTime.now());
        dealTrackerManager.processDealTracker(dt2);

        // waiting for app to ack - PROCESSING
        val dt3 = new DealTracker("chain123::3", "chainBCD", 1, LocalDateTime.now(), null, null, null);
        dealTrackerManager.processDealTracker(dt3);
        dt3.setSentTime(LocalDateTime.now());
        dealTrackerManager.processDealTracker(dt3);

        // waiting for previous version - WAITING
        val dt4 = dealTrackerManager.create("chain123::4", "chainCDE", 1, LocalDateTime.now(), null, null, null);
        dealTrackerManager.processDealTracker(dt4);

        val x = dealTrackerManager.getDealsInProcessing().size();

        log.info("getDealsInProcessing is {}", x);

        assert (dealTrackerManager.getDealsInProcessing().size() == 2);

        val y = dealTrackerManager.getDealsWaiting().size();

        log.info("getDealsWaiting is {}", y);

        assert (dealTrackerManager.getDealsWaiting().size() == 1);

        // tidy up
        Stream.of(dt1, dt2, dt3, dt4)
                .forEach(dealTracker -> dealTrackerManager.getByTransactionRef(dealTracker.getDealReference())
                        .ifPresent(toDelete -> dealTrackerManager.delete(toDelete)));
    }

}

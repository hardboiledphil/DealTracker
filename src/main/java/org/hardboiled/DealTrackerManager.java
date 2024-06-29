package org.hardboiled;

import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@ApplicationScoped
public class DealTrackerManager {

    @Transactional()
    public List<DealTracker> getAll() {
        return DealTracker.findAll().list();
    }

    Comparator<DealTracker> chainAndNumberAndTransactionRefSorter
            = Comparator
                .comparing(DealTracker::getChain)
                .thenComparing(DealTracker::getChainNumber)
                .thenComparing(DealTracker::getDealReference);

    /**
     * We will take in a dealtracker item
     * If it doesn't exist then we will create it and store it (updating the Id)
     * If it does exist then we will update it where appCompleteTime is not set
     * and we will delete it where the appCompleteTime is set
     * @param dealToProcess the deal to process
     */
    @Transactional
    public void processDealTracker(DealTracker dealToProcess) {
        log.info("Processing deal -> {}", dealToProcess.dealReference);
        // no id - so we assume it's new and needs persisting
        if (dealToProcess.id == null
          && dealToProcess.appCompleteTime == null) {
            log.info("Persisting new DealTracker -> {}", dealToProcess.dealReference);
            dealToProcess.persist();
            return;
        }
        // If we got here then deal has already been persisted - so try and find it
        val savedDealOptional = DealTracker
                .<DealTracker>findAll()
                .stream()
                .filter(dealTracker -> dealTracker.id.equals(dealToProcess.id)).findFirst();

        if (savedDealOptional.isEmpty()) {
            log.error("Could not find DealTracker id -> {}", dealToProcess.id);
            return;
        }

        // Optional will have a value or it would have returned in section above
        val savedDeal = savedDealOptional.get();

        // if we find the deal in the db but the new version is app complete then delete
        if (dealToProcess.appCompleteTime != null) {
            log.info("  Deleting deal -> {}", savedDeal.dealReference);
            savedDeal.delete();
            return;
        }

        // else update it
        savedDeal.setArrivalTime(dealToProcess.getArrivalTime());
        savedDeal.setSentTime(dealToProcess.getSentTime());
        savedDeal.setVestCompleteTime(dealToProcess.getVestCompleteTime());
        savedDeal.setAppCompleteTime(dealToProcess.getAppCompleteTime());
        savedDeal.persist();
        log.info("  Updating deal -> {}", savedDeal.dealReference);
    }

    @Transactional
    public List<DealTracker> getDealsInProcessing() {
        log.info("In getDealsInProcessing");
        return DealTracker
                .<DealTracker>findAll()
                .stream()
                .filter(dealTrackerItem -> !(dealTrackerItem.sentTime == null
                        && dealTrackerItem.vestCompleteTime == null
                        && dealTrackerItem.appCompleteTime == null))
                .sorted(chainAndNumberAndTransactionRefSorter)
                .peek(dealTrackerItem -> log.info("  tracking deal -> {} {} {} {} {}", dealTrackerItem.dealReference,
                        dealTrackerItem.arrivalTime, dealTrackerItem.sentTime, dealTrackerItem.vestCompleteTime,
                        dealTrackerItem.appCompleteTime))
                .toList();
    }

    @Transactional
    public List<DealTracker> getDealsWaiting() {
        log.info("In getDealsWaiting");
        return DealTracker
                .<DealTracker>findAll()
                .stream()
                .filter(dealTrackerItem -> dealTrackerItem.sentTime == null
                        && dealTrackerItem.vestCompleteTime == null
                        && dealTrackerItem.appCompleteTime == null)
                .sorted(chainAndNumberAndTransactionRefSorter)
                .peek(dealTrackerItem -> log.info("  tracking deal -> {} {} {} {} {}", dealTrackerItem.dealReference,
                        dealTrackerItem.arrivalTime, dealTrackerItem.sentTime, dealTrackerItem.vestCompleteTime,
                        dealTrackerItem.appCompleteTime))
                .toList();
    }

    @Transactional
    public DealTracker create(final String dealReference,
                              final String chain,
                              final int chainNumber,
                              final LocalDateTime arrivalTime,
                              final LocalDateTime sentTime,
                              final LocalDateTime vestCompleteTime,
                              final LocalDateTime appCompleteTime) {
        val newDealTrackerItem = DealTracker.builder()
                .dealReference(dealReference)
                .chain(chain)
                .chainNumber(chainNumber)
                .arrivalTime(arrivalTime)
                .sentTime(sentTime)
                .vestCompleteTime(vestCompleteTime)
                .appCompleteTime(appCompleteTime)
                .build();
        newDealTrackerItem.persist();
        log.info("Created deal instance for -> {}", newDealTrackerItem.dealReference);
        return newDealTrackerItem;
    }

    @Transactional
    public void update(final DealTracker dealTrackerItem) {
        val prevDealTrackerItem = DealTracker.<DealTracker>findById(dealTrackerItem.id);
        if (prevDealTrackerItem != null) {
            prevDealTrackerItem.setArrivalTime(dealTrackerItem.arrivalTime);
            prevDealTrackerItem.setSentTime(dealTrackerItem.sentTime);
            prevDealTrackerItem.setVestCompleteTime(dealTrackerItem.vestCompleteTime);
            prevDealTrackerItem.setAppCompleteTime(dealTrackerItem.appCompleteTime);
            prevDealTrackerItem.persist();
            log.info("updated deal -> {}", prevDealTrackerItem.dealReference );
        } else {
            log.error("Could not update DealTracker entity -> {}", dealTrackerItem.id);
        }
    }

    @Transactional
    public void delete(final DealTracker dealTrackerItem) {
        val prevDealTrackerItem = DealTracker.<DealTracker>findById(dealTrackerItem.id);

        if (prevDealTrackerItem != null) {
            prevDealTrackerItem.delete();
            log.info("Deleted by entity -> {}", dealTrackerItem.id);
        } else {
            log.error("Could not delete DealTracker entity -> {}", dealTrackerItem.id);
        }
    }

    @Transactional
    public void deleteByTransactionRef(final String transactionRef) {
        val dealToDelete = DealTracker.<DealTracker>findAll()
                .stream()
                .filter(dealTracker -> dealTracker.dealReference.equals(transactionRef))
                .findFirst();

        dealToDelete.ifPresent(PanacheEntityBase::delete);
        log.info("Deleted by transactionRef -> {}", transactionRef);
    }

    @Transactional
    public Optional<DealTracker> getByTransactionRef(final String transactionRef) {
        log.info("Getting by transRef -> {}", transactionRef);
        return getAll().stream().filter(dealTracker -> dealTracker.dealReference.equals(transactionRef)).findFirst();
    }

    // uncomment to create an entry on start up - useful for then doing a get on so you can see the json produced
    // and you can edit it and send it back in
//    void onStart(@Observes StartupEvent ev) {
//        // Your startup logic here
//        System.out.println("Application started - adding an entity automatically");
//        this.create("abc123::1",
//                "chainABC",
//                1,
//                LocalDateTime.of(2024, Month.SEPTEMBER, 4, 5, 6),
//                LocalDateTime.of(2024, Month.SEPTEMBER, 5, 6, 7),
//                LocalDateTime.of(2024, Month.SEPTEMBER, 6, 7, 8),
//                LocalDateTime.of(2024, Month.SEPTEMBER, 7, 8, 9));
//    }

}

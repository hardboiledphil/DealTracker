package org.hardboiled;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "dealtracker")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DealTracker extends PanacheEntityBase {

    public DealTracker(
            final String dealReference,
            final String chain,
            final int chainNumber,
            final LocalDateTime arrivalTime,
            final LocalDateTime sentTime,
            final LocalDateTime vestCompleteTime,
            final LocalDateTime appCompleteTime) {
        this.dealReference = dealReference;
        this.chain = chain;
        this.chainNumber = chainNumber;
        this.arrivalTime = arrivalTime;
        this.sentTime = sentTime;
        this.vestCompleteTime = vestCompleteTime;
        this.appCompleteTime = appCompleteTime;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
            String dealReference;
            String chain;
            int chainNumber;
    @Setter
    LocalDateTime arrivalTime;
    @Setter
    LocalDateTime sentTime;
    @Setter
    LocalDateTime vestCompleteTime;
    @Setter
    LocalDateTime appCompleteTime;
}
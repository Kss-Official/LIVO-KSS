package com.livo.api.modules.trip.repository;

import com.livo.api.modules.trip.entity.ItineraryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItineraryItemRepository extends JpaRepository<ItineraryItemEntity, UUID> {

    List<ItineraryItemEntity> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    Optional<ItineraryItemEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    Optional<ItineraryItemEntity> findByIdAndTripIdAndUserIdAndDeletedAtIsNull(UUID id, UUID tripId, UUID userId);

    List<ItineraryItemEntity> findAllByTripIdAndUserIdAndDeletedAtIsNullOrderByItemDateAscItemTimeAsc(UUID tripId, UUID userId);

    List<ItineraryItemEntity> findAllByTripIdAndDeletedAtIsNullOrderByItemDateAscItemTimeAsc(UUID tripId);

    List<ItineraryItemEntity> findAllByTripIdAndUserIdAndDeletedAtIsNull(UUID tripId, UUID userId);

    List<ItineraryItemEntity> findAllByTripIdInAndUserIdAndDeletedAtIsNull(Collection<UUID> tripIds, UUID userId);

    long countByTripIdAndUserIdAndDeletedAtIsNull(UUID tripId, UUID userId);
}

package com.deliveryflow.tracking.mapper;

import com.deliveryflow.tracking.domain.TrackingEntry;
import com.deliveryflow.tracking.dto.CreateTrackingEntryRequest;
import com.deliveryflow.tracking.dto.TrackingEntryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrackingEntryMapper {

    TrackingEntryResponse toResponse(TrackingEntry entry);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trackingId", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "recordedAt", expression = "java(java.time.Instant.now())")
    TrackingEntry toEntity(CreateTrackingEntryRequest request);
}

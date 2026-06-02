package com.deliveryflow.tracking.service;

import com.deliveryflow.tracking.domain.TrackingEntry;
import com.deliveryflow.tracking.dto.CreateTrackingEntryRequest;
import com.deliveryflow.tracking.dto.TrackingEntryResponse;
import com.deliveryflow.tracking.exception.TrackingNotFoundException;
import com.deliveryflow.tracking.mapper.TrackingEntryMapper;
import com.deliveryflow.tracking.repository.TrackingEntryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingServiceTest {

    @Mock
    private TrackingEntryRepository repository;

    @Mock
    private TrackingEntryMapper mapper;

    @InjectMocks
    private TrackingService trackingService;

    private static final UUID ORDER_ID    = UUID.randomUUID();
    private static final UUID TRACKING_ID = UUID.randomUUID();

    private TrackingEntry buildEntry() {
        TrackingEntry entry = new TrackingEntry();
        entry.setId(1L);
        entry.setTrackingId(TRACKING_ID);
        entry.setOrderId(ORDER_ID);
        entry.setStatus("PENDING");
        entry.setLocation(null);
        entry.setNote(null);
        entry.setRecordedAt(Instant.now());
        return entry;
    }

    private TrackingEntryResponse buildResponse(TrackingEntry entry) {
        return new TrackingEntryResponse(
                entry.getTrackingId(),
                entry.getOrderId(),
                entry.getStatus(),
                entry.getLocation(),
                entry.getNote(),
                entry.getRecordedAt()
        );
    }

    @Nested
    @DisplayName("createEntry")
    class CreateEntry {

        @Test
        @DisplayName("should save entry and return response DTO")
        void shouldSaveEntryAndReturnResponse() {
            var request  = new CreateTrackingEntryRequest(ORDER_ID, "PENDING", null, null);
            var entry    = buildEntry();
            var response = buildResponse(entry);

            when(mapper.toEntity(request)).thenReturn(entry);
            when(repository.save(entry)).thenReturn(entry);
            when(mapper.toResponse(entry)).thenReturn(response);

            TrackingEntryResponse result = trackingService.createEntry(request);

            assertThat(result).isEqualTo(response);
            verify(mapper).toEntity(request);
            verify(repository).save(entry);
            verify(mapper).toResponse(entry);
        }

        @Test
        @DisplayName("should pass mapper result directly to repository save")
        void shouldPassMappedEntityToRepository() {
            var request  = new CreateTrackingEntryRequest(ORDER_ID, "SHIPPED", "Warsaw", "On the way");
            var entry    = buildEntry();
            var response = buildResponse(entry);

            when(mapper.toEntity(request)).thenReturn(entry);
            when(repository.save(any(TrackingEntry.class))).thenReturn(entry);
            when(mapper.toResponse(entry)).thenReturn(response);

            trackingService.createEntry(request);

            verify(repository).save(entry);
        }
    }

    @Nested
    @DisplayName("getHistory")
    class GetHistory {

        @Test
        @DisplayName("should return mapped response list ordered by recordedAt ascending")
        void shouldReturnMappedResponseList() {
            var entry1 = buildEntry();

            var entry2 = new TrackingEntry();
            entry2.setId(2L);
            entry2.setTrackingId(UUID.randomUUID());
            entry2.setOrderId(ORDER_ID);
            entry2.setStatus("CONFIRMED");
            entry2.setRecordedAt(Instant.now());

            var response1 = buildResponse(entry1);
            var response2 = buildResponse(entry2);

            when(repository.findByOrderIdOrderByRecordedAtAsc(ORDER_ID)).thenReturn(List.of(entry1, entry2));
            when(mapper.toResponse(entry1)).thenReturn(response1);
            when(mapper.toResponse(entry2)).thenReturn(response2);

            List<TrackingEntryResponse> result = trackingService.getHistory(ORDER_ID);

            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(response1, response2);
        }

        @Test
        @DisplayName("should return empty list when no entries exist for orderId")
        void shouldReturnEmptyListWhenNoEntries() {
            when(repository.findByOrderIdOrderByRecordedAtAsc(ORDER_ID)).thenReturn(List.of());

            List<TrackingEntryResponse> result = trackingService.getHistory(ORDER_ID);

            assertThat(result).isEmpty();
            verify(mapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("getLatest")
    class GetLatest {

        @Test
        @DisplayName("should return latest entry when it exists")
        void shouldReturnLatestEntryWhenExists() {
            var entry    = buildEntry();
            var response = buildResponse(entry);

            when(repository.findTopByOrderIdOrderByRecordedAtDesc(ORDER_ID)).thenReturn(Optional.of(entry));
            when(mapper.toResponse(entry)).thenReturn(response);

            TrackingEntryResponse result = trackingService.getLatest(ORDER_ID);

            assertThat(result).isEqualTo(response);
        }

        @Test
        @DisplayName("should throw TrackingNotFoundException when no entries exist")
        void shouldThrowTrackingNotFoundExceptionWhenNoEntries() {
            when(repository.findTopByOrderIdOrderByRecordedAtDesc(ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> trackingService.getLatest(ORDER_ID))
                    .isInstanceOf(TrackingNotFoundException.class)
                    .hasMessageContaining(ORDER_ID.toString());

            verify(mapper, never()).toResponse(any());
        }
    }
}

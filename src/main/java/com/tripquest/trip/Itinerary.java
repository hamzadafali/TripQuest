package com.tripquest.trip;

import com.tripquest.shared.DomainResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDate;

final class Itinerary {
    private final Map<ActivityId, ItineraryPlacement> placements = new LinkedHashMap<>();
    private final List<ActivityId> order = new ArrayList<>();

    DomainResult<ItineraryPlacement, ItineraryError> place(ActivityId activityId, ResolvedSchedule schedule,
                                                            java.util.Optional<Money> price) {
        if (placements.containsKey(activityId)) {
            return DomainResult.rejected(new ItineraryError.AlreadyPlaced(activityId));
        }
        ItineraryPlacement placement = new ItineraryPlacement(activityId, schedule, price);
        placements.put(activityId, placement);
        order.add(activityId);
        return DomainResult.success(placement);
    }

    DomainResult<ItineraryPlacement, ItineraryError> move(ActivityId activityId, ResolvedSchedule schedule) {
        ItineraryPlacement current = placements.get(activityId);
        if (current == null) {
            return DomainResult.rejected(new ItineraryError.PlacementNotFound(activityId));
        }
        ItineraryPlacement moved = new ItineraryPlacement(activityId, schedule, current.price());
        placements.put(activityId, moved);
        return DomainResult.success(moved);
    }

    DomainResult<ItineraryPlacement, ItineraryError> remove(ActivityId activityId) {
        ItineraryPlacement removed = placements.remove(activityId);
        if (removed == null) {
            return DomainResult.rejected(new ItineraryError.PlacementNotFound(activityId));
        }
        order.remove(activityId);
        return DomainResult.success(removed);
    }

    boolean contains(ActivityId activityId) { return placements.containsKey(activityId); }

    Map<ActivityId, ItineraryPlacement> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(placements));
    }

    List<ItineraryPlacement> placementsForDay(LocalDate day) {
        return order.stream()
                .map(placements::get)
                .filter(placement -> placement.schedule().itineraryDay().equals(day))
                .toList();
    }

    DomainResult<List<ItineraryPlacement>, ItineraryError> reorderDay(LocalDate day, List<ActivityId> requestedOrder) {
        List<ActivityId> current = placementsForDay(day).stream().map(ItineraryPlacement::activityId).toList();
        if (requestedOrder.size() != current.size()
                || !Set.copyOf(requestedOrder).equals(Set.copyOf(current))) {
            return DomainResult.rejected(new ItineraryError.InvalidDayOrder());
        }
        order.removeAll(current);
        order.addAll(requestedOrder);
        return DomainResult.success(placementsForDay(day));
    }

    List<OverlapWarning> overlaps() {
        List<ItineraryPlacement> values = new ArrayList<>(placements.values());
        List<OverlapWarning> warnings = new ArrayList<>();
        for (int first = 0; first < values.size(); first++) {
            for (int second = first + 1; second < values.size(); second++) {
                ItineraryPlacement left = values.get(first);
                ItineraryPlacement right = values.get(second);
                if (left.schedule().overlaps(right.schedule())) {
                    warnings.add(new OverlapWarning(left.activityId(), right.activityId()));
                }
            }
        }
        return List.copyOf(warnings);
    }

    IndicativeTotal total(Currency currency) {
        BigDecimal amount = placements.values().stream()
                .flatMap(placement -> placement.price().stream())
                .map(Money::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Money total = successValue(Money.create(amount, currency));
        boolean missing = placements.values().stream().anyMatch(placement -> placement.price().isEmpty());
        return new IndicativeTotal(total, missing);
    }

    Set<ActivityId> outside(TripPeriod period, ZoneId zoneId) {
        Instant start = period.start().atStartOfDay(zoneId).toInstant();
        Instant endBoundary = period.end().plusDays(1).atStartOfDay(zoneId).toInstant();
        return placements.values().stream()
                .filter(placement -> placement.schedule().start().isBefore(start)
                        || placement.schedule().end().isAfter(endBoundary))
                .map(ItineraryPlacement::activityId)
                .collect(Collectors.toUnmodifiableSet());
    }

    @SuppressWarnings("unchecked")
    private static <T, E> T successValue(DomainResult<T, E> result) {
        if (result instanceof DomainResult.Success<?, ?> success) {
            return (T) success.value();
        }
        throw new IllegalStateException("Internally computed itinerary money must be valid");
    }
}

package com.tripquest.trip;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public sealed interface ItineraryError permits ItineraryError.TripNotPlanning, ItineraryError.NotOrganizer,
        ItineraryError.ActivityNotFound, ItineraryError.ActivityNotSelected, ItineraryError.AlreadyPlaced,
        ItineraryError.PlacementNotFound, ItineraryError.InvalidDuration,
        ItineraryError.NonexistentLocalTime, ItineraryError.AmbiguousLocalTime,
        ItineraryError.InvalidOffset, ItineraryError.OutsideTripPeriod, ItineraryError.InvalidDayOrder {
    String code();

    String message();

    record TripNotPlanning(TripState state) implements ItineraryError {
        public String code() { return "ITINERARY_TRIP_NOT_PLANNING"; }
        public String message() { return "Itinerary can only change while the trip is planning"; }
    }

    record NotOrganizer(UserId actorId) implements ItineraryError {
        public String code() { return "ITINERARY_NOT_ORGANIZER"; }
        public String message() { return "Only an organizer may change the itinerary"; }
    }

    record ActivityNotFound(ActivityId activityId) implements ItineraryError {
        public String code() { return "ITINERARY_ACTIVITY_NOT_FOUND"; }
        public String message() { return "Activity proposal was not found"; }
    }

    record ActivityNotSelected(ActivityId activityId) implements ItineraryError {
        public String code() { return "ITINERARY_ACTIVITY_NOT_SELECTED"; }
        public String message() { return "Only a selected activity can enter the itinerary"; }
    }

    record AlreadyPlaced(ActivityId activityId) implements ItineraryError {
        public String code() { return "ITINERARY_ALREADY_PLACED"; }
        public String message() { return "Activity is already present in the itinerary"; }
    }

    record PlacementNotFound(ActivityId activityId) implements ItineraryError {
        public String code() { return "ITINERARY_PLACEMENT_NOT_FOUND"; }
        public String message() { return "Itinerary placement was not found"; }
    }

    record InvalidDuration() implements ItineraryError {
        public String code() { return "ITINERARY_INVALID_DURATION"; }
        public String message() { return "Placement duration must be positive"; }
    }

    record NonexistentLocalTime(LocalDateTime localDateTime) implements ItineraryError {
        public String code() { return "ITINERARY_NONEXISTENT_LOCAL_TIME"; }
        public String message() { return "Local time does not exist in the trip time zone"; }
    }

    record AmbiguousLocalTime(LocalDateTime localDateTime, List<ZoneOffset> validOffsets) implements ItineraryError {
        public String code() { return "ITINERARY_AMBIGUOUS_LOCAL_TIME"; }
        public String message() { return "Local time is ambiguous; an explicit offset is required"; }
    }

    record InvalidOffset(ZoneOffset offset) implements ItineraryError {
        public String code() { return "ITINERARY_INVALID_OFFSET"; }
        public String message() { return "Chosen offset is not valid for this local time"; }
    }

    record OutsideTripPeriod() implements ItineraryError {
        public String code() { return "ITINERARY_OUTSIDE_TRIP_PERIOD"; }
        public String message() { return "The complete activity interval must stay within the trip period"; }
    }

    record InvalidDayOrder() implements ItineraryError {
        public String code() { return "ITINERARY_INVALID_DAY_ORDER"; }
        public String message() { return "Day order must contain every placement of that day exactly once"; }
    }
}

package com.caboolo.backend.review.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReviewTag {
    // Positive
    PUNCTUAL("Punctual"),
    FRIENDLY("Friendly"),
    CLEAN("Clean"),
    GOOD_COMMUNICATION("Good communication"),
    SAFE_CO_TRAVELLER("Safe co-traveller"),

    // Neutral
    AVERAGE("Average"),
    SLIGHTLY_LATE("Slightly late"),
    QUIET("Quiet"),

    // Negative
    LATE("Late"),
    NO_SHOW("No show"),
    RUDE("Rude"),
    UNSAFE("Unsafe"),
    POOR_COMMUNICATION("Poor communication"),

    // 5 Stars
    GREAT_CONVERSATION("Great conversation"),
    RESPECTFUL("Respectful"),
    CLEAN_PASSENGER("Clean passenger"),
    HELPFUL("Helpful"),
    SMOOTH_RIDE("Smooth ride"),
    COOPERATIVE("Cooperative"),
    RECOMMENDED("Recommended"),

    // 4 Stars
    POLITE("Polite"),
    MOSTLY_ON_TIME("Mostly on time"),
    GOOD_BEHAVIOUR("Good behaviour"),
    COMFORTABLE_RIDE("Comfortable ride"),
    EASY_GOING("Easy going"),
    RESPONSIVE("Responsive"),

    // 3 Stars
    AVERAGE_EXPERIENCE("Average experience"),
    OKAY_RIDE("Okay ride"),
    COULD_IMPROVE_PUNCTUALITY("Could improve punctuality"),
    LESS_INTERACTIVE("Less interactive"),
    NEUTRAL_EXPERIENCE("Neutral experience"),
    ACCEPTABLE_BEHAVIOUR("Acceptable behaviour"),

    // 2 Stars
    LATE_ARRIVAL("Late arrival"),
    UNTIDY("Untidy"),
    UNCOMFORTABLE_RIDE("Uncomfortable ride"),
    DISTRACTING_BEHAVIOUR("Distracting behaviour"),
    INCONSIDERATE("Inconsiderate"),

    // 1 Star
    VERY_LATE("Very late"),
    RUDE_BEHAVIOUR("Rude behaviour"),
    HARASSMENT("Harassment"),
    SMOKING_DURING_RIDE("Smoking during ride"),
    VEHICLE_UNCLEAN("Vehicle unclean"),
    RECKLESS_BEHAVIOUR("Reckless behaviour"),
    ABUSIVE_LANGUAGE("Abusive language"),
    FELT_UNSAFE("Felt unsafe");

    private final String displayName;

    ReviewTag(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ReviewTag fromString(String value) {
        if (value == null) {
            return null;
        }
        for (ReviewTag tag : values()) {
            if (tag.name().equalsIgnoreCase(value) || tag.displayName.equalsIgnoreCase(value)) {
                return tag;
            }
        }
        throw new IllegalArgumentException("Unknown ReviewTag: " + value);
    }
}

package com.crowdsimulation.model.core.agent.passenger;

import com.crowdsimulation.model.simulator.Simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Demographic {
    public static HashMap<AgeRange, Double> ageRangeRatio;
    public static HashMap<AgeRange, Double> walkingSpeedsByAgeRange;

    public static List<AgeRange> allDemographics;

    static {
        ageRangeRatio = new HashMap<>();

        ageRangeRatio.put(AgeRange.YOUNGER_THAN_OR_14, 0.3242);
        ageRangeRatio.put(AgeRange.FROM_15_TO_24, 0.1916);
        ageRangeRatio.put(AgeRange.FROM_25_TO_54, 0.3737);
        ageRangeRatio.put(AgeRange.FROM_55_TO_64, 0.0618);
        ageRangeRatio.put(AgeRange.OLDER_THAN_OR_65, 0.0486);

        walkingSpeedsByAgeRange = new HashMap<>();

        walkingSpeedsByAgeRange.put(AgeRange.YOUNGER_THAN_OR_14, 1.34);
        walkingSpeedsByAgeRange.put(AgeRange.FROM_15_TO_24, 1.34);
        walkingSpeedsByAgeRange.put(AgeRange.FROM_25_TO_54, 1.27);
        walkingSpeedsByAgeRange.put(AgeRange.FROM_55_TO_64, 1.22);
        walkingSpeedsByAgeRange.put(AgeRange.OLDER_THAN_OR_65, 1.21);

        allDemographics = new ArrayList<>();

        allDemographics.add(AgeRange.YOUNGER_THAN_OR_14);
        allDemographics.add(AgeRange.FROM_15_TO_24);
        allDemographics.add(AgeRange.FROM_25_TO_54);
        allDemographics.add(AgeRange.FROM_55_TO_64);
        allDemographics.add(AgeRange.OLDER_THAN_OR_65);
    }

    private final AgeRange ageRange;

    public Demographic(AgeRange ageRange) {
        this.ageRange = ageRange;
    }

    public AgeRange getAgeRange() {
        return ageRange;
    }

    public static Demographic generateDemographic() {
        // Gather all demographic ratios
        List<Double> demographicRatios = new ArrayList<>();

        demographicRatios.add(Demographic.ageRangeRatio.get(AgeRange.YOUNGER_THAN_OR_14));
        demographicRatios.add(Demographic.ageRangeRatio.get(AgeRange.FROM_15_TO_24));
        demographicRatios.add(Demographic.ageRangeRatio.get(AgeRange.FROM_25_TO_54));
        demographicRatios.add(Demographic.ageRangeRatio.get(AgeRange.FROM_55_TO_64));
        demographicRatios.add(Demographic.ageRangeRatio.get(AgeRange.OLDER_THAN_OR_65));

        AgeRange chosenAgeRange;
        int choiceIndex = 0;

        // Use the floor field values as weights to choose among patches
        for (
                double randomNumber = Simulator.RANDOM_NUMBER_GENERATOR.nextDouble();
                choiceIndex < Demographic.allDemographics.size() - 1;
                choiceIndex++) {
            randomNumber -= demographicRatios.get(choiceIndex);

            if (randomNumber <= 0.0) {
                break;
            }
        }

        chosenAgeRange = Demographic.allDemographics.get(choiceIndex);

        return new Demographic(chosenAgeRange);
    }

    // Describes the demographic this passenger is part of
    public enum AgeRange {
        YOUNGER_THAN_OR_14,
        FROM_15_TO_24,
        FROM_25_TO_54,
        FROM_55_TO_64,
        OLDER_THAN_OR_65
    }
}

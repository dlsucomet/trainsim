package com.trainsimulation.model.utility;

// Represents the demographic information of passengers
public class PassengerDemographic {
    // Denotes the age of the passenger
    private final Age age;

    // Denotes the gender of the passenger
    private final Gender gender;

    public PassengerDemographic(Age age, Gender gender) {
        this.age = age;
        this.gender = gender;
    }

    public Age getAge() {
        return age;
    }

    public Gender getGender() {
        return gender;
    }

    private enum Age {
        BELOW_15,
        RANGE_15_24,
        RANGE_25_54,
        RANGE_55_64,
        ABOVE_64
    }

    private enum Gender {
        FEMALE,
        MALE
    }
}

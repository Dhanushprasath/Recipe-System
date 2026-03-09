//package com.recipe.backend.service;
//
//import com.recipe.backend.dto.ProfileRequest;
//import com.recipe.backend.dto.ProfileResponse;
//import com.recipe.backend.dto.ProfileUpdateRequest;
//import com.recipe.backend.model.*;
//import com.recipe.backend.repository.ProfileRepository;
//import com.recipe.backend.repository.UserRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class ProfileService {
//
//
//    private final ProfileRepository profileRepository;
//    private final UserRepository userRepository;
//
//    // ---------------- BMI ----------------
//    private double calculateBMI(double weightKg, double heightCm) {
//        double heightM = heightCm / 100;
//        return Math.round((weightKg / (heightM * heightM)) * 100.0) / 100.0;
//    }
//
//    private String bmiCategory(double bmi) {
//        if (bmi < 16) return "Severe Thinness";
//        if (bmi < 17) return "Moderate Thinness";
//        if (bmi < 18.5) return "Mild Thinness";
//        if (bmi < 25) return "Normal";
//        if (bmi < 30) return "Overweight";
//        if (bmi < 35) return "Obese Class I";
//        if (bmi < 40) return "Obese Class II";
//        return "Obese Class III";
//    }
//
//    // ---------------- BMR ----------------
//    private double calculateBMR(Profile profile) {
//        Gender gender = profile.getGender();
//        double weight = profile.getWeight();
//        double height = profile.getHeight();
//        double age = profile.getAge();
//
//        if (gender == Gender.MALE) {
//            return (10 * weight) + (6.25 * height) - (5 * age) + 5;
//        } else { // FEMALE or OTHER
//            return (10 * weight) + (6.25 * height) - (5 * age) - 161;
//        }
//    }
//    // ---------- Calories ----------
//    // ---------- Activity Multiplier ----------
//    private double getActivityMultiplier(String activityLevel) {
//        switch (activityLevel.toLowerCase()) {
//            case "sedentary": return 1.2;
//            case "light": return 1.375;
//            case "moderate": return 1.55;
//            case "active": return 1.725;
//            case "very_active": return 1.9;
//            default: return 1.55; // default moderate
//        }
//    }
//
//    private double maintenanceCalories(Profile profile) {
//        return calculateBMR(profile) * profile.getActivityLevel().getMultiplier();
//    }
//
//    private double finalDailyCalories(Profile profile) {
//
//        double maintenance = maintenanceCalories(profile);
//
//        if (profile.getGoal() == Goal.MAINTAIN) {
//            return maintenance;
//        }
//
//        int adjustment = switch (profile.getGoalIntensity()) {
//            case MILD -> profile.getGoal() == Goal.LOSE ? -300 : 250;
//            case STANDARD -> profile.getGoal() == Goal.LOSE ? -500 : 500;
//            case AGGRESSIVE -> profile.getGoal() == Goal.LOSE ? -750 : 750;
//        };
//
//        return maintenance + adjustment;
//    }
//
//
//    // ---------------- API METHOD ----------------
//    public ProfileResponse getProfile(String email) {
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Profile profile = profileRepository.findByUser_Id(user.getId())
//                .orElse(null);
//
//        if (profile == null || !profile.isProfileCompleted()) return null;
//
//        double bmi = calculateBMI(profile.getWeight(), profile.getHeight());
//        double bmr = calculateBMR(profile);
//        double maintenance = maintenanceCalories(profile);
//        double dailyCalories = finalDailyCalories(profile);
//
//        return ProfileResponse.builder()
//                .username(user.getUsername())
//                .email(user.getEmail())
//                .role(user.getRole().name())
//                .age(profile.getAge())
//                .gender(profile.getGender())
//                .height(profile.getHeight())
//                .weight(profile.getWeight())
//                .bmi(bmi)
//                .bmiCategory(bmiCategory(bmi))
//                .bmr((double) Math.round(bmr))
//                .activityLevel(profile.getActivityLevel().name())
//                .goal(profile.getGoal().name())
//                .goalIntensity(profile.getGoalIntensity().name())
//                .maintenanceCalories((double) Math.round(maintenance))
//                .dailyCalories((double) Math.round(dailyCalories))
//                .build();
//    }
//
//
//
//
//    // ---------------- Update / Save Profile ----------------
//        public ProfileResponse updateProfile(String email, ProfileUpdateRequest request) {
//
//            User user = userRepository.findByEmail(email)
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//
//            Profile profile = profileRepository.findByUser_Id(user.getId())
//                    .orElse(Profile.builder().user(user).build());
//
//            profile.setAge(request.getAge());
//            profile.setGender(request.getGender());
//            profile.setHeight(request.getHeight());
//            profile.setWeight(request.getWeight());
//            profile.setActivityLevel(ActivityLevel.valueOf(request.getActivityLevel()));
//            profile.setGoal(Goal.valueOf(request.getGoal()));
//            profile.setGoalIntensity(GoalIntensity.valueOf(request.getGoalIntensity()));
//
//            profile.setProfileCompleted(true);
//
//            profileRepository.save(profile);
//
//            return ProfileResponse.builder()
//                    .username(user.getUsername())
//                    .email(user.getEmail())
//                    .role(user.getRole().name())
//                    .age(profile.getAge())
//                    .gender(profile.getGender())
//                    .height(profile.getHeight())
//                    .weight(profile.getWeight())
//                    .bmi(profile.getBmi())
//
//                    .bmiCategory(profile.getBmiCategory())
//                    .dailyCalories(Double.valueOf(profile.getDailyCalories()))
//                    .build();
//        }
//    public ProfileResponse previewCalories(ProfileRequest request) {
//
//        Profile temp = new Profile();
//        temp.setAge(request.getAge());
//        temp.setGender(request.getGender());
//        temp.setHeight(request.getHeight());
//        temp.setWeight(request.getWeight());
//        temp.setActivityLevel(ActivityLevel.valueOf(String.valueOf(request.getActivityLevel())));
//        temp.setGoal(Goal.valueOf(String.valueOf(request.getGoal())));
//        temp.setGoalIntensity(GoalIntensity.valueOf(String.valueOf(request.getGoalIntensity())));
//
//        double bmi = calculateBMI(temp.getWeight(), temp.getHeight());
//        double bmr = calculateBMR(temp);
//        double maintenance = maintenanceCalories(temp);
//        double daily = finalDailyCalories(temp);
//
//        return ProfileResponse.builder()
//                .bmi(bmi)
//                .bmiCategory(bmiCategory(bmi))
//                .bmr((double) Math.round(bmr))
//                .maintenanceCalories((double) Math.round(maintenance))
//                .dailyCalories((double) Math.round(daily))
//                .build();
//    }
//
//}
//
//
//


package com.recipe.backend.service;

import com.recipe.backend.dto.ProfileRequest;
import com.recipe.backend.dto.ProfileResponse;
import com.recipe.backend.dto.ProfileUpdateRequest;
import com.recipe.backend.model.*;
import com.recipe.backend.repository.ProfileRepository;
import com.recipe.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    // ---------------- BMI ----------------
    private double calculateBMI(double weightKg, double heightCm) {
        double heightM = heightCm / 100.0;
        return Math.round((weightKg / (heightM * heightM)) * 100.0) / 100.0;
    }

    private String bmiCategory(double bmi) {
        if (bmi < 16) return "Severe Thinness";
        if (bmi < 17) return "Moderate Thinness";
        if (bmi < 18.5) return "Mild Thinness";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        if (bmi < 35) return "Obese Class I";
        if (bmi < 40) return "Obese Class II";
        return "Obese Class III";
    }

    // ---------------- BMR ----------------
    private double calculateBMR(Profile profile) {
        Gender gender = profile.getGender();
        double weight = profile.getWeight();
        double height = profile.getHeight();
        double age = profile.getAge();

        if (gender == Gender.MALE) {
            return (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else { // FEMALE or OTHER
            return (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }
    }

    // ---------- Activity Multiplier ----------
    private double getActivityMultiplier(ActivityLevel activityLevel) {
        if (activityLevel == null) return 1.55; // default moderate
        return switch (activityLevel) {
            case SEDENTARY -> 1.2;
            case LIGHT -> 1.375;
            case MODERATE -> 1.55;
            case ACTIVE -> 1.725;
            case VERY_ACTIVE -> 1.9;
        };
    }

    private double maintenanceCalories(Profile profile) {
        return calculateBMR(profile) * getActivityMultiplier(profile.getActivityLevel());
    }

    private double finalDailyCalories(Profile profile) {
        double maintenance = maintenanceCalories(profile);

        if (profile.getGoal() == null || profile.getGoal() == Goal.MAINTAIN) {
            return maintenance;
        }

        if (profile.getGoalIntensity() == null) {
            return profile.getGoal() == Goal.LOSE ? maintenance - 500 : maintenance + 500;
        }

        int adjustment = switch (profile.getGoalIntensity()) {
            case MILD -> profile.getGoal() == Goal.LOSE ? -300 : 250;
            case STANDARD -> profile.getGoal() == Goal.LOSE ? -500 : 500;
            case AGGRESSIVE -> profile.getGoal() == Goal.LOSE ? -750 : 750;
        };

        return maintenance + adjustment;
    }

    // ---------------- GET PROFILE ----------------
    public ProfileResponse getProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileRepository.findByUser_Id(user.getId()).orElse(null);
        if (profile == null || !profile.isProfileCompleted()) return null;

        double bmi = calculateBMI(profile.getWeight(), profile.getHeight());
        double bmr = calculateBMR(profile);
        double maintenance = maintenanceCalories(profile);
        double dailyCalories = finalDailyCalories(profile);

        return ProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .age(profile.getAge())
                .gender(profile.getGender())
                .height(profile.getHeight())
                .weight(profile.getWeight())
                .bmi(bmi)
                .bmiCategory(bmiCategory(bmi))
                .bmr((double) Math.round(bmr))
                .activityLevel(profile.getActivityLevel() != null ? profile.getActivityLevel().name() : null)
                .goal(profile.getGoal() != null ? profile.getGoal().name() : null)
                .goalIntensity(profile.getGoalIntensity() != null ? profile.getGoalIntensity().name() : null)
                .maintenanceCalories((double) Math.round(maintenance))
                .dailyCalories((double) Math.round(dailyCalories))
                .build();
    }

    // ---------------- UPDATE / SAVE PROFILE ----------------
    public ProfileResponse updateProfile(String email, ProfileUpdateRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileRepository.findByUser_Id(user.getId())
                .orElse(Profile.builder().user(user).build());

        profile.setAge(request.getAge());
        profile.setGender(request.getGender());
        profile.setHeight(request.getHeight());
        profile.setWeight(request.getWeight());

        // Handle enums safely
        if (request.getActivityLevel() != null)
            profile.setActivityLevel(ActivityLevel.valueOf(request.getActivityLevel().toUpperCase()));
        if (request.getGoal() != null)
            profile.setGoal(Goal.valueOf(request.getGoal().toUpperCase()));
        if (request.getGoalIntensity() != null)
            profile.setGoalIntensity(GoalIntensity.valueOf(request.getGoalIntensity().toUpperCase()));

        profile.setProfileCompleted(true);
        profileRepository.save(profile);

        // calculate values dynamically
        double bmi = calculateBMI(profile.getWeight(), profile.getHeight());
        double bmr = calculateBMR(profile);
        double maintenance = maintenanceCalories(profile);
        double dailyCalories = finalDailyCalories(profile);

        return ProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .age(profile.getAge())
                .gender(profile.getGender())
                .height(profile.getHeight())
                .weight(profile.getWeight())
                .bmi(bmi)
                .bmiCategory(bmiCategory(bmi))
                .bmr((double) Math.round(bmr))
                .activityLevel(profile.getActivityLevel() != null ? profile.getActivityLevel().name() : null)
                .goal(profile.getGoal() != null ? profile.getGoal().name() : null)
                .goalIntensity(profile.getGoalIntensity() != null ? profile.getGoalIntensity().name() : null)
                .maintenanceCalories((double) Math.round(maintenance))
                .dailyCalories((double) Math.round(dailyCalories))
                .build();
    }

    // ---------------- PREVIEW CALORIES ----------------
    public ProfileResponse previewCalories(ProfileRequest request) {

        Profile temp = new Profile();
        temp.setAge(request.getAge());
        temp.setGender(request.getGender());
        temp.setHeight(request.getHeight());
        temp.setWeight(request.getWeight());

        if (request.getActivityLevel() != null)
            temp.setActivityLevel(ActivityLevel.valueOf(String.valueOf(request.getActivityLevel())));
        if (request.getGoal() != null)
            temp.setGoal(Goal.valueOf(String.valueOf(request.getGoal())));
        if (request.getGoalIntensity() != null)
            temp.setGoalIntensity(GoalIntensity.valueOf(String.valueOf(request.getGoalIntensity())));

        double bmi = calculateBMI(temp.getWeight(), temp.getHeight());
        double bmr = calculateBMR(temp);
        double maintenance = maintenanceCalories(temp);
        double daily = finalDailyCalories(temp);

        return ProfileResponse.builder()
                .bmi(bmi)
                .bmiCategory(bmiCategory(bmi))
                .bmr((double) Math.round(bmr))
                .maintenanceCalories((double) Math.round(maintenance))
                .dailyCalories((double) Math.round(daily))
                .activityLevel(temp.getActivityLevel() != null ? temp.getActivityLevel().name() : null)
                .goal(temp.getGoal() != null ? temp.getGoal().name() : null)
                .goalIntensity(temp.getGoalIntensity() != null ? temp.getGoalIntensity().name() : null)
                .build();
    }

}



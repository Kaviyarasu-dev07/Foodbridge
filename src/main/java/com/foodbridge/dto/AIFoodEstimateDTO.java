package com.foodbridge.dto;

public class AIFoodEstimateDTO {
    private String foodName;
    private int quantity;
    private String condition;
    private String foodType;
    private String confidence;

    public AIFoodEstimateDTO() {
    }

    public AIFoodEstimateDTO(String foodName, int quantity, String condition, String foodType, String confidence) {
        this.foodName = foodName;
        this.quantity = quantity;
        this.condition = condition;
        this.foodType = foodType;
        this.confidence = confidence;
    }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public String getFoodType() { return foodType; }
    public void setFoodType(String foodType) { this.foodType = foodType; }
    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public static AIFoodEstimateDTOBuilder builder() {
        return new AIFoodEstimateDTOBuilder();
    }

    public static class AIFoodEstimateDTOBuilder {
        private String foodName;
        private int quantity;
        private String condition;
        private String foodType;
        private String confidence;

        AIFoodEstimateDTOBuilder() {}

        public AIFoodEstimateDTOBuilder foodName(String foodName) { this.foodName = foodName; return this; }
        public AIFoodEstimateDTOBuilder quantity(int quantity) { this.quantity = quantity; return this; }
        public AIFoodEstimateDTOBuilder condition(String condition) { this.condition = condition; return this; }
        public AIFoodEstimateDTOBuilder foodType(String foodType) { this.foodType = foodType; return this; }
        public AIFoodEstimateDTOBuilder confidence(String confidence) { this.confidence = confidence; return this; }

        public AIFoodEstimateDTO build() {
            return new AIFoodEstimateDTO(foodName, quantity, condition, foodType, confidence);
        }
    }
}

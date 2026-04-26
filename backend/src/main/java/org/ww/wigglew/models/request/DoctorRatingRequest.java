package org.ww.wigglew.models.request;

public class DoctorRatingRequest {
    private String doctorId;
    private String appointmentId;
    private int stars;
    private String reviewText;

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
}

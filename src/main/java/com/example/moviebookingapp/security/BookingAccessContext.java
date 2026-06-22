package com.example.moviebookingapp.security;

public record BookingAccessContext(Long userId, boolean admin) {

    public static BookingAccessContext guest() {
        return new BookingAccessContext(null, false);
    }

    public boolean authenticated() {
        return userId != null;
    }
}

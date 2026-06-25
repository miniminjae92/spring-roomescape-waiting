package roomescape.domain.reservation;

public enum ReservationStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    PAYMENT_FAILED,
    CANCELED,
    EXPIRED;

    public boolean occupiesSlot() {
        return this == PENDING_PAYMENT || this == CONFIRMED;
    }
}

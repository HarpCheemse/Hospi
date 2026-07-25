package com.hospi.manage.features.reservation.enums;

/**
 * Reasons a reservation was cancelled.
 */
public enum CancellationReason {
    /** Pending booking expired before confirmation. */
    EXPIRED_PENDING,
    /** Guest requested cancellation. */
    GUEST_CANCELLED,
    /** Staff cancelled the reservation. */
    STAFF_CANCELLED,
    /** Guest did not check in on the arrival date. */
    NO_SHOW
}

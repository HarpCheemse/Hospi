package com.hospi.manage.common.constant;
/** Constants for model attribute and session attribute keys used across controllers. */
public class Attributes {

    /** Sidebar identifier for the currently active staff feature. */
    public static final String ACTIVE_SIDEBAR = "activeSidebar";

    /** Model attribute key for a form-backing object. */
    public static final String FORM = "form";
    /** Model attribute key for a view-model object. */
    public static final String VIEW = "view";

    /** Flash attribute key for a success message. */
    public static final String SUCCESS = "success";
    /** Flash attribute key for an error message. */
    public static final String ERROR = "error";

    /** Sidebar section keys used in staff views. */
    public static final String ACTIVE_BOOKINGS = "ACTIVE_BOOKINGS";
    public static final String CURRENT_STAYS = "CURRENT_STAYS";

    /** Misc model attribute keys used across various views. */
    public static final String RESERVATIONS = "reservations";
    public static final String REVIEW_MAP = "reviewMap";
    public static final String REVIEW_FORM = "reviewForm";
    public static final String PILL_CLASSES = "pillClasses";
    public static final String VERIFY_EMAIL = "verifyEmail";
    public static final String OTP_FORM = "otpForm";
    public static final String OTP_SENT = "otpSent";
    public static final String DRAFT = "draft";
    public static final String GUEST_DETAIL_FORM = "guestDetailForm";
    public static final String AVAILABILITY = "availability";
    public static final String MAX_ROOMS = "maxRooms";
    public static final String DEPOSIT_PERCENTAGE = "depositPercentage";
    public static final String BOOKING_CODE = "bookingCode";
    public static final String ROOM_TYPES = "roomTypes";
    public static final String HOTEL = "hotel";
    public static final String BANNERS = "banners";

    /** Model attribute keys for booking tracking. */
    public static final String TRACKED_BOOKING_CODES = "trackedBookingCodes";
    public static final String TRACKED_EMAIL = "trackedEmail";
    public static final String PENDING_CODE = "pendingCode";
    public static final String BOOKING_DRAFT = "bookingDraft";
    public static final String CHECKED_OUT = "checkedOut";

    /** Session attribute keys for the booking flow wizard. */
    public static final String OTP_TOKEN = "otpToken";
    public static final String PENDING_PAYMENT_KEY = "pendingPaymentKey";

    /** Model attribute keys for the booking flow wizard. */
    public static final String EMAIL = "email";
    public static final String NIGHTS = "nights";
    public static final String TOKEN = "token";

    /** Payment type constant for online booking. */
    public static final String ONLINE_BOOKING = "ONLINE_BOOKING";

    /** Model attribute key for the selected year filter. */
    public static final String SELECTED_YEAR = "selectedYear";

    /** Model attribute key for the selected month filter. */
    public static final String SELECTED_MONTH = "selectedMonth";

    /** Dashboard model attribute keys. */
    public static final String CHECK_INS_TODAY = "checkInsToday";
    public static final String CHECK_OUTS_TODAY = "checkOutsToday";
    public static final String PENDING_COUNT = "pendingCount";
    public static final String REVENUE_THIS_MONTH = "revenueThisMonth";
    public static final String REVENUE_CHANGE = "revenueChange";
    public static final String TOTAL_ROOMS = "totalRooms";
    public static final String OCCUPIED_ROOMS = "occupiedRooms";
    public static final String VACANT_ROOMS = "vacantRooms";
    public static final String DIRTY_ROOMS = "dirtyRooms";
    public static final String MAINTENANCE_ROOMS = "maintenanceRooms";
    public static final String RECENT_RESERVATIONS = "recentReservations";
    public static final String STAFF_NAME = "staffName";
    public static final String STAFF_ROLE = "staffRole";
    public static final String UNREAD_NOTIFICATION_COUNT = "unreadNotificationCount";
    public static final String RECENT_NOTIFICATIONS = "recentNotifications";

}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package constant;

/**
 *
 * @author HarpCheemse
 */
public class Views {

    public static final String HOME = "index";

    //MANAGER
    public static final String MANAGER_VIEW_PATH
            = "views/staff/manager/";

    public static final String MANAGER_DASHBOARD
            = MANAGER_VIEW_PATH + "dashboard";

    public static final String MANAGER_ROOM_TYPES
            = MANAGER_VIEW_PATH + "room-types/room-types";
    public static final String MANAGER_ROOM_TYPES_CREATE
            = MANAGER_VIEW_PATH + "room-types/create";
    public static final String MANAGER_ROOM_TYPES_EDIT
            = MANAGER_VIEW_PATH + "room-types/edit";

    public static final String MANAGER_RESERVATIONS
            = MANAGER_VIEW_PATH + "reservations";
    public static final String MANAGER_NOTIFICATIONS
            = MANAGER_VIEW_PATH + "notifications";
    public static final String MANAGER_TASKS
            = MANAGER_VIEW_PATH + "tasks";
    public static final String MANAGER_HOTEL_DETAILS
            = MANAGER_VIEW_PATH + "hotel-details";
    public static final String MANAGER_REVENUES
            = MANAGER_VIEW_PATH + "revenues";
    public static final String MANAGER_BUFFETS
            = MANAGER_VIEW_PATH + "buffets";

    //RECEPTIONIST
    public static final String RECEPTIONIST_VIEW_PATH
            = "views/staff/receptionist/";
    public static final String RECEPTIONIST_DASHBOARD = RECEPTIONIST_VIEW_PATH + "dashboard";
    public static final String RECEPTIONIST_RESERVATIONS = RECEPTIONIST_VIEW_PATH + "reservations";
    public static final String RECEPTIONIST_ROOM_STATUS = RECEPTIONIST_VIEW_PATH + "room-status";
    public static final String RECEPTIONIST_BUFFETS = RECEPTIONIST_VIEW_PATH + "buffets";
    public static final String RECEPTIONIST_REQUESTS = RECEPTIONIST_VIEW_PATH + "requests";
    public static final String RECEPTIONIST_NOTIFICATIONS = RECEPTIONIST_VIEW_PATH + "notifications";
    public static final String RECEPTIONIST_TASKS = RECEPTIONIST_VIEW_PATH + "tasks";

    //ADMIN
    public static final String ADMIN_VIEW_PATH
            = "views/staff/admin/";
    public static final String ADMIN_DASHBOARD = ADMIN_VIEW_PATH + "dashboard";
    public static final String ADMIN_STAFF_ACCOUNTS = ADMIN_VIEW_PATH + "staff-accounts";
    public static final String ADMIN_SYSTEM_CONFIGS = ADMIN_VIEW_PATH + "system-configs";
    public static final String ADMIN_NOTIFICATIONS = ADMIN_VIEW_PATH + "notifications";

    //MANAGER
    public static final String LEADER_VIEW_PATH
            = "views/staff/leader/";
    public static final String LEADER_DASHBOARD = LEADER_VIEW_PATH + "dashboard";
    public static final String LEADER_TASKS = LEADER_VIEW_PATH + "tasks";

}

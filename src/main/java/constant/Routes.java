/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package constant;

public class Routes {

    public static final String HOME = "/";

    //MANAGER
    private static final String BASE_MANAGER_ROUTE = "/manager";
    public static final String MANAGER_DASHBOARD = BASE_MANAGER_ROUTE;
    public static final String MANAGER_ROOM_TYPES = BASE_MANAGER_ROUTE + "/room-types";
    public static final String MANAGER_RESERVATIONS = BASE_MANAGER_ROUTE + "/reservation";
    public static final String MANAGER_NOTIFICATION = BASE_MANAGER_ROUTE + "/notifications";
    public static final String MANAGER_TASKS = BASE_MANAGER_ROUTE + "/tasks";
    public static final String MANAGER_HOTEL_DETAILS = BASE_MANAGER_ROUTE + "/hotel-details";
    public static final String MANAGER_REVENUES = BASE_MANAGER_ROUTE + "/revenues";
    public static final String MANAGER_BUFFETS = BASE_MANAGER_ROUTE + "/buffets";

    //RECEPTIONIST
    private static final String BASE_RECEPTIONIST_ROUTE = "/receptionist";
    public static final String RECEPTIONIST_DASHBOARD = BASE_RECEPTIONIST_ROUTE;
    public static final String RECEPTIONIST_RESERVATIONS
            = BASE_RECEPTIONIST_ROUTE + "/reservations";
    public static final String RECEPTIONIST_ROOM_STATUS
            = BASE_RECEPTIONIST_ROUTE + "/room-status";
    public static final String RECEPTIONIST_BUFFETS
            = BASE_RECEPTIONIST_ROUTE + "/buffets";
    public static final String RECEPTIONIST_REQUESTS
            = BASE_RECEPTIONIST_ROUTE + "/requests";
    public static final String RECEPTIONIST_NOTIFICATIONS
            = BASE_RECEPTIONIST_ROUTE + "/notifications";
    public static final String RECEPTIONIST_TASKS
            = BASE_RECEPTIONIST_ROUTE + "/tasks";

    //ADMIN
    private static final String BASE_ADMIN_ROUTE = "/admin";
    public static final String ADMIN_DASHBOARD = BASE_ADMIN_ROUTE;
    public static final String ADMIN_STAFF_ACCOUNTS = BASE_ADMIN_ROUTE + "staff-accounts";
    public static final String ADMIN_SYSTEM_CONFIGS = BASE_ADMIN_ROUTE + "system-configs";
    public static final String ADMIN_NOTIFICATIONS = BASE_ADMIN_ROUTE + "notifications";
}

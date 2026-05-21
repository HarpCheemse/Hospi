package controller;

import dal.LocationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.Location;
import utils.CloudinaryUtils;
import utils.EmailUtils;
import utils.PayPalUtils;

@WebServlet("/test/*")
@MultipartConfig
public class TestController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        if (path == null) {
            path = "/";
        }

        switch (path) {
            case "/db":
                LocationDAO locationDAO = new LocationDAO();
                List<Location> locations = new ArrayList<>();
                try {
                    locations = locationDAO.getAll();
                    req.setAttribute("locations", locations);
                    req.getRequestDispatcher("/WEB-INF/views/test/db-test.jsp").forward(req, res);
                    break;
                } catch (Exception e) {
                    System.out.println(e);
                }
                break;
            case "/upload":

                req.getRequestDispatcher("/WEB-INF/views/test/upload-test.jsp")
                        .forward(req, res);
                break;
            case "/gmail":
                req.getRequestDispatcher("/WEB-INF/views/test/gmail-test.jsp")
                        .forward(req, res);
                break;
            case "/payment":
                req.getRequestDispatcher("/WEB-INF/views/test/payment-test.jsp")
                        .forward(req, res);
                break;
            case "/payment/success":
                handlePaymentSuccess(req, res);
                break;
            case "/payment/cancel":
                req.setAttribute("cancelled", "Payment was cancelled.");
                req.getRequestDispatcher("/WEB-INF/views/test/payment-test.jsp")
                        .forward(req, res);
                break;
            default:
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        if (path == null) {
            path = "/";
        }

        switch (path) {
            case "/upload":
                handleUpload(req, res);
                break;
            case "/gmail":
                handleGmail(req, res);
                break;
            case "/payment":
                handlePaymentCreate(req, res);
                break;
            default:
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleUpload(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        Part filePart = req.getPart("image");

        if (filePart == null || filePart.getSize() == 0) {
            System.out.println("[UploadTest] No file received");
            res.sendError(400, "No file uploaded");
            return;
        }

        String url = CloudinaryUtils.upload(filePart, "test");
        System.out.println("[UploadTest] Upload success: " + url);

        req.setAttribute("uploadedUrl", url);
        req.getRequestDispatcher("/WEB-INF/views/test/upload-test.jsp")
                .forward(req, res);
    }

    private void handleGmail(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String toEmail = req.getParameter("toEmail");
        String subject = req.getParameter("subject");
        String body = req.getParameter("body");

        try {
            EmailUtils.send(toEmail, subject, body);
            System.out.println("[GmailTest] Email sent to: " + toEmail);
            req.setAttribute("success", "Email sent successfully to " + toEmail);
        } catch (Exception e) {
            System.out.println("[GmailTest] Failed: " + e.getMessage());
            req.setAttribute("error", "Failed to send email: " + e.getMessage());
        }

        req.getRequestDispatcher("/WEB-INF/views/test/gmail-test.jsp")
                .forward(req, res);
    }

    private void handlePaymentCreate(HttpServletRequest req,
            HttpServletResponse res) throws ServletException, IOException {

        String amount = req.getParameter("amount");
        String base = req.getScheme() + "://" + req.getServerName()
                + ":" + req.getServerPort()
                + req.getContextPath();

        String returnUrl = base + "/test/payment/success";
        String cancelUrl = base + "/test/payment/cancel";

        try {
            String approvalUrl = PayPalUtils.createOrder(
                    amount, returnUrl, cancelUrl);
            System.out.println("[PaymentTest] Redirecting to: " + approvalUrl);
            res.sendRedirect(approvalUrl);
        } catch (Exception e) {
            System.out.println("[PaymentTest] Failed: " + e.getMessage());
            req.setAttribute("error", "Payment failed: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/test/payment-test.jsp")
                    .forward(req, res);
        }
    }

    private void handlePaymentSuccess(HttpServletRequest req,
            HttpServletResponse res) throws ServletException, IOException {

        String orderId = req.getParameter("token");
        System.out.println("[PaymentTest] Capturing order: " + orderId);

        try {
            boolean paid = PayPalUtils.captureOrder(orderId);
            if (paid) {
                req.setAttribute("success",
                        "Payment captured! Order ID: " + orderId);
            } else {
                req.setAttribute("error", "Capture failed — order not completed.");
            }
        } catch (Exception e) {
            System.out.println("[PaymentTest] Capture failed: " + e.getMessage());
            req.setAttribute("error", "Capture error: " + e.getMessage());
        }

        req.getRequestDispatcher("/WEB-INF/views/test/payment-test.jsp")
                .forward(req, res);
    }
}

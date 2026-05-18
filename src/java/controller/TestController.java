package controller;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author HarpCheemse
 */
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.Map;
import utils.CloudinaryUtils;

@WebServlet("/test/upload")
@MultipartConfig
public class TestController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/test/upload-test.jsp")
                .forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
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
}

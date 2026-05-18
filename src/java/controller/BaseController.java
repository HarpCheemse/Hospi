package controller;

import constant.Views;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
public abstract class BaseController extends HttpServlet {

    protected void view(HttpServletRequest req, HttpServletResponse res, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher(
                "/WEB-INF/" + view + ".jsp"
        ).forward(req, res);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse res, String route)
            throws ServletException, IOException {
        res.sendRedirect(req.getContextPath() + route);
    }
}

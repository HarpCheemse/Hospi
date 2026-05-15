<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Hospi</title>
        <!-- BASE -->
        <%@include file="/WEB-INF/views/layout/header.jsp"%>
        <!-- COMPONENTS -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components/button.css">
        <!-- LAYOUT -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout/grid.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout/nav.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout/footer.css">
        <!-- PAGE -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/pages/home.css">
    </head>
    <body>

        <header class="header">
            <div class="container flex items-center justify-between">
                <div class="logo flex items-center gap-2">
                    <img src="images/logo.png" alt="Hospi logo">
                    <h2>Hospi</h2>
                </div>
                <nav class="nav flex items-center gap-3">
                    <a href="#" class="nav-link">About</a>
                    <a href="#" class="nav-link">Info</a>
                    <a href="#" class="nav-link">Bookings</a>
                    <a href="#" class="nav-link btn btn-primary">Login</a>
                </nav>
            </div>
        </header>

        <section class="hero">
            <div class="container grid grid-cols-2 items-center gap-5">
                <div class="hero-left flex flex-col gap-4">
                    <h1>The solution<br>to <em>booking</em></h1>
                    <p>Enjoy the breathtaking view of the nature.<br>Relax and cherish your dreams to the fullest.</p>
                    <button class="btn btn-primary">Book now</button>
                </div>
                <div class="grid grid-cols-2 gap-3 hero-collage">
                    <img class="collage-img" src="images/hotel2.jpg" alt="Hotel room">
                    <img class="collage-img" src="images/hotel3.jpg" alt="Hotel pool">
                    <img class="collage-img collage-img--tall" src="images/hotel1.jpg" alt="Hotel lobby">
                </div>
            </div>
        </section>

        <footer class="footer">
            <div class="container flex items-center justify-between">
                <div class="logo flex items-center gap-2">
                    <img src="images/logo.png" alt="Hospi logo">
                    <span>Hospi</span>
                </div>
                <p class="text-sm text-tertiary">© 2026 Hospi. All rights reserved.</p>
                <nav class="flex items-center gap-3">
                    <a href="#" class="nav-link">About</a>
                    <a href="#" class="nav-link">Info</a>
                    <a href="#" class="nav-link">Bookings</a>
                </nav>
            </div>
        </footer>

    </body>
</html>
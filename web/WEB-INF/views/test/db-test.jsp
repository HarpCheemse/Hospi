<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Locations Test</title>
    </head>
    <body>

        <h1>Locations</h1>

        <ul>
            <c:forEach items="${locations}" var="loc">
                <li>
                    ${loc.id} - ${loc.name}
                </li>
            </c:forEach>
        </ul>

    </body>
</html>
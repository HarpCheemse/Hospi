<%@ page contentType="text/html;charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
    <body>

        <h2>Cloudinary Upload Test</h2>

        <form action="${pageContext.request.contextPath}/test/upload"
              method="post"
              enctype="multipart/form-data">
            <input type="file" name="image" accept="image/*" />
            <button type="submit">Upload</button>
        </form>

        <%-- Show result if upload happened --%>
    <c:if test="${not empty uploadedUrl}">
        <p>URL: <a href="${uploadedUrl}">${uploadedUrl}</a></p>
        <img src="${uploadedUrl}" width="300" />
    </c:if>

</body>
</html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
    <body>
        <h2>Gmail Send Test</h2>

        <form action="${pageContext.request.contextPath}/test/gmail"
              method="post">
            <label>To Email:</label><br/>
            <input type="email" name="toEmail" placeholder="recipient@gmail.com" required />
            <br/><br/>
            <label>Subject:</label><br/>
            <input type="text" name="subject" value="Test Email from Hospi" required />
            <br/><br/>
            <label>Message:</label><br/>
            <textarea name="body" rows="4" cols="40">This is a test email from Hospi hotel system.</textarea>
            <br/><br/>
            <button type="submit">Send Email</button>
        </form>

        <%-- Show result --%>
    <c:if test="${not empty success}">
        <p style="color:green">${success}</p>
    </c:if>
    <c:if test="${not empty error}">
        <p style="color:red">${error}</p>
    </c:if>

</body>
</html>
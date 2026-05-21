<%@ page contentType="text/html;charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
    <body>
        <h2>PayPal Payment Test</h2>

        <form action="${pageContext.request.contextPath}/test/payment"
              method="post">
            <label>Amount (USD):</label><br/>
            <input type="number" name="amount" value="10.00"
                   step="0.01" min="1" required />
            <br/><br/>
            <button type="submit">Pay with PayPal</button>
        </form>

        <%-- success --%>
    <c:if test="${not empty success}">
        <p style="color:green">${success}</p>
    </c:if>

    <%-- error --%>
    <c:if test="${not empty error}">
        <p style="color:red">${error}</p>
    </c:if>

    <%-- cancelled --%>
    <c:if test="${not empty cancelled}">
        <p style="color:orange">${cancelled}</p>
    </c:if>
</body>
</html>
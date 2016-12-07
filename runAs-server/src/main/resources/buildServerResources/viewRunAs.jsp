<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="bean" class="jetbrains.buildServer.runAs.server.RunAsBean"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<div class="parameter">
  Run as user: <strong><props:displayValue name="${bean.runAsUserKey}" emptyValue="none specified"/></strong>

  <div class="parameter">
    Windows Integrity Level:
    <strong>
      <c:set var="type" value="${propertiesBean.properties[bean.windowsIntegrityLevelKey]}"/>
      <c:forEach var="item" items="${bean.windowsIntegrityLevels}">
        <c:if test="${type eq item.value}"><c:out value="${item.description}"/></c:if>
      </c:forEach>
    </strong>
  </div>

  <div class="parameter">
    Logging Level:
    <strong>
      <c:set var="type" value="${propertiesBean.properties[bean.windowsLoggingLevelKey]}"/>
      <c:forEach var="item" items="${bean.loggingLevels}">
        <c:if test="${type eq item.value}"><c:out value="${item.description}"/></c:if>
      </c:forEach>
    </strong>
  </div>

  Additional parameters: <strong><props:displayValue name="${bean.additionalCommandLineParametersKey}" emptyValue="none specified"/></strong>

</div>
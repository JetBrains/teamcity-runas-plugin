<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%--
  ~ Copyright 2000-2022 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

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
    Windows Logging Level:
    <strong>
      <c:set var="type" value="${propertiesBean.properties[bean.windowsLoggingLevelKey]}"/>
      <c:forEach var="item" items="${bean.loggingLevels}">
        <c:if test="${type eq item.value}"><c:out value="${item.description}"/></c:if>
      </c:forEach>
    </strong>
  </div>

  Additional parameters: <strong><props:displayValue name="${bean.additionalCommandLineParametersKey}" emptyValue="none specified"/></strong>

</div>
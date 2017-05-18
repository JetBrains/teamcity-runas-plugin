<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="buildForm" type="jetbrains.buildServer.controllers.admin.projects.BuildTypeForm" scope="request"/>
<jsp:useBean id="bean" class="jetbrains.buildServer.runAs.server.RunAsBean"/>

<script type="text/javascript">
  BS.RunAsFeature = {
    showHomePage: function() {
      var winSize = BS.Util.windowSize();
      BS.Util.popupWindow('https://github.com/JetBrains/teamcity-runas-plugin/wiki', 'runAs', { width: 0.9 * winSize[0], height: 0.9 * winSize[1] });
      BS.stopPropagation(event);
    }
  }
</script>

<tr>
  <td colspan="2"><em>This build feature allows running build steps under the specified user account <i class='icon-external-link' title='Open in new window' onclick='BS.RunAsFeature.showHomePage()'/i></em></td>
</tr>

<tr>
  <th><label for="${bean.runAsUserKey}">Username:</label></th>
  <td>
    <div class="posRel">
      <props:textProperty name="${bean.runAsUserKey}" className="longField"/>
    </div>
    <span class="error" id="error_${bean.runAsUserKey}"></span>
    <span class="smallNote">Specify the username</span>
  </td>
</tr>

<tr>
  <th><label for="${bean.runAsPasswordKey}">Password:</label></th>
  <td>
    <div class="posRel">
      <props:passwordProperty name="${bean.runAsPasswordKey}" className="longField"/>
    </div>
    <span class="error" id="error_${bean.runAsPasswordKey}"></span>
    <span class="smallNote">Specify the password</span>
  </td>
</tr>

<tr>
  <th rowspan="2"><label>
    dows: </label></th>
  <td>
    <label for="${bean.windowsIntegrityLevelKey}" class="fixedLabel">Integrity:</label>
    <props:selectProperty name="${bean.windowsIntegrityLevelKey}" enableFilter="true" className="smallField">
      <c:forEach var="item" items="${bean.windowsIntegrityLevels}">
        <props:option value="${item.value}"><c:out value="${item.description}"/></props:option>
      </c:forEach>
    </props:selectProperty>
    <span class="smallNote">Select Windows integrity level</span>
  </td>
</tr>

<tr>
  <td>
    <label for="${bean.windowsLoggingLevelKey}" class="fixedLabel">Logging:</label>
    <props:selectProperty name="${bean.windowsLoggingLevelKey}" enableFilter="true" className="smallField">
      <c:forEach var="item" items="${bean.loggingLevels}">
        <props:option value="${item.value}"><c:out value="${item.description}"/></props:option>
      </c:forEach>
    </props:selectProperty>
    <span class="smallNote">Select logging level</span>
  </td>
</tr>

<tr>
  <th rowspan="2"/>
  <td>
    <label for="${bean.additionalCommandLineParametersKey}">Additional parameters:</label>
    <props:textProperty name="${bean.additionalCommandLineParametersKey}" className="longField"/>
    <span class="error" id="error_${bean.additionalCommandLineParametersKey}"></span>
    <span class="smallNote">Enter additional parameters</span>
  </td>
</tr>

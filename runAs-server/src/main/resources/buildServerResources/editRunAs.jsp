<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="buildForm" type="jetbrains.buildServer.controllers.admin.projects.BuildTypeForm" scope="request"/>
<jsp:useBean id="bean" class="jetbrains.buildServer.runAs.server.RunAsBean"/>

<script type="text/javascript">
  BS.RunAs = {
    showHomePage: function() {
      var winSize = BS.Util.windowSize();
      BS.Util.popupWindow('https://github.com/JetBrains/teamcity-runas-plugin/wiki', 'runAs', { width: 0.9 * winSize[0], height: 0.9 * winSize[1] });
      BS.stopPropagation(event);
    }
  }
</script>

<l:settingsGroup title="Run as <i class='icon-external-link' title='Open in new window'/i>">
  <tr class="advancedSetting">
    <th><label for="${bean.runAsUserKey}">User name:</label></th>
    <td>
      <div class="completionIconWrapper">
        <props:textProperty name="${bean.runAsUserKey}" className="longField"/>
      </div>
      <span class="error" id="error_${bean.runAsUserKey}"></span>
      <span class="smallNote">Specify the user name.</span>
    </td>
  </tr>

  <tr class="advancedSetting">
    <th><label for="${bean.runAsPasswordKey}">Password:</label></th>
    <td>
      <div class="completionIconWrapper">
        <props:passwordProperty name="${bean.runAsPasswordKey}" className="longField"/>
      </div>
      <span class="error" id="error_${bean.runAsPasswordKey}"></span>
      <span class="smallNote">Specify the password.</span>
    </td>
  </tr>

  <tr class="advancedSetting">
    <th rowspan="2"><label>Windows: </label></th>
    <td>
      <label for="${bean.windowsIntegrityLevelKey}" class="fixedLabel">Integrity Level:</label>
      <props:selectProperty name="${bean.windowsIntegrityLevelKey}" enableFilter="true" className="smallField">
        <c:forEach var="item" items="${bean.windowsIntegrityLevels}">
          <props:option value="${item.value}"><c:out value="${item.description}"/></props:option>
        </c:forEach>
      </props:selectProperty>
    </td>
  </tr>

  <tr class="advancedSetting">
    <td>
      <label for="${bean.windowsLoggingLevelKey}" class="fixedLabel">Logging Level:</label>
      <props:selectProperty name="${bean.windowsLoggingLevelKey}" enableFilter="true" className="smallField">
        <c:forEach var="item" items="${bean.loggingLevels}">
          <props:option value="${item.value}"><c:out value="${item.description}"/></props:option>
        </c:forEach>
      </props:selectProperty>
    </td>
  </tr>

  <tr class="advancedSetting">
    <th><label for="${bean.additionalCommandLineParametersKey}">Additional parameters:</label></th>
    <td>
      <div class="posRel">
        <props:textProperty name="${bean.additionalCommandLineParametersKey}" className="longField"/>
      </div>
      <span class="error" id="error_${bean.additionalCommandLineParametersKey}"></span>
      <span class="smallNote">Enter additional parameters</span>
    </td>
  </tr>

</l:settingsGroup>
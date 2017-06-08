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
    },

    showIntegrityLevelsPage: function() {
     var winSize = BS.Util.windowSize();
     BS.Util.popupWindow('https://github.com/JetBrains/runAs/wiki#windows-integrity-mechanism', 'runAs', {width: 0.9 * winSize[0], height: 0.9 * winSize[1]});
     BS.stopPropagation(event);
    },

    showCommandLinePage: function() {
      var winSize = BS.Util.windowSize();
      BS.Util.popupWindow('https://github.com/JetBrains/runAs/blob/master/README.md#command-line-arguments', 'runAs', {width: 0.9 * winSize[0], height: 0.9 * winSize[1]});
      BS.stopPropagation(event);
    }
  }
</script>

<l:settingsGroup title="Run as <a class='helpIcon' onclick='BS.RunAs.showHomePage()' title='View help'><i class='icon icon16 tc-icon_help_small'></i></a>">
  <tr class="advancedSetting">
    <th><label for="${bean.runAsUserKey}">Username:</label></th>
    <td>
      <div class="posRel">
        <props:textProperty name="${bean.runAsUserKey}" className="longField"/>
      </div>
      <span class="error" id="error_${bean.runAsUserKey}"></span>
      <span class="smallNote">Specify the username</span>
    </td>
  </tr>

  <tr class="advancedSetting">
    <th><label for="${bean.runAsPasswordKey}">Password:</label></th>
    <td>
      <div class="posRel">
        <props:passwordProperty name="${bean.runAsPasswordKey}" className="longField"/>
      </div>
      <span class="error" id="error_${bean.runAsPasswordKey}"></span>
      <span class="smallNote">Specify the password</span>
    </td>
  </tr>

  <tr class="advancedSetting">
    <th rowspan="3"><label>Windows: </label></th>
    <td>
      <label for="${bean.windowsIntegrityLevelKey}">Integrity:
        <a class="helpIcon" onclick="BS.RunAs.showIntegrityLevelsPage()" title="View help"><bs:helpIcon/></a>
      </label>
      <props:selectProperty name="${bean.windowsIntegrityLevelKey}" enableFilter="true" className="smallField">
        <c:forEach var="item" items="${bean.windowsIntegrityLevels}">
          <props:option value="${item.value}"><c:out value="${item.description}"/></props:option>
        </c:forEach>
      </props:selectProperty>
      <span class="smallNote">Select Windows integrity level</span>
    </td>
  </tr>

  <tr class="advancedSetting">
    <td>
      <label for="${bean.windowsLoggingLevelKey}">Logging:</label>
      <props:selectProperty name="${bean.windowsLoggingLevelKey}" enableFilter="true" className="smallField">
        <c:forEach var="item" items="${bean.loggingLevels}">
          <props:option value="${item.value}"><c:out value="${item.description}"/></props:option>
        </c:forEach>
      </props:selectProperty>
      <span class="smallNote">Select logging level</span>
    </td>
  </tr>

  <tr class="advancedSetting">
    <td>
      <label for="${bean.additionalCommandLineParametersKey}">Additional parameters:
        <a class="helpIcon" onclick="BS.RunAs.showCommandLinePage()" title="View help"><bs:helpIcon/></a>
      </label>
      <br/>
      <props:textProperty name="${bean.additionalCommandLineParametersKey}" className="longField"/>
      <span class="error" id="error_${bean.additionalCommandLineParametersKey}"></span>
      <span class="smallNote">Enter additional parameters</span>
    </td>
  </tr>

</l:settingsGroup>

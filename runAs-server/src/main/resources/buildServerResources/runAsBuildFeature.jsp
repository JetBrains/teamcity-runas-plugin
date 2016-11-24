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
  <th><label for="${bean.runAsUserKey}">User name: <l:star/></label></th>
  <td>
    <div class="completionIconWrapper">
      <props:textProperty name="${bean.runAsUserKey}" className="longField"/>
    </div>
    <span class="error" id="error_${bean.runAsUserKey}"></span>
    <span class="smallNote">Specify the user name."</span>
  </td>
</tr>

<tr>
  <th><label for="${bean.runAsPasswordKey}">Password: <l:star/></label></th>
  <td>
    <div class="completionIconWrapper">
      <props:passwordProperty name="${bean.runAsPasswordKey}" className="longField"/>
    </div>
    <span class="error" id="error_${bean.runAsPasswordKey}"></span>
    <span class="smallNote">Specify the password.</span>
  </td>
</tr>

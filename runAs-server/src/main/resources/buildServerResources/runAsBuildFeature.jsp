<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="buildForm" type="jetbrains.buildServer.controllers.admin.projects.BuildTypeForm" scope="request"/>
<jsp:useBean id="bean" class="jetbrains.buildServer.runAs.server.RunAsBean"/>

<style type="text/css">
</style>

<tr>
  <td colspan="2"><em>This build feature allows running build steps under the specified user account.</em></td>
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

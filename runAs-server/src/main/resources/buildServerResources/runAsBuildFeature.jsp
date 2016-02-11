<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="buildForm" type="jetbrains.buildServer.controllers.admin.projects.BuildTypeForm" scope="request"/>
<jsp:useBean id="constants" scope="request" class="jetbrains.buildServer.ssh.SshKeyBuildFeatureConstants"/>

<style type="text/css">

</style>
<script type="text/javascript">
  BS.SshAgentBuildFeature = {
    keySelected: function(encrypted) {
      if (encrypted) {
        $j('#passphraseRow').show();
      } else {
        $j('#secure\\:passphrase').val('');
        $j('#passphraseRow').hide();
      }
    }
  };
</script>

<tr>
  <td colspan="2"><em>This build feature runs an SSH agent with an uploaded SSH key during a build.</em></td>
</tr>
<tr>
  <th>
    <label for="${constants.sshKeyProp}">Uploaded key:</label>
  </th>
  <td>
    <admin:sshKeys projectId="${buildForm.project.externalId}" keySelectionCallback="BS.SshAgentBuildFeature.keySelected"/>
    <span class="error" id="error_${constants.sshKeyProp}"></span>
  </td>
</tr>
<tr id="passphraseRow" style="display: none;">
  <th>
    <label for="secure:passphrase">Passphrase:</label>
  </th>
  <td>
    <props:passwordProperty name="secure:passphrase"/>
  </td>
</tr>
<script type="text/javascript">
  var encrypted = $j('#${constants.sshKeyProp}').find(':selected').hasClass('encrypted');
  BS.SshAgentBuildFeature.keySelected(encrypted);
</script>
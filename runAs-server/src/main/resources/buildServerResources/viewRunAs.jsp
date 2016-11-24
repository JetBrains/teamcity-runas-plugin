<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="bean" class="jetbrains.buildServer.runAs.server.RunAsBean"/>

<div class="parameter">
  Run as user: <strong><props:displayValue name="${bean.runAsUserKey}" emptyValue="none specified"/></strong>
</div>
<fly:composition freeLayout="N">
	<fly:labelObject buddy="${formId}:hostname" text="主机名称" width="30%" />
	<fly:inputText id="${formId}:hostname" name="${formId}:hostname" type="text" value="${dbMeta.hostname}" width="65%" class="ui-helper-clearfix" />
	
	<fly:labelObject buddy="${formId}:SAPSystemNumber" text="系统编号" width="30%" />
	<fly:inputText id="${formId}:SAPSystemNumber" name="${formId}:SAPSystemNumber" type="text" value="${dbMeta.attributes['SAPSystemNumber']}" width="65%" class="ui-helper-clearfix" />

	<fly:labelObject buddy="${formId}:SAPClient" text="SAP客户端" width="30%" />
	<fly:inputText id="${formId}:SAPClient" name="${formId}:SAPClient" type="text" value="${dbMeta.attributes['SAPClient']}" width="65%" class="ui-helper-clearfix" />

	<fly:labelObject buddy="${formId}:SAPLanguage" text="语言" width="30%" />
	<fly:inputText id="${formId}:SAPLanguage" name="${formId}:SAPLanguage" type="text" value="${dbMeta.attributes['SAPLanguage']}" width="65%" class="ui-helper-clearfix" />
</fly:composition>

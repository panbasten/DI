<form id="${formId}" class="fly-dialog-form" method="post">

	<fly:verticalLayout margin="10">
		<fly:labelObject buddy="${formId}:check" text="警告和错误" />
		<fly:dataGrid id="${formId}:check" singleSelect="true" data="" height="300">
			<fly:columns>
				<fly:row>
					<fly:column field="key" title="步骤名称" width="100" editor="text" align="center" />
					<fly:column field="value" title="结果" width="100" editor="text" align="center" />
					<fly:column field="desc" title="备注" width="300" editor="text" align="center" />
				</fly:row>
			</fly:columns>
		</fly:dataGrid>
		<fly:inputText id="${formId}:showSuccess" name="${formId}:showSuccess" type="checkbox"
				text="显示成功结果" marginTop="10" />
	</fly:verticalLayout>

</form>
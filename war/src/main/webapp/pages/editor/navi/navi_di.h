<div id="editorContent-navi-${entity.code}" class="fly-editor-tab">
	<div id="editorContent-navi-${entity.code}-bc" class="ui-breadcrumb ui-widget-header ui-helper-clearfix ui-corner-all ui-tranc-priv">
		<div class="ui-toolbar-group-left" style="width: 20%;">
			<ul>
			</ul>
		</div>
		<div class="ui-toolbar-group-right">
			<fly:pushbutton iconCls="ui-icon-folder-open"
            	label="新增" title="新增">
				<fly:menuItem text="新增转换" iconCls="ui-icon-folder-open" onclick="Flywet.di.create('trans')"></fly:menuItem>
				<fly:menuItem text="新增作业" iconCls="ui-icon-folder-open" onclick="Flywet.di.create('job')"></fly:menuItem>
				<fly:menuItem text="新增目录" iconCls="ui-icon-folder-open" onclick="Flywet.di.createDir()"></fly:menuItem>
			</fly:pushbutton>
            <fly:pushbutton iconCls="ui-icon-folder-open"
            	label="编辑" title="编辑"
            	onclick="Flywet.di.edit()" />
            <fly:pushbutton iconCls="ui-icon-folder-open"
            	label="删除" title="删除"
            	onclick="Flywet.di.remove()" />
			<fly:pushbutton iconCls="ui-icon-folder-open"
            	label="上传" title="上传"
            	onclick="Flywet.di.uploadFile()" />
            <fly:pushbutton iconCls="ui-icon-folder-open"
            	label="下载" title="下载"
            	onclick="Flywet.di.downloadFile()" />
		</div>
	</div>
	<div id="editorContent-navi-${entity.code}-bp" class="fly-editor-content-height-browse-panel">
	</div>
</div>
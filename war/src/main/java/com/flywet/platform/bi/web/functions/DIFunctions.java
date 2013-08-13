package com.flywet.platform.bi.web.functions;

import java.util.List;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;

import com.flywet.platform.bi.component.components.selectMenu.OptionsData;
import com.flywet.platform.bi.component.utils.FLYFunctionMapper;

public class DIFunctions {

	private static Class<?> PKG = DIFunctions.class;

	private static String PREFIX = "di";

	private static OptionsData transStatusOptionsData = OptionsData
			.instance(new String[] {
					"",
					BaseMessages.getString(PKG,
							"Page.Trans.Transstatus.Draft.Label"),
					BaseMessages.getString(PKG,
							"Page.Trans.Transstatus.Production.Label") });

	/**
	 * 获得转换状态选项
	 * 
	 * @return
	 */
	public static List<String[]> transStatusOptions() {
		return transStatusOptionsData.getOptions();
	}

	/**
	 * 获得所有数据库连接的选项
	 * 
	 * @param transMeta
	 * @return
	 */
	public static List<String[]> allDatabaseOptions(TransMeta transMeta) {
		OptionsData od = OptionsData.instance();
		DatabaseMeta dm;

		for (int i = 0; i < transMeta.nrDatabases(); i++) {
			dm = transMeta.getDatabase(i);
			od.addOption(dm.getObjectId().getId(), dm.getName());
		}

		return od.getOptions();
	}

	public static void register() {
		if (!FLYFunctionMapper.singleton.contains(PREFIX)) {
			FLYFunctionMapper.singleton.register(PREFIX, DIFunctions.class);
		}
	}
}

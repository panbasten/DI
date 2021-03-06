/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.SAPR3DatabaseMeta;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ConstDB {
	/**
	 * Select the SAP ERP databases in the List of databases.
	 * 
	 * @param databases
	 *            All the databases
	 * @return SAP ERP databases in a List of databases.
	 */
	public static final List<DatabaseMeta> selectSAPR3Databases(
			List<DatabaseMeta> databases) {
		List<DatabaseMeta> sap = new ArrayList<DatabaseMeta>();

		for (DatabaseMeta db : databases) {
			if (db.getDatabaseInterface() instanceof SAPR3DatabaseMeta) {
				sap.add(db);
			}
		}

		return sap;
	}

	/**
	 * 通过字段名称和值的Map，获得RowMetaAndData
	 * 
	 * @param map
	 * @return
	 */
	public static RowMetaAndData getRowMetaAndData(Map<String, Object> map) {
		RowMetaAndData rmd = new RowMetaAndData();
		for (String key : map.keySet()) {
			rmd.addValue(key, getValueMetaType(map.get(key)), map.get(key));
		}
		return rmd;
	}

	public static int getValueMetaType(Object data) {
		int type = ValueMetaInterface.TYPE_STRING;
		if (data == null) {
			type = ValueMetaInterface.TYPE_NONE;
		} else if (data instanceof String) {
		} else if (data instanceof Long || data instanceof Integer) {
			type = ValueMetaInterface.TYPE_INTEGER;
		} else if (data instanceof Double) {
			type = ValueMetaInterface.TYPE_NUMBER;
		} else if (data instanceof BigDecimal) {
			type = ValueMetaInterface.TYPE_BIGNUMBER;
		} else if (data instanceof Date) {
			type = ValueMetaInterface.TYPE_DATE;
		} else if (data instanceof Boolean) {
			type = ValueMetaInterface.TYPE_BOOLEAN;
		} else if (data instanceof byte[]) {
			type = ValueMetaInterface.TYPE_BINARY;
		}

		return type;
	}

	public static ValueMetaInterface getValueMeta(Object data) {
		int type = ValueMetaInterface.TYPE_STRING;
		if (data == null) {
			type = ValueMetaInterface.TYPE_NONE;
		} else if (data instanceof String) {
		} else if (data instanceof Long || data instanceof Integer) {
			type = ValueMetaInterface.TYPE_INTEGER;
		} else if (data instanceof Double) {
			type = ValueMetaInterface.TYPE_NUMBER;
		} else if (data instanceof BigDecimal) {
			type = ValueMetaInterface.TYPE_BIGNUMBER;
		} else if (data instanceof Date) {
			type = ValueMetaInterface.TYPE_DATE;
		} else if (data instanceof Boolean) {
			type = ValueMetaInterface.TYPE_BOOLEAN;
		} else if (data instanceof byte[]) {
			type = ValueMetaInterface.TYPE_BINARY;
		}

		return new ValueMeta("", type);
	}

}

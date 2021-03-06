package com.flywet.platform.bi.services.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.Repository;
import org.springframework.stereotype.Service;

import com.flywet.platform.bi.component.components.browse.BrowseMeta;
import com.flywet.platform.bi.component.components.browse.BrowseNodeMeta;
import com.flywet.platform.bi.component.utils.HTML;
import com.flywet.platform.bi.core.exception.BIException;
import com.flywet.platform.bi.core.exception.BIKettleException;
import com.flywet.platform.bi.core.pools.RepPool;
import com.flywet.platform.bi.core.utils.Utils;
import com.flywet.platform.bi.model.BIDatabaseConnectionDelegate;
import com.flywet.platform.bi.services.intf.BIDatabaseDelegates;

@Service("bi.service.databaseServices")
public class BIDatabaseServices implements BIDatabaseDelegates {

	private final Logger log = Logger.getLogger(BIDatabaseServices.class);

	@Override
	public void getNavigatorsDatabase(BrowseMeta browseMeta) throws BIException {

		Repository rep = null;
		try {
			rep = RepPool.instance().borrowRep();
			List<DatabaseMeta> databaseMetas = rep.getDatabases();
			for (DatabaseMeta d : databaseMetas) {
				BrowseNodeMeta node = new BrowseNodeMeta();
				node.setCategory(Utils.CATEGORY_DB);
				node.setId(d.getObjectId().getId());
				node.addAttribute(BrowseNodeMeta.ATTR_DISPLAY_NAME, d.getName());
				node.addAttribute(BrowseNodeMeta.ATTR_ICON_STYLE, "ui-"
						+ Utils.CATEGORY_DB + "-icon");
				node.addAttribute(HTML.ATTR_TYPE, Utils.DOM_LEAF);
				node.addAttribute(HTML.ATTR_SRC, Utils.CATEGORY_DB + "/object/"
						+ d.getObjectId().getId());
				node.addEvent("mouseup", "Flywet.browse.showOperationForFile");
				node.addEvent("dblclick", "Flywet.browse.openFile");
				browseMeta.addContent(node);
			}
		} catch (Exception ex) {
			log.error("获得数据库对象列表出现异常", ex);
			throw new BIException("获得数据库对象列表出现异常");
		} finally {
			RepPool.instance().returnRep(rep);
		}

	}

	@Override
	public BIDatabaseConnectionDelegate getDatabaseConnectionDelegate(Long id)
			throws BIException {
		DatabaseMeta dbm = getDatabaseMeta(id);
		return new BIDatabaseConnectionDelegate(dbm);
	}

	@Override
	public DatabaseMeta getDatabaseMeta(Long id) throws BIException {
		Repository rep = null;
		try {
			rep = RepPool.instance().borrowRep();
			return rep.loadDatabaseMeta(new LongObjectId(id), null);
		} catch (Exception ex) {
			log.error("获得数据库对象出现异常", ex);
			throw new BIException("获得数据库对象出现异常");
		} finally {
			RepPool.instance().returnRep(rep);
		}
	}

	@Override
	public void saveDatabaseMeta(DatabaseMeta databaseMeta) throws BIException {
		Repository rep = null;
		try {
			rep = RepPool.instance().borrowRep();
			rep.save(databaseMeta, null, null);
		} catch (Exception ex) {
			log.error("保存数据库对象出现异常", ex);
			throw new BIException("保存数据库对象出现异常");
		} finally {
			RepPool.instance().returnRep(rep);
		}
	}

	@Override
	public void deleteDatabaseMeta(String dbName) throws BIException {
		Repository rep = null;
		try {
			rep = RepPool.instance().borrowRep();
			rep.deleteDatabaseMeta(dbName);
		} catch (Exception ex) {
			log.error("删除数据库对象出现异常", ex);
			throw new BIException("删除数据库对象出现异常");
		} finally {
			RepPool.instance().returnRep(rep);
		}
	}

	@Override
	public boolean existDatabaseMeta(String dbName) throws BIKettleException {
		Repository rep = null;
		try {
			rep = RepPool.instance().borrowRep();
			return rep.getDatabaseID(dbName) != null;
		} catch (Exception ex) {
			log.error("判断数据库对象是否存在出现异常", ex);
		} finally {
			RepPool.instance().returnRep(rep);
		}
		return false;
	}

}

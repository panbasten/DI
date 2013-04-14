package com.plywet.platform.bi.web.service.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.plywet.platform.bi.component.components.breadCrumb.BreadCrumbMeta;
import com.plywet.platform.bi.component.components.browse.BrowseMeta;
import com.plywet.platform.bi.component.components.browse.BrowseNodeMeta;
import com.plywet.platform.bi.component.utils.HTML;
import com.plywet.platform.bi.core.exception.BIException;
import com.plywet.platform.bi.core.utils.Utils;
import com.plywet.platform.bi.delegates.intf.BIReportAdaptor;
import com.plywet.platform.bi.delegates.utils.BIAdaptorFactory;
import com.plywet.platform.bi.delegates.utils.BIReportCategory;
import com.plywet.platform.bi.web.service.AbstractDirectoryServices;
import com.plywet.platform.bi.web.service.BIReportDelegates;

@Service("bi.service.reportService")
public class BIReportService extends AbstractDirectoryServices implements
		BIReportDelegates {
	private final Logger log = Logger.getLogger(BIFileSystemService.class);

	public final static String FILE_PATH_PREFIX = "/report/dir/";

	public final static long DIRECTORY_ROOT_ID_REPORT = 1L;

	@Override
	public BreadCrumbMeta getParentDirectories(String repository, Long id)
			throws BIException {
		return parentDirectories(repository, DIRECTORY_ROOT_ID_REPORT, id,
				"报表", FILE_PATH_PREFIX);
	}

	@Override
	public void getSubDirectory(String repository, Long id, BrowseMeta browse)
			throws BIException {
		subDirectory(repository, DIRECTORY_ROOT_ID_REPORT, id, browse,
				FILE_PATH_PREFIX);
	}

	@Override
	public void getSubDirectoryObject(String repository, Long id,
			BrowseMeta browse) throws BIException {
		// 获得该子目录下面的报表对象
		BIReportAdaptor adaptor = BIAdaptorFactory
				.createAdaptor(BIReportAdaptor.class);
		List<Object[]> rows = adaptor
				.getSubDirectoryObjects(String.valueOf(id));

		if (rows == null) {
			return;
		}

		for (Object[] r : rows) {
			BrowseNodeMeta node = new BrowseNodeMeta();
			String reportId = String.valueOf(r[0]);
			String category = BIReportCategory.getCategoryById(
					((Long) r[1]).intValue()).getCategory();
			node.setId(reportId);
			node.setCategory(category);
			node.addAttribute(BrowseNodeMeta.ATTR_DISPLAY_NAME, String
					.valueOf(r[3]));
			String style = "ui-" + category + "-icon";
			if (Utils.toBoolean(String.valueOf(r[2]), false)) {
				style += "-ref";
			}

			node.addAttribute(BrowseNodeMeta.ATTR_ICON_STYLE, style);
			node.addAttribute(HTML.ATTR_TYPE, Utils.DOM_LEAF);
			node.addAttribute(HTML.ATTR_SRC, "report/" + category + "/"
					+ reportId);
			node.addEvent("mouseup", "Plywet.browse.showOperationForFile");
			node.addEvent("dblclick", "Plywet.browse.openFile");
			browse.addContent(node);
		}
	}

}
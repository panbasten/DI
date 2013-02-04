package com.yonyou.bq8.di.web.rest;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.yonyou.bq8.di.core.exception.DIException;
import com.yonyou.bq8.di.core.model.NameValuePair;
import com.yonyou.bq8.di.core.utils.Utils;
import com.yonyou.bq8.di.delegates.vo.FunctionType;
import com.yonyou.bq8.di.web.entity.AjaxResult;
import com.yonyou.bq8.di.web.entity.AjaxResultEntity;
import com.yonyou.bq8.di.web.service.DIPageDelegates;

@Service("di.resource.sysToolsResource")
@Path("/systools")
public class DISysToolsResource {
	private final Logger logger = Logger.getLogger(DISysToolsResource.class);

	private static final String ID_EDITOR_CONTENT_NAVI_SYSTOOLS_BP = "editorContent-navi-systools-bp";

	@Resource(name = "di.service.pageServices")
	private DIPageDelegates pageDelegates;

	@GET
	@Path("/navi")
	@Produces(MediaType.TEXT_PLAIN)
	public String createNaviContentSystemTools(
			@CookieParam("repository") String repository,
			@QueryParam("parentId") long parentId) throws DIException {
		try {
			List<FunctionType> funcs = pageDelegates.getFunctionsByParent(repository, parentId);
			StringBuffer sb = new StringBuffer();
			if (funcs != null && !funcs.isEmpty()) {
				for (int i = 0; i < funcs.size(); i++) {
					FunctionType ft = funcs.get(i);
					sb.append("<div class=\"func-line-"	+ ((i % 2 == 0) ? "odd" : "even") + "\">");
					sb.append("<div class=\"func-title-con\">");
					sb.append("<div class=\"func-item-text\">" + ft.getDesc() + "</div>");
					sb.append("</div>");

					List<FunctionType> children = ft.getChildren();
					if (children == null) {
						continue;
					}
					for (FunctionType child : children) {
						List<NameValuePair> extAttrs = child.getExtAttrs();
						StringBuffer data = new StringBuffer();
						if (!Utils.isEmpty(extAttrs)) {
							for (NameValuePair pair :extAttrs) {
								data.append("data-" + pair.getName() + "=\"" + pair.getValue() + "\"");
							}
						}
						
						sb.append("<div class=\"func-item-con\" onmouseover=\"$(this).addClass('ui-state-hover')\" onmouseout=\"$(this).removeClass('ui-state-hover')\">");
						sb.append("<div class=\"func-item\" data-data=\"" + child.getExtAttrString() + "\" onclick='YonYou.sysTools.openTool(this)' style=\"background:url('resources/images/functype/" + child.getCode() + ".png') no-repeat\"></div>");
						sb.append("<div class=\"func-item-text\">" + child.getDesc() + "</div>");
						sb.append("</div>");
					}
					sb.append("</div>");

					if (i < funcs.size() - 1) {
						sb.append("<div class=\"split-line-"
								+ ((i % 2 == 0) ? "odd" : "even")
									+ "\"></div>");
					}
				}
			}

			// 1.为一级导航页面创建一个更新操作
			AjaxResultEntity naviResult = AjaxResultEntity.instance()
					.setOperation(Utils.RESULT_OPERATION_APPEND).setTargetId(
							ID_EDITOR_CONTENT_NAVI_SYSTOOLS_BP).setDom(
							sb.toString());

			return AjaxResult.instance().addEntity(naviResult).toJSONString();
		} catch (Exception ex) {
			logger.error("创建导航的功能内容页面出现错误。");
			throw new DIException("创建导航的功能内容页面出现错误。", ex);
		}
	}

}

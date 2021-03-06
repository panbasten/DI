package com.flywet.platform.bi.dashboard.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.pentaho.di.core.xml.XMLUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.flywet.platform.bi.base.cache.TemplateCache;
import com.flywet.platform.bi.base.enums.BIReportCategory;
import com.flywet.platform.bi.base.model.TemplateMeta;
import com.flywet.platform.bi.base.rest.AbstractReportResource;
import com.flywet.platform.bi.base.service.intf.BIReportDelegates;
import com.flywet.platform.bi.component.components.browse.BrowseMeta;
import com.flywet.platform.bi.component.components.browse.BrowseNodeMeta;
import com.flywet.platform.bi.component.exceptions.BIComponentException;
import com.flywet.platform.bi.component.utils.FLYVariableResolver;
import com.flywet.platform.bi.component.utils.HTML;
import com.flywet.platform.bi.component.utils.PageTemplateInterpolator;
import com.flywet.platform.bi.component.utils.PageTemplateResolverType;
import com.flywet.platform.bi.component.vo.ComponentPlugin;
import com.flywet.platform.bi.component.web.ActionMessage;
import com.flywet.platform.bi.component.web.AjaxResult;
import com.flywet.platform.bi.component.web.AjaxResultEntity;
import com.flywet.platform.bi.core.exception.BIException;
import com.flywet.platform.bi.core.utils.Utils;
import com.flywet.platform.bi.dashboard.utils.PageEditTemplateInterpolator;
import com.flywet.platform.bi.rest.BIBaseResource;
import com.flywet.platform.bi.rest.EditorResourceInterface;

@Service("bi.resource.dashboardResource")
@Path("/dashboard")
public class BIDashboardResource extends AbstractReportResource implements
		EditorResourceInterface {

	private final Logger logger = Logger.getLogger(BIDashboardResource.class);

	private static final String ICON_PATH = "resources/images/plugins/";

	@Resource(name = "bi.service.reportService")
	private BIReportDelegates reportService;

	/**
	 * 打开一个Dashboard编辑页面
	 * 
	 * @param id
	 * @return
	 * @throws BIException
	 */
	@GET
	@Path("/{id}/open")
	@Produces(MediaType.TEXT_PLAIN)
	public String openDashboardEditor(@PathParam("id") String id)
			throws BIException {

		try {
			// 获得报表对象
			Object[] report = reportService.getReportObject(Long.valueOf(id));
			Document doc = PageEditTemplateInterpolator
					.getDomForEditorWithContent((String) report[1],
							TemplateMeta.TEMPLATE_ATTRIBUTE_EDITOR_ID_PREFIX
									+ id);

			// 保留编辑状态，对于集群应用需要使用共享缓存
			TemplateMeta templateMeta = new TemplateMeta(id, doc);
			TemplateCache.put(id, templateMeta);

			return getReportPageJson(id, templateMeta).toJSONString();
		} catch (Exception ex) {
			logger.error("创建仪表板[" + id + "]编辑页面出现错误。");
			throw new BIException("创建仪表板[" + id + "]编辑页面出现错误。", ex);
		}
	}

	@GET
	@Path("/{id}/save")
	@Produces(MediaType.TEXT_PLAIN)
	public String saveDashboardEditor(@PathParam("id") String id)
			throws BIException {
		try {
			TemplateMeta templateMeta = TemplateCache.get(id);
			Document doc = templateMeta.getDoc();

			XMLUtils.toXMLString(doc);

			return ActionMessage.instance().success("保存仪表板成功!").toJSONString();
		} catch (Exception ex) {
			logger.error("保存仪表板[" + id + "]出现错误。");
			throw new BIException("保存仪表板[" + id + "]出现错误。", ex);
		}
	}

	@GET
	@Path("/{id}/resized")
	@Produces(MediaType.TEXT_PLAIN)
	public String resizedDashboard(@PathParam("id") String id,
			@QueryParam("target") String target, @QueryParam("x") String x,
			@QueryParam("y") String y, @QueryParam("width") String width,
			@QueryParam("height") String height, @QueryParam("type") String type)
			throws BIException {
		try {
			TemplateMeta templateMeta = TemplateCache.get(id);
			Document doc = templateMeta.getDoc();
			Node targetNode = getNodeWithEditorId(doc, target);
			String targetType = XMLUtils.getTagOrAttribute(targetNode,
					TemplateMeta.TEMPLATE_ATTRIBUTE_EDITOR_TYPE);
			ComponentPlugin targetPlugin = PageTemplateResolverType
					.getPlugin(targetType);

			// 如果为空，表示目标节点为HTML元素
			if (targetPlugin == null) {
				// 判断是否绝对定位，设置top, left属性
				// TODO
			} else {
				// 判断是否自由布局，设置top, left属性
				boolean freeLayout = Utils.toBoolean(XMLUtils
						.getTagOrAttribute(targetNode, HTML.ATTR_FREE_LAYOUT),
						false);
				if (freeLayout) {
					XMLUtils.setAttribute(targetNode, HTML.ATTR_X, x);
					XMLUtils.setAttribute(targetNode, HTML.ATTR_Y, y);
				}

				if (type.indexOf("w") > -1 || type.indexOf("e") > -1) {
					XMLUtils.setAttribute(targetNode, HTML.ATTR_WIDTH, width);
				}
				if (type.indexOf("s") > -1 || type.indexOf("n") > -1) {
					XMLUtils.setAttribute(targetNode, HTML.ATTR_HEIGHT, height);
				}

			}

			return getReportPageJson(id, templateMeta).toJSONString();
		} catch (Exception ex) {
			logger.error("调整仪表板[" + id + "]页面元素出现错误。");
			throw new BIException("调整仪表板[" + id + "]页面元素出现错误。", ex);
		}
	}

	@GET
	@Path("/{id}/move")
	@Produces(MediaType.TEXT_PLAIN)
	public String moveDashboard(@PathParam("id") String id,
			@QueryParam("source") String source,
			@QueryParam("target") String target, @QueryParam("type") String type)
			throws BIException {
		try {
			TemplateMeta templateMeta = TemplateCache.get(id);
			Document doc = templateMeta.getDoc();
			Node sourceNode = getNodeWithEditorId(doc, source);
			Node targetNode = getNodeWithEditorId(doc, target);

			// 主界面上的拖拽请求
			if (Utils.isJSEmpty(type)) {
				moveDashboardFromMainPage(templateMeta, sourceNode, targetNode);
			} else {
				moveDashboardFromTree(type, sourceNode, targetNode);
			}

			return getReportPageJson(id, templateMeta).toJSONString();
		} catch (Exception ex) {
			logger.error("移动仪表板[" + id + "]页面元素出现错误。");
			throw new BIException("移动仪表板[" + id + "]页面元素出现错误。", ex);
		}
	}

	private void moveDashboardFromTree(String type, Node sourceNode,
			Node targetNode) throws Exception {
		if ("append".equals(type)) {
			XMLUtils.appendTo(sourceNode, targetNode);
		} else if ("top".equals(type)) {
			XMLUtils.insertBefore(sourceNode, targetNode);
		} else {
			XMLUtils.insertBefore(targetNode, sourceNode);
		}
	}

	private void moveDashboardFromMainPage(TemplateMeta templateMeta,
			Node sourceNode, Node targetNode) throws Exception {
		String sourceType = XMLUtils.getTagOrAttribute(sourceNode,
				TemplateMeta.TEMPLATE_ATTRIBUTE_EDITOR_TYPE);

		String targetType = XMLUtils.getTagOrAttribute(targetNode,
				TemplateMeta.TEMPLATE_ATTRIBUTE_EDITOR_TYPE);

		ComponentPlugin sourcePlugin = PageTemplateResolverType
				.getPlugin(sourceType);
		ComponentPlugin targetPlugin = PageTemplateResolverType
				.getPlugin(targetType);

		// 如果源节点是GridLayoutItem
		if (sourcePlugin != null
				&& HTML.COMPONENT_TYPE_GRID_LAYOUT_ITEM
						.equalsIgnoreCase(sourcePlugin.getId())) {
			if (targetPlugin != null
					&& HTML.COMPONENT_TYPE_GRID_LAYOUT_ITEM
							.equalsIgnoreCase(targetPlugin.getId())) {
				insertBefore(sourceNode, targetNode);
			} else if (targetPlugin != null
					&& HTML.COMPONENT_TYPE_GRID_LAYOUT
							.equalsIgnoreCase(targetPlugin.getId())) {
				XMLUtils.appendTo(sourceNode, targetNode);
			} else {
				throw new BIComponentException("不允许将栅格布局的项目拖拽到其他类型的节点。");
			}
		}
		// 如果目标节点类型是GridLayout，且源节点不是GridLayoutItem
		else if (targetPlugin != null
				&& HTML.COMPONENT_TYPE_GRID_LAYOUT
						.equalsIgnoreCase(targetPlugin.getId())) {
			// 创建一个GridLayoutItem
			Node node = createElement(templateMeta, PageTemplateResolverType
					.getPlugin(HTML.COMPONENT_TYPE_GRID_LAYOUT_ITEM));

			XMLUtils.appendTo(sourceNode, node);
			XMLUtils.appendTo(node, targetNode);
		}
		// 如果目标节点类型是容器属性或者是HTML容器，添加到其内部
		else if (isContainer(targetType)) {
			XMLUtils.appendTo(sourceNode, targetNode);
		}
		// 其他情况
		else {
			insertBefore(sourceNode, targetNode);
		}
	}

	private Node createElement(TemplateMeta templateMeta, ComponentPlugin plugin) {
		Node node = templateMeta.createElement(plugin.getCategory(), plugin
				.getId());
		// 添加默认属性
		plugin.setDefaultAttributesForNode(node);

		return node;
	}

	private void insertBefore(Node sourceNode, Node targetNode)
			throws TransformerException {
		// 如果源节点的下一个节点是目标节点，调整两者的顺序
		if (isBefore(sourceNode, targetNode)) {
			XMLUtils.insertBefore(targetNode, sourceNode);
		} else {
			XMLUtils.insertBefore(sourceNode, targetNode);
		}
	}

	private boolean isBefore(Node sourceNode, Node targetNode)
			throws TransformerException {
		Node temp = sourceNode.getNextSibling();
		while (temp != null) {
			if (XMLUtils.isTextNode(temp)) {
				if (Utils.isEmpty(Utils.trim(XMLUtils.toXMLString(temp)))) {
					temp = temp.getNextSibling();
				}
			} else if (temp == targetNode) {
				return true;
			} else {
				return false;
			}
		}
		return false;

	}

	private boolean isContainer(String targetType) {
		ComponentPlugin targetPlugin = PageTemplateResolverType
				.getPlugin(targetType);
		if (targetPlugin == null) {
			// TODO 判断HTML可容纳的元素
		} else if (targetPlugin != null && targetPlugin.isContainer()) {
			return true;
		}
		return false;
	}

	private Node getNodeWithEditorId(Node node, String editorId) {
		return XMLUtils.selectSingleNode(node, "//*[@"
				+ TemplateMeta.TEMPLATE_ATTRIBUTE_EDITOR_ID + "='" + editorId
				+ "']");
	}

	@GET
	@Path("/{id}/append")
	@Produces(MediaType.TEXT_PLAIN)
	public String appendDashboard(@PathParam("id") String id,
			@QueryParam("source") String sourceType,
			@QueryParam("target") String target) throws BIException {
		try {
			TemplateMeta templateMeta = TemplateCache.get(id);

			Document doc = templateMeta.getDoc();
			Node targetNode = getNodeWithEditorId(doc, target);
			String targetType = XMLUtils.getTagOrAttribute(targetNode,
					TemplateMeta.TEMPLATE_ATTRIBUTE_EDITOR_TYPE);

			ComponentPlugin sourcePlugin = PageTemplateResolverType
					.getPlugin(sourceType);
			ComponentPlugin targetPlugin = PageTemplateResolverType
					.getPlugin(targetType);

			Node sourceNode = createElement(templateMeta, sourcePlugin);
			// 如果源节点插件类型为GridLayoutItem，只能添加到GridLayout中
			if (sourcePlugin != null
					&& HTML.COMPONENT_TYPE_GRID_LAYOUT_ITEM
							.equalsIgnoreCase(sourcePlugin.getId())) {
				if (targetPlugin != null
						&& HTML.COMPONENT_TYPE_GRID_LAYOUT_ITEM
								.equalsIgnoreCase(targetPlugin.getId())) {
					XMLUtils.insertBefore(sourceNode, targetNode);
				} else if (targetPlugin != null
						&& HTML.COMPONENT_TYPE_GRID_LAYOUT
								.equalsIgnoreCase(targetPlugin.getId())) {
					XMLUtils.appendTo(sourceNode, targetNode);
				} else {
					throw new BIComponentException("不允许将栅格布局的项目拖拽到其他类型的节点。");
				}
			}
			// 如果目标是容器，添加到内部
			else if (isContainer(targetType)) {
				XMLUtils.appendTo(sourceNode, targetNode);
			}
			// 其他添加到目标节点前面
			else {
				XMLUtils.insertBefore(sourceNode, targetNode);
			}

			return getReportPageJson(id, templateMeta).toJSONString();
		} catch (Exception ex) {
			logger.error("添加仪表板[" + id + "]页面元素出现错误。");
			throw new BIException("添加仪表板[" + id + "]页面元素出现错误。", ex);
		}
	}

	/**
	 * 初始化加载Dashboard编辑器页面
	 * 
	 * @return
	 * @throws BIException
	 */
	@SuppressWarnings("unchecked")
	@Override
	@GET
	@Path("/editor")
	@Produces(MediaType.TEXT_PLAIN)
	public String loadEditor() throws BIException {
		try {
			String cate = BIReportCategory.REPORT_TYPE_DASHBOARD.getCategory();

			// 生成插件工具栏
			List<String> dashboardStepBar = PageTemplateResolverType
					.getCategoryNames();
			List<BrowseMeta> dashboardStepBrowses = new ArrayList<BrowseMeta>();
			Map<String, List<ComponentPlugin>> categories = PageTemplateResolverType
					.getCategories();

			for (String f : PageTemplateResolverType.getCategoryCodes()) {
				List<ComponentPlugin> cps = categories.get(f);
				BrowseMeta browseMeta = new BrowseMeta();

				for (ComponentPlugin cp : cps) {
					if (cp.isIgnoreInDesigner()) {
						continue;
					}

					BrowseNodeMeta plugin = new BrowseNodeMeta();
					plugin.setId(cp.getId());
					plugin.setCategory(cp.getCategory());
					plugin.addClass("fly-" + cate + "-step-plugin");
					plugin.addAttribute(HTML.ATTR_TYPE, Utils.DOM_LEAF);
					plugin.addAttribute(HTML.ATTR_ICON, ICON_PATH
							+ cp.getIconfile());
					plugin.addAttribute(BrowseNodeMeta.ATTR_DISPLAY_NAME, cp
							.getDescription());
					plugin.addAttribute(HTML.ATTR_TITLE, cp.getTooltip());
					plugin.addExtendAttribute("isContainer", cp.isContainer());

					Map dataMap = new HashMap();
					dataMap.put("props", cp.getAttributesJSONArray());
					JSONArray ja = cp.getSignalsJSONArray();
					if (ja != null) {
						dataMap.put("signals", ja);
					}
					ja = cp.getSlotsJSONArray();
					if (ja != null) {
						dataMap.put("slots", ja);
					}
					plugin.setData(dataMap);

					browseMeta.addContent(plugin);
				}

				dashboardStepBrowses.add(browseMeta);
			}

			FLYVariableResolver attrsMap = FLYVariableResolver.instance();
			attrsMap.addVariable("editorId", cate);
			attrsMap.addVariable("dashboardStepBar", dashboardStepBar);
			attrsMap.addVariable("dashboardStepBrowses", dashboardStepBrowses);

			Object[] domString = PageTemplateInterpolator.interpolate(
					getTemplateString(cate), attrsMap);

			// 创建一个更新操作
			AjaxResultEntity entity = AjaxResultEntity.instance().setOperation(
					Utils.RESULT_OPERATION_APPEND).setTargetId(
					BIBaseResource.ID_EDITOR_CONTENT_PANELS).setDomAndScript(
					domString);

			AjaxResult result = AjaxResult.instance();
			result.addEntity(entity);

			return result.toJSONString();
		} catch (Exception ex) {
			logger.error("初始化加载仪表板编辑器页面出现错误。");
			throw new BIException("初始化加载仪表板编辑器页面出现错误。", ex);
		}
	}

}

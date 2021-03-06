package com.flywet.platform.bi.component.resolvers.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.json.simple.JSONObject;
import org.pentaho.di.core.xml.XMLUtils;
import org.pentaho.pms.util.Const;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.flywet.platform.bi.component.core.ComponentResolverInterface;
import com.flywet.platform.bi.component.resolvers.BaseComponentResolver;
import com.flywet.platform.bi.component.resolvers.base.ForEachResolver;
import com.flywet.platform.bi.component.utils.FLYVariableResolver;
import com.flywet.platform.bi.component.utils.HTML;
import com.flywet.platform.bi.component.utils.HTMLWriter;
import com.flywet.platform.bi.core.exception.BIPageException;
import com.flywet.platform.bi.core.utils.JSONUtils;
import com.flywet.platform.bi.core.utils.Utils;

public class FLYGridLayoutResolver extends BaseComponentResolver implements
		ComponentResolverInterface {

	public static final String ATTR_COLUMN = "column";

	public static final String ATTR_COLS = "cols";

	public static final String ATTR_ITEM_WIDTH = "itemWidth";

	public static final String ATTR_ITEM_MARGIN = "itemMargin";

	private static final String GRID_LAYOUT_CLASS = "ui-grid-layout "
			+ HTML.LAYOUT_CLASS;

	private static final String GRID_LAYOUT_ITEM_CLASS = "ui-grid-layout-item "
			+ HTML.LAYOUT_ITEM_CLASS;

	private static final String GRID_LAYOUT_ITEM_EMPTY = "ui-grid-layout-item-empty";

	@SuppressWarnings("unchecked")
	@Override
	public void renderSub(Node node, HTMLWriter html, List<String> script,
			FLYVariableResolver attrs, String fileUrl) throws BIPageException {
		try {
			html.startElement(HTML.COMPONENT_TYPE_BASE_DIV);

			String weightVar = HTML.getTagAttribute(node, HTML.TAG_WEIGHT_VAR,
					attrs);
			String id = HTML.getId(node, attrs);

			html.writeAttribute(HTML.ATTR_ID, id);

			HTML.writeStyleClassAttribute(node, html, attrs, GRID_LAYOUT_CLASS);

			HTML.writeStyleAttribute(node, html, attrs);

			String columnStr = HTML.getTagAttribute(node, ATTR_COLUMN, attrs);
			String itemWidthStr = HTML.getTagAttribute(node, ATTR_ITEM_WIDTH,
					attrs);
			int column = 1;
			if (columnStr != null) {
				column = Integer.valueOf(columnStr);
			}
			int[] itemWidth;
			if (itemWidthStr != null) {
				String[] itemWidthStrArr = itemWidthStr.split(",");
				itemWidth = new int[itemWidthStrArr.length];
				for (int i = 0; i < itemWidthStrArr.length; i++) {
					if (itemWidthStrArr[i].endsWith("%")) {
						itemWidth[i] = Integer.valueOf(itemWidthStrArr[i]
								.substring(0, itemWidthStrArr[i].length() - 1));
					} else {
						itemWidth[i] = Integer.valueOf(itemWidthStrArr[i]);
					}
				}
			} else {
				int per = (int) Math.floor(100 / column);
				itemWidth = new int[] { per };
			}

			HTML.writeAttributes(node.getAttributes(), new String[] {
					ATTR_COLUMN, ATTR_ITEM_WIDTH }, html, attrs);

			List<String> subScript = new ArrayList<String>();

			int index = 0;
			renderItems(node, html, subScript, attrs, fileUrl, column,
					itemWidth, index);

			html.endElement(HTML.COMPONENT_TYPE_BASE_DIV);

			Map<String, Object> map = HTML.getAttributesMap(node
					.getAttributes(), attrs);
			JSONObject jo = JSONUtils.convertToJSONObject(map);
			jo.put(HTML.ATTR_ID, id);
			jo.put(ATTR_COLUMN, columnStr);
			jo.put(ATTR_ITEM_WIDTH, itemWidthStr);

			script.add("Flywet.cw('GridLayout','" + Const.NVL(weightVar, "")
					+ "'," + jo.toJSONString() + ");");
			script.addAll(subScript);
		} catch (BIPageException e) {
			throw e;
		} catch (Exception e) {
			throw new BIPageException("解析栅格布局出现错误。", e);
		}
	}

	private int renderItems(Node node, HTMLWriter html, List<String> script,
			FLYVariableResolver attrs, String fileUrl, int column,
			int[] itemWidth, int index) throws BIPageException {
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node subNode = nodeList.item(i);
			index = renderItemAsType(subNode, html, script, attrs, fileUrl,
					column, itemWidth, index);
		}
		return index;
	}

	private int renderItemAsType(Node subNode, HTMLWriter html,
			List<String> script, FLYVariableResolver attrs, String fileUrl,
			int column, int[] itemWidth, int index) throws BIPageException {
		if (HTML.COMPONENT_TYPE_GRID_LAYOUT_ITEM.equalsIgnoreCase(subNode
				.getNodeName())) {
			return renderLayoutItem(subNode, html, script, attrs, fileUrl,
					column, itemWidth, index);
		} else if (HTML.COMPONENT_TYPE_FOR_EACH.equalsIgnoreCase(subNode
				.getNodeName())) {
			return renderForEach(subNode, html, script, attrs, fileUrl, column,
					itemWidth, index);
		} else {
			// 其他类型的节点，不做操作
		}
		return index;
	}

	private int renderForEach(Node subNode, HTMLWriter html,
			List<String> script, FLYVariableResolver attrs, String fileUrl,
			int column, int[] itemWidth, int index) throws BIPageException {
		String varStr = HTML.getTagAttribute(subNode,
				ForEachResolver.ATTRIBUTE_VAR, attrs);
		String indexVarStr = HTML.getTagAttribute(subNode,
				ForEachResolver.ATTRIBUTE_INDEX_VAR, attrs);
		String sizeVarStr = HTML.getTagAttribute(subNode,
				ForEachResolver.ATTRIBUTE_SIZE_VAR, attrs);
		Object obj = HTML.getTagAttributeObject(subNode,
				ForEachResolver.ATTRIBUTE_ITEMS, attrs);
		if (obj instanceof Object[]) {
			Object[] items = (Object[]) obj;
			if (items != null && items.length > 0) {
				if (sizeVarStr != null) {
					attrs.addVariable(sizeVarStr, items.length);
				}
				for (int i = 0; i < items.length; i++) {
					attrs.addVariable(varStr, items[i]);
					if (indexVarStr != null) {
						attrs.addVariable(indexVarStr, i);
					}
					index = renderItems(subNode, html, script, attrs, fileUrl,
							column, itemWidth, index);
				}
			}
		} else {
			Collection<?> items = (Collection<?>) obj;
			if (items != null && items.size() > 0) {
				if (sizeVarStr != null) {
					attrs.addVariable(sizeVarStr, items.size());
				}
				int itemIdx = 0;
				for (Iterator<?> itet = items.iterator(); itet.hasNext();) {
					attrs.addVariable(varStr, itet.next());
					if (indexVarStr != null) {
						attrs.addVariable(indexVarStr, itemIdx);
					}
					itemIdx++;
					super.renderSub(subNode, html, script, attrs, fileUrl);
					index = renderItems(subNode, html, script, attrs, fileUrl,
							column, itemWidth, index);
				}
			}
		}
		return index;
	}

	private int renderLayoutItem(Node subNode, HTMLWriter html,
			List<String> script, FLYVariableResolver attrs, String fileUrl,
			int column, int[] itemWidth, int index) throws BIPageException {

		int cols = 1, w = 0;
		String colsStr = HTML.getTagAttribute(subNode, ATTR_COLS, attrs);
		if (colsStr != null) {
			cols = Integer.valueOf(colsStr);
		}
		for (int c = 0; c < cols; c++) {
			w += getWidth(index, column, itemWidth);
			index++;
		}

		html.startElement(HTML.COMPONENT_TYPE_BASE_DIV);
		html.writeAttribute(HTML.ATTR_CLASS, GRID_LAYOUT_ITEM_CLASS);

		String style = Const.NVL(HTML.getTagAttribute(subNode, HTML.ATTR_STYLE,
				attrs), "");
		style = "width:" + w + "%;" + style;
		style = HTML.getShowStyle(subNode, attrs) + style;
		html.writeAttribute(HTML.ATTR_STYLE, style);

		HTML.writeAttributes(subNode.getAttributes(), null, html, attrs);

		if (isEmptyItem(subNode)) {
			html.writeText("<div class='" + GRID_LAYOUT_ITEM_EMPTY
					+ "'>--空--</div>");
		} else {
			super.renderSub(subNode, html, script, attrs, fileUrl);
		}

		html.endElement(HTML.COMPONENT_TYPE_BASE_DIV);

		return index;
	}

	private boolean isEmptyItem(Node subNode) throws BIPageException {
		try {
			// 如果内容为空，返回true
			NodeList subNodeChildList = subNode.getChildNodes();
			if (subNodeChildList == null)
				return true;
			for (int si = 0; si < subNodeChildList.getLength(); si++) {
				Node n = subNodeChildList.item(si);
				if (XMLUtils.isTextNode(n)) {
					if (Utils.isEmpty(Utils.trim(XMLUtils.toXMLString(n)))) {
						continue;
					}
				}
				return false;
			}

			return true;

		} catch (TransformerException e) {
			throw new BIPageException("渲染GridLayout组件出现错误。");
		}
	}

	private int getWidth(int index, int column, int[] itemWidth) {
		int wIdx = index % column;
		wIdx = (wIdx > itemWidth.length - 1) ? (itemWidth.length - 1) : wIdx;
		return itemWidth[wIdx];
	}

}

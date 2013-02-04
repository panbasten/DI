package com.yonyou.bq8.di.component.components.base;

import org.json.simple.JSONObject;

import com.yonyou.bq8.di.component.components.SimpleComponentMeta;
import com.yonyou.bq8.di.component.utils.HTML;
import com.yonyou.bq8.di.core.exception.DIJSONException;

/**
 * 标签功能
 * 
 * @author panwei
 *@version 1.0
 *@since 2009/06/22
 */
public class LabelMeta extends SimpleComponentMeta {

	public static final String LABEL_TEXT_DIRECTION_LEFT = "ltr";
	public static final String LABEL_TEXT_DIRECTION_RIGHT = "rtl";

	private String labelDirection = LABEL_TEXT_DIRECTION_LEFT;

	public static final String ATTR_EXTEND_DIRECTION = "direction";

	@Override
	public JSONObject getFormJo() throws DIJSONException {
		this.addExtendAttribute(ATTR_EXTEND_DIRECTION, labelDirection);
		return super.getFormJo();
	}

	public String getLabelDirection() {
		return labelDirection;
	}

	public void setLabelDirection(String labelDirection) {
		this.labelDirection = labelDirection;
	}

	@Override
	public String[] getAttributesName() {
		return HTML.LABEL_ATTRS;
	}

	@Override
	public String getComponentType() {
		return HTML.COMPONENT_TYPE_BASE_LABEL;
	}

}

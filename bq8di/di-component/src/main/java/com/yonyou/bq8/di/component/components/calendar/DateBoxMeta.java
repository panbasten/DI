package com.yonyou.bq8.di.component.components.calendar;

import com.yonyou.bq8.di.component.components.combo.ComboMeta;
import com.yonyou.bq8.di.component.core.ComponentMetaInterface;
import com.yonyou.bq8.di.component.utils.HTML;
import com.yonyou.bq8.di.core.exception.DIJSONException;

public class DateBoxMeta extends ComboMeta implements ComponentMetaInterface {

	public static final String ATTR_CURRENT_TEXT = "currentText";
	public static final String ATTR_CLOSE_TEXT = "closeText";
	public static final String ATTR_OK_TEXT = "okText";

	public static final String ATTR_FORMATTER = "formatter";
	public static final String ATTR_PARSER = "parser";

	public static final String ATTR_ON_SELECT = "onSelect";

	@Override
	public String getComponentType() {
		return HTML.COMPONENT_TYPE_DATE_BOX;
	}

	public String getCurrentText() {
		return (String) this.getAttribute(ATTR_CURRENT_TEXT);
	}

	public String getCloseText() {
		return (String) this.getAttribute(ATTR_CLOSE_TEXT);
	}

	public String getOkText() {
		return (String) this.getAttribute(ATTR_OK_TEXT);
	}

	public String getFormatter() {
		return (String) this.getAttribute(ATTR_FORMATTER);
	}

	public String getParser() {
		return (String) this.getAttribute(ATTR_PARSER);
	}

	public String getOnSelect() {
		return (String) this.getAttribute(ATTR_ON_SELECT);
	}

	public DateBoxMeta getCurrentText(String val) throws DIJSONException {
		this.addAttribute(ATTR_CURRENT_TEXT, val);
		return this;
	}

	public DateBoxMeta getCloseText(String val) throws DIJSONException {
		this.addAttribute(ATTR_CLOSE_TEXT, val);
		return this;
	}

	public DateBoxMeta getOkText(String val) throws DIJSONException {
		this.addAttribute(ATTR_OK_TEXT, val);
		return this;
	}

	public DateBoxMeta setFormatter(String val) throws DIJSONException {
		this.addAttribute(ATTR_FORMATTER, val);
		return this;
	}

	public DateBoxMeta setParser(String val) throws DIJSONException {
		this.addAttribute(ATTR_PARSER, val);
		return this;
	}

	public DateBoxMeta setOnSelect(String val) throws DIJSONException {
		this.addAttribute(ATTR_ON_SELECT, val);
		return this;
	}
}

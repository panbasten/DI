package com.yonyou.bq8.di.web.rest;

import java.net.URLDecoder;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.yonyou.bq8.di.component.utils.BQVariableResolver;
import com.yonyou.bq8.di.component.utils.PageTemplateInterpolator;
import com.yonyou.bq8.di.core.exception.DIException;
import com.yonyou.bq8.di.core.utils.Utils;
import com.yonyou.bq8.di.delegates.vo.User;
import com.yonyou.bq8.di.web.entity.ActionMessage;
import com.yonyou.bq8.di.web.entity.AjaxResult;
import com.yonyou.bq8.di.web.entity.AjaxResultEntity;
import com.yonyou.bq8.di.web.model.ParameterContext;
import com.yonyou.bq8.di.web.service.DIUserDelegate;
import com.yonyou.bq8.di.web.utils.DIWebUtils;

@Service("di.resource.userResource")
@Path("/user")
public class DIUserResource {
	private Logger logger = Logger.getLogger(DIUserResource.class);
	
	private static final String TEMPLATE_USER_CREATE = "editor/user/create.h";
	private static final String TEMPLATE_USER_LIST = "editor/user/list.h";
	
	@Resource(name="di.service.userService")
	private DIUserDelegate userDelegate;
	
	@GET
	@Path("/setting")
	@Produces(MediaType.APPLICATION_JSON)
	public String openCreate(@QueryParam("targetId") String targetId) throws Exception {
		Object[] domString = PageTemplateInterpolator.interpolate(TEMPLATE_USER_CREATE, new BQVariableResolver());

		AjaxResultEntity emptyEntity = new AjaxResultEntity();
		emptyEntity.setOperation(Utils.RESULT_OPERATION_EMPTY);
		emptyEntity.setTargetId(targetId);

		AjaxResultEntity content = AjaxResultEntity.instance().setOperation(
				Utils.RESULT_OPERATION_APPEND).setTargetId(targetId)
				.setDomAndScript(domString);
		
		String res = AjaxResult.instance().addEntity(emptyEntity).addEntity(
				content).toJSONString();
		return res;
	}
	
	@POST
	@Path("/setting")
	@Produces(MediaType.APPLICATION_JSON)
	public String save(String body) throws Exception {
		ActionMessage am = new ActionMessage();
		try {
			ParameterContext parameterContext = DIWebUtils.fillParameterContext(body);
			
			User user = new User();
			user.setLogin(parameterContext.getParameter("login"));
			user.setName(parameterContext.getParameter("name"));
			user.setPassword(parameterContext.getParameter("password"));
			user.setDesc(parameterContext.getParameter("desc"));
			user.setEnabled(parameterContext.getParameter("enabled"));
		
			userDelegate.saveUser(user);
			am.addMessage("保存用户信息成功");
		} catch (DIException e) {
			logger.error("保存用户信息失败", e);
			am.addMessage("保存用户信息失败");
		}
		
		return am.toJSONString();
	}
	
	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	public String listUsers(@QueryParam("targetId") String targetId) throws DIException {
		BQVariableResolver vr = new BQVariableResolver();
		List<User> userList = userDelegate.getAllUser();
		vr.addVariable("userList", userList);
		
		Object[] domString = PageTemplateInterpolator.interpolate(TEMPLATE_USER_LIST, vr);
		
		AjaxResultEntity emptyEntity = new AjaxResultEntity();
		emptyEntity.setOperation(Utils.RESULT_OPERATION_EMPTY);
		emptyEntity.setTargetId(targetId);

		AjaxResultEntity content = AjaxResultEntity.instance().setOperation(
				Utils.RESULT_OPERATION_APPEND).setTargetId(targetId)
				.setDomAndScript(domString);
		
		String res = AjaxResult.instance().addEntity(emptyEntity).addEntity(
				content).toJSONString();
		return res;
	}
	
}

package com.flywet.platform.bi.web.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import com.flywet.platform.bi.component.utils.FLYVariableResolver;
import com.flywet.platform.bi.component.utils.PageTemplateInterpolator;
import com.flywet.platform.bi.core.exception.BIException;
import com.flywet.platform.bi.core.exception.BISecurityException;
import com.flywet.platform.bi.core.sec.KeyGenerater;
import com.flywet.platform.bi.core.sec.SignProvider;
import com.flywet.platform.bi.core.sec.WebMarshal;
import com.flywet.platform.bi.core.utils.FileUtils;
import com.flywet.platform.bi.core.utils.JSONUtils;
import com.flywet.platform.bi.core.utils.Utils;
import com.flywet.platform.bi.delegates.BIEnvironmentDelegate;
import com.flywet.platform.bi.web.entity.ActionMessage;
import com.flywet.platform.bi.web.entity.AjaxResult;
import com.flywet.platform.bi.web.i18n.BIWebMessages;
import com.flywet.platform.bi.web.model.ParameterContext;
import com.flywet.platform.bi.web.utils.BISecurityUtils;
import com.flywet.platform.bi.web.utils.BIWebUtils;

@Service("bi.resource.identification")
@Path("/identification")
public class BIIdentification {
	private final Logger log = Logger.getLogger(BIIdentification.class);

	public static final String TEMPLATE_SYS_USER_INFO = "editor/sys/user_info.h";

	public static final String TEMPLATE_SYS_LOGIN_SLIDE = "editor/sys/login_slide.h";

	public static final String TEMPLATE_SYS_SETTING = "editor/sys/sys_setting.h";

	private static final String KEY = "FLYWET@2013";

	@GET
	@Path("/repositoryNames")
	@Produces(MediaType.APPLICATION_JSON)
	public String loadRepNames() throws BIException {
		try {
			String[] repNames = BIEnvironmentDelegate.instance().getRepNames();
			return JSONUtils.toJsonString(repNames);
		} catch (Exception ex) {
			throw new BIException("活动资源库命名出现错误。", ex);
		}
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/slides")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSlideShows() throws BIException {
		try {
			// 获得登陆滚动页面
			Document doc = PageTemplateInterpolator
					.getDom(TEMPLATE_SYS_LOGIN_SLIDE);
			Object[] domString = PageTemplateInterpolator.interpolate(
					TEMPLATE_SYS_LOGIN_SLIDE, doc, FLYVariableResolver
							.instance());

			JSONObject jo = new JSONObject();
			jo.put("dom", (String) domString[0]);
			jo.put("script", JSONUtils
					.convertToJSONArray((List<String>) domString[1]));

			return jo.toJSONString();
		} catch (Exception ex) {
			throw new BIException("活动资源库命名出现错误。", ex);
		}
	}

	@GET
	@Path("/messages")
	@Produces(MediaType.TEXT_PLAIN)
	public String getMessages() throws BIException {
		try {
			return BIWebMessages.getMessages().toJSONString();
		} catch (Exception ex) {
			throw new BIException("获得页面多语资源出现错误。", ex);
		}
	}

	/**
	 * 打开系统设置对话框
	 * 
	 * @param repository
	 * @param targetId
	 * @return
	 * @throws BIException
	 */
	@SuppressWarnings("unchecked")
	@GET
	@Path("/openSettingDialog")
	@Produces(MediaType.TEXT_PLAIN)
	public String openSettingDialog(
			@CookieParam("repository") String repository,
			@QueryParam("targetId") String targetId) throws BIException {
		try {

			FLYVariableResolver attrsMap = FLYVariableResolver.instance();

			Object[] domString = PageTemplateInterpolator.interpolate(
					TEMPLATE_SYS_SETTING, attrsMap);
			List<String> script = (List<String>) domString[1];
			if (script == null) {
				domString[1] = new ArrayList<String>();
				script = (List<String>) domString[1];
			}
			script.add("$('#login_setting_create_key')."
					+ "bind('click', function(){" + "Flywet.Login.createKey();"
					+ "})");

			return AjaxResult.instanceDialogContent(targetId, domString)
					.toJSONString();
		} catch (Exception ex) {
			throw new BIException("创建系统设置页面出现错误。", ex);
		}

	}

	private void createPubKey(KeyGenerater kg) throws BIException {
		try {

			File pubKeyFile = new File(SignProvider.getPublicKeyFilePath());

			if (pubKeyFile.exists()) {
				pubKeyFile.delete();
			}

			FileOutputStream out = null;
			try {
				out = new FileOutputStream(pubKeyFile);
				out.write(kg.getPubKey().getBytes());
			} catch (IOException e) {
				log.error("create public key file exception:", e);
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						log.error("create public key file exception:", e);
					}
				}
			}

		} catch (Exception ex) {
			log.error("createPubKey exception:", ex);
		}
	}

	@GET
	@Path("/createKey")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public void createKey(@Context HttpServletRequest request,
			@Context HttpServletResponse response, String body)
			throws IOException {
		InputStream is = null;

		try {
			KeyGenerater kg = KeyGenerater.instance(KEY);

			createPubKey(kg);

			String key = kg.getPriKey();
			is = FileUtils.getInputStream(key);
			response.setContentType("application/octet-stream");
			request.setCharacterEncoding(Const.XML_ENCODING);
			response.setCharacterEncoding(Const.XML_ENCODING);
			String fileName = "ba.prikey";
			response.setHeader("Content-Disposition", "attachment;filename="
					+ fileName);
			byte[] b = new byte[1024];
			int i;
			OutputStream os = response.getOutputStream();
			while ((i = is.read(b)) != -1) {
				os.write(b, 0, i);
			}
			os.flush();
		} catch (Exception e) {
			log.error("download private key file exception:", e);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	/**
	 * 创建用户信息弹出页
	 * 
	 * @throws BIException
	 */
	@SuppressWarnings("unchecked")
	@GET
	@Path("/usersettingpage")
	@Produces(MediaType.TEXT_PLAIN)
	public String createUserSettingPage(@CookieParam("user") String userInfo)
			throws BIException {
		try {
			JSONObject user = JSONUtils.convertStringToJSONObject(BIWebUtils
					.decode(userInfo));
			FLYVariableResolver resolver = FLYVariableResolver.instance();
			resolver.addVariable("user", user);

			Document doc = PageTemplateInterpolator
					.getDom(TEMPLATE_SYS_USER_INFO);
			Object[] domString = PageTemplateInterpolator.interpolate(
					TEMPLATE_SYS_USER_INFO, doc, resolver);

			JSONObject jo = new JSONObject();
			jo.put("username", user.get("username"));
			jo.put("dom", (String) domString[0]);
			jo.put("script", JSONUtils
					.convertToJSONArray((List<String>) domString[1]));

			return jo.toJSONString();
		} catch (Exception ex) {
			throw new BIException("创建用户信息弹出页出现错误。", ex);
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String identification(String userinfo) throws BIException {
		ActionMessage am = new ActionMessage();
		Repository rep = null;
		String repository = null;
		try {
			ParameterContext paramContext = BIWebUtils
					.fillParameterContext(userinfo);

			repository = paramContext.getParameter("repository");
			String username = paramContext.getParameter("username");
			String password = paramContext.getParameter("password");

			try {
				WebMarshal.getInstance();
			} catch (BISecurityException e) {
				am.addErrorMessage(e.getMessage().trim());
			}

			if (Utils.isEmpty(repository)) {
				am.addErrorMessage("请选择一个资源库");
			}
			if (Utils.isEmpty(username)) {
				am.addErrorMessage("请输入用户名");
			}
			if (Utils.isEmpty(password)) {
				am.addErrorMessage("请输入登录密码");
			}

			if (!am.state()) {
				return am.toJSONString();
			}

			rep = BIEnvironmentDelegate.instance().borrowRep(repository, null);
			if (rep == null) {
				am.addErrorMessage("指定的资源库不存在!");
				return am.toJSONString();
			}

			IUser user = rep.loadUserInfo(username, password);
			if (user == null) {
				am.addErrorMessage("登录认证失败!");
				return am.toJSONString();
			}

			String cookie = populateCookie(repository, user, rep);
			am.setData(cookie);
		} catch (Exception ex) {
			log.error("identifacation exception:", ex);
			am.addErrorMessage(ex.getMessage());
		} finally {
			BIEnvironmentDelegate.instance().returnRep(repository, rep);
		}

		return am.toJSONString();
	}

	private String populateCookie(String repository, IUser user, Repository rep)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("username", user.getUsername());
		map.put("user", JSONUtils.convertToJSONObject(user.getUserInfo())
				.toJSONString());
		map.put("repository", repository);
		if (rep instanceof KettleDatabaseRepository) {
			map.put("repositoryType", "db");
		} else if (rep instanceof KettleFileRepository) {
			map.put("repositoryType", "file");
		}

		// TODO 写入用户和有效时间的密文，和path信息
		String cookie = BISecurityUtils.createCookieString(map);
		return cookie;
	}

}

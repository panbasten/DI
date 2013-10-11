package com.flywet.platform.bi.web.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.vfs.FileObject;
import org.apache.log4j.Logger;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.springframework.stereotype.Service;

import com.flywet.platform.bi.component.utils.FLYVariableResolver;
import com.flywet.platform.bi.component.utils.PageTemplateInterpolator;
import com.flywet.platform.bi.component.web.ActionMessage;
import com.flywet.platform.bi.component.web.AjaxResult;
import com.flywet.platform.bi.core.ContextHolder;
import com.flywet.platform.bi.core.exception.BIException;
import com.flywet.platform.bi.core.exception.BIJSONException;
import com.flywet.platform.bi.core.utils.FileUtils;
import com.flywet.platform.bi.core.utils.PropertyUtils;
import com.flywet.platform.bi.core.utils.ReflectionUtils;
import com.flywet.platform.bi.core.utils.Utils;
import com.flywet.platform.bi.delegates.enums.AuthorizationObjectCategory;
import com.flywet.platform.bi.delegates.enums.PermissionCategory;
import com.flywet.platform.bi.delegates.utils.BIAdaptorFactory;
import com.flywet.platform.bi.delegates.vo.PortalAction;
import com.flywet.platform.bi.delegates.vo.PortalMenu;
import com.flywet.platform.bi.delegates.vo.User;
import com.flywet.platform.bi.web.service.BIFileSystemDelegate;
import com.flywet.platform.bi.web.service.BIPortalDelegates;
import com.flywet.platform.bi.web.service.BIUserDelegate;

@Service("bi.resource.portalet")
@Path("/portalet")
public class BIPortaletResource {
	private final Logger log = Logger.getLogger(BIPortaletResource.class);

	@Resource(name = "bi.service.portalServices")
	private BIPortalDelegates portalDelegates;

	@Resource(name = "bi.service.filesystemService")
	private BIFileSystemDelegate filesysService;

	@Resource(name = "bi.service.userService")
	private BIUserDelegate userService;

	public static final long PORTAL_MENU_ROOT_ID = 0L;

	public static final String PORTAL_ONLY_PARAM = "param";

	private static final String TEMPLATE_UPLOAD_FILES = "portal/menu/uploadFiles.h";

	@GET
	@Path("/upload/open")
	@Produces(MediaType.APPLICATION_JSON)
	public String openUploadFilesDialog(
			@QueryParam("targetId") String targetId,
			@QueryParam("filesNum") String filesNum,
			@QueryParam("rootDir") String rootDir,
			@QueryParam("workDir") String workDir,
			@QueryParam("category") String category) throws BIJSONException {
		try {
			// 获得页面
			FLYVariableResolver attrsMap = new FLYVariableResolver();

			int num = Integer.valueOf(Const.NVL(filesNum, "1"));

			String[] files = new String[num];
			for (int i = 0; i < num; i++) {
				files[i] = "上传文件" + (i + 1);
			}
			attrsMap.addVariable("files", files);
			attrsMap.addVariable("filesNum", num);
			attrsMap.addVariable("rootDir", rootDir);
			attrsMap.addVariable("workDir", workDir);
			attrsMap.addVariable("category", category);

			Object[] domString = PageTemplateInterpolator.interpolate(
					TEMPLATE_UPLOAD_FILES, attrsMap);

			// 设置响应
			return AjaxResult.instanceDialogContent(targetId, domString)
					.toJSONString();
		} catch (Exception e) {
			log.error("打开当月预测填报界面出现问题。");
		}

		return ActionMessage.instance().failure("打开当月预测填报界面出现问题。")
				.toJSONString();
	}

	@Path("/upload")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaTypeUtils.MULTIPART_FORM_DATA)
	public String uploadFiles(@Context HttpServletRequest request)
			throws Exception {
		ActionMessage resultMsg = new ActionMessage();

		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(4096); // 设置缓冲区大小，这里是4kb

		ServletFileUpload upload = new ServletFileUpload(factory);
		String fileSizeMax = PropertyUtils.getProperty("fs.upload.maxsize");
		upload.setFileSizeMax(Long.parseLong(fileSizeMax));

		List<FileItem> items = upload.parseRequest(request);// 得到所有的文件
		// 提取参数
		Map<String, String> dataObj = extractParams(items);

		String rootDir = PropertyUtils.getProperty(dataObj.get("rootDir"));
		String workDir = dataObj.get("workDir");
		String category = PropertyUtils.getProperty(dataObj.get("category"));

		// 遍历文件并逐次上传
		for (FileItem item : items) {
			if (item.isFormField() || Const.isEmpty(item.getName())) {
				continue;
			}

			InputStream is = null;
			OutputStream os = null;

			File fullFile = new File(item.getName());
			try {
				String destFileStr = FileUtils.dirAppend(workDir, fullFile
						.getName());
				FileObject destFileObj = filesysService.composeVfsObject(
						category, destFileStr, rootDir);

				is = item.getInputStream();
				os = destFileObj.getContent().getOutputStream();

				byte[] bytes = new byte[1024];
				int in = 0;
				while ((in = is.read(bytes)) != -1) {
					os.write(bytes);
				}
				os.flush();
			} catch (IOException ioe) {
				log.error("read or write file exception:", ioe);
				resultMsg.addErrorMessage("上传文件" + fullFile.getName() + "失败");
				return resultMsg.toJSONString();
			} finally {
				if (os != null) {
					os.close();
				}
				if (is != null) {
					is.close();
				}
				item.delete();
			}
		}
		resultMsg.addMessage("上传操作成功");
		return resultMsg.toJSONString();
	}

	private Map<String, String> extractParams(List<FileItem> items)
			throws UnsupportedEncodingException, Exception {
		Map<String, String> params = new HashMap<String, String>();
		for (FileItem item : items) {
			if (!item.isFormField()) {
				continue;
			}
			String fieldName = item.getFieldName();
			String value = item.getString(Const.XML_ENCODING);
			params.put(fieldName, value);
		}
		return params;
	}

	/**
	 * 获得文件
	 * 
	 * @param dataStr
	 *            文件标识信息
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@GET
	@Path("/getfile")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public void getFile(@QueryParam("path") String workPath,
			@QueryParam("rootPath") String rootPathProp,
			@QueryParam("category") String categoryProp,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response, String body)
			throws IOException {
		InputStream is = null;

		try {
			String rootDir = PropertyUtils.getProperty(rootPathProp);
			String category = PropertyUtils.getProperty(categoryProp);
			// 拼装文件信息
			FileObject fileObj = filesysService.composeVfsObject(category,
					workPath, rootDir);

			is = fileObj.getContent().getInputStream();
			response.setContentType("application/octet-stream");
			request.setCharacterEncoding(Const.XML_ENCODING);
			response.setCharacterEncoding(Const.XML_ENCODING);
			String fileName = Const.replace(fileObj.getName().getBaseName(),
					" ", "%20");
			// 保证另存为文件名为中文
			response.setHeader("Content-Disposition", "attachment;filename="
					+ new String(fileName.getBytes(), "ISO8859_1"));
			byte[] b = new byte[1024];
			int i;
			OutputStream os = response.getOutputStream();
			while ((i = is.read(b)) != -1) {
				os.write(b, 0, i);
			}
			os.flush();
		} catch (Exception e) {
			log.error("download file exception:", e);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	/**
	 * 打开Portal菜单，通过注册ID
	 * 
	 * @param id
	 * @param targetId
	 * @return
	 * @throws BIException
	 */
	@GET
	@Path("/menu/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String openPortalDialog(@PathParam("id") String id,
			@QueryParam("targetId") String targetId) throws BIException {
		try {
			// 通过ID获得注册的菜单
			PortalMenu pm = portalDelegates.getPortalMenuById(Long.valueOf(id));

			// 验证权限
			if (pm.isAuthenticate()
					&& !userService
							.authenticate(
									AuthorizationObjectCategory.PORTAL_MENU, pm
											.getId())) {
				return ActionMessage.instance().failure("未登录或者当前用户不具有权限。")
						.toJSONString();
			}

			Map<String, Object> context = getDefaultContext(id, pm
					.getExtAttr(PORTAL_ONLY_PARAM));

			return invokeMethod(pm.getExtAttr("cls"), pm.getExtAttr("method"),
					context, targetId);
		} catch (Exception ex) {
			throw new BIException("打开Portal的菜单出现错误。", ex);
		}
	}

	@GET
	@Path("/action/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String openPortalActionDialog(@PathParam("id") String id,
			@QueryParam("targetId") String targetId,
			@QueryParam("param") String param) throws BIException {
		try {
			// 通过ID获得注册的菜单
			PortalAction pa = portalDelegates.getPortalActionById(Long
					.valueOf(id));

			Map<String, Object> context = getDefaultContext(id, param);

			return invokeMethod(pa.getCls(), pa.getMethod(), context, targetId);
		} catch (Exception ex) {
			throw new BIException("执行Portal的行为出现错误。", ex);
		}
	}

	@GET
	@Path("/menu/{id}/update")
	@Produces(MediaType.APPLICATION_JSON)
	public String updatePortalDialog(@PathParam("id") String id,
			@QueryParam("targetId") String targetId,
			@QueryParam("action") String action,
			@QueryParam("param") String param) throws BIException {
		try {
			// 通过ID获得注册的菜单
			PortalMenu pm = portalDelegates.getPortalMenuById(Long.valueOf(id));
			param = (Const.isEmpty(param)) ? pm.getExtAttr(PORTAL_ONLY_PARAM)
					: param;

			Map<String, Object> context = getDefaultContext(id, param);

			PortalAction pa = portalDelegates.getPortalActionById(Long
					.valueOf(action));

			return invokeMethod(pa.getCls(), pa.getMethod(), context, targetId);
		} catch (Exception ex) {
			throw new BIException("打开Portal的菜单出现错误。", ex);
		}
	}

	private Map<String, Object> getDefaultContext(String id, String param)
			throws UnsupportedEncodingException {
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("id", id);
		if (!Const.isEmpty(param)) {
			context.put(PORTAL_ONLY_PARAM, Utils.decodeURL(param));
		}

		return context;
	}

	/**
	 * 调用业务方法
	 * 
	 * @param cls
	 * @param method
	 * @param param
	 * @return
	 * @throws BIException
	 */
	private String invokeMethod(String cls, String method,
			Map<String, Object> context, String targetId) throws BIException {
		try {
			Class<?> clazz = Class.forName(cls);
			Object prog = BIAdaptorFactory.createCustomAdaptor(clazz);

			return (String) ReflectionUtils.invokeMethod(prog, method,
					targetId, context);

		} catch (Exception ex) {
			log.error("打开Portal的菜单出现错误。");
		}
		return ActionMessage.instance().failure("打开Portal的菜单出现错误。")
				.toJSONString();
	}

	@GET
	@Path("/menus")
	@Produces(MediaType.APPLICATION_JSON)
	public String getPortalMenus() throws BIException {
		try {
			String repository = ContextHolder.getRepositoryName();

			// 如果repository仍为空，返回空值
			if (Const.isEmpty(repository)) {
				return "[]";
			} else {
				List<PortalMenu> menus = portalDelegates
						.getPortalMenusByParent(PORTAL_MENU_ROOT_ID);
				User currentUser = userService.getCurrentUser();
				JSONArray ja = null;

				if (currentUser == null) {
					ja = getNotAuthenticatePortalMenus(menus);
				} else {
					ja = getAuthenticatePortalMenus(currentUser.getId(), menus);
				}
				return ja.toJSONString();
			}
		} catch (Exception ex) {
			throw new BIException("获得Portal的菜单出现错误。", ex);
		}
	}

	/**
	 * 返回不需要权限验证的菜单
	 * 
	 * @param menus
	 * @return
	 * @throws BIException
	 */
	@SuppressWarnings("unchecked")
	private JSONArray getNotAuthenticatePortalMenus(List<PortalMenu> menus)
			throws BIException {
		JSONArray ja = new JSONArray();
		if (menus != null) {
			for (PortalMenu pm : menus) {
				JSONObject jo = getNotAuthenticatePortalMenu(pm);
				if (jo != null) {
					ja.add(jo);
				}
			}
		}
		return ja;
	}

	@SuppressWarnings("unchecked")
	private JSONObject getNotAuthenticatePortalMenu(PortalMenu pm)
			throws BIException {
		JSONObject jo = null;
		if (pm != null) {
			// 校验权限
			if (!pm.isAuthenticate()) {
				jo = pm.getSimpleJSON();
				jo.put("children", getNotAuthenticatePortalMenus(pm
						.getChildren()));
			}
		}
		return jo;
	}

	/**
	 * 返回所有菜单，对于需要权限验证的进行权限验证
	 * 
	 * @param uid
	 * @param menus
	 * @return
	 * @throws BIException
	 */
	@SuppressWarnings("unchecked")
	private JSONArray getAuthenticatePortalMenus(long uid,
			List<PortalMenu> menus) throws BIException {
		JSONArray ja = new JSONArray();
		if (menus != null) {
			for (PortalMenu pm : menus) {
				JSONObject jo = getAuthenticatePortalMenu(uid, pm);
				if (jo != null) {
					ja.add(jo);
				}
			}
		}
		return ja;
	}

	@SuppressWarnings("unchecked")
	private JSONObject getAuthenticatePortalMenu(long uid, PortalMenu pm)
			throws BIException {
		JSONObject jo = null;
		if (pm != null) {
			// 校验权限
			if (pm.isAuthenticate()) {
				// 拥有读权限，可以显示出来
				if (userService.authenticate(uid,
						AuthorizationObjectCategory.PORTAL_MENU, pm.getId(),
						PermissionCategory.R)) {
					jo = pm.getSimpleJSON();
					// 拥有执行权限，可以执行，否则提示没有权限执行。
					if (userService.authenticate(uid,
							AuthorizationObjectCategory.PORTAL_MENU,
							pm.getId(), PermissionCategory.X)) {
						jo.put("disabled", false);
					} else {
						jo.put("disabled", true);
					}

				}
			} else {
				jo = pm.getSimpleJSON();
			}

			if (jo != null) {
				jo.put("children", getAuthenticatePortalMenus(uid, pm
						.getChildren()));
			}
		}
		return jo;
	}
}

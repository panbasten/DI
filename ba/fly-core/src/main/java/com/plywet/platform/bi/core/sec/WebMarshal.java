package com.plywet.platform.bi.core.sec;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.version.BuildVersion;

import com.plywet.platform.bi.core.exception.BISecurityException;
import com.plywet.platform.bi.core.utils.FileUtils;

public class WebMarshal {

	private static Class<?> PKG = WebMarshal.class;

	public static final String OS_NAME = "os.name";
	public static final String OS_ARCH = "os.arch";
	public static final String OS_VER = "os.version";

	private static final String LIC_FILE_NAME = "ba.lic";
	private static final String MARK_ALL_MAC = "-ALL-";

	private static final String LIC_CATEGORY_SYS = "SYS";
	private static final String LIC_CATEGORY_DB = "DB";
	private static final String LIC_CATEGORY_DN = "DN";
	private static final String LIC_CATEGORY_DI = "DI";
	private static final String LIC_CATEGORY_BA = "BA";
	private static final String LIC_CATEGORY_FS = "FS";

	public static final String TRIAL_VERSION = "-TRIAL-";
	public static final String BASE_VERSION = "-BASE-";
	public static final String OFFICIAL_VERSION = "-OFFICIAL-";
	public static final String UNREGISTERED_CUSTOMER = "-UNREGISTERED-";

	public static final String CHECK_MODULE_OK = "OK";

	private String os_name;
	private String os_arch;
	private String os_version;

	private String version;
	private String revision;
	private String build_date;
	private String build_user;

	private String mac_address;
	private String customerFullName;
	private String licVersion;

	private String[] moduleCodes = new String[] { LIC_CATEGORY_DB,
			LIC_CATEGORY_DN, LIC_CATEGORY_DI, LIC_CATEGORY_BA, LIC_CATEGORY_FS };
	private boolean[] moduleValids = new boolean[moduleCodes.length];

	private Map<Integer, LicenseControlObject> authModuleById = new HashMap<Integer, LicenseControlObject>();
	private Map<String, LicenseControlObject> authModuleByCode = new HashMap<String, LicenseControlObject>();

	private Date minValidDate;

	private static WebMarshal webMarshal;

	public static final WebMarshal getInstance() throws BISecurityException {
		if (webMarshal != null)
			return webMarshal;

		try {
			webMarshal = new WebMarshal();
		} catch (BISecurityException e) {
			webMarshal = null;
			throw e;
		}

		return webMarshal;
	}

	private WebMarshal() throws BISecurityException {
		try {
			initOsProperty();
			initBuildVersion();
			initMacAddress();
			checkLicense();
		} catch (BISecurityException e) {
			throw e;
		} catch (Exception e) {
			throw new BISecurityException(BaseMessages.getString(PKG,
					"Lic.Message.Cann.Get.Machine.Code"));
		}
	}

	public String checkModuleById(int id) {
		LicenseControlObject lco = authModuleById.get(Integer.valueOf(id));
		if (lco != null) {
			if (lco.getExpiredDate().compareTo(new Date()) > 0) {
				return CHECK_MODULE_OK;
			}
			return BaseMessages.getString(PKG,
					"Lic.Message.Extended.Auth.Module");
		}
		return BaseMessages.getString(PKG, "Lic.Message.No.Auth.Module");
	}

	public boolean isModuleById(int id) {
		if (CHECK_MODULE_OK.equals(checkModuleById(id))) {
			return true;
		}
		return false;
	}

	public String checkModuleByCode(String code) {
		LicenseControlObject lco = authModuleByCode.get(code);
		if (lco != null) {
			if (lco.getExpiredDate().compareTo(new Date()) > 0) {
				return CHECK_MODULE_OK;
			}
			return BaseMessages.getString(PKG,
					"Lic.Message.Extended.Auth.Module");
		}
		return BaseMessages.getString(PKG, "Lic.Message.No.Auth.Module");
	}

	public boolean isModuleByCode(String code) {
		if (CHECK_MODULE_OK.equals(checkModuleByCode(code))) {
			return true;
		}
		return false;
	}

	public String[] getModuleCodes() {
		String[] rtn = new String[moduleCodes.length];
		for (int i = 0; i < rtn.length; i++) {
			rtn[i] = moduleCodes[i];
		}
		return rtn;
	}

	public boolean[] getModuleValids() {
		boolean[] rtn = new boolean[moduleValids.length];
		for (int i = 0; i < rtn.length; i++) {
			rtn[i] = moduleValids[i];
		}
		return rtn;
	}

	public String[] getModuleDescriptions() {
		String[] text = new String[moduleCodes.length];
		for (int i = 0; i < moduleCodes.length; i++) {
			text[i] = BaseMessages.getString(PKG, "Lic.Model." + moduleCodes[i]
					+ ".Description");
			if (!moduleValids[i]) {
				text[i] = BaseMessages.getString(PKG,
						"Lic.Model.Message.Unauthorized")
						+ text[i];
			}
		}
		return text;
	}

	public String[] getModuleHelpTexts() {
		String[] text = new String[moduleCodes.length];
		for (int i = 0; i < moduleCodes.length; i++) {
			text[i] = BaseMessages.getString(PKG, "Lic.Model." + moduleCodes[i]
					+ ".HelpText");
			if (!moduleValids[i]) {
				text[i] = BaseMessages.getString(PKG,
						"Lic.Model.Message.Unauthorized")
						+ text[i];
			}
		}
		return text;
	}

	private final void checkLicense() throws BISecurityException {
		String lic = getLicenseFileString();
		try {
			List<String[]> licList = SignProvider.decodeLicense(lic);
			if (licList == null) {
				throw new BISecurityException(BaseMessages.getString(PKG,
						"Lic.Message.Invalid.License"));
			}

			this.customerFullName = licList.get(0)[0];
			this.licVersion = licList.get(0)[2];

			if (!this.mac_address.equalsIgnoreCase(licList.get(0)[1])
					&& !MARK_ALL_MAC.equals(licList.get(0)[1])) {
				throw new BISecurityException(BaseMessages.getString(PKG,
						"Lic.Message.Invalid.License"));
			}

			this.authModuleById.clear();
			this.authModuleByCode.clear();
			this.minValidDate = Const.MAX_DATE;
			for (int i = 1; i < licList.size(); i++) {
				LicenseControlObject lco = new LicenseControlObject(licList
						.get(i));
				this.authModuleById.put(Integer.valueOf(lco.getId()), lco);
				this.authModuleByCode.put(lco.getCode(), lco);

				if (this.minValidDate.compareTo(lco.getExpiredDate()) > 0) {
					this.minValidDate = lco.getExpiredDate();
				}

				// 注册模块
				for (int m = 0; m < moduleCodes.length; m++) {
					if (lco.getCode().startsWith(moduleCodes[m] + ".")) {
						moduleValids[m] = true;
						break;
					}
				}
			}

		} catch (BISecurityException e) {
			throw e;
		} catch (Exception e) {
			throw new BISecurityException(BaseMessages.getString(PKG,
					"Lic.Message.Load.License.Fail"));
		}

	}

	private final void initMacAddress() throws BISecurityException {

		try {
			this.mac_address = Const.getMACAddress();
		} catch (Exception e) {
			throw new BISecurityException(BaseMessages.getString(PKG,
					"Lic.Message.Cann.Get.Machine.Code"));
		}

		if (this.mac_address == null) {
			throw new BISecurityException(BaseMessages.getString(PKG,
					"Lic.Message.Cann.Get.Machine.Code"));
		}
	}

	private final void initOsProperty() {
		this.os_name = System.getProperty(OS_NAME);
		this.os_arch = System.getProperty(OS_ARCH);
		this.os_version = System.getProperty(OS_VER);
	}

	private final void initBuildVersion() {
		this.version = BuildVersion.getInstance().getVersion();
		this.revision = BuildVersion.getInstance().getRevision();
		this.build_date = BuildVersion.getInstance().getBuildDate();
		this.build_user = BuildVersion.getInstance().getBuildUser();
	}

	/**
	 * 获得License文件内容
	 * 
	 * @throws BISecurityException
	 */
	private final String getLicenseFileString() throws BISecurityException {
		String licFilePath = Const.getKettleDirectory() + Const.FILE_SEPARATOR
				+ LIC_FILE_NAME;
		if (!FileUtils.isFileExist(licFilePath)) {
			throw new BISecurityException(BaseMessages.getString(PKG,
					"Lic.Message.No.License"));
		}

		try {
			InputStream is = new FileInputStream(licFilePath);
			return FileUtils.getString(is);
		} catch (Exception e) {
			throw new BISecurityException(BaseMessages.getString(PKG,
					"Lic.Message.No.License"));
		}

	}

	public String getOsName() {
		return os_name;
	}

	public String getOsArch() {
		return os_arch;
	}

	public String getOsVersion() {
		return os_version;
	}

	public String getVersion() {
		return version;
	}

	public String getRevision() {
		return revision;
	}

	public String getBuildDate() {
		return build_date;
	}

	public String getBuildUser() {
		return build_user;
	}

	public String getCustomerFullName() {
		if (UNREGISTERED_CUSTOMER.equals(customerFullName)) {
			return BaseMessages.getString(PKG, "Lic.Customer.Unregistered");
		}
		return customerFullName;
	}

	public String getLicVersion() {
		return licVersion;
	}

	public Date getMinValidDate() {
		return minValidDate;
	}

	public String getLicVersionString() {
		String ver = licVersion.replaceAll("-", "");
		return BaseMessages.getString(PKG, "Lic.Version." + ver);
	}

	class LicenseControlObject {
		private int id;
		private String code;

		private Date expiredDate;
		private int concurrent = -1;

		public LicenseControlObject(String[] lic) throws ParseException {
			this.id = Integer.valueOf(lic[0]);
			this.code = lic[1];
			this.expiredDate = StringUtil.getParseDateTime(lic[2]);
			this.concurrent = Integer.valueOf(lic[3]);
		}

		public int getId() {
			return id;
		}

		public String getCode() {
			return code;
		}

		public Date getExpiredDate() {
			return expiredDate;
		}

		public int getConcurrent() {
			return concurrent;
		}

	}

}

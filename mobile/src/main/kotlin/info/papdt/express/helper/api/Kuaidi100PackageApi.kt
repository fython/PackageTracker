package info.papdt.express.helper.api

import android.util.Log
import com.github.promeg.pinyinhelper.Pinyin

import com.google.gson.Gson
import com.spreada.utils.chinese.ZHConverter

import java.util.ArrayList

import info.papdt.express.helper.model.BaseMessage
import info.papdt.express.helper.model.Kuaidi100Package
import info.papdt.express.helper.model.MaterialPalette
import info.papdt.express.helper.support.HttpUtils
import info.papdt.express.helper.support.MaterialColorGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author Fung Go (fython@163.com)
 * @version 2.0
 */
object Kuaidi100PackageApi {

	/** API Url  */
	private const val API_HOST = "https://www.kuaidi100.com"
	private const val QUERY_URL = "$API_HOST/query?type=%1\$s&postid=%2\$s"
	private const val COMPANY_DETECT_URL = "$API_HOST/autonumber/autoComNum?text=%s"
	private const val COMPANY_LIST_URL = "$API_HOST/js/share/company.js"

	private val TAG = Kuaidi100PackageApi::class.java.simpleName

	/**
	 * @param com Shipment company
	 * @param number Kuaidi100Package number
	 * @return query url
	 */
	@JvmStatic
	fun getQueryUrl(com: String?, number: String): String {
		val url = String.format(QUERY_URL, com, number)
		Log.i(TAG, "query url: $url")
		return url
	}

	/**
	 * @param number The number of package which you want to get its company
	 * @return company code query url
	 */
	@JvmStatic
    fun getCompantDetectUrl(number: String): String {
		return String.format(COMPANY_DETECT_URL, number)
	}

	/**
	 * @param number The number of package which you want to get its company
	 * @return company code
	 */
	@JvmStatic
    fun detectCompanyByNumber(number: String): String? {
		val message = HttpUtils.getString(getCompantDetectUrl(number), false)
		return if (message.code == BaseMessage.CODE_OKAY) {
			try {
				val result = Gson().fromJson(message.data, DetectResult::class.java)
				if (result.auto!!.size > 0) {
					result.auto!![0].comCode
				} else {
					null
				}
			} catch (e: Exception) {
				//return data may not be json
				e.printStackTrace()
				null
			}

		} else {
			null
		}
	}

	@JvmStatic
    val companyList: ArrayList<CompanyInfo.Company>?
			// return data may not be json
		get() {
			val message = HttpUtils.getString(COMPANY_LIST_URL, false)
			if (message.code == BaseMessage.CODE_OKAY) {
				var strJson = message.data!!.replace("var jsoncom=", "")
				strJson = strJson.replace("};", "}")
				strJson = strJson.replace("'", "\"")
				try {
					val result = Gson().fromJson(strJson, CompanyListResult::class.java)
					return if (result.company!!.size > 0 && result.error_size < 0) {
						val info = result.company!!.indices.mapTo(ArrayList()) {
							CompanyInfo.Company(
									result.company!![it].companyname!!,
									result.company!![it].code!!,
									result.company!![it].tel!!,
									result.company!![it].comurl!!
							)
						}
						info
					} else {
						null
					}
				} catch (e: Exception) {
					e.printStackTrace()
					return null
				}

			} else {
				return null
			}
		}

	/**
	 * @param number The number of package which you want to query
	 * @return Kuaidi100Package and status code
	 */
	@JvmStatic
    fun getPackageByNumber(number: String): BaseMessage<out Kuaidi100Package?> {
		val comcode = detectCompanyByNumber(number)
		return getPackage(comcode, number)
	}

	/**
	 * @param comcode The company code of package
	 * @param number The number of package
	 * @return Kuaidi100Package and status code
	 */
	@JvmStatic
    fun getPackage(comcode: String?, number: String): BaseMessage<out Kuaidi100Package?> {
		val message = HttpUtils.getString(getQueryUrl(comcode, number), false)
		return if (message.code == BaseMessage.CODE_OKAY) {
			try {
				val pkg = Kuaidi100Package.buildFromJson(message.data!!)
				if (pkg.status == "200") {
					BaseMessage(BaseMessage.CODE_OKAY, pkg)
				} else {
					pkg.number = number
					pkg.companyType = comcode
					pkg.companyChineseName = Kuaidi100PackageApi.CompanyInfo.getNameByCode(pkg.companyType)
					pkg.data = ArrayList()
					BaseMessage(BaseMessage.CODE_OKAY, pkg)
				}
			} catch (e: Exception) {
				e.printStackTrace()
				BaseMessage<Kuaidi100Package?>(BaseMessage.CODE_ERROR)
			}
		} else {
			BaseMessage(BaseMessage.CODE_ERROR)
		}
	}

	/** Filter companies by keyword  */
	@JvmStatic
    fun searchCompany(keyword: String?): ArrayList<CompanyInfo.Company>? {
        if (keyword == null) {
            return CompanyInfo.info
        }
		val keywordPy = ZHConverter.convert(keyword, ZHConverter.SIMPLIFIED)
		val src = ArrayList<CompanyInfo.Company>()
		if (keywordPy.trim { it <= ' ' }.isNotEmpty()) {
			CompanyInfo.info!!.indices
					.filterNot { !CompanyInfo.names[it].contains(keywordPy) && !CompanyInfo.pinyin[it].contains(keywordPy) }
					.mapTo(src) { CompanyInfo.info!![it] }
		} else {
			return CompanyInfo.info
		}
		return src
	}

	class DetectResult {
		internal var comCode: String? = null
		internal var num: String? = null
		internal var auto: ArrayList<AutoInfo>? = null

		internal inner class AutoInfo {
			var comCode: String? = null
			var id: String? = null
			var noPre: String? = null
			var startTime: String? = null
			var noCount: Int = 0
		}
	}

	private class CompanyListResult {
		internal var error_size: Int = 0
		internal var company: ArrayList<CompanyInfo>? = null

		internal inner class CompanyInfo {
			var cid: String? = null
			var id: String? = null
			var companyname: String? = null
			var shortname: String? = null
			var tel: String? = null
			var url: String? = null
			var code: String? = null
			var comurl: String? = null
			var isavailable: String? = null
			var promptinfo: String? = null
			var testnu: String? = null
			var freg: String? = null
			var freginfo: String? = null
			var telcomplaintnum: String? = null
			var queryurl: String? = null
			var serversite: String? = null
			var hasvali: Int = 0
		}
	}

	object CompanyInfo {

		var info: ArrayList<Company>? = null
		lateinit var names: Array<String>
		lateinit var pinyin: Array<String>

		class Company(var name: String,
					  var code: String,
					  var phone: String?,
					  var website: String?) {

			fun getPalette(): MaterialPalette {
				return MaterialColorGenerator.getPalette(name)
			}

		}

		fun findCompanyByCode(code: String?): Int {
			return if (code == null) -1 else (info!!.indices.firstOrNull { info!![it].code == code } ?: -1)
		}

		fun getNameByCode(code: String?): String? {
			if (code == null) return null
			val index = findCompanyByCode(code)
			return if (index != -1) info!![index].name else null
		}

		init {
			info = ArrayList()
			with(info!!) {
				add(Company("申通快递", "shentong", "95543", "http://www.sto.cn"))
				add(Company("EMS", "ems", "11183", "http://www.ems.com.cn/"))
				add(Company("顺丰速运", "shunfeng", "95338", "http://www.sf-express.com"))
				add(Company("韵达快递", "yunda", "95546", "http://www.yundaex.com"))
				add(Company("圆通速递", "yuantong", "95554", "http://www.ytoexpress.com/"))
				add(Company("中通快递", "zhongtong", "95311", "http://www.zto.cn"))
				add(Company("百世快递", "huitongkuaidi", "4009 565656", "http://www.800bestex.com/"))
				add(Company("天天快递", "tiantian", "400-188-8888", "http://www.ttkdex.com"))
				add(Company("宅急送", "zhaijisong", "400-6789-000", "http://www.zjs.com.cn"))
				add(Company("鑫飞鸿", "xinhongyukuaidi", "021-69781999", "http://www.kuaidi100.com/all/xfh.shtml"))
				add(Company("CCES/国通快递", "cces", "400-111-1123", "http://www.gto365.com"))
				add(Company("全一快递", "quanyikuaidi", "400-663-1111", "http://www.unitop-apex.com/"))
				add(Company("彪记快递", "biaojikuaidi", "+886 (02) 2562-3533", "http://www.pewkee.com"))
				add(Company("星晨急便", "xingchengjibian", "", ""))
				add(Company("亚风速递", "yafengsudi", "4001-000-002", "http://www.airfex.net/"))
				add(Company("源伟丰", "yuanweifeng", "400-601-2228", "http://www.ywfex.com"))
				add(Company("全日通", "quanritongkuaidi", "020-86298999", "http://www.at-express.com/"))
				add(Company("安信达", "anxindakuaixi", "400-716-1919", "http://www.axdkd.com"))
				add(Company("民航快递", "minghangkuaidi", "400-817-4008", "http://www.cae.com.cn"))
				add(Company("凤凰快递", "fenghuangkuaidi", "010-85826200", "http://www.phoenixexp.com"))
				add(Company("京广速递", "jinguangsudikuaijian", "0769-88629888", "http://www.szkke.com/"))
				add(Company("配思货运", "peisihuoyunkuaidi", "010-65489928,65489571,65489469,65489456", "http://www.peisi.cn"))
				add(Company("中铁物流", "ztky", "400-000-5566", "http://www.ztky.com "))
				add(Company("UPS", "ups", "400-820-8388", "http://www.ups.com/cn"))
				add(Company("FedEx-国际件", "fedex", "400-886-1888", "http://fedex.com/cn"))
				add(Company("DHL-中国件", "dhl", "800-810-8000", "http://www.cn.dhl.com"))
				add(Company("AAE-中国件", "aae", "400-610-0400", "http://cn.aaeweb.com"))
				add(Company("大田物流", "datianwuliu", "400-626-1166", "http://www.dtw.com.cn"))
				add(Company("德邦物流", "debangwuliu", "95353", "http://www.deppon.com"))
				add(Company("新邦物流", "xinbangwuliu", "4008-000-222", "http://www.xbwl.cn"))
				add(Company("龙邦速递", "longbanwuliu", "021-59218889", "http://www.lbex.net"))
				add(Company("一邦速递", "yibangwuliu", "000-000-0000", "http://www.ebon-express.com"))
				add(Company("速尔快递", "suer", "400-158-9888", "http://www.sure56.com"))
				add(Company("联昊通", "lianhaowuliu", "400-8888887", "http://www.lhtex.com.cn"))
				add(Company("广东邮政", "guangdongyouzhengwuliu", "020-38181677", "http://www.ep183.cn/"))
				add(Company("中邮物流", "zhongyouwuliu", "11183", "http://www.cnpl.com.cn"))
				add(Company("天地华宇", "tiandihuayu", "400-808-6666", "http://www.hoau.net"))
				add(Company("盛辉物流", "shenghuiwuliu", "4008-222-222", "http://www.shenghui56.com"))
				add(Company("长宇物流", "changyuwuliu", "4007-161-262", "http://61.145.121.47/custSearch.jsp"))
				add(Company("飞康达", "feikangda", "010-84223376,84223378", "http://www.fkd.com.cn"))
				add(Company("元智捷诚", "yuanzhijiecheng", "400-081-2345", "http://www.yjkd.com"))
				add(Company("邮政包裹/平邮", "youzhengguonei", "11185", "http://yjcx.chinapost.com.cn"))
				add(Company("国际包裹", "youzhengguoji", "11185", "http://intmail.183.com.cn/"))
				add(Company("万家物流", "wanjiawuliu", "4001-156-561", "http://www.manco-logistics.com/"))
				add(Company("远成物流", "yuanchengwuliu", "400-820-1646", "http://www.ycgwl.com/"))
				add(Company("信丰物流", "xinfengwuliu", "400-830-6333", "http://www.xf-express.com.cn"))
				add(Company("文捷航空", "wenjiesudi", "020-88561502,85871501,31683301", "http://www.wjexpress.com"))
				add(Company("全晨快递", "quanchenkuaidi", "0769-82026703", "http://www.qckd.net/"))
				add(Company("佳怡物流", "jiayiwuliu", "400-631-9999", "http://www.jiayi56.com/"))
				add(Company("优速物流", "youshuwuliu", "400-1111-119", "http://www.uc56.com"))
				add(Company("快捷速递", "kuaijiesudi", "4008-333-666", "http://www.kjkd.com/"))
				add(Company("D速快递", "dsukuaidi", "0531-88636363", "http://www.d-exp.cn"))
				add(Company("全际通", "quanjitong", "400-0179-888", "http://www.quanjt.com"))
				add(Company("能达速递", "ganzhongnengda", "400-6886-765", "http://www.nd56.com/"))
				add(Company("青岛安捷快递", "anjiekuaidi", "400-056-5656", "http://www.anjelex.com"))
				add(Company("越丰物流", "yuefengwuliu", "852-23909969", "http://www.yfexpress.com.hk"))
				add(Company("DPEX", "dpex", "400-920-7011/800-820-7011", "https://www.dpex.com/"))
				add(Company("急先达", "jixianda", "021-59766363", "http://www.joust.net.cn/"))
				add(Company("百福东方", "baifudongfang", "400-706-0609", "http://www.ees.com.cn"))
				add(Company("BHT", "bht", "010-58633508", "http://www.bht-exp.com/"))
				add(Company("伍圆速递", "wuyuansudi", "0592—5050535", "http://www.f5xm.com"))
				add(Company("蓝镖快递", "lanbiaokuaidi", "0769-82898999", "http://www.bluedart.cn"))
				add(Company("COE", "coe", "0755-83575000", "http://www.coe.com.hk"))
				add(Company("南京100", "nanjing", "025-84510043", "http://www.100cskd.com"))
				add(Company("恒路物流", "hengluwuliu", "400-182-6666", "http://www.e-henglu.com"))
				add(Company("金大物流", "jindawuliu", "0755-82262209", "http://www.szkingdom.com.cn"))
				add(Company("华夏龙", "huaxialongwuliu", "400-716-6133", "http://www.chinadragon56.com"))
				add(Company("运通中港", "yuntongkuaidi", "0769-81156999", "http://www.ytkd168.com"))
				add(Company("佳吉快运", "jiajiwuliu", "400-820-5566", "http://www.jiaji.com"))
				add(Company("盛丰物流", "shengfengwuliu", "0591-83621111", "http://www.sfwl.com.cn"))
				add(Company("源安达", "yuananda", "0769-85157789", "http://www.yadex.com.cn"))
				add(Company("加运美", "jiayunmeiwuliu", "0769-85515555", "http://www.jym56.cn/"))
				add(Company("万象物流", "wanxiangwuliu", "400-820-8088", "http://www.ewinshine.com"))
				add(Company("宏品物流", "hongpinwuliu", "400-612-1456", "http://www.hpexpress.com.cn"))
				add(Company("GLS", "gls", "877-914-5465", "http://www.gls-group.net"))
				add(Company("上大物流", "shangda", "400-021-9122", "http://www.sundapost.net"))
				add(Company("中铁快运", "zhongtiewuliu", "95572", "http://www.cre.cn "))
				add(Company("原飞航", "yuanfeihangwuliu", "0769-87001100", "http://www.yfhex.com"))
				add(Company("海外环球", "haiwaihuanqiu", "010-59790107", "http://www.haiwaihuanqiu.com/"))
				add(Company("三态速递", "santaisudi", "400-881-8106  ", "http://www.sfcservice.com/"))
				add(Company("晋越快递", "jinyuekuaidi", "400-638-9288", "http://www.byondex.com"))
				add(Company("联邦快递", "lianbangkuaidi", "400-889-1888", "http://cndxp.apac.fedex.com/dxp.html"))
				add(Company("飞快达", "feikuaida", "400-716-6666", "http://www.fkdex.com"))
				add(Company("全峰快递", "quanfengkuaidi", "400-100-0001", "http://www.qfkd.com.cn"))
				add(Company("如风达", "rufengda", "400-010-6660", "http://www.rufengda.com"))
				add(Company("乐捷递", "lejiedi", "400-618-1400", "http://www.ljd365.com"))
				add(Company("忠信达", "zhongxinda", "400-646-6665", "http://www.zhongxind.cn/index.asp"))
				add(Company("芝麻开门", "zhimakaimen", "400-105-6056", "http://www.zmkmex.com/ "))
				add(Company("赛澳递", "saiaodi", "4000-345-888", "http://www.51cod.com "))
				add(Company("海红网送", "haihongwangsong", "400-632-9988", "http://www.haihongwangsong.com/index.asp"))
				add(Company("共速达", "gongsuda", "400-111-0005", "http://www.gongsuda.com"))
				add(Company("嘉里大通", "jialidatong", "400-610-3188", "http://www.kerryeas.com"))
				add(Company("OCS", "ocs", "400-118-8588", "http://www.ocschina.com"))
				add(Company("USPS", "usps", "800-275-8777", "https://zh.usps.com"))
				add(Company("美国快递", "meiguokuaidi", "888-611-1888", "http://www.us-ex.com"))
				add(Company("成都立即送", "lijisong", "400-028-5666", "http://www.cdljs.com"))
				add(Company("银捷速递", "yinjiesudi", "0755-88999000", "www.sjfd-express.com"))
				add(Company("门对门", "menduimen", "400-700-7676", "http://www.szdod.com"))
				add(Company("递四方", "disifang", "0755-33933895", "http://www.4px.com"))
				add(Company("郑州建华", "zhengzhoujianhua", "0371-65995266", "http://www.zzjhtd.com/"))
				add(Company("河北建华", "hebeijianhua", "0311-86123186", "http://116.255.133.172/hebeiwebsite/index.jsp"))
				add(Company("微特派", "weitepai", "400-6363-000", "http://www.vtepai.com/ "))
				add(Company("DHL-德国件（DHL Deutschland）", "dhlde", "+49 (0) 180 5 345300-1*", "http://www.dhl.de/en.html"))
				add(Company("通和天下", "tonghetianxia", "400-0056-516 ", "http://www.cod56.com"))
				add(Company("EMS-国际件", "emsguoji", "11183", "http://www.ems.com.cn"))
				add(Company("FedEx-美国件", "fedexus", "800-463-3339", "http://www.fedex.com/us/"))
				add(Company("风行天下", "fengxingtianxia", "4000-404-909", "http://www.fxtxsy.com"))
				add(Company("康力物流", "kangliwuliu", "400-156-5156 ", "http://www.kangliex.com/"))
				add(Company("跨越速运", "kuayue", "4008-098-098 ", "http://www.ky-express.com/"))
				add(Company("海盟速递", "haimengsudi", "400-080-6369 ", "http://www.hm-express.com"))
				add(Company("圣安物流", "shenganwuliu", "4006-618-169 ", "http://www.sa56.net"))
				add(Company("一统飞鸿", "yitongfeihong", "61501533-608", "http://218.97.241.58:8080/yitongfeihongweb/common?action=toindex"))
				add(Company("中速快递", "zhongsukuaidi", "11183", "http://www.ems.com.cn/mainservice/ems/zhong_su_guo_ji_kuai_jian.html"))
				add(Company("新蛋奥硕", "neweggozzo", "400-820-4400", "http://www.ozzo.com.cn"))
				add(Company("OnTrac", "ontrac", "800-334-5000", "http://www.ontrac.com"))
				add(Company("七天连锁", "sevendays", "400-882-1202", "http://www.92856.cn"))
				add(Company("明亮物流", "mingliangwuliu", "400-035-6568", "http://www.szml56.com/"))
				add(Company("凡客配送（作废）", "vancl", "400-600-6888", "http://www.vancl.com/"))
				add(Company("华企快运", "huaqikuaiyun", "400-806-8111", "13055209678"))
				add(Company("城市100", "city100", "400-820-0088", "http://www.bjcs100.com/"))
				add(Company("红马甲物流", "sxhongmajia", "0351-5225858", "http://www.hmj.com.cn/"))
				add(Company("穗佳物流", "suijiawuliu", "400-880-9771", "http://www.suijiawl.com"))
				add(Company("飞豹快递", "feibaokuaidi", "400-000-5566", "http://www.ztky.com/feibao/KJCX.aspx"))
				add(Company("传喜物流", "chuanxiwuliu", "400-777-5656 ", "http://www.cxcod.com/"))
				add(Company("捷特快递", "jietekuaidi", "400-820-8585", "http://www.jet185.com/"))
				add(Company("隆浪快递", "longlangkuaidi", "021-31171576 61552015", "http://www.56l6.com/"))
				add(Company("EMS-英文", "emsen", "11183", "http://www.ems.com.cn/english.html"))
				add(Company("中天万运", "zhongtianwanyun", "400-0056-001", "http://www.ztwy56.cn/"))
				add(Company("香港(HongKong Post)", "hkpost", "(852) 2921 2222", "http://www.hongkongpost.hk"))
				add(Company("邦送物流", "bangsongwuliu", "021-20965696", "http://express.banggo.com"))
				add(Company("国通快递", "guotongkuaidi", "400-111-1123", "http://www.gto365.com"))
				add(Company("澳大利亚(Australia Post)", "auspost", "0061-3-88479045", "http://auspost.com.au"))
				add(Company("加拿大(Canada Post)", "canpost", "416-979-8822", "http://www.canadapost.ca"))
				add(Company("加拿大邮政", "canpostfr", "", ""))
				add(Company("UPS-全球件", "upsen", "1-800-742-5877 ", "http://www.ups.com/"))
				add(Company("TNT-全球件", "tnten", "", "http://www.tnt.com"))
				add(Company("DHL-全球件", "dhlen", "", "http://www.dhl.com/en.html"))
				add(Company("顺丰-美国件", "shunfengen", "1-855-901-1133", "http://www.sf-express.com/us/en/"))
				add(Company("汇强快递", "huiqiangkuaidi", "", ""))
				add(Company("希优特", "xiyoutekuaidi", "4008400365", "http://www.cod365.com/"))
				add(Company("昊盛物流", "haoshengwuliu", "400-186-5566", "http://www.hs-express.cn/"))
				add(Company("尚橙物流", "shangcheng", "400-890-0101", "http://www.suncharms.net/"))
				add(Company("亿领速运", "yilingsuyun", "400-1056-400", "http://www.yelee.com.cn/"))
				add(Company("大洋物流", "dayangwuliu", "400-820-0088", "http://www.dayang365.cn/"))
				add(Company("递达速运", "didasuyun", "400-687-8123", "http://www.dida.hk/"))
				add(Company("易通达", "yitongda", "0898-65339299", "http://www.etd365.com/"))
				add(Company("邮必佳", "youbijia", "400-687-8123", "http://www.ubjia.com/"))
				add(Company("亿顺航", "yishunhang", "4006-018-268 ", "http://www.igoex.com/"))
				add(Company("飞狐快递", "feihukuaidi", "010-51389299", "http://www.feihukuaidi.com/"))
				add(Company("潇湘晨报", "xiaoxiangchenbao", "", ""))
				add(Company("巴伦支", "balunzhi", "400-885-6561", "http://cnbd.hendari.com/"))
				add(Company("Aramex", "aramex", "4006318388", "http://www.aramex.com/"))
				add(Company("闽盛快递", "minshengkuaidi", "0592-3725988", "http://www.xmms-express.com/"))
				add(Company("佳惠尔", "syjiahuier", "024-23904138", "http://www.jhekd.com/"))
				add(Company("民邦速递", "minbangsudi", "0769-81515303", "http://www.mbex168.com/"))
				add(Company("上海快通", "shanghaikuaitong", "", ""))
				add(Company("北青小红帽", "xiaohongmao", "010-67756666", "http://www.kuaidi100.com/all/xiaohongmao.shtml"))
				add(Company("GSM", "gsm", "021-64656011 ", "http://www.gsmnton.com "))
				add(Company("安能物流", "annengwuliu", "400-104-0088", "http://www.ane56.com"))
				add(Company("KCS", "kcs", "800-858-5590", "http://www.kcs56.com"))
				add(Company("City-Link", "citylink", "603-5565 8399", "http://www.citylinkexpress.com/"))
				add(Company("店通快递", "diantongkuaidi", "021-20917385 66282857", "http://www.shdtkd.com.cn/"))
				add(Company("凡宇快递", "fanyukuaidi", "4006-580-358 ", "http://www.fanyu56.com.cn/"))
				add(Company("平安达腾飞", "pingandatengfei", "4009-990-998", "http://www.padtf.com/"))
				add(Company("广东通路", "guangdongtonglu", "", ""))
				add(Company("中睿速递", "zhongruisudi", "400-0375-888", "http://www.zorel.cn/"))
				add(Company("快达物流", "kuaidawuliu", "", ""))
				add(Company("佳吉快递", "jiajikuaidi", "400-820-5566", "http://www.jiaji.com/"))
				add(Company("ADP国际快递", "adp", "1588-1330", "http://www.adpair.co.kr/"))
				add(Company("颿达国际快递", "fardarww", "0755-27332618", "http://www.fardar.com/"))
				add(Company("颿达国际快递-英文", "fandaguoji", "0755-27332618", "http://www.fardar.com/"))
				add(Company("林道国际快递", "shlindao", "4008-200-112", "http://www.ldxpress.com/"))
				add(Company("中外运速递-中文", "sinoex", "010-8041 8611", "http://www.sinoex.com.cn"))
				add(Company("中外运速递", "zhongwaiyun", "010-8041 8611", "http://www.sinoex.com.cn/index.aspx"))
				add(Company("深圳德创物流", "dechuangwuliu", "4006-989-833", "http://www.dc56.cn/"))
				add(Company("林道国际快递-英文", "ldxpres", "800-820-1470 ", "http://www.ldxpress.com/"))
				add(Company("瑞典（Sweden Post）", "ruidianyouzheng", "+46 8 23 22 20", "http://www.posten.se/en"))
				add(Company("PostNord(Posten AB)", "postenab", "+46 771 33 33 10", "http://www.posten.se/en"))
				add(Company("偌亚奥国际快递", "nuoyaao", "4008 871 871", "http://www.royaleinternational.com/"))
				add(Company("城际速递", "chengjisudi", "4000-523-525 ", "http://chengji-express.com"))
				add(Company("祥龙运通物流", "xianglongyuntong", "4008-908-908", "http://www.ldl.com.cn"))
				add(Company("品速心达快递", "pinsuxinda", "400-800-3693 ", "http://www.psxd88.com/"))
				add(Company("宇鑫物流", "yuxinwuliu", "0371-66368798", "http://www.yx56.cn/"))
				add(Company("陪行物流", "peixingwuliu", "400-993-0555", "http://www.peixingexpress.com"))
				add(Company("户通物流", "hutongwuliu", "400-060-1656", "http://www.cnhtwl.com"))
				add(Company("西安城联速递", "xianchengliansudi", "029-89113508", "http://www.city-link.net.cn/"))
				add(Company("煜嘉物流", "yujiawuliu", "", "http://www.yujia56.net/"))
				add(Company("一柒国际物流", "yiqiguojiwuliu", "001-(971) 238-9990", "http://www.17htb.com/"))
				add(Company("Fedex-国际件-中文", "fedexcn", "400-889-1888", "http://www.fedex.com/cn/index.html"))
				add(Company("联邦快递-英文", "lianbangkuaidien", "400-889-1888", "http://cndxp.apac.fedex.com/tracking/track.html"))
				add(Company("中通（带电话）", "zhongtongphone", "", ""))
				add(Company("赛澳递for买卖宝", "saiaodimmb", "", ""))
				add(Company("上海无疆for买卖宝", "shanghaiwujiangmmb", "", ""))
				add(Company("新加坡小包(Singapore Post)", "singpost", "", "http://www.singpost.com/"))
				add(Company("音素快运", "yinsu", "400-007-1118", "http://www.yskd168.com/"))
				add(Company("南方传媒物流", "ndwl", "", ""))
				add(Company("速呈宅配", "sucheng", "", ""))
				add(Company("创一快递", "chuangyi", "", ""))
				add(Company("云南滇驿物流", "dianyi", "", ""))
				add(Company("重庆星程快递", "cqxingcheng", "", ""))
				add(Company("四川星程快递", "scxingcheng", "", ""))
				add(Company("贵州星程快递", "gzxingcheng", "", ""))
				add(Company("运通中港快递(作废)", "ytkd", "", ""))
				add(Company("Gati-英文", "gatien", "4000-804-284", "http://www.gati.com/"))
				add(Company("Gati-中文", "gaticn", "4000-804-284 ", "http://www.gaticn.com/"))
				add(Company("jcex", "jcex", "", ""))
				add(Company("派尔快递", "peex", "", ""))
				add(Company("凯信达", "kxda", "", ""))
				add(Company("安达信", "advancing", "", ""))
				add(Company("汇文", "huiwen", "", ""))
				add(Company("亿翔", "yxexpress", "", ""))
				add(Company("东红物流", "donghong", "4000-081-556", "http://www.donghong56.com/"))
				add(Company("飞远配送", "feiyuanvipshop", "4007-031-313", "http://www.fyps.cn/"))
				add(Company("好运来", "hlyex", "020-86293333", "http://www.hlyex.com/"))
				add(Company("Toll", "dpexen", "", "http://www.dpex.com/"))
				add(Company("增益速递", "zengyisudi", "4008-456-789 ", "http://www.zeny-express.com/"))
				add(Company("四川快优达速递", "kuaiyouda", "4006-068-555", "http://www.sckyd.net/"))
				add(Company("日昱物流", "riyuwuliu", "4008-820-800", "http://www.rywl.cn/"))
				add(Company("速通物流", "sutongwuliu", "", ""))
				add(Company("晟邦物流", "nanjingshengbang", "400-666-6066", "http://www.3856.cc/"))
				add(Company("爱尔兰(An Post)", "anposten", "01-7057600 ", "http://www.anpost.ie/AnPost/ "))
				add(Company("日本（Japan Post）", "japanposten", "+81 0570-046111", "http://www.post.japanpost.jp/english/index.html "))
				add(Company("丹麦(Post Denmark)", "postdanmarken", "+45 80 20 70 30 ", "http://www.postdanmark.dk/en/Pages/home.aspx "))
				add(Company("巴西(Brazil Post/Correios)", "brazilposten", "+55 61 3003 0100", "http://www.correios.com.br/ "))
				add(Company("荷兰挂号信(PostNL international registered mail)", "postnlcn", "34819", "http://www.postnl.post"))
				add(Company("荷兰挂号信(PostNL international registered mail)", "postnl", "34819", "http://www.postnl.post/details/"))
				add(Company("乌克兰EMS-中文(EMS Ukraine)", "emsukrainecn", "+38 044 234-73-84", "http://dpsz.ua/en"))
				add(Company("乌克兰EMS(EMS Ukraine)", "emsukraine", "+38 044 234-73-84", "http://dpsz.ua/en"))
				add(Company("乌克兰邮政包裹", "ukrpostcn", "", ""))
				add(Company("乌克兰小包、大包(UkrPost)", "ukrpost", "+380 (0) 800-500-440", "http://www.ukrposhta.com/"))
				add(Company("海红for买卖宝", "haihongmmb", "", ""))
				add(Company("FedEx-英国件（FedEx UK)", "fedexuk", "+ 44 2476 706 660", "http://www.fedex.com/gb/ukservices/"))
				add(Company("FedEx-英国件", "fedexukcn", "+ 44 2476 706 660", "http://www.fedex.com/gb/ukservices/"))
				add(Company("叮咚快递", "dingdong", "", ""))
				add(Company("DPD", "dpd", "+31 20 480 2900", "http://www.dpd.com/"))
				add(Company("UPS Freight", "upsfreight", "+1 800-333-7400", "http://ltl.upsfreight.com/"))
				add(Company("ABF", "abf", "(479) 785-6486", "http://www.abfs.com/"))
				add(Company("Purolator", "purolator", "-8754", "http://www.purolator.com/"))
				add(Company("比利时（Bpost）", "bpost", "+32 (0)2 278 50 90", "http://www.bpostinternational.com/"))
				add(Company("比利时国际(Bpost international)", "bpostinter", "+32 (0)2 278 50 90", "http://www.bpostinternational.com/"))
				add(Company("LaserShip", "lasership", "+1 (800) 527-3764", "http://www.lasership.com/"))
				add(Company("英国大包、EMS（Parcel Force）", "parcelforce", "08448 00 44 66", "http://www.parcelforce.com/"))
				add(Company("英国邮政大包EMS", "parcelforcecn", "08448 00 44 66", "http://www.parcelforce.com/"))
				add(Company("YODEL", "yodel", "+44 800 0152 662", "http://www.myyodel.co.uk/"))
				add(Company("DHL-荷兰（DHL Netherlands）", "dhlnetherlands", "+31 26-324 6700", "http://www.dhl.nl"))
				add(Company("MyHermes", "myhermes", "+44 844 543 7000", "https://www.myhermes.co.uk/"))
				add(Company("DPD Germany", "dpdgermany", "+49 01806 373 200", "https://www.dpd.com/de/(portal)/de/(rememberCountry)/0"))
				add(Company("Fastway Ireland", "fastway", "+353 1 4242 900", "http://www.fastway.ie/index.php"))
				add(Company("法国大包、EMS-法文（Chronopost France）", "chronopostfra", "+33 (0) 969 391 391", "http://www.chronopost.fr/"))
				add(Company("Selektvracht", "selektvracht", "+31 0900-2222120", "http://www.selektvracht.nl/"))
				add(Company("蓝弧快递", "lanhukuaidi", "4000661646", "http://www.lanhukd.com/"))
				add(Company("比利时(Belgium Post)", "belgiumpost", "+32 2 276 22 74", "http://www.bpost.be/"))
				add(Company("UPS Mail Innovations", "upsmailinno", "+1 800-500-2224", "http://www.upsmailinnovations.com/"))
				add(Company("挪威（Posten Norge）", "postennorge", "+47 21316260", "http://www.posten.no/en/"))
				add(Company("瑞士邮政", "swisspostcn", "+41848888888", "https://www.post.ch/de/privat?wt_shortcut=www-swisspost-com&WT.mc_id=shortcut_www-swisspost-com"))
				add(Company("瑞士(Swiss Post)", "swisspost", "+41 848 888 888", "http://www.post.ch/en"))
				add(Company("英国邮政小包", "royalmailcn", "", ""))
				add(Company("英国小包（Royal Mail）", "royalmail", "+44 1752387112", "http://www.royalmail.com/"))
				add(Company("DHL Benelux", "dhlbenelux", "+31 26-324 6700", "http://www.dhl.nl/nl.html"))
				add(Company("Nova Poshta", "novaposhta", "+7 (0) 800 500 609", "http://novaposhta.ua/"))
				add(Company("DHL-波兰（DHL Poland）", "dhlpoland", "+48 42 6 345 345", "http://www.dhl.com.pl/pl.html"))
				add(Company("Estes", "estes", "1-866-378-3748", "http://www.estes-express.com/"))
				add(Company("TNT UK", "tntuk", "+44 0800 100 600", "http://www.tnt.com/portal/location/en.html"))
				add(Company("Deltec Courier", "deltec", "+44 (0) 20 8569 6767", "https://www.deltec-courier.com"))
				add(Company("OPEK", "opek", "+48 22 732 79 99", "http://www.opek.com.pl/"))
				add(Company("DPD Poland", "dpdpoland", "+48 801 400 373", "http://www.dpd.com.pl/"))
				add(Company("Italy SDA", "italysad", "+39 199 113366", "http://wwww.sda.it/"))
				add(Company("MRW", "mrw", "+34 902 300 402", "http://www.mrw.es/"))
				add(Company("Chronopost Portugal", "chronopostport", "+351 707 20 28 28", "http://chronopost.pt/"))
				add(Company("西班牙(Correos de Espa?a)", "correosdees", "+34 902197197", "http://www.correos.es"))
				add(Company("Direct Link", "directlink", "+1 (908) 289-0703", "http://www.directlink.com"))
				add(Company("ELTA Hellenic Post", "eltahell", "+30 801 11 83000", "https://www.elta-courier.gr"))
				add(Company("捷克（?eská po?ta）", "ceskaposta", "+420 840 111 244", "http://www.ceskaposta.cz/index"))
				add(Company("Siodemka", "siodemka", "+48 22 777 77 77", "http://www.siodemka.com/"))
				add(Company("International Seur", "seur", "+34 93 336 85 85", "http://www.seur.com/"))
				add(Company("久易快递", "jiuyicn", "021-64206088", "http://www.jiuyicn.com/"))
				add(Company("克罗地亚（Hrvatska Posta）", "hrvatska", "+385 0800 303 304", "http://www.posta.hr/"))
				add(Company("保加利亚（Bulgarian Posts）", "bulgarian", "+3592/949 3280", "http://www.bgpost.bg/"))
				add(Company("Portugal Seur", "portugalseur", "+351 707 50 10 10", "http://www.seur.com/"))
				add(Company("EC-Firstclass", "ecfirstclass", "+86 4006 988 223", "http://www.ec-firstclass.org/Details.aspx"))
				add(Company("DTDC India", "dtdcindia", "+91 33004444", "http://dtdc.com"))
				add(Company("Safexpress", "safexpress", "+91 11 26783281", "http://www.safexpress.com"))
				add(Company("韩国（Korea Post）", "koreapost", "+82 2 2195 1114", "http://www.koreapost.go.kr/kpost/main/index.jsp"))
				add(Company("TNT Australia", "tntau", "+61 13 11 50", "https://www.tntexpress.com.au"))
				add(Company("泰国（Thailand Thai Post）", "thailand", "0 2573 5463", "http://www.thailandpost.co.th "))
				add(Company("SkyNet Malaysia", "skynetmalaysia", "+60 3- 56239090", "http://www.skynet.com.my/"))
				add(Company("马来西亚小包（Malaysia Post(Registered)）", "malaysiapost", "+603 27279100", "http://www.pos.com.my/"))
				add(Company("马来西亚大包、EMS（Malaysia Post(parcel,EMS)）", "malaysiaems", "+603 27279100", "http://www.pos.com.my/"))
				add(Company("京东", "jd", "400-603-3600", "http://www.jd-ex.com"))
				add(Company("沙特阿拉伯(Saudi Post)", "saudipost", "+966 9200 05700", "http://www.sp.com.sa"))
				add(Company("南非（South African Post Office）", "southafrican", "+27 0860 111 502", "http://www.postoffice.co.za"))
				add(Company("OCA Argentina", "ocaargen", "+34 800-999-7700", "http://www.oca.com.ar/"))
				add(Company("尼日利亚(Nigerian Postal)", "nigerianpost", "234-09-3149531", "http://www.nipost.gov.ng"))
				add(Company("智利(Correos Chile)", "chile", "+562 600 950 2020", "http://www.correos.cl"))
				add(Company("以色列(Israel Post)", "israelpost", "+972 2 629 0691", "http://www.israelpost.co.il"))
				add(Company("Toll Priority(Toll Online)", "tollpriority", "+61 13 15 31", "https://online.toll.com.au"))
				add(Company("Estafeta", "estafeta", "+52 1-800-378-2338", "http://rastreo3.estafeta.com"))
				add(Company("港快速递", "gdkd", "400-11-33333", "http://www.gksd.com/"))
				add(Company("墨西哥（Correos de Mexico）", "mexico", "+52 01 800 701 7000", "http://www.correosdemexico.gob.mx"))
				add(Company("罗马尼亚（Posta Romanian）", "romanian", "+40 021 9393 111", "http://www.posta-romana.ro/posta-romana.html"))
				add(Company("TNT Italy", "tntitaly", "+39 199 803 868", "http://www.tnt.it"))
				add(Company("Mexico Multipack", "multipack", "+52 1800 7023200", "http://www.multipack.com.mx/"))
				add(Company("葡萄牙（Portugal CTT）", "portugalctt", "+351 707 26 26 26", "http://www.ctt.pt"))
				add(Company("Interlink Express", "interlink", "+44 8702 200 300", "http://www.interlinkexpress.com/"))
				add(Company("DPD UK", "dpduk", "+44 845 9 300 350", "http://www.dpd.co.uk/"))
				add(Company("华航快递", "hzpl", "400-697-0008", "http://www.hz3pl.com"))
				add(Company("Gati-KWE", "gatikwe", "+91 1800-180-4284", "http://www.gatikwe.com/"))
				add(Company("Red Express", "redexpress", "+91 1800-123-2400", "https://www.getsetred.net"))
				add(Company("Mexico Senda Express", "mexicodenda", "+52 1800 833 93 00", "http://www.sendaexpress.com.mx/rastreo.asp#af"))
				add(Company("TCI XPS", "tcixps", "18002000977", "http://www.tcixps.com/"))
				add(Company("高铁速递", "hre", "400-999-7777", "http://www.hre-e.com/"))
				add(Company("新加坡EMS、大包(Singapore Speedpost)", "speedpost", "+65 6222 5777", "http://www.speedpost.com.sg/"))
				add(Company("EMS-国际件-英文", "emsinten", "", "http://www.ems.com.cn/"))
				add(Company("Asendia USA", "asendiausa", "+1 610 461 3661", "http://www.asendiausa.com/"))
				add(Company("法国大包、EMS-英文(Chronopost France)", "chronopostfren", "+33 (0) 969 391 391", "http://www.chronopost.fr/"))
				add(Company("意大利(Poste Italiane)", "italiane", "+39 803 160", "http://www.poste.it/"))
				add(Company("冠达快递", "gda", "400-990-0088", "http://www.gda-e.com.cn/"))
				add(Company("出口易", "chukou1", "4006-988-223", "http://www.chukou1.com"))
				add(Company("黄马甲", "huangmajia", "029-96128", "http://www.huangmajia.com"))
				add(Company("新干线快递", "anlexpress", "", ""))
				add(Company("飞洋快递", "shipgce", "001-877-387-9799", "http://express.shipgce.com/"))
				add(Company("贝海国际速递", "xlobo", "086-400-082-2200", "http://www.xlobo.com/"))
				add(Company("阿联酋(Emirates Post)", "emirates", "600-599-999", "http://www.epg.gov.ae/"))
				add(Company("新顺丰（NSF）", "nsf", "0064-9-5258288", "http://www.nsf.co.nz/"))
				add(Company("巴基斯坦(Pakistan Post)", "pakistan", "（+92 51）926 00 37", "http://ep.gov.pk/"))
				add(Company("世运快递", "shiyunkuaidi", "400-666-1111", "http://www.sehoex.com/"))
				add(Company("合众速递(UCS）", "ucs", "024-31515566", "http://www.ucsus.com"))
				add(Company("阿富汗(Afghan Post)", "afghan", "+93 20 2104075", "http://track.afghanpost.gov.af/"))
				add(Company("白俄罗斯(Belpochta)", "belpost", "+375 17 293 59 10", "http://www.belpost.by/"))
				add(Company("全通快运", "quantwl", "", ""))
				add(Company("宅急便", "zhaijibian", "", ""))
				add(Company("EFS Post", "efs", "0773-2308246", "http://www.efspost.com/ "))
				add(Company("TNT Post", "tntpostcn", "+31（0）900 0570 ", "http://parcels-uk.tntpost.com/ "))
				add(Company("英脉物流", "gml", "400-880-5088", "http://www.gml.cn/"))
				add(Company("广通速递", "gtongsudi", "400-801-5567", "http://www.gto56.com"))
				add(Company("东瀚物流", "donghanwl", "400-092-2229", "http://www.donghanwl.com/"))
				add(Company("rpx", "rpx", "", ""))
				add(Company("日日顺物流", "rrs", "400-800-9999", "http://www.rrs.com/wl/fwwl"))
				add(Company("黑猫雅玛多", "yamato", "", ""))
				add(Company("华通快运", "htongexpress", "", ""))
				add(Company("吉尔吉斯斯坦(Kyrgyz Post)", "kyrgyzpost", "", "http://www.posta.kg"))
				add(Company("拉脱维亚(Latvijas Pasts)", "latvia", "", "http://www.pasts.lv"))
				add(Company("黎巴嫩(Liban Post)", "libanpost", "+961 1 629628", "http://www.libanpost.com.lb"))
				add(Company("立陶宛（Lietuvos pa?tas）", "lithuania", "+370 700 55 400", "http://www.post.lt"))
				add(Company("马尔代夫(Maldives Post)", "maldives", "+960 331 5555", "https://www.maldivespost.com/store/"))
				add(Company("马耳他（Malta Post）", "malta", "800 7 22 44", "http://maltapost.com"))
				add(Company("马其顿(Macedonian Post)", "macedonia", "", "http://www.posta.com.mk/"))
				add(Company("新西兰（New Zealand Post）", "newzealand", "", "https://www.nzpost.co.nz"))
				add(Company("摩尔多瓦(Posta Moldovei)", "moldova", "+373 - 22 270 044", "http://www.posta.md/"))
				add(Company("孟加拉国(EMS)", "bangladesh", "9558006", "www.bangladeshpost.gov.b"))
				add(Company("塞尔维亚(PE Post of Serbia)", "serbia", "0700 100 300", "http://www.posta.rs"))
				add(Company("塞浦路斯(Cyprus Post)", "cypruspost", "77778013", "http://www.mcw.gov.cy/mcw/postal/dps.nsf/index_en/index_en"))
				add(Company("突尼斯EMS(Rapid-Poste)", "tunisia", "(+216) 71 888 888 ", "http://www.e-suivi.poste.tn"))
				add(Company("乌兹别克斯坦(Post of Uzbekistan)", "uzbekistan", "(99871) 233 57 47", "http://www.pochta.uz/en/"))
				add(Company("新喀里多尼亚[法国](New Caledonia)", "caledonia", "", "http://www.opt.nc"))
				add(Company("叙利亚(Syrian Post)", "republic", "", "http://www.syrianpost.gov.sy/"))
				add(Company("亚美尼亚(Haypost-Armenian Postal)", "haypost", "", "http://www.haypost.am"))
				add(Company("也门(Yemen Post)", "yemen", "", "http://www.post.ye/"))
				add(Company("印度(India Post)", "india", "1800-11-2011", "http://www.indiapost.gov.in"))
				add(Company("英国(大包,EMS)", "england", "", ""))
				add(Company("约旦(Jordan Post)", "jordan", "-4292044", "http://www.jordanpost.com.jo/"))
				add(Company("越南小包(Vietnam Posts)", "vietnam", "(+84) 1900 54 54 81", "http://www.vnpost.vn/"))
				add(Company("黑山(Po?ta Crne Gore)", "montenegro", "", "http://www.postacg.me"))
				add(Company("哥斯达黎加(Correos de Costa Rica)", "correos", "", "https://www.correos.go.cr"))
				add(Company("西安喜来快递", "xilaikd", "", ""))
				add(Company("格陵兰[丹麦]（TELE Greenland A/S）", "greenland", "", "http://sp.post.gl"))
				add(Company("菲律宾（Philippine Postal）", "phlpost", "", "https://www.phlpost.gov.ph"))
				add(Company("厄瓜多尔(Correos del Ecuador)", "ecuador", "(593-2) 3829210", "http://www.correosdelecuador.gob.ec/"))
				add(Company("冰岛(Iceland Post)", "iceland", "", "http://www.postur.is/"))
				add(Company("波兰小包(Poczta Polska)", "emonitoring", "801 333 444", "http://www.poczta-polska.pl/"))
				add(Company("阿尔巴尼亚(Posta shqipatre)", "albania", "", "http://www.postashqiptare.al/"))
				add(Company("阿鲁巴[荷兰]（Post Aruba）", "aruba", "+297 528-7637 ", "http://www.postaruba.com"))
				add(Company("埃及（Egypt Post）", "egypt", "23910011", "http://www.egyptpost.org/"))
				add(Company("爱尔兰(An Post)", "ireland", "+353 1850 57 58 59", "http://www.anpost.ie/AnPost/"))
				add(Company("爱沙尼亚(Eesti Post)", "omniva", "", "https://www.omniva.ee/"))
				add(Company("云豹国际货运", "leopard", "", ""))
				add(Company("中外运空运", "sinoairinex", "", ""))
				add(Company("上海昊宏国际货物", "hyk", "", ""))
				add(Company("城晓国际快递", "ckeex", "", ""))
				add(Company("匈牙利（Magyar Posta）", "hungary", "+36 1 421 7296 Search", "http://posta.hu/international"))
				add(Company("澳门(Macau Post)", "macao", "", "http://www.macaupost.gov.mo/"))
				add(Company("台湾（中华邮政）", "postserv", "", "http://postserv.post.gov.tw"))
				add(Company("北京EMS", "bjemstckj", "010-8417 9386", "http://www.bj-cnpl.com/webpage/contactus.asp"))
				add(Company("快淘快递", "kuaitao", "400-770-3370", "http://www.4007703370.com/"))
				add(Company("秘鲁(SERPOST)", "peru", "511-500", "http://www.serpost.com.pe/"))
				add(Company("印度尼西亚EMS(Pos Indonesia-EMS)", "indonesia", "+62 21 161", "http://ems.posindonesia.co.id/"))
				add(Company("哈萨克斯坦(Kazpost)", "kazpost", "8 800 080 08 80", "http://www.kazpost.kz/en/"))
				add(Company("立白宝凯物流", "lbbk", "020-81258022", "http://bkls.liby.com.cn/"))
				add(Company("百千诚物流", "bqcwl", "0755-2222 2232", "WWW.1001000.COM"))
				add(Company("皇家物流", "pfcexpress", "0755-29801942", "http://www.pfcexpress.com/"))
				add(Company("法国(La Poste)", "csuivi", "+33 3631", "http://www.colissimo.fr"))
				add(Company("奥地利(Austrian Post)", "austria", "+43 810 010 100", "http://www.post.at"))
				add(Company("乌克兰小包、大包(UkrPoshta)", "ukraine", "+380 (0) 800-500-440", "http://www.ukrposhta.com/www/upost_en.nsf"))
				add(Company("乌干达(Posta Uganda)", "uganda", "", "http://www.ugapost.co.ug/"))
				add(Company("阿塞拜疆EMS(EMS AzerExpressPost)", "azerbaijan", "", "http://www.azems.az/en"))
				add(Company("芬兰(Itella Posti Oy)", "finland", "+358 200 71000", "http://www.posti.fi/english/"))
				add(Company("斯洛伐克(Slovenská Posta)", "slovak", "(+421) 48 437 87 77", "http://www.posta.sk/en"))
				add(Company("埃塞俄比亚(Ethiopian postal)", "ethiopia", "", "http://www.ethiopostal.com/"))
				add(Company("卢森堡(Luxembourg Post)", "luxembourg", "8002 8004 ", "http://www.post.lu/"))
				add(Company("毛里求斯(Mauritius Post)", "mauritius", "208 2851", "http://www.mauritiuspost.mu/"))
				add(Company("文莱(Brunei Postal)", "brunei", "673-2382888 ", "http://www.post.gov.bn/"))
				add(Company("Quantium", "quantium", "", ""))
				add(Company("坦桑尼亚(Tanzania Posts)", "tanzania", "", "http://www.posta.co.tz"))
				add(Company("阿曼(Oman Post)", "oman", "24769925", "http://www.omanpost.om"))
				add(Company("直布罗陀[英国]( Royal Gibraltar Post)", "gibraltar", "", "http://www.post.gi/"))
				add(Company("博源恒通", "byht", "15834177000", "http://www.56soft.com"))
				add(Company("越南EMS(VNPost Express)", "vnpost", "", "http://www.ems.com.vn/default.aspx"))
				add(Company("安迅物流", "anxl", "010-59288730", "http://www.anxl.com.cn/"))
				add(Company("达方物流", "dfpost", "400 700 7049", "http://www.dfpost.com/"))
				add(Company("兰州伙伴物流", "huoban", "0931-5345730/32", "http://www.lzhbwl.com/"))
				add(Company("天纵物流", "tianzong", "400-990-8816", "http://www.tianzong56.cn/"))
				add(Company("波黑(JP BH Posta)", "bohei", "", "http://www.posta.ba/pocetna/2/0/0.html"))
				add(Company("玻利维亚", "bolivia", "", ""))
				add(Company("柬埔寨(Cambodia Post)", "cambodia", "", ""))
				add(Company("巴林(Bahrain Post)", "bahrain", "", "http://mot.gov.bh/en"))
				add(Company("纳米比亚(NamPost)", "namibia", "+264 61 201 3042", "https://www.nampost.com.na/"))
				add(Company("卢旺达(Rwanda i-posita)", "rwanda", "", "http://i-posita.rw/spip.php?article97"))
				add(Company("莱索托(Lesotho Post)", "lesotho", "", "http://lesothopost.org.ls/"))
				add(Company("肯尼亚(POSTA KENYA)", "kenya", "", ""))
				add(Company("喀麦隆(CAMPOST)", "cameroon", "", ""))
				add(Company("伯利兹(Belize Postal)", "belize", "", ""))
				add(Company("巴拉圭(Correo Paraguayo)", "paraguay", "", "http://www.correoparaguayo.gov.py/"))
				add(Company("十方通物流", "sfift", "", ""))
				add(Company("飞鹰物流", "hnfy", "400-6291-666", "http://www.hnfy56.com/"))
				add(Company("UPS i-parcel", "iparcel", "400-078-1183", "http://www.i-parcel.com/en/"))
				add(Company("鑫锐达", "bjxsrd", "", ""))
				add(Company("麦力快递", "mailikuaidi", "400-0000-900", "http://www.mailikuaidi.com/"))
				add(Company("瑞丰速递", "rfsd", "400-063-9000", "http://www.rfsd88.com/"))
				add(Company("美联快递", "letseml", "", ""))
				add(Company("CNPEX中邮快递", "cnpex", "", ""))
				add(Company("鑫世锐达", "xsrd", "", ""))
				add(Company("同舟行物流", "chinatzx", "18062512813/18062699168", "http://www.chinatzx.com/"))
				add(Company("秦邦快运", "qbexpress", "", ""))
				add(Company("大达物流", "idada", "400-098-5656", "http://www.idada56.com/"))
				add(Company("skynet", "skynet", "", ""))
				add(Company("红马速递", "nedahm", "", ""))
				add(Company("云南中诚", "czwlyn", "", ""))
				add(Company("万博快递", "wanboex", "", ""))
				add(Company("腾达速递", "nntengda", "", "http://www.nntengda.com"))
				add(Company("郑州速捷", "sujievip", "", ""))
				add(Company("UBI Australia", "gotoubi", "", ""))
				add(Company("ECMS Express", "ecmsglobal", "", ""))
				add(Company("速派快递(FastGo)", "fastgo", "400 886 3278 ", "http://www.fastgo.com.au"))
				add(Company("易客满", "ecmscn", "86+(400) 086-1756", "http://www.trans4e.com/cn/index.html"))
				add(Company("俄顺达", "eshunda", "0592-5798079", "http://www.007ex.com/"))
				add(Company("广东速腾物流", "suteng", "4001136666", "http://www.ste56.com"))
				add(Company("新鹏快递", "gdxp", "", ""))
				add(Company("美国云达", "yundaexus", "888-408-3332", "http://www.yundaex.us/"))
				add(Company("Toll", "toll", "", ""))
				add(Company("深圳DPEX", "szdpex", "", ""))
				add(Company("百世物流", "baishiwuliu", "400-8856-561", "http://www.800best.com/"))
				add(Company("荷兰包裹(PostNL International Parcels)", "postnlpacle", "34819", "http://www.postnl.com/"))
				add(Company("乐天速递", "ltexp", "021-62269059 ", "http://www.ltexp.com.cn "))
				add(Company("智通物流", "ztong", "4000561818", "http://www.ztong56.com"))
				add(Company("鑫通宝物流", "xtb", "13834168880", "www.xtb56.com"))
				add(Company("airpak expresss", "airpak", "", ""))
				add(Company("荷兰邮政-中国件", "postnlchina", "34819", "http://www.postnl.com/"))
				add(Company("法国小包（colissimo）", "colissimo", "+33 3631", "http://www.colissimo.fr"))
				add(Company("PCA Express", "pcaexpress", "1800 518 000 ", "http://www.pcaexpress.com.au/ "))
				add(Company("韩润", "hanrun", "400-636-4311", "http://www.hr-sz.com/"))
				add(Company("TNT", "tnt", "800-820-9868", "http://www.tnt.com.cn"))
				add(Company("中远e环球", "cosco", "", ""))
				add(Company("顺达快递", "sundarexpress", "", ""))
				add(Company("捷记方舟", "ajexpress", "", ""))
				add(Company("方舟速递", "arkexpress", "", ""))
				add(Company("明大快递", "adaexpress", "", ""))
				add(Company("长江国际速递", "changjiang", "", ""))
				add(Company("八达通", "bdatong", "", ""))
				add(Company("美国申通", "stoexpress", "", ""))
				add(Company("泛捷国际速递", "epanex", "", ""))
				add(Company("顺捷丰达", "shunjiefengda", "0755—88999000", "http://www.sjfd-express.com"))
				add(Company("华赫物流", "nmhuahe", "", "http://nmhuahe.com"))
				add(Company("德国(Deutsche Post)", "deutschepost", "0180 2 3333*", "http://www.dpdhl.com/en.html"))
				add(Company("百腾物流", "baitengwuliu", "400-9989-256", "http://www.beteng.com"))
				add(Company("品骏快递", "pjbest", "400-9789-888", "http://www.pjbest.com/"))
				add(Company("全速通", "quansutong", "", ""))
				add(Company("中技物流", "zhongjiwuliu", "", ""))
				add(Company("九曳供应链", "jiuyescm", "4006-199-939", "http://jiuyescm.com"))
				add(Company("天翼快递", "tykd", "", ""))
				add(Company("德意思", "dabei", "", ""))
				add(Company("城际快递", "chengji", "", ""))
				add(Company("程光快递", "chengguangkuaidi", "", ""))
				add(Company("佐川急便", "sagawa", "", ""))
				add(Company("蓝天快递", "lantiankuaidi", "", ""))
				add(Company("永昌物流", "yongchangwuliu", "", ""))
				add(Company("笨鸟海淘", "birdex", "4008-890-788", "http://birdex.cn/"))
				add(Company("一正达速运", "yizhengdasuyun", "", ""))
				add(Company("京东订单", "jdorder", "400-606-5500", "http://m.jd.com"))
				add(Company("优配速运", "sdyoupei", "0531 89977777", "www.sdyoupei.com"))
				add(Company("TRAKPAK", "trakpak", "", ""))
				add(Company("GTS快递", "gts", "4000-159-111", "http://www.gto315.com"))
				add(Company("AOL澳通速递", "aolau", "0424047888", "http://www.aol-au.com/"))
				add(Company("宜送物流", "yiex", "4008636658", "http://www.yi-express.com"))
				add(Company("通达兴物流", "tongdaxing", "4001-006-609", "http://www.tongdaxing.com/"))
				add(Company("香港(HongKong Post)英文", "hkposten", "", ""))
				add(Company("苏宁订单", "suningorder", "4008-365-365", ""))
				add(Company("飞力士物流", "flysman", "86-755-83448000", "http://www.flysman.com.cn"))
				add(Company("转运四方", "zhuanyunsifang", "", "http://www.transrush.com/"))
				add(Company("logen路坚", "ilogen", "", ""))
				add(Company("成都东骏物流", "dongjun", "028-85538888", "http://www.dj56.cc/"))
				add(Company("日本郵便", "japanpost", "", ""))
				add(Company("佳家通货运", "jiajiatong56", "4008-056-356", "http://www.jiajiatong56.com/"))
				add(Company("吉日优派", "jrypex", "400-0531-951", "http://www.jrypex.com"))
				add(Company("西安胜峰", "xaetc", "400-029-8171", "http://www.xaetc.cn/"))
				add(Company("CJ物流", "doortodoor", "", ""))
				add(Company("信天捷快递", "xintianjie", "400-718-7518", "http:/www.bjxintianjie.com"))
				add(Company("泰国138国际物流", "sd138", "66880089916 ", "http://www.138sd.net/"))
				add(Company("猴急送", "hjs", "400-8888-798", "http://www.hjs777.com"))
				add(Company("全信通快递", "quanxintong", "400-882-6886", "http://www.all-express.com.cn/"))
				add(Company("amazon-国际订单", "amusorder", "400-910-5668 ", ""))
				add(Company("骏丰国际速递", "junfengguoji", "0773-2218104", "http://www.peakmorepost.com/"))
				add(Company("货运皇", "kingfreight", "", "http://www.kingfreight.com.au/ "))
				add(Company("远成快运", "ycexpress", "", "http://www.ycgky.com/"))
				add(Company("速必达", "subida", "0752-3270594", "http://www.speedex.com.cn/"))
				add(Company("特急便物流", "sucmj", "", "http://www.sucmj.com/ "))
				add(Company("亚马逊中国", "yamaxunwuliu", "400-910-5669", "http://www.z-exp.com/"))
				add(Company("锦程物流", "jinchengwuliu", "400-020-5556", "http://www.jc56.com/"))
				add(Company("景光物流", "jgwl", "400-700-1682 ", "http://www.jgwl.cn/"))
				add(Company("御风速运", "yufeng", "400-611-3348", "http://www.shyfwl.cn/"))
				add(Company("至诚通达快递", "zhichengtongda", "400-151-8918 ", "http://www.zctdky.com/    "))
				add(Company("日益通速递", "rytsd", "400-041-5858 ", "http://www.rytbj.com/"))
				add(Company("航宇快递", "hangyu", "021-54478850", "http://www.hyexp.cn/"))
				add(Company("急顺通", "pzhjst", "0812-6688669", "http://www.pzhjst.com/"))
				add(Company("优速通达", "yousutongda", "400-651-8331", "http://www.yousutongda8.com/ "))
				add(Company("秦远物流", "qinyuan", "09-8372888", "http://www.chinz56.co.nz/"))
				add(Company("澳邮中国快运", "auexpress", "130 007 9988, +612 8774 3", "http://www.auexpress.com.au/"))
				add(Company("众辉达物流", "zhdwl", "400-622-6193", "http://www.zhdpt.com/"))
				add(Company("飞邦快递", "fbkd", "400-016-8756", "http://www.fbkd.net/"))
				add(Company("华达快运", "huada", "400-895-1110", "http://www.zz-huada.com/"))
				add(Company("FOX国际快递", "fox", "400-965-8885", "http://www.foxglobal.nl/"))
				add(Company("环球速运", "huanqiu", "139-1076-0364", "http://www.pantoscn.com/"))
				add(Company("辉联物流", "huilian", "139-1076-0364", "http://www.pantoscn.com/"))
				add(Company("A2U速递", "a2u", "03 9877 4330", "http://www.a2u.com.au/"))
				add(Company("UEQ快递", "ueq", "020-37639835", "http://www.ueq.com/"))
				add(Company("中加国际快递", "scic", "(604) 207-0338", "http://scicglobal.com/"))
				add(Company("易达通", "yidatong", "", ""))
				add(Company("宜送", "yisong", "", "http://www.yi-express.com/"))
				add(Company("捷网俄全通", "ruexp", "4007287156", "http://www.ruexp.cn/"))
				add(Company("华通务达物流", "htwd", "0351-5603868", "http://www.htwd56.com/ "))
				add(Company("申必达", "speedoex", "713-482-1198", "http://www.speedoex.com/cn"))
				add(Company("联运快递", "lianyun", " (02) 8541 8607 ", "http://121.40.93.72/"))
				add(Company("捷安达", "jieanda", "03 9544 8304", "http://www.giantpost.com.au/"))
				add(Company("SHL畅灵国际物流", "shlexp", "400-098-5066", "http://www.shlexp.com/ "))
				add(Company("EWE全球快递", "ewe", "+61 2 9644 2648", "https://www.ewe.com.au"))
				add(Company("全球快运", "abcglobal", "626-363-4161", "http://www.abcglobalexpress.com/"))
				add(Company("芒果速递", "mangguo", "", "http://mangoex.cn/"))
				add(Company("金海淘", "goldhaitao", "626-330-7733", "http://www.goldhaitao.us/"))
				add(Company("极光转运", "jiguang", "0755-86535662 ", "http://www.jiguangus.com/"))
				add(Company("富腾达国际货运", "ftd", "09-4432342", "http://www.ftdlogistics.co.nz/"))
				add(Company("DCS", "dcs", "400-678-0856", "http://www.dcslogistics.us/"))
				add(Company("成达国际速递", "chengda", "00852-56078835 ", "http://www.chengda-express.com/"))
				add(Company("中环快递", "zhonghuan", "400-007-9988", "http://www.zhexpress.com.au/"))
				add(Company("顺邦国际物流", "shunbang", "95040391701", "http://shunbangus.com/ "))
				add(Company("启辰国际速递", "qichen", "", "http://www.qichen.hk/"))
				add(Company("澳货通", "auex", "", ""))
				add(Company("澳速物流", "aosu", "", ""))
				add(Company("澳世速递", "aus", "", ""))
				add(Company("当当", "dangdang", "", ""))
				add(Company("天马迅达", "tianma", "", "http://www.expresstochina.com/"))
				add(Company("美龙快递", "mjexp", "323-208-9848", "http://www.mjexp.com/"))
				add(Company("唯品会(vip)", "vipshop", "", ""))
				add(Company("香港骏辉物流", "chunfai", "", "http://www.chunfai.hk/"))
				add(Company("三三国际物流", "zenzen", "", "http://zenzen.hk/"))
				add(Company("淼信快递", "mxe56", "", ""))
				add(Company("海派通", "hipito", "021-54723815", "http://www.hipito.com/"))
				add(Company("国美", "gome", "", ""))
				add(Company("鹏程快递", "pengcheng", "0800-166-188", "http://www.pcexpress.co.nz/ "))
				add(Company("冠庭国际物流", "guanting", "00852-2318 1213", "http://www.quantiumsolutions.com/hk-sc/"))
				add(Company("1号店", "yhdshop", "", ""))
				add(Company("金岸物流", "jinan", "626-818-2750", "http://www.gpl-express.com/"))
				add(Company("海带宝", "haidaibao", "400-825-8585", "http://www.haidaibao.com/"))
				add(Company("澳通华人物流", "cllexpress", "61 8 9457 9339", "http://www.cllexpress.com.au/"))
				add(Company("斑马物流", "banma", "", "http://www.360zebra.com/"))
				add(Company("友家速递", "youjia", "", ""))
				add(Company("百通物流", "buytong", "", "http://www.buytong.cn/"))
				add(Company("新元快递", "xingyuankuaidi", "", ""))
				add(Company("amazon-国内订单", "amcnorder", "", ""))
				add(Company("全速物流", "quansu", "400-679-3883", "http://www.china-quansu.com/"))
				add(Company("新杰物流", "sunjex", "", "http://www.sunjex.com/"))
				add(Company("鲁通快运", "lutong", "400-055-5656 ", "http://www.lutongky.com/ "))
				add(Company("新元国际", "xynyc", "", ""))
				add(Company("小C海淘", "xiaocex", "400-108-3006", "http://www.xiaocex.com"))
				add(Company("航空快递", "airgtc", "18640151012 ", "http://air-gtc.com/"))
				add(Company("叮咚澳洲转运", "dindon", "010-57853244", "http://www.dindonexpress.com/"))
				add(Company("环球通达 ", "hqtd", "400-078-1805 ", "http://www.hqtdkd.com/ "))
				add(Company("小米", "xiaomi", "", ""))
				add(Company("顺丰优选", "sfbest", "", ""))
				add(Company("好又快物流", "haoyoukuai", "400-800-3838 ", "http://www.ff56.com.cn/ "))
				add(Company("永旺达快递", "yongwangda", "400-0607-290 ", "http://www.yongwangda8.com/ "))
				add(Company("木春货运", "mchy", "400-6359-800 ", "http://www.mcchina-express.com/ "))
				add(Company("程光快递", "flyway", "6499482780 ", "www.flyway.co.nz "))
				add(Company("全之鑫物流", "qzx56", "400-080-5658", "http://www.qzx56.com/"))
				add(Company("百事亨通", "bsht", "400-185-6666", "http://www.bsht-express.com"))
				add(Company("ILYANG", "ilyang", "", "http://www.ilyangexpress.com/ "))
				add(Company("先锋快递", "xianfeng", "0311-69046652 ", "http://xianfengex.com/ "))
				add(Company("万家通快递", "timedg", "", ""))
				add(Company("美快国际物流", "meiquick", "020-39141092 ", "http://www.meiquick.com/ "))
				add(Company("泰中物流", "tny", "", ""))
				add(Company("美通", "valueway", "", ""))
				add(Company("新速航", "sunspeedy", "852-64797448", "http://www.sunspeedy.hk "))
				add(Company("速方", "bphchina", "", ""))
				add(Company("英超物流", "yingchao", "（+44）01213680088 ", "http://www.51parcel.com/ "))
				add(Company("阿根廷(Correo Argentina)", "correoargentino", "", ""))
				add(Company("瓦努阿图(Vanuatu Post)", "vanuatu", "", ""))
				add(Company("巴巴多斯(Barbados Post)", "barbados", "", ""))
				add(Company("萨摩亚(Samoa Post)", "samoa", "", ""))
				add(Company("斐济(Fiji Post)", "fiji", "", ""))
				add(Company("益递物流", "edlogistics", "021-64050106", "http://www.ed-logistics.net"))
				add(Company("中外运", "esinotrans", "", ""))
				add(Company("跨畅（直邮易）", "kuachangwuliu", "4000381917，020-38937441", "http://www.zhiyouyi.xin"))
				add(Company("中澳速递", "cnausu", "", ""))
				add(Company("联合快递", "gslhkd", "", ""))
				add(Company("河南次晨达", "ccd", "400-003-3506 ", "http://www.ccd56.com "))
				add(Company("奔腾物流", "benteng", "0531-89005678 ", "http://www.benteng56.com"))
				add(Company("今枫国际快运", "mapleexpress", "", ""))
				add(Company("中运全速", "topspeedex", "010-65175288、010-65175388", "http://www.topspeedex.com.cn "))
				add(Company("宜家行", "yjxlm", "", ""))
				add(Company("中欧快运", "otobv", "088-188-8989", "http://www.otobv.com"))
				add(Company("金马甲", "jmjss", "028-87058515 ", "http://www.jmjss.com "))
				add(Company("一号仓", "onehcang", "0755-89391959，0755-893913", "http://www.1hcang.com "))
				add(Company("和丰同城", "hfwuxi", "0510-82863199 ", "http://www.hfwuxi.com/ "))
				add(Company("威时沛运货运", "wtdchina", "", ""))
				add(Company("顺捷达", "shunjieda", "", ""))
				add(Company("千顺快递", "qskdyxgs", "4000-444-668", "http://www.qskdyxgs.com"))
				add(Company("天联快运", "tlky", "400-133-5256 ", "http://www.tl5256.com "))
				add(Company("CE易欧通国际速递", "cloudexpress", "+31 367851419，+31 6280884", ""))
				add(Company("行必达", "speeda", "", ""))
				add(Company("中通国际", "zhongtongguoji", "", ""))
				add(Company("西邮寄", "xipost", "400-0911-882", "http://www.xipost.com"))
				add(Company("NLE", "nle", "", ""))
				add(Company("亚欧专线", "nlebv", "+31 88 668 1277", "http://www.euasia.eu/"))
				add(Company("顺通快递", "stkd", "400-113-8789", "http://www.st-kd.com"))
				add(Company("信联通", "sinatone", "4008-290-296", "http://www.sinatone.com"))
				add(Company("澳德物流", "auod", "", ""))
				add(Company("德方物流", "ahdf", "055165883415", "http://www.ahdf56.com"))
				add(Company("微转运", "wzhaunyun", "4007883376", "http://www.wzhuanyun.com"))
				add(Company("沈阳特急送", "lntjs", "", ""))
				add(Company("iExpress", "iexpress", "", ""))
				add(Company("BCWELT", "bcwelt", "", ""))
				add(Company("欧亚专线", "euasia", "", ""))
				add(Company("远成快运", "ycgky", "", ""))
			}

			initNamesAndPinyin()

            // TODO Change a normal way to update company
            CoroutineScope(Dispatchers.IO).launch {
                val result = companyList
                if (result != null) {
                    info = result
                    initNamesAndPinyin()
                }
            }
		}

		private fun initNamesAndPinyin() {
			names = info!!.map { it.name }.toTypedArray()
			pinyin = info!!.map {
				it.name.let { name ->
                    Pinyin.toPinyin(name, "").toLowerCase()
                }
			}.toTypedArray()
		}

	}

}

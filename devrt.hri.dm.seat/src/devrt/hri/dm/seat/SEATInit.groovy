package devrt.hri.dm.seat

import jp.go.aist.rtm.RTC.Manager
import jp.go.aist.rtm.RTC.RTObject_impl
import jp.go.aist.rtm.RTC.RegisterModuleFunc
import jp.go.aist.rtm.RTC.RtcDeleteFunc
import jp.go.aist.rtm.RTC.RtcNewFunc
import jp.go.aist.rtm.RTC.util.Properties

public class SEATInit implements RtcNewFunc, RtcDeleteFunc, RegisterModuleFunc {

  //  Module specification
  //  <rtc-template block="module_spec">
  public static String[] component_conf = [
    "implementation_id", "SEAT",
    "type_name",         "SEAT",
    "description",       "SEAT compatible state transition engine",
    "version",           "1.0",
    "vendor",            "Yosuke Matsusaka, MID",
    "category",          "hri",
    "activity_type",     "DataFlowComponent",
    "max_instance",      "10",
    "language",          "Groovy",
    "lang_type",         "compile",
    ""
  ];
  //  </rtc-template>

  public RTObject_impl createRtc(Manager mgr) {
    return new SEAT(mgr)
  }

  public void deleteRtc(RTObject_impl rtcBase) {
    rtcBase = null
  }

  public void registerModule() {
    Properties prop = new Properties(component_conf)
    final Manager manager = Manager.instance()
    manager.registerFactory(prop, new SEAT(), new SEAT())
  }
}
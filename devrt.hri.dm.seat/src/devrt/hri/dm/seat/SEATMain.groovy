package devrt.hri.dm.seat

import jp.go.aist.rtm.RTC.Manager
import jp.go.aist.rtm.RTC.ModuleInitProc
import jp.go.aist.rtm.RTC.RTObject_impl
import jp.go.aist.rtm.RTC.util.Properties

public class SEATMain implements ModuleInitProc {

  public void myModuleInit(Manager mgr) {
    Properties prop = new Properties(SEATInit.component_conf)
    mgr.registerFactory(prop, new SEAT(), new SEAT())

    // Create a component
    System.out.println("Creating a component: \"SEAT\"....")
    RTObject_impl comp = mgr.createComponent("SEAT")
    System.out.println("succeed.")
  }

  public static void main(String[] args) {
    // Initialize manager
    final Manager manager = Manager.init(args)

    // Set module initialization procedure
    // This procedure will be invoked in activateManager() function.
    SEATInit init = new SEATInit()
    manager.setModuleInitProc(init)

    // Activate manager and register to naming service
    manager.activateManager()

    // run the manager in blocking mode
    // runManager(false) is the default.
    manager.runManager()

    // If you want to run the manager in non-blocking mode, do like this
    // manager.runManager(true)
  }

}
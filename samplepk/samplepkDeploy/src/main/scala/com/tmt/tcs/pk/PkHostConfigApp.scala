package com.tmt.tcs.pk

import csw.framework.deploy.hostconfig.HostConfig

object PkHostConfigApp extends App {

  HostConfig.start("pk-host-config-app", args)

}

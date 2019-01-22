package com.tmt.tcs.mcs.samplemcsDeploy

import csw.framework.deploy.containercmd.ContainerCmd

object SamplemcsContainerCmdApp extends App {

  ContainerCmd.start("samplemcs-container-cmd-app", args)

}

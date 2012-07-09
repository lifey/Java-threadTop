arrComputers = Array("localhost")
For Each strComputer In arrComputers
   Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\CIMV2")
   Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_Thread", "WQL", _
                                          wbemFlagReturnImmediately + wbemFlagForwardOnly)
   For Each objItem In colItems
       if objItem.ProcessHandle = Wscript.Arguments(0) then
          Wscript.Echo objItem.ProcessHandle & "," & objItem.Handle & "," & objItem.UserModeTime & "," & objItem.KernelModeTime
      end if
   Next
Next

rem // lan - it is adapter name. To change adapter name in windows -> Open 'Change adapter settings' -> right click on adapter name -> click to rename
netsh interface set interface name="lan" disable
ping 127.0.0.1 -n 6 > nul
netsh interface set interface name="lan" enable
